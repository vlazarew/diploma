package application.geodecoder.response;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GetObjectCollection {
    MetaDataProperty metaDataProperty;
    ArrayList<Object> featureMember = new ArrayList<>();
}
