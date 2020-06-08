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

google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
  fetch('/temperature-data').then(resp => resp.json()).then(tempData => {
    const data = new google.visualization.DataTable();
    data.addColumn('date', 'Date');
    data.addColumn('number', 'Avg. Temperature');
    data.addColumn('number', 'Max. Temperature')

    Object.keys(tempData).forEach(date => {
      data.addRow([new Date(Date.parse(date)), tempData[date].avg, tempData[date].max]);
    });

    const options = {
      'title': 'Historic Temperatures',
      'width': 600,
      'height': 500
    }

    const chart = new google.visualization.LineChart(
      document.getElementById('temperature-chart'));

    chart.draw(data, options);
  });
}