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

import java.util.UUID;

/** A single listing posted by a seller. */
public class Listing {

  private UUID id;
  private String user;
  private String title;
  private String text;
  private long timestamp;
  private double lat;
  private double lng;
  private String content;

  /**
   * Constructs a new {@link Listing} posted by {@code user} with {@code title} Lable and {@code text} content. Generates a
   * random ID and uses the current system time for the creation time.
   */
  public Listing(String user, String title, String text, double lat, double lng, String content) {
    this(UUID.randomUUID(), user, title, text, System.currentTimeMillis(), lat, lng, content);
  }

  public Listing(UUID id, String user, String title, String text, long timestamp, double lat, double lng, String content) {
    this.id = id;
    this.user = user;
    this.title = title;
    this.text = text;
    this.timestamp = timestamp;
    this.lat = lat;
    this.lng = lng;
    this.content = content;
  }

  public UUID getId() {
    return id;
  }

  public String getUser() {
    return user;
  }

  public String getTitle() {
    return title;
  }

  public String getText() {
    return text;
  }

  public long getTimestamp() {
    return timestamp;
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
}
