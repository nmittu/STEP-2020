package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    String referer = req.getHeader("referer");
    if (referer == null) {
      referer = "/";
    }

    String returnUrl = "/create-display-name?return=" + referer;

    if (!userService.isUserLoggedIn()) {
      resp.sendRedirect(userService.createLoginURL(returnUrl));
    } else {
      resp.sendRedirect(returnUrl);
    }
  }
}