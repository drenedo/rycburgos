package me.renedo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.vertx.core.eventbus.EventBus;
import me.renedo.pojo.geojson.Map;
import me.renedo.pojo.geojson.Town;
import me.renedo.service.GeoService;
import me.renedo.service.RadarService;

@Path("/rest/radar")
public class RadarResource {

    @Inject
    GeoService geoService;

    @Inject
    RadarService radarService;

    @ConfigProperty(name = "radares.towns")
    String[] towns;

    @ConfigProperty(name = "radares.towns.lat")
    double[] lats;

    @ConfigProperty(name = "radares.towns.lon")
    double[] lons;

    @Inject
    EventBus bus;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/geoJson")
    @Transactional
    public Response geoJson() {
        List<Town> townsObj = new ArrayList<>();
        for (int i = 0; i < towns.length; i++) {
            if(lats[i]!=0&&lons[i]!=0){
                townsObj.add(new Town(lats[i], lons[i], towns[i]));
            }
        }
        return Response.ok(Map.of(radarService.findGroupedRadars(), townsObj)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/scrap")
    @Transactional
    public Response scrap() {
        bus.send("scrap", 1);
        return Response.status(200).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/updateLatLon/{term}")
    @Transactional
    public Response updateLatLon(@PathParam String term) {
        geoService.updateLatitudeLongitudeByTerm(term);
        return Response.status(200).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/updateLatLon")
    @Transactional
    public Response updateLatLon() {
        geoService.updateLatitudeLongitude();
        return Response.status(200).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/updateNullLatLon")
    @Transactional
    public Response updateNullLatLon() {
        geoService.updateNullLatitudeLongitude();
        return Response.status(200).build();
    }
}
