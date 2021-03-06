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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/delete-data")
public class DeleteServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    long id = Long.parseLong(request.getParameter("id"));

    try {
      Key commentKey = KeyFactory.createKey("Comment", id);
      Entity entity = datastore.get(commentKey);

      UserService userService = UserServiceFactory.getUserService();
      boolean isOwner = userService.isUserLoggedIn() &&
          ((String) entity.getProperty("userId")).equals(userService.getCurrentUser().getUserId());

      if (!isOwner) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized to delete this comment.");
      }

      String blobKeyString = (String) datastore.get(commentKey).getProperty("imageBlob");
      if (blobKeyString != null) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        BlobKey blobKey = new BlobKey(blobKeyString);

        blobstoreService.delete(blobKey);
        System.out.println("Deleted");
      }


      datastore.delete(commentKey);
    } catch (EntityNotFoundException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Key does not exist");
    }
  }
}
