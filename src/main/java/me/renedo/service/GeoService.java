package me.renedo.service;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import com.google.common.base.Strings;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import me.renedo.entity.Radar;
import me.renedo.pojo.Address;
import me.renedo.repository.RadarRepository;

@Slf4j
@ApplicationScoped
public class GeoService {

    private final static String URL_REGEXP = "https://t.co/.{10}";
    private final static Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);

    @ConfigProperty(name = "google.maps.key")
    String googleKey;

    @ConfigProperty(name = "radares.term.keywords")
    String[] keyWords;

    @ConfigProperty(name = "radares.term.police")
    String[] police;

    @ConfigProperty(name = "radares.term.stopwords")
    String[] stopWords;

    @ConfigProperty(name = "radares.term.carsBrands")
    String[] carsBrands;

    @ConfigProperty(name = "radares.term.carsModels")
    String[] carsModels;

    @ConfigProperty(name = "radares.term.colors")
    String[] colors;

    @ConfigProperty(name = "radares.towns")
    String[] towns;

    @ConfigProperty(name = "radares.min.lat")
    double minLat;

    @ConfigProperty(name = "radares.max.lat")
    double maxLat;

    @ConfigProperty(name = "radares.min.lon")
    double minLon;

    @ConfigProperty(name = "radares.max.lon")
    double maxLon;

    @ConfigProperty(name = "radares.ignore")
    String[] ignoreCoords;

    @ConfigProperty(name = "radares.places.indicators")
    String[] placesIndicators;

    @Inject
    RadarRepository radarRepository;

    @Inject
    RadarService radarService;

    @Inject
    EventBus bus;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    ThreadContext threadContext;

    public List<Pair<Double, Double>> getLatitudeLongitude(String address) {
        GeoApiContext context = new GeoApiContext.Builder().apiKey(googleKey).build();
        GeocodingApiRequest request = GeocodingApi.newRequest(context);
        request.address(address);
        List<GeocodingResult> results = new ArrayList<>();
        try {
            results.addAll(asList(request.await()));
        } catch (ApiException | InterruptedException | IOException e) {
            log.error(e.getMessage());
        }
        log.info(address);
        results.forEach(r -> log.info("{} , {}", r.geometry.location.lat, r.geometry.location.lng));
        return results.stream().filter(this::validate).map(r -> Pair.of(r.geometry.location.lat, r.geometry.location.lng)).collect(toList());
    }

    public Map<String, List<String>> splitQuery(URL url) {
        if (Strings.isNullOrEmpty(url.getQuery())) {
            return Collections.emptyMap();
        }
        return Arrays.stream(url.getQuery().split("&"))
                .map(this::splitQueryParameter)
                .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new, mapping(Map.Entry::getValue, toList())));
    }

    public SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new SimpleImmutableEntry<>(key, value);
    }

    @Transactional
    public void updateLatitudeLongitude() {
        PanacheQuery<Radar> all = radarRepository.findAll().page(Page.ofSize(25));
        IntStream.range(0, all.pageCount()).forEach(page -> updateLatitudeLongitudePage(all, page));
    }

    @Transactional
    public void updateLatitudeLongitudeByTerm(String term) {
        radarRepository.findAllByTerm(term).stream().filter(this::isValid).forEach(r -> bus.send("updateLocation", r.toJsonObject(toAddress(r))));
    }

    @ConsumeEvent("updateLocation")
    public void updateLocation(JsonObject jsonObject) {
        List<Pair<Double, Double>> latitudeLongitude = getLatitudesLongitudes(jsonObject);
        if (isNull(latitudeLongitude) || latitudeLongitude.size() == 0) {
            managedExecutor.runAsync(threadContext.contextualRunnable(() -> radarService
                    .updateLatLon(jsonObject.getLong("id"), 0D, 0D)));
        } else if (latitudeLongitude.size() == 1) {
            Pair<Double, Double> latLon = latitudeLongitude.get(0);
            managedExecutor.runAsync(threadContext.contextualRunnable(() -> radarService
                    .updateLatLon(jsonObject.getLong("id"), latLon.getLeft(), latLon.getRight())));
        } else {
            managedExecutor.runAsync(threadContext.contextualRunnable(() -> radarService.updateLatLon(jsonObject.getLong("id"), latitudeLongitude)));
        }
    }

    @Transactional
    public void updateNullLatitudeLongitude() {
        radarRepository.findAllWithoutCoordinates().stream().filter(this::isValid)
                .forEach(r -> bus.send("updateLocation", r.toJsonObject(toAddress(r))));
    }

    protected String toAddress(Radar radar) {
        Address address = Address.of(radar.getText());
        address.clean("\\d\\d\\-...\\. \\d\\d:\\d\\d");
        address.clean("\n");
        address.clean(",");
        address.clean("\\.");
        address.clean("#\\S+");
        asList(police).forEach(address::cleanWord);
        asList(keyWords).forEach(address::cleanWord);
        asList(stopWords).forEach(address::cleanWord);
        asList(carsBrands).forEach(address::cleanWord);
        asList(carsModels).forEach(address::cleanWord);
        asList(colors).forEach(address::cleanWord);
        address.clean("dirección\\b.+\\b");
        address.clean("sentido\\b.+\\b");
        address.clean("\\s+", " ");
        address.clean(URL_REGEXP);
        log.info("{} -> {}", radar.getText(), address.getText());
        return address.getText().trim();
    }

    private List<Pair<Double, Double>> fromGoogle(JsonObject jsonObject) {
        String town = getTown(jsonObject.getString("text")).orElse(ofNullable(jsonObject.getString("hash")).orElse("Burgos"));
        return getLatitudeLongitude(jsonObject.getString("address") + ", " + town + ", spain");
    }

    private List<Pair<Double, Double>> getCoordinatesFromUrl(String url) {
        List<Pair<Double, Double>> coordinates = new ArrayList<>();
        try {
            URLConnection con = new URL(url).openConnection();
            con.connect();
            InputStream is = con.getInputStream();
            URL finalUrl = con.getURL();
            if (finalUrl.toString().contains("google")) {
                log.info("Get url from google {}", finalUrl.toString());
                Map<String, List<String>> params = splitQuery(finalUrl);
                ofNullable(params.get("q")).map(l -> Pair.of(Double.valueOf(l.get(0).split(",")[0]), Double.valueOf(l.get(0).split(",")[1])))
                        .ifPresent(coordinates::add);
            }
            is.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return coordinates;
    }

    private Pair<Double, Double> getIgnoredCoord(String text) {
        String[] parts = text.split("#");
        if (parts.length == 2) {
            return Pair.of(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
        }
        return Pair.of(0D, 0D);
    }

    private List<Pair<Double, Double>> getIgnoredCoords() {
        return asList(ignoreCoords).stream().map(this::getIgnoredCoord).collect(Collectors.toList());
    }

    private List<Pair<Double, Double>> getLatitudesLongitudes(JsonObject jsonObject) {
        List<Pair<Double, Double>> latitudeLongitude;
        Matcher matcher = URL_PATTERN.matcher(jsonObject.getString("text"));
        if (matcher.find()) {
            latitudeLongitude = ofNullable(getCoordinatesFromUrl(matcher.group(0))).orElseGet(() -> fromGoogle(jsonObject));
        } else {
            latitudeLongitude = fromGoogle(jsonObject);
        }
        return latitudeLongitude;
    }

    private Optional<String> getTown(String text) {
        String original = text.toLowerCase();
        return Stream.of(towns).map(String::toLowerCase).filter(original::contains)
                .filter(t -> Stream.of(placesIndicators).map(i -> i + " " + t).anyMatch(original::contains)).findFirst();
    }

    private boolean isValid(Radar radar) {
        String normalized = radar.getText().toLowerCase();
        return asList(keyWords).stream().anyMatch(normalized::contains) && !normalized.contains("@");
    }

    private void updateLatitudeLongitudePage(PanacheQuery<Radar> all, int page) {
        log.info("Page {}", page);
        List<Radar> radars = all.page(Page.of(page, 25)).list();
        radars.stream().filter(this::isValid).forEach(r -> bus.send("updateLocation", r.toJsonObject(toAddress(r))));
    }

    private boolean validate(GeocodingResult reoRes) {
        return reoRes.geometry.location.lat >= minLat && reoRes.geometry.location.lat <= maxLat &&
                reoRes.geometry.location.lng >= minLon && reoRes.geometry.location.lng <= maxLon &&
                !getIgnoredCoords().stream().anyMatch(p -> p.getLeft().equals(reoRes.geometry.location.lat) &&
                        p.getRight().equals(reoRes.geometry.location.lng));
    }
}
