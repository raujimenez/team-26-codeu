package com.google.codeu.data;

import java.util.UUID;

public class Marker {

  private double lat;
  private double lng;
  private String content;
  private UUID markerid;

  public Marker(double lat, double lng, String content, UUID markerid) {
    this.lat = lat;
    this.lng = lng;
    this.content = content;
    this.markerid=markerid;
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public String getContent() {
    return content;
  }

  public UUID getMarkerid(){
    return markerid;
  }
}