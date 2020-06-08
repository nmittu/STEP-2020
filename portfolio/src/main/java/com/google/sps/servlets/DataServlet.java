// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
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
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.sps.comments.Comment;
import java.util.ArrayList;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.sps.servlets.RequestHelper.getParameter;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/json;");

    String limitString = request.getParameter("limit");
    int limit;

    try {
      limit = Integer.parseInt(limitString);
    } catch (NumberFormatException e) {
      // default value
      limit = 10;
    }

    String pageString = request.getParameter("page");
    int page;

    try {
      page = Integer.parseInt(pageString);
    } catch (NumberFormatException e) {
      page = 1;
    }

    String targetLang = getParameter(request, "targetLang", "en");

    Translate translate = TranslateOptions.getDefaultInstance().getService();

    ArrayList<Comment> comments = new ArrayList<>();
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(limit).offset((page - 1)*limit))) {
      long id = entity.getKey().getId();
      String displayName = (String) entity.getProperty("displayName");
      String comment = (String) entity.getProperty("comment");
      String blobKey = (String) entity.getProperty("imageBlob");
      String imageUrl = getImageUrl(blobKey);

      UserService userService = UserServiceFactory.getUserService();
      boolean isOwner = userService.isUserLoggedIn() &&
        ((String) entity.getProperty("userId")).equals(userService.getCurrentUser().getUserId());
      
      Translation translation = translate.translate(comment, Translate.TranslateOption.targetLanguage(targetLang));

      comments.add(new Comment(id, displayName, translation.getTranslatedText(), imageUrl, isOwner));
    }

    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String displayName = getParameter(request, "display-name", "");
    String comment = getParameter(request, "comment", "");
    long timestamp = System.currentTimeMillis();
    String imageBlob = getBlobKey(request, "image");

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/login");
    }

    Entity entity = new Entity("Comment");
    entity.setProperty("displayName", displayName);
    entity.setProperty("comment", comment);
    entity.setProperty("timestamp", timestamp);
    entity.setProperty("imageBlob", imageBlob);
    entity.setProperty("userId", userService.getCurrentUser().getUserId());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entity);
    
    response.sendRedirect("/index.html");
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getBlobKey(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    return blobKey.getKeyString();
  }

  private String getImageUrl(String blobKeyString) {
    if (blobKeyString == null) {
      return null;
    }

    BlobKey blobKey = new BlobKey(blobKeyString);

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }
}
