package me.renedo.pojo.geojson;

import static java.util.stream.Collectors.toList;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Value;
import me.renedo.entity.Radar;

@Value
@AllArgsConstructor
public class Map {
    String type;
    List<Feature> features;

    public static Map of(Collection<List<Radar>> radars, List<Town> towns) {
        List<Feature> features = radars.stream().map(Feature::createPoint).collect(toList());
        towns.stream().map(t -> createBall(t, radars)).filter(Optional::isPresent).map(Optional::get).forEach(features::add);
        return new Map("FeatureCollection", features);
    }

    private static Optional<Feature> createBall(Town town, Collection<List<Radar>> radars) {
        List<Radar> closets = getNumberAtMinDistance(town, radars);
        return closets.size() > 2 ? getBallFeature(town, closets) : Optional.empty();
    }

    private static Optional<Feature> getBallFeature(Town town, List<Radar> radars) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
        Date max = radars.stream().max(Comparator.comparing(Radar::getDate)).get().getDate();
        return Optional.of(Feature.createBall(town.getName() + " " + radars.size() + " avisos\n Ultimo aviso: " + sdf.format(max),
                town.getLon() + 0.0001D, town.getLat() + 0.0001D, radars.size()));
    }

    private static List<Radar> getNumberAtMinDistance(Town town, Collection<List<Radar>> radars) {
        return radars.stream()
                .filter(rs -> Math.sqrt(Math.pow(rs.get(0).getLat() - town.getLat(), 2) + Math.pow(rs.get(0).getLon() - town.getLon(), 2)) < 0.01D)
                .flatMap(List::stream).collect(Collectors.toList());
    }
}
