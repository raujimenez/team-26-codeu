/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codeu.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

import com.google.appengine.api.datastore.FetchOptions;

/** Provides access to the data stored in Datastore. */
public class Datastore {

  private DatastoreService datastore;

  public Datastore() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity("Message", message.getId().toString());
    messageEntity.setProperty("user", message.getUser());
    messageEntity.setProperty("text", message.getText());
    messageEntity.setProperty("timestamp", message.getTimestamp());

    datastore.put(messageEntity);
  }


  /* Stores new listing in Datastore*/
  public void storeListing(Listing listing) {
    Entity listingEntity = new Entity("Listing", listing.getId().toString());
    listingEntity.setProperty("id", listing.getId().toString());
    listingEntity.setProperty("user", listing.getUser());
    listingEntity.setProperty("title", listing.getTitle());
    listingEntity.setProperty("text", listing.getText());
    listingEntity.setProperty("timestamp", listing.getTimestamp());
    listingEntity.setProperty("lat", listing.getLat());
    listingEntity.setProperty("lng", listing.getLng());
    listingEntity.setProperty("content", listing.getContent());
    listingEntity.setProperty("price", listing.getPrice());

    datastore.put(listingEntity);
  }

  /**
   * Gets messages posted by a specific user.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessages(String user) {
    Query query =
        new Query("Message")
            .setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
            .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    return readMessages(results);
  }

  /**
   * Gets listing posted by a specific user.
   *
   * @return a list of listing posted by the user, or empty list if user has never posted a
   *     listing. List is sorted by time descending.
   */
  public List<Listing> getListings(String key, String searchIndex) {
    Query query =
        new Query("Listing")
            .setFilter(new Query.FilterPredicate(searchIndex, FilterOperator.EQUAL, key))
            .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    return readListings(results);
  }

  /**
   * Gets all the messages posted by all the users.
   *
   * @return a list of messages posted by all the users, or empty list if no one has
   *         never posted a message. List is sorted by time descending.
   */
  public List<Message> getAllMessages() {
    Query query = 
      new Query("Message")
        .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    return readMessages(results);
  }
  
  /**
   * Gets all the listings posted by all the users.
   *
   * @return a list of Listings posted by all the users, or empty list if no one has
   *         never posted a listing. List is sorted by time descending.
   */
  public List<Listing> getAllListings() {
    Query query = 
      new Query("Listing")
        .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    return readListings(results);
  }
  
  /**
   * Reads the message of the entity and stores it in the Message List
   */
  private List<Message> readMessages(PreparedQuery results) {
    List<Message> messages = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String user = (String) entity.getProperty("user");
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");

        Message message = new Message(id, user, text, timestamp);
        messages.add(message);
      } catch (Exception e) {
        System.err.println("Error reading message.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return messages;
  }

  /**
   * Reads the Listing of the entity and stores it in the Listings List
   */
  private List<Listing> readListings(PreparedQuery results) {
    List<Listing> listings = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String user = (String) entity.getProperty("user");
        String title = (String) entity.getProperty("title");
        String text = (String) entity.getProperty("text");
        long timestamp = (long) entity.getProperty("timestamp");
        double lat= (double) entity.getProperty("lat");
        double lng= (double) entity.getProperty("lng");
        String content= (String) entity.getProperty("content");
        double price = (double) entity.getProperty("price");
        Listing listing = new Listing(id, user, title, text, timestamp, lat, lng, content, price);
        listings.add(listing);
      } catch (Exception e) {
        System.err.println("Error reading listing.");
        System.err.println(entity.toString());
        e.printStackTrace();
      }
    }

    return listings;
  }

  /** Returns the total number of messages for all users. */
  public int getTotalMessageCount(){
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    return results.countEntities(FetchOptions.Builder.withLimit(1000));
  }

  /** Returns the total number of listings for all users. */
  public int getTotalListingCount(){
    Query query = new Query("Listing");
    PreparedQuery results = datastore.prepare(query);
    return results.countEntities(FetchOptions.Builder.withLimit(1000));
  }
  
  /** Get the list of all users.
   * @return a set that adds all users.
   * */
  public Set<String> getUsers(){
    Set<String> users = new HashSet<>();
    Query query = new Query("Message");
    PreparedQuery results = datastore.prepare(query);
    for(Entity entity : results.asIterable()) {
      users.add((String) entity.getProperty("user"));
    }
    return users;
  }
}
