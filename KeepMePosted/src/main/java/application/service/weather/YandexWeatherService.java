package application.service.weather;

import application.data.model.YandexWeather.*;
import application.data.repository.YandexWeather.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("classpath:yandex.properties")
@Log4j2
public class YandexWeatherService {

    @Autowired
    YandexWeatherRepository yandexWeatherRepository;
    @Autowired
    YandexWeatherFactRepository yandexWeatherFactRepository;
    @Autowired
    YandexWeatherForecastRepository yandexWeatherForecastRepository;
    @Autowired
    YandexWeatherHoursRepository yandexWeatherHoursRepository;
    @Autowired
    YandexWeatherInfoRepository yandexWeatherInfoRepository;
    @Autowired
    YandexWeatherPartsRepository yandexWeatherPartsRepository;
    @Autowired
    YandexWeatherTZInfoRepository yandexWeatherTZInfoRepository;

    final HashMap<String, String> conditionMap = new HashMap<String, String>() {
        {
            put("clear", "Ясно");
            put("partly-cloudy", "Малооблачно");
            put("cloudy", "Облачно с прояснениями");
            put("overcast", "Пасмурно");
            put("partly-cloudy-and-light-rain", "Небольшой дождь");
            put("cloudy-and-light-rain", "Небольшой дождь");
            put("overcast-and-light-rain", "Небольшой дождь");
            put("partly-cloudy-and-rain", "Дождь");
            put("cloudy-and-rain", "Дождь");
            put("overcast-and-rain", "Сильный дождь");
            put("overcast-thunderstorms-with-rain", "Сильный дождь, гроза");
            put("overcast-and-wet-snow", "Дождь со снегом");
            put("partly-cloudy-and-light-snow", "Небольшой снег");
            put("cloudy-and-light-snow", "Небольшой снег");
            put("overcast-and-light-snow", "Небольшой снег");
            put("partly-cloudy-and-snow", "Снег");
            put("cloudy-and-snow", "Снег");
            put("overcast-and-snow", "Снегопад");

        }
    };
    final HashMap<String, String> windDirectionMap = new HashMap<String, String>() {{
        put("nw", "северо-западное");
        put("n", "северое");
        put("ne", "северо-восточное");
        put("e", "восточное");
        put("se", "юго-восточное");
        put("s", "южное");
        put("sw", "юго-западное");
        put("w", "западное");
        put("c", "штиль");
    }};
    final HashMap<Float, String> typePrecMap = new HashMap<Float, String>() {{
        put(0F, "Без осадков");
        put(1F, "Дождь");
        put(2F, "Дождь со снегом");
        put(3F, "Снег");
    }};
    final HashMap<Float, String> strengthPrecMap = new HashMap<Float, String>() {{
        put(0F, "Без осадков");
        put(0.25F, "Слабый дождь");
        put(0.5F, "Дождь");
        put(0.75F, "Сильный дождь");
        put(1F, "Сильный ливень");
    }};
    final HashMap<Float, String> cloudnessMap = new HashMap<Float, String>() {{
        put(0F, "Ясно");
        put(0.25F, "Малооблачно");
        put(0.5F, "Облачно с прояснениями");
        put(0.75F, "Облачно с прояснениями");
        put(1F, "Пасмурно");
    }};
    final HashMap<Float, String> moonCodeMap = new HashMap<Float, String>() {{
        put(0F, "Полнолуние");
        put(1F, "Убывающая Луна");
        put(2F, "Убывающая Луна");
        put(3F, "Убывающая Луна");
        put(4F, "Последняя четверть");
        put(5F, "Убывающая Луна");
        put(6F, "Убывающая Луна");
        put(7F, "Убывающая Луна");
        put(8F, "Новолуние");
        put(9F, "Растущая Луна");
        put(10F, "Растущая Луна");
        put(11F, "Растущая Луна");
        put(12F, "Первая четверть");
        put(13F, "Растущая Луна");
        put(14F, "Растущая Луна");
        put(15F, "Растущая Луна");
    }};
    final HashMap<String, String> moonStatusMap = new HashMap<String, String>() {{
        put("full-moon", "Полнолуние");
        put("decreasing-moon", "Убывающая Луна");
        put("last-quarter", "Последняя четверть");
        put("new-moon", "Новолуние");
        put("growing-moon", "Растущая Луна");
        put("first-quarter", "Первая четверть");
    }};

    static String apiKey;
    static String defaultUrl;

    @Value("${yandex.weather.apiKey}")
    public void setApiKey(String value) {
        apiKey = value;
    }

    @Value("${yandex.weather.url}")
    public void setDefaultUrl(String value) {
        defaultUrl = value;
    }

    public YandexWeather getWeatherByCoordinates(String longitude, String latitude) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = createRequest(longitude, latitude);

        ObjectMapper mapper = new ObjectMapper();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String str = new String(IOUtils.toByteArray(response.getEntity().getContent()), StandardCharsets.UTF_8);

                // Подготовка всех данных
                YandexWeather yandexWeather = mapper.readValue(str, YandexWeather.class);
                YandexWeatherInfo yandexWeatherInfo = yandexWeather.getInfo();
                YandexWeatherTZInfo yandexWeatherTZInfo = yandexWeatherInfo.getTzInfo();
                YandexWeatherFact yandexWeatherFact = yandexWeather.getFact();
                List<YandexWeatherForecast> yandexWeatherForecasts = yandexWeather.getForecasts();
                // Ибо parts надо парсить по-особенному
                setPartsToForecasts(mapper, str, yandexWeatherForecasts);

                return saveWeatherData(yandexWeather, yandexWeatherInfo, yandexWeatherTZInfo, yandexWeatherFact,
                        yandexWeatherForecasts);
            } else {
                log.error("Сервис не отвечает");
                return null;
            }

        } catch (
                IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Transactional
    YandexWeather saveWeatherData(YandexWeather yandexWeather, YandexWeatherInfo yandexWeatherInfo,
                                  YandexWeatherTZInfo yandexWeatherTZInfo, YandexWeatherFact yandexWeatherFact,
                                  List<YandexWeatherForecast> yandexWeatherForecasts) {
        yandexWeatherTZInfoRepository.save(yandexWeatherTZInfo);
        yandexWeatherInfoRepository.save(yandexWeatherInfo);
        yandexWeatherFactRepository.save(yandexWeatherFact);

        yandexWeatherForecasts.forEach(yandexWeatherForecast -> {
            List<YandexWeatherParts> yandexWeatherParts = yandexWeatherForecast.getParts();
            yandexWeatherParts.forEach(yandexWeatherPart -> {
                yandexWeatherPartsRepository.save(yandexWeatherPart);
            });

            List<YandexWeatherHours> yandexWeatherHours = yandexWeatherForecast.getHours();
            yandexWeatherHours.forEach(yandexWeatherHour -> {
                yandexWeatherHoursRepository.save(yandexWeatherHour);
            });

            yandexWeatherForecastRepository.save(yandexWeatherForecast);
        });

        return yandexWeatherRepository.save(yandexWeather);
    }

    private void setPartsToForecasts(ObjectMapper mapper, String str, List<YandexWeatherForecast> yandexWeatherForecasts) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(str);
        JsonObject yandexWeatherObject = element.getAsJsonObject();
        JsonArray forecasts = yandexWeatherObject.get("forecasts").getAsJsonArray();

        for (int index = 0; index < yandexWeatherForecasts.size(); index++) {
            JsonObject forecastJsonObject = forecasts.get(index).getAsJsonObject();
            JsonObject parts = forecastJsonObject.get("parts").getAsJsonObject();

            List<YandexWeatherParts> yandexWeatherPartsList = new ArrayList<>();

            forecastJsonObject.get("parts").getAsJsonObject().keySet().forEach(key -> {
                if (!key.contains("_short")) {
                    String partJsonString = parts.get(key).getAsJsonObject().toString();

                    YandexWeatherParts yandexWeatherPart = null;
                    try {
                        yandexWeatherPart = mapper.readValue(partJsonString, YandexWeatherParts.class);
                        yandexWeatherPart.setName(key);
                        yandexWeatherPartsList.add(yandexWeatherPart);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            });
            yandexWeatherForecasts.get(index).setParts(yandexWeatherPartsList);
        }
    }

    private HttpGet createRequest(String longitude, String latitude) {
        StringBuilder requestUrl = new StringBuilder(defaultUrl);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>() {{
            add(new BasicNameValuePair("lat", latitude));
            add(new BasicNameValuePair("lon", longitude));
            add(new BasicNameValuePair("extra", "true"));
            add(new BasicNameValuePair("limit", "3"));
        }};

        urlParameters.forEach(nameValuePair -> requestUrl
                .append(nameValuePair.getName())
                .append("=")
                .append(nameValuePair.getValue())
                .append("&"));

        HttpGet request = new HttpGet(requestUrl.toString());
        request.addHeader("X-Yandex-API-Key", apiKey);
        return request;
    }

    public String englishWeatherConditionToRussian(String condition) {
        return conditionMap.get(condition);
    }

    public String englishWindDirectionToRussian(String windDirection) {
        return windDirectionMap.get(windDirection);
    }

    public String floatTypePrecToRussian(float precType) {
        return typePrecMap.get(precType);
    }

    public String floatStrengthPrecToRussian(float precStrength) {
        return strengthPrecMap.get(precStrength);
    }

    public String floatCloudnessToRussian(float cloudness) {
        return cloudnessMap.get(cloudness);
    }

    public String floatMoonCodeToRussian(float moonCode) {
        return moonCodeMap.get(moonCode);
    }

    public String englishMoonStatusToRussian(String moonText) {
        return moonStatusMap.get(moonText);
    }
}
