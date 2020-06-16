package me.renedo.pojo.geojson;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PointGeometry {
    String type;
    double[] coordinates;
}
