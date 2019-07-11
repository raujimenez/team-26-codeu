package com.google.codeu.servlets;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.codeu.data.Marker;
import com.google.codeu.data.Listing;
import com.google.codeu.data.Datastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


/**
 * Handles fetching and saving markers data.
 */
@WebServlet("/markers")
public class MarkerDataServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }


  /** Stores a marker in Datastore. */
  public void storeMarker(Marker marker) {

      Entity markerEntity = new Entity("Marker");
      markerEntity.setProperty("lat", marker.getLat());
      markerEntity.setProperty("lng", marker.getLng());
      markerEntity.setProperty("content", marker.getContent());

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(markerEntity);

  }

  /** Fetches markers from Datastore. */
  private List<Marker> getMarkers() {
    List<Marker> markers = new ArrayList<>();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Marker");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      double lat = (double) entity.getProperty("lat");
      double lng = (double) entity.getProperty("lng");
      String content = (String) entity.getProperty("content");

      Marker marker = new Marker(lat, lng, content);
      markers.add(marker);
    }
    return markers;
  }

  /** Responds with a JSON array containing marker data. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    List<Marker> markers = getMarkers();
    Gson gson = new Gson();
    String json = gson.toJson(markers);

    response.getOutputStream().println(json);
  }

  /** Accepts a POST request containing a new marker. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException {
    double lat = Double.parseDouble(request.getParameter("lat"));
    double lng = Double.parseDouble(request.getParameter("lng"));
    String content = Jsoup.clean(request.getParameter("content"), Whitelist.none());

    UserService userService = UserServiceFactory.getUserService();
      if (userService.isUserLoggedIn()) {
        String user = userService.getCurrentUser().getEmail();
        String images = "";
        Listing listing = new Listing(user,"marker_in_map", null, lat, lng, content, 0.00, images);
        datastore.storeListing(listing);

        Marker marker = new Marker(lat, lng, content);
        storeMarker(marker);
      }
    }

  }