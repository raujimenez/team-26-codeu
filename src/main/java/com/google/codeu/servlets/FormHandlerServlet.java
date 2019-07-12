package com.google.codeu.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.data.Listing;

/**
 * When the user submits the form, Blobstore processes the file upload and then
 * forwards the request to this servlet. This servlet can then process the
 * request using the file URL we get from Blobstore.
 */
@WebServlet("/my-form-handler")
public class FormHandlerServlet extends HttpServlet {

    private Datastore datastore;

    @Override
    public void init() {
        datastore = new Datastore();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        UserService userService = UserServiceFactory.getUserService();
        if (!userService.isUserLoggedIn()) {
            response.sendRedirect("/index.html");
            return;
        }

        String user = userService.getCurrentUser().getEmail();
        // Get the message entered by the user.
        String text = request.getParameter("text");

        // Get the BlobKey that points to the image uploaded by the user.
        BlobKey blobKey = getBlobKey(request, "image");

        String userEnteredContent;
        String imageUrl = "";
        //Add the html tags if the user uploaded an image
        if (blobKey != null) {
            // Get the URL of the image that the user uploaded.
            imageUrl = getUploadedFileUrl(blobKey);
            String tags = "<img src=\"" + imageUrl + "\"/>";

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
            userEnteredContent = text + "  " + joinedLabels;
        }
        else {
            userEnteredContent = text;
        }

        Whitelist whitelist = Whitelist.relaxed();
        whitelist.addTags("span");
        whitelist.addAttributes("span", "style");
        whitelist.addTags("s");
        String userText = Jsoup.clean(userEnteredContent, whitelist);

        //Changes the images that are not uploaded by the user
        String regex = "(https?://\\S+\\.(png|jpg|gif))";
        String replacement = "<img src=\"$1\" />";
        String textWithImagesReplaced = userText.replaceAll(regex, replacement);

        if(request.getParameter("title") != null) {
            Listing listing = new Listing(user, request.getParameter("title"), textWithImagesReplaced, 0.0, 0.0, null, Double.parseDouble(request.getParameter("price")), imageUrl);
            datastore.storeListing(listing);
            response.sendRedirect("/viewListing.html?id=" + listing.getId().toString());
        }
        else {
            Message message = new Message(user , textWithImagesReplaced);
            datastore.storeMessage(message);
            response.sendRedirect("/user-page.html?user=" + user);
        }
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

    /**
     * Blobstore stores files as binary data. This function retrieves the binary
     * data stored at the BlobKey parameter.
     */
    private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

        int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
        long currentByteIndex = 0;
        boolean continueReading = true;
        while (continueReading) {
            // end index is inclusive, so we have to subtract 1 to get fetchSize bytes
            byte[] b = blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
            outputBytes.write(b);

            // if we read fewer bytes than we requested, then we reached the end
            if (b.length < fetchSize) {
                continueReading = false;
            }

            currentByteIndex += fetchSize;
        }

        return outputBytes.toByteArray();
    }

    /**
     * Uses the Google Cloud Vision API to generate a list of labels that apply to
     * the image represented by the binary data stored in imgBytes.
     */
    private List<EntityAnnotation> getImageLabels(byte[] imgBytes) throws IOException {
        ByteString byteString = ByteString.copyFrom(imgBytes);
        Image image = Image.newBuilder().setContent(byteString).build();

        Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        ImageAnnotatorClient client = ImageAnnotatorClient.create();
        BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
        client.close();
        List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
        AnnotateImageResponse imageResponse = imageResponses.get(0);

        if (imageResponse.hasError()) {
            System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
            return null;
        }

        return imageResponse.getLabelAnnotationsList();
    }

    /**
     * Returns a URL that points to the uploaded file.
     */
    private String getUploadedFileUrl(BlobKey blobKey) {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
        return imagesService.getServingUrl(options);
    }

}