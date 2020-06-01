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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.comments.Comment;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
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

    ArrayList<Comment> comments = new ArrayList<>();
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    int i = 0;
    for (Entity entity : results.asIterable()) {
      i++;
      if (i <= (page-1)*limit) {
        continue;
      }
      
      String displayName = (String) entity.getProperty("displayName");
      String comment = (String) entity.getProperty("comment");

      comments.add(new Comment(displayName, comment));

      if (comments.size() >= limit) {
        break;
      }
    }

    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String displayName = getParameter(request, "display-name", "");
    String comment = getParameter(request, "comment", "");
    long timestamp = System.currentTimeMillis();

    Entity entity = new Entity("Comment");
    entity.setProperty("displayName", displayName);
    entity.setProperty("comment", comment);
    entity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entity);
    
    response.sendRedirect("index.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
