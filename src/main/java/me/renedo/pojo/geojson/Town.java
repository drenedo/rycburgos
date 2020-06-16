package me.renedo.pojo.geojson;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Town {
    double lat;
    double lon;
    String name;
}
