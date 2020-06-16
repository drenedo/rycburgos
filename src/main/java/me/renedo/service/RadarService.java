package me.renedo.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;
import me.renedo.entity.Radar;
import me.renedo.repository.RadarRepository;
import twitter4j.Status;

@Slf4j
@ApplicationScoped
public class RadarService {

    @Inject
    RadarRepository radarRepository;

    @Transactional
    public boolean processStatus(Status status) {
        boolean created = false;
        if (radarRepository.findByTweetId(status.getId()).isEmpty()) {
            created = true;
            radarRepository.persist(Radar.of(status));
        }
        return created;
    }

    @Transactional
    public void updateLatLon(Long id, double lat, double lon){
        Radar radar = radarRepository.findById(id);
        radar.setLat(lat);
        radar.setLon(lon);
    }

    @Transactional
    public void updateLatLon(Long id, List<Pair<Double, Double>> latlons){
        long greatCount = 0;
        double lat = 0;
        double lon = 0;
        for (Pair<Double, Double> latlon : latlons) {
            long count = radarRepository.countByLatLon(latlon.getLeft(), latlon.getRight());
            if(count>0 && count>greatCount){
                greatCount = count;
                lat = latlon.getLeft();
                lon = latlon.getRight();
            }
        }
        if(greatCount > 0 ){
            updateLatLon(id, lat, lon);
        }
    }

    @Transactional
    public Collection<List<Radar>> findGroupedRadars(){
        return radarRepository.findAllWithCoordinates().stream().sorted(Comparator.comparing(Radar::getDate))
                .collect(Collectors.groupingBy(r -> r.getLat() + "," + r.getLon())).values();
    }
}
