package application.service.geocoder;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("classpath:yandex.properties")
@Log4j2
public class YandexGeoCoderService {

    static String apiKey;
    static String defaultUrl;

    @Value("${yandex.geoCoder.apiKey}")
    public void setApiKey(String value) {
        apiKey = value;
    }

    @Value("${yandex.geoCoder.url}")
    public void setDefaultUrl(String value) {
        defaultUrl = value;
    }

    public String getCityByCoordinates(String coordinates) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = createRequest(coordinates);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {

                JsonObject geoObject = getJSONObject(response);

                return (geoObject == null) ? null : geoObject.get("name").getAsString();
            } else {
                log.error("Сервис геокодирования не отвечает. Код ответа: " + statusCode);
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, Float> getCoordinatesByCity(String city) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = createRequest(city);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject geoObject = getJSONObject(response);
                if (geoObject == null) {
                    return null;
                }

                JsonObject point = geoObject.getAsJsonObject("Point");

                String pos = point.get("pos").getAsString();
                String[] posArray = pos.split(" ");

                HashMap<String, Float> resultMap = new HashMap<String, Float>();
                resultMap.put("longitude", Float.valueOf(posArray[0]));
                resultMap.put("latitude", Float.valueOf(posArray[1]));

                return resultMap;
            } else {
                log.error("Сервис не отвечает");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private HttpGet createRequest(String geocode) {
        StringBuilder requestUrl = new StringBuilder(defaultUrl);

        List<NameValuePair> urlParameters = getListUrlParameters(geocode);

        urlParameters.forEach(nameValuePair -> requestUrl
                .append(nameValuePair.getName())
                .append("=")
                .append(nameValuePair.getValue())
                .append("&"));

        requestUrl.deleteCharAt(requestUrl.length() - 1);

        return new HttpGet(requestUrl.toString());
    }

    private List<NameValuePair> getListUrlParameters(String geocode) {
        return new ArrayList<NameValuePair>() {{
            add(new BasicNameValuePair("geocode", geocode));
            add(new BasicNameValuePair("apikey", apiKey));
            add(new BasicNameValuePair("format", "json"));
            add(new BasicNameValuePair("kind", "locality"));
            add(new BasicNameValuePair("results", "1"));
        }};
    }

    private static JsonObject getJSONObject(CloseableHttpResponse response) throws IOException {
        String str = new String(IOUtils.toByteArray(response.getEntity().getContent()), StandardCharsets.UTF_8);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(str);

        // Начинаем парсить
        JsonObject rootObject = element.getAsJsonObject();
        JsonObject responseObject = rootObject.getAsJsonObject("response");
        JsonObject geoObjectCollectionObject = responseObject.getAsJsonObject("GeoObjectCollection");
        JsonArray featureMemberObject = geoObjectCollectionObject.getAsJsonArray("featureMember");

        if (featureMemberObject.size() == 0) {
            log.error("По указанным координатам не найдено адреса");
            return null;
        } else if (featureMemberObject.size() > 1) {
            log.error("По указанным координатам вернули более 1 адреса");
        }

        JsonObject firstAddressBlock = featureMemberObject.get(0).getAsJsonObject();

        return firstAddressBlock.getAsJsonObject("GeoObject");
    }

}
