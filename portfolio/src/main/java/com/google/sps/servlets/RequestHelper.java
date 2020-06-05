package com.google.sps.servlets;

import javax.servlet.http.HttpServletRequest;

public final class RequestHelper {
  private RequestHelper() {}

  /**
  * @return the request parameter, or the default value if the parameter
  *         was not specified by the client
  */
  public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}