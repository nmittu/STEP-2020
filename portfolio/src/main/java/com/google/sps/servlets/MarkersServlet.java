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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.maps.Marker;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/markers")
public class MarkersServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ArrayList<Marker> markers = new ArrayList<>();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    Query query = new Query("Marker");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      double lat = (double) entity.getProperty("lat");
      double lng = (double) entity.getProperty("lng");
      String desc = (String) entity.getProperty("desc");
      boolean isOwner = userService.isUserLoggedIn() &&
        ((String) entity.getProperty("userId")).equals(userService.getCurrentUser().getUserId());
      
      markers.add(new Marker(id, lat, lng, desc, isOwner));
    }

    response.setContentType("text/json;");

    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(markers));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      double lat = Double.parseDouble(getParameter(request, "lat", "0"));
      double lng = Double.parseDouble(getParameter(request, "lng", "0"));
      String desc = getParameter(request, "desc", "");

      Entity entity = new Entity("Marker");
      entity.setProperty("lat", lat);
      entity.setProperty("lng", lng);
      entity.setProperty("desc", desc);
      entity.setProperty("userId", userService.getCurrentUser().getUserId());

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      long id = datastore.put(entity).getId();

      response.setContentType("text/json;");

      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(new Marker(id, lat, lng, desc, true)));
    } else {
      response.sendRedirect("/login");
    }
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
