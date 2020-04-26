package application.service.geocoder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
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
public class YandexGeoCoder {

    @Getter
    @Value("yandex.geoDecoder.apiKey")
    static String apiKey;

    static String getURL = "https://geocode-maps.yandex.ru/1.x/?";
    final static CloseableHttpClient httpClient = HttpClients.createDefault();

    public static String getCityByCoordinates(String coordinates) {
        StringBuilder requestUrl = new StringBuilder(getURL);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("geocode", coordinates));
        urlParameters.add(new BasicNameValuePair("apikey", "6ae7a924-5b75-44c1-921a-ddfc3481ea28"));
        urlParameters.add(new BasicNameValuePair("format", "json"));
        urlParameters.add(new BasicNameValuePair("kind", "locality"));
        urlParameters.add(new BasicNameValuePair("results", "1"));

        urlParameters.forEach(nameValuePair -> requestUrl
                .append(nameValuePair.getName())
                .append("=")
                .append(nameValuePair.getValue())
                .append("&"));

        HttpGet request = new HttpGet(requestUrl.toString());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
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

                JsonObject geoObject = firstAddressBlock.getAsJsonObject("GeoObject");

                return geoObject.get("name").getAsString();
            } else {
                log.error("Сервис не отвечает");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, Float> getCoordinatesByCity(String city) {
        StringBuilder requestUrl = new StringBuilder(getURL);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("geocode", city));
        urlParameters.add(new BasicNameValuePair("apikey", "6ae7a924-5b75-44c1-921a-ddfc3481ea28"));
        urlParameters.add(new BasicNameValuePair("format", "json"));
        urlParameters.add(new BasicNameValuePair("kind", "locality"));
        urlParameters.add(new BasicNameValuePair("results", "1"));

        urlParameters.forEach(nameValuePair -> requestUrl
                .append(nameValuePair.getName())
                .append("=")
                .append(nameValuePair.getValue())
                .append("&"));

        HttpGet request = new HttpGet(requestUrl.toString());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
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

                JsonObject geoObject = firstAddressBlock.getAsJsonObject("GeoObject");
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

}
