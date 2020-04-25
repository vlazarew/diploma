package application.service.weather;

import application.data.model.YandexWeather.*;
import application.data.repository.YandexWeather.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.inject.internal.cglib.core.$CollectionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.glassfish.grizzly.utils.ArraySet;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PropertySource("classpath:yandex.properties")
@Log4j2
@RequiredArgsConstructor
public class YandexWeatherService {

    YandexWeatherRepository yandexWeatherRepository;
    YandexWeatherFactRepository yandexWeatherFactRepository;
    YandexWeatherForecastRepository yandexWeatherForecastRepository;
    YandexWeatherHoursRepository yandexWeatherHoursRepository;
    YandexWeatherInfoRepository yandexWeatherInfoRepository;
    YandexWeatherPartsRepository yandexWeatherPartsRepository;
    YandexWeatherTZInfoRepository yandexWeatherTZInfoRepository;

    @Transactional
    public YandexWeather getWeatherByCoordinates(String longitude, String latitude) {
//        public static void getWeatherByCoordinates(String longitude, String latitude, YandexWeatherRepository yandexWeatherRepository,
//                YandexWeatherInfoRepository yandexWeatherInfoRepository, YandexWeatherFactRepository yandexWeatherFactRepository,
//                YandexWeatherForecastRepository yandexWeatherForecastRepository) {

        String getURL = "https://api.weather.yandex.ru/v1/forecast?";
        CloseableHttpClient httpClient = HttpClients.createDefault();

        StringBuilder requestUrl = new StringBuilder(getURL);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("lat", latitude));
        urlParameters.add(new BasicNameValuePair("lon", longitude));
        urlParameters.add(new BasicNameValuePair("extra", "true"));
        urlParameters.add(new BasicNameValuePair("limit", "3"));

        urlParameters.forEach(nameValuePair -> requestUrl
                .append(nameValuePair.getName())
                .append("=")
                .append(nameValuePair.getValue())
                .append("&"));

        HttpGet request = new HttpGet(requestUrl.toString());
        request.addHeader("X-Yandex-API-Key", "c257f385-24b3-4756-933b-c491c50b7dd2");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String str = new String(IOUtils.toByteArray(response.getEntity().getContent()), StandardCharsets.UTF_8);

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(str);

                // Начинаем парсить
                JsonObject yandexWeatherObject = element.getAsJsonObject();
                JsonObject info = yandexWeatherObject.getAsJsonObject("info");
                JsonObject tzInfo = info.getAsJsonObject("tzinfo");
                JsonObject fact = yandexWeatherObject.getAsJsonObject("fact");
                JsonArray forecasts = yandexWeatherObject.get("forecasts").getAsJsonArray();

                // Сохранение TZInfo
                YandexWeatherTZInfo yandexWeatherTZInfo = new YandexWeatherTZInfo();
                yandexWeatherTZInfo.setAbbr(tzInfo.get("abbr").getAsString());
                yandexWeatherTZInfo.setName(tzInfo.get("name").getAsString());
                yandexWeatherTZInfo.setDst(tzInfo.get("dst").getAsBoolean());
                yandexWeatherTZInfo.setOffset(tzInfo.get("offset").getAsFloat());
                yandexWeatherTZInfoRepository.save(yandexWeatherTZInfo);

                // Сохранение Info
                YandexWeatherInfo yandexWeatherInfo = new YandexWeatherInfo();
                yandexWeatherInfo.setLat(info.get("lat").getAsFloat());
                yandexWeatherInfo.setLon(info.get("lon").getAsFloat());
                yandexWeatherInfo.setDefPressureMm(info.get("def_pressure_mm").getAsFloat());
                yandexWeatherInfo.setDefPressurePa(info.get("def_pressure_pa").getAsFloat());
                yandexWeatherInfo.setUrl(info.get("url").getAsString());
                yandexWeatherInfo.setTzInfo(yandexWeatherTZInfo);
                yandexWeatherInfoRepository.save(yandexWeatherInfo);

                // Сохранение Fact
                YandexWeatherFact yandexWeatherFact = new YandexWeatherFact();
                yandexWeatherFact.setTemp(fact.get("temp").getAsFloat());
                yandexWeatherFact.setFeelsLike(fact.get("feels_like").getAsFloat());
                if (fact.get("temp_water") != null) {
                    yandexWeatherFact.setTempWater(fact.get("temp_water").getAsFloat());
                }
                yandexWeatherFact.setIcon(fact.get("icon").getAsString());
                yandexWeatherFact.setWeatherCondition(fact.get("condition").getAsString());
                yandexWeatherFact.setWindSpeed(fact.get("wind_speed").getAsFloat());
                yandexWeatherFact.setWindGust(fact.get("wind_gust").getAsFloat());
                yandexWeatherFact.setWindDir(fact.get("wind_dir").getAsString());
                yandexWeatherFact.setPressureMm(fact.get("pressure_mm").getAsFloat());
                yandexWeatherFact.setPressurePa(fact.get("pressure_pa").getAsFloat());
                yandexWeatherFact.setHumidity(fact.get("humidity").getAsFloat());
                yandexWeatherFact.setPolar(fact.get("polar").getAsBoolean());
                yandexWeatherFact.setSeason(fact.get("season").getAsString());
                yandexWeatherFact.setObsTime(fact.get("obs_time").getAsFloat());
                yandexWeatherFact.setPrecType(fact.get("prec_type").getAsFloat());
                yandexWeatherFact.setPrecStrength(fact.get("prec_strength").getAsFloat());
                yandexWeatherFact.setCloudness(fact.get("cloudness").getAsFloat());
                yandexWeatherFactRepository.save(yandexWeatherFact);

                // Сохранение forecasts
                forecasts.forEach(forecastObject -> {

                    JsonObject forecastJsonObject = forecastObject.getAsJsonObject();

                    // Сохранение частей
                    JsonObject parts = forecastJsonObject.get("parts").getAsJsonObject();
                    Set<YandexWeatherParts> yandexWeatherPartsList = new HashSet<>();
                    forecastJsonObject.get("parts").getAsJsonObject().keySet().forEach(key -> {
                        if (!key.contains("_short")) {
                            JsonObject partJsonObject = parts.get(key).getAsJsonObject();
                            YandexWeatherParts yandexWeatherPart = new YandexWeatherParts();
                            yandexWeatherPart.setName(key);
                            yandexWeatherPart.setTempMin(partJsonObject.get("temp_min").getAsFloat());
                            yandexWeatherPart.setTempMax(partJsonObject.get("temp_max").getAsFloat());
                            yandexWeatherPart.setTempAvg(partJsonObject.get("temp_avg").getAsFloat());
                            yandexWeatherPart.setFeelsLike(partJsonObject.get("feels_like").getAsFloat());
                            yandexWeatherPart.setIcon(partJsonObject.get("icon").getAsString());
                            yandexWeatherPart.setWeatherCondition(partJsonObject.get("condition").getAsString());
                            yandexWeatherPart.setDaytime(partJsonObject.get("daytime").getAsString());
                            yandexWeatherPart.setPolar(partJsonObject.get("polar").getAsBoolean());
                            yandexWeatherPart.setWindSpeed(partJsonObject.get("wind_speed").getAsFloat());
                            yandexWeatherPart.setWindGust(partJsonObject.get("wind_gust").getAsFloat());
                            yandexWeatherPart.setWindDir(partJsonObject.get("wind_dir").getAsString());
                            yandexWeatherPart.setPressureMm(partJsonObject.get("pressure_mm").getAsFloat());
                            yandexWeatherPart.setPressurePa(partJsonObject.get("pressure_pa").getAsFloat());
                            yandexWeatherPart.setHumidity(partJsonObject.get("humidity").getAsFloat());
                            yandexWeatherPart.setPrecMm(partJsonObject.get("prec_mm").getAsFloat());
                            yandexWeatherPart.setPrecPeriod(partJsonObject.get("prec_period").getAsFloat());
                            yandexWeatherPart.setPrecType(partJsonObject.get("prec_type").getAsFloat());
                            yandexWeatherPart.setPrecStrength(partJsonObject.get("prec_strength").getAsFloat());
                            yandexWeatherPart.setCloudness(partJsonObject.get("cloudness").getAsFloat());

                            yandexWeatherPartsRepository.save(yandexWeatherPart);
                            yandexWeatherPartsList.add(yandexWeatherPart);
                        }
                    });

                    // Сохранение часов
                    JsonArray hours = forecastJsonObject.get("hours").getAsJsonArray();
                    Set<YandexWeatherHours> yandexWeatherHoursList = new HashSet<>();
                    hours.forEach(hour -> {

                        JsonObject hourObject = hour.getAsJsonObject();

                        YandexWeatherHours yandexWeatherHour = new YandexWeatherHours();
                        yandexWeatherHour.setHour(hourObject.get("hour").getAsString());
                        yandexWeatherHour.setHourTs(hourObject.get("hour_ts").getAsFloat());
                        yandexWeatherHour.setTemp(hourObject.get("temp").getAsFloat());
                        yandexWeatherHour.setFeelsLike(hourObject.get("feels_like").getAsFloat());
                        yandexWeatherHour.setIcon(hourObject.get("icon").getAsString());
                        yandexWeatherHour.setWeatherCondition(hourObject.get("condition").getAsString());
                        yandexWeatherHour.setWindSpeed(hourObject.get("wind_speed").getAsFloat());
                        yandexWeatherHour.setWindGust(hourObject.get("wind_gust").getAsFloat());
                        yandexWeatherHour.setWindDir(hourObject.get("wind_dir").getAsString());
                        yandexWeatherHour.setPressureMm(hourObject.get("pressure_mm").getAsFloat());
                        yandexWeatherHour.setPressurePa(hourObject.get("pressure_pa").getAsFloat());
                        yandexWeatherHour.setHumidity(hourObject.get("humidity").getAsFloat());
                        yandexWeatherHour.setPrecMm(hourObject.get("prec_mm").getAsFloat());
                        yandexWeatherHour.setPrecPeriod(hourObject.get("prec_period").getAsFloat());
                        yandexWeatherHour.setPrecType(hourObject.get("prec_type").getAsFloat());
                        yandexWeatherHour.setPrecStrength(hourObject.get("prec_strength").getAsFloat());
                        yandexWeatherHour.setCloudness(hourObject.get("cloudness").getAsFloat());

                        yandexWeatherHoursRepository.save(yandexWeatherHour);
                        yandexWeatherHoursList.add(yandexWeatherHour);
                    });


                    // Сохранение forecast
                    YandexWeatherForecast forecast = new YandexWeatherForecast();
                    forecast.setDate(forecastJsonObject.get("date").getAsString());
                    forecast.setDateTs(forecastJsonObject.get("date_ts").getAsFloat());
                    forecast.setWeek(forecastJsonObject.get("week").getAsFloat());
                    if (forecastJsonObject.get("sunrise") != null) {
                        forecast.setSunrise(forecastJsonObject.get("sunrise").getAsString());
                    }
                    if (forecastJsonObject.get("sunset") != null) {
                        forecast.setSunset(forecastJsonObject.get("sunset").getAsString());
                    }
                    forecast.setMoonCode(forecastJsonObject.get("moon_code").getAsInt());
                    forecast.setMoonText(forecastJsonObject.get("moon_text").getAsString());
                    forecast.setParts(yandexWeatherPartsList);
                    forecast.setHours(yandexWeatherHoursList);

                    yandexWeatherForecastRepository.save(forecast);
                });

                // Сохранение YandexWeather
                YandexWeather yandexWeather = new YandexWeather();
                yandexWeather.setNow(yandexWeatherObject.get("now").getAsFloat());
                yandexWeather.setNowDateTime(yandexWeatherObject.get("now_dt").getAsString());
                yandexWeather.setInfo(yandexWeatherInfo);
                yandexWeather.setFact(yandexWeatherFact);
                yandexWeatherRepository.save(yandexWeather);

                return yandexWeather;
            } else {
                log.error("Сервис не отвечает");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
