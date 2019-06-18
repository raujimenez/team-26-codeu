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

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.List;

import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        // Get the URL of the image that the user uploaded to Blobstore.
        String imageUrl = getUploadedFileUrl(request, "image");
        String userEnteredContent;

        //Add the html tags if the user uploaded an image
        if (imageUrl != null) {
            String tags = "<img src=\"" + imageUrl + "\"/>";
            userEnteredContent = text + " " + tags;
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

        Message message = new Message(user, textWithImagesReplaced);
        datastore.storeMessage(message);

        response.sendRedirect("/user-page.html?user=" + user);
    }

    /**
     * Returns a URL that points to the uploaded file, or null if the user didn't
     * upload a file.
     */
    private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> blobKeys = blobs.get("image");

        // User submitted form without selecting a file, so we can't get a URL.
        // (devserver)
        if (blobKeys == null || blobKeys.isEmpty()) {
            return null;
        }

        // Our form only contains a single file input, so get the first index.
        BlobKey blobKey = blobKeys.get(0);

        // User submitted form without selecting a file, so we can't get a URL. (live
        // server)
        BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
        if (blobInfo.getSize() == 0) {
            blobstoreService.delete(blobKey);
            return null;
        }

        // We could check the validity of the file here, e.g. to make sure it's an image
        // file
        // https://stackoverflow.com/q/10779564/873165

        // Use ImagesService to get a URL that points to the uploaded file.
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
        return imagesService.getServingUrl(options);
    }
}