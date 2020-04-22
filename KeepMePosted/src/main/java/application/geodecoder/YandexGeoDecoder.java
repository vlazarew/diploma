package application.geodecoder;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.DecompressingEntity;
import org.apache.http.client.entity.GZIPInputStreamFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

//@Data
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
//@RequiredArgsConstructor
@PropertySource("classpath:yandex.properties")
public class YandexGeoDecoder {

    //region Параметры в get запросе

    // Адрес или координаты
//    String geocode;
//
//    // Апи-ключ из кабинета разработчика
//   String apiKey;
//
//    // Порядок чтения координат. longlat или latlong
//    String sco;
//
//    // house — дом;
//    // street — улица;
//    // metro — станция метро;
//    // district — район города;
//    // locality — населенный пункт (город/поселок/деревня/село/...).
//    String kind;
//
//    // Формат ответа геокодера (xml или json)
//    String format;
//
//    // Количество ответов
//    Integer results;
//    // ru_RU, en_US
//    String lang;

    //endregion



    @Getter
    @Value("yandex.geoDecoder.apiKey")
    static String apiKey;

    static String getURL = "https://geocode-maps.yandex.ru/1.x/?";
    final static CloseableHttpClient httpClient = HttpClients.createDefault();

    public static String getCityByCoordinates(String coordinates) {

//        URL url = new URLBuilder("https://ru.stackoverflow.com/unanswered/tagged/")
//                .withParam("page", "2")
//                .withParam("tab", "новые")
//                .build();

        StringBuilder requestUrl = new StringBuilder(getURL);

//        HttpGet request = new HttpGet(getURL);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("geocode", coordinates));
        urlParameters.add(new BasicNameValuePair("apikey", "6ae7a924-5b75-44c1-921a-ddfc3481ea28"));
        urlParameters.add(new BasicNameValuePair("format", "json"));
//        urlParameters.add(new BasicNameValuePair("kind", coordinates));

//        List<KeyValuePair> params = new ArrayList<>();
////        params.setParameter("apikey", apiKey);
//        params.add(new KeyValuePair("apikey", "6ae7a924-5b75-44c1-921a-ddfc3481ea28"));
//        params.add("geocode", coordinates);
//        params.add("format", "json");


//        request.setParams(params);

        urlParameters.forEach(nameValuePair -> requestUrl
                .append(nameValuePair.getName())
                .append("=")
                .append(nameValuePair.getValue())
                .append("&"));

        HttpGet request = new HttpGet(requestUrl.toString());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responceStatus = response.getStatusLine().toString();

//            InputStream temp = response.getEntity().getContent();

//            ByteArrayInputStream temp = new ByteArrayInputStream();
            String encoding = "UTF-8";
            String str = new String(IOUtils.toByteArray(response.getEntity().getContent()), encoding);
            System.out.println(str);

            Gson gson = new Gson();

//            String res = gson.fromJson();

//            YandexApiResponse res = gson.fromJson(str, YandexApiResponse.class)
//            if (entity != null) {
//                int s = 1;
//            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
