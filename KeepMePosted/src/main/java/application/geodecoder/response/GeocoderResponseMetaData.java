package application.geodecoder.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeocoderResponseMetaData {
    Point PointObject;
    String request;
    String found;
    String results;
}
