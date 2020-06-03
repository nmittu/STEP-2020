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

      resp.sendRedirect(returnUrl);
    } else {
      resp.sendRedirect("/login");
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