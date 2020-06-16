package me.renedo.pojo.geojson;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Properties {
    String name;
    String description;
    Map<String, String> _umap_options;
}
