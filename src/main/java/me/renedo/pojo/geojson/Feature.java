package me.renedo.pojo.geojson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Value;
import me.renedo.entity.Radar;

@Value
@AllArgsConstructor
public class Feature {
    String type;
    Properties properties;
    Object geometry;

    public static Feature createPoint(List<Radar> radars) {
        String name = radars.size() + " veces";
        String description = radars.stream().map(Radar::getText).collect(Collectors.joining("\n"));
        Map<String, String> umapProps = new HashMap<>();
        umapProps.put("color", getColor(radars.size()));
        umapProps.put("iconClass", "Circle");
        return new Feature(Feature.class.getSimpleName(), new Properties(name, description, umapProps),
                new PointGeometry("Point", new double[] {radars.get(0).getLon().doubleValue(), radars.get(0).getLat().doubleValue()}));
    }

    public static Feature createBall(String name, double lat, double lon, int total) {
        Map<String, String> umapProps = new HashMap<>();
        umapProps.put("color", getColor(total));
        umapProps.put("iconClass", "Ball");
        return new Feature(Feature.class.getSimpleName(), new Properties(name, null, umapProps),
                new PointGeometry("Point", new double[] {lat, lon}));
    }

    private static String getColor(int size) {
        return size > 10 ? "Red" : size > 5 ? "Orange" : size > 2 ? "Yellow" : "Lime";
    }

    private static List<double[]> getOrdered(List<List<Radar>> radars) {
        List<double[]> finalPoints = new ArrayList<>();
        if (radars.size() > 0) {
            List<double[]> points = radars.stream().map(r -> new double[] {r.get(0).getLon().doubleValue(), r.get(0).getLat().doubleValue()})
                    .collect(Collectors.toList());
            double[] greatLat = points.stream().max(Comparator.comparingDouble(p -> p[1])).get();
            finalPoints.add(greatLat);
            points.stream().filter(p -> p[0] > greatLat[0]).sorted(Comparator.comparingDouble(p -> p[1])).forEach(finalPoints::add);
            points.stream().filter(p -> p[0] <= greatLat[0] && p != greatLat).sorted(Comparator.comparingDouble(p -> p[1])).sorted(
                    Collections.reverseOrder()).forEach(finalPoints::add);
        }
        return finalPoints;
    }
}
