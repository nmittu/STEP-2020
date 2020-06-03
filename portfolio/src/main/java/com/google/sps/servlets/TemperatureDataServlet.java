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

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/temperature-data")
public class TemperatureDataServlet extends HttpServlet {
	private final LinkedHashMap<Date, Double> temperatureData = new LinkedHashMap<>();

  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(
      "/WEB-INF/temperature_data.csv"
    ));

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (Character.isDigit(line.charAt(0))) {
        try {
          String[] cells = line.split(",");

          if (cells.length >= 2) {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(cells[0]);
            Double temp = Double .parseDouble(cells[1]);

            temperatureData.put(date, temp);
          }
        } catch (ParseException e) {
          continue;
        }
      }
    }

    scanner.close();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    Gson gson = new Gson();
    resp.getWriter().println(gson.toJson(temperatureData));
  }
}