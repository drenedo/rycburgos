package me.renedo.repository;

import static java.util.Optional.ofNullable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import me.renedo.entity.Radar;

@ApplicationScoped
public class RadarRepository implements PanacheRepository<Radar> {

    public Radar findByDate(Date date) {
        return find("date", date).firstResult();
    }

    public Optional<Radar> findByTweetId(Long tweeterId) {
        return ofNullable(find("tweeterId", tweeterId).firstResult());
    }

    public long countByLatLon(double lat, double lon){
        return find("lat = ?1 and lon = ?2",lat, lon).count();
    }

    public List<Radar> findAllWithCoordinates(){
        return find("lat is not null and lon is not null and lat != 0 and lon != 0").list();
    }

    public List<Radar> findAllWithoutCoordinates(){
        return find("lat is null and lon is null or lat = 0 and lon = 0").list();
    }

    public List<Radar> findAllByTerm(String term){
        return find("text like ?1", "%"+term+"%").list();
    }
}
