package me.renedo.entity;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import twitter4j.HashtagEntity;
import twitter4j.Status;

@Data
@Entity
@NoArgsConstructor
public class Radar extends PanacheEntityBase {

    @Id @GeneratedValue
    private Long id;

    @Column(length = 500)
    private String text;

    private Date date;

    private String hash;

    private Long tweeterId;

    private Double lat;

    private Double lon;

    @Column(length = 2)
    private RadarType type;

    public JsonObject toJsonObject(String address){
        JsonObject jsonObject = JsonObject.mapFrom(this);
        jsonObject.put("address", address);
        return jsonObject;
    }

    public static Radar of(Status status){
        Radar radar = new Radar();
        radar.setText(formatText(status.getText()));
        radar.setDate(status.getCreatedAt());
        radar.setHash(Stream.of(status.getHashtagEntities()).map(HashtagEntity::getText).collect(Collectors.joining(",")));
        radar.setTweeterId(status.getId());
        return radar;
    }

    private static String formatText(String source){
        String text = source.replaceAll("/n", " ");
        if(text.split("  ").length > 2 && text.split(" ").length > text.length()/3){
            text = text.replaceAll("  ", "\\\\").replaceAll(" ", "").replaceAll("\\\\"," ");
        }
        return text;
    }
}
