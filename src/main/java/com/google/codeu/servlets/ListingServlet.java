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

package com.google.codeu.servlets;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Listing;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/** Handles fetching and saving {@link Message} instances. */
@WebServlet("/listings")
public class ListingServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with a JSON representation of {@link Listing} data for a specific user or a given id.
   * Responds with an empty array if the user is not provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("application/json");

    String user = request.getParameter("user");
    String id = request.getParameter("id");

    if ((isAbsent(user) && isAbsent(id)) || (!isAbsent(user) && !isAbsent(id)) ) {
      /**
       * if /listings contains user and id or if it does not contain any of the parameter
       * return an empty array
       */
      response.getWriter().println("[]");
      return;
    }
    else if (!isAbsent(user) && isAbsent(id)) {
      /** user present but no id present
       * return a json of all listings from that user
       */
      List<Listing> listings = datastore.getListings(user,"user");
      Gson gson = new Gson();
      String json = gson.toJson(listings);
      response.getWriter().println(json);
      return;
    }
    else if (isAbsent(user) && !isAbsent(id)) {
      /** user absent but id present
       * return a json of all listings given that id
       */
      List<Listing> listings = datastore.getListings(id, "id");
      Gson gson = new Gson();
      String json = gson.toJson(listings);
      response.getWriter().println(json);
      return;
    }

  }

  /** Stores a new {@link Listing}. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String user = userService.getCurrentUser().getEmail();
    String listingTitle = request.getParameter("title");
    String userEnteredContent = request.getParameter("text");
    double price = Double.parseDouble(request.getParameter("price"));

    BlobKey blobKey = getBlobKey(request, "image");
    String imageUrl = "";
    //Add the html tags if the user uploaded an image
    if (blobKey != null) {
      // Get the URL of the image that the user uploaded.
      imageUrl = getUploadedFileUrl(blobKey);

      ArrayList<String> labels = new ArrayList<String>();

            /*
            // Get the labels of the image that the user uploaded.
            byte[] blobBytes = getBlobBytes(blobKey);
            List<EntityAnnotation> imageLabels = getImageLabels(blobBytes);

            for (EntityAnnotation label : imageLabels) {
                labels.add(label.getDescription() + ": " + label.getScore());
            }
            */

      String joinedLabels = String.join(" ", labels);
      userEnteredContent +=  "   " + joinedLabels;
    }

    Whitelist whitelist = Whitelist.relaxed();
    whitelist.addTags("span");
    whitelist.addAttributes("span", "style");
    whitelist.addTags("s");
    String userText = Jsoup.clean(userEnteredContent, whitelist);

    String regex = "(https?://\\S+\\.(png|jpg|gif))";
    String replacement = "<img src=\"$1\" />";
    
    String textWithImagesReplaced = userText.replaceAll(regex, replacement);

    Listing listing = new Listing(user, request.getParameter("title"), textWithImagesReplaced, 0, 0, "", price, imageUrl);
    datastore.storeListing(listing);

    response.sendRedirect("/user-page.html?user=" + user);
  }

  /**
   * Checks if the parameter is valid on a request
   * Used as a helper function for doGet method
   * @param requestParameter value returned from request.getParameter()
   * @return boolean true if absent, false if not absent
   **/
  private boolean isAbsent(String requestParameter) {
    return requestParameter == null || requestParameter.equals("");
  }


  /**
   * Returns the BlobKey that points to the file uploaded by the user, or null if
   * the user didn't upload a file.
   */
  private BlobKey getBlobKey(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a BlobKey.
    // (devserver)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so the BlobKey is empty. (live
    // server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    return blobKey;
  }

  private String getUploadedFileUrl(BlobKey blobKey) {
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    return imagesService.getServingUrl(options);
  }

}
