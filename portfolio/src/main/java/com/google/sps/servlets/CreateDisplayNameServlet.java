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

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import static com.google.sps.servlets.RequestHelper.getParameter;


@WebServlet("/create-display-name")
public class CreateDisplayNameServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();

    String returnUrl = getParameter(req, "return", "/");

    if (userService.isUserLoggedIn()) {
      if (getDisplayName(userService.getCurrentUser().getEmail()) == null) {
        RequestDispatcher view = req.getRequestDispatcher("/WEB-INF/create-display-name.html");
        view.forward(req, resp);
      } else {
        resp.sendRedirect(returnUrl);
      }
    } else {
      resp.sendRedirect("/login");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String returnUrl = getParameter(req, "return", "/");
    String displayName = getParameter(req, "display-name", "");

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      Entity entity = new Entity("User");
      entity.setProperty("email", userService.getCurrentUser().getEmail());
      entity.setProperty("displayName", displayName);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(entity);

      resp.sendRedirect(returnUrl);
    } else {
      resp.sendRedirect("/login");
    }
  }

  public String getDisplayName(String email) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("User");
    query.setFilter(new Query.FilterPredicate(
      "email", 
      Query.FilterOperator.EQUAL, 
      email));

    PreparedQuery results = datastore.prepare(query);

    Iterator<Entity> entities = results.asIterable().iterator();

    if (entities.hasNext()) {
      return (String) entities.next().getProperty("displayName");
    } else {
      return null;
    }
  }
}