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

let map;

let marker;

/** Creates a map and adds it to the page. */
function createMap() {
  map = new google.maps.Map(
    document.getElementById('map'),
    {center: {lat: 41.8317, lng: -95.9914}, zoom: 4});

  map.addListener('click', (event) => {
    createMarkerEdit(event.latLng.lat(), event.latLng.lng());
  });

  fetchMarkers();
}

function fetchMarkers() {
  fetch('/markers').then(resp => resp.json()).then(markers => {
    markers.forEach(marker => {
      createMarkerDisplay(marker.lat, marker.lng, marker.desc);
    });
  })
}

function createMarkerDisplay(lat, lng, desc) {
  const marker = new google.maps.Marker({
    position: {
      lat: lat,
      lng: lng
    },
    map: map
  });

  const infoWindow = new google.maps.InfoWindow({content: desc});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  })
}

function createMarkerEdit(lat, lng) {
	if(marker) {
    marker.setMap(null);
  }

  marker = new google.maps.Marker({
    position: {
      lat:lat,
      lng,lng
    },
    map: map
  });

  const infoWindow = new google.maps.InfoWindow({
    content: buildInfoWindow(lat, lng)
  });

  google.maps.event.addListener(infoWindow, 'closeclick', () => {
    marker.setMap(null);
  });

  infoWindow.open(map, marker);
}

function saveMarker(lat, lng, desc) {
  const params = new URLSearchParams();
  params.append("lat", lat);
  params.append("lng", lng);
  params.append("desc", desc);

  fetch("/markers", {method: "POST", body: params});
}

function buildInfoWindow(lat, lng) {
  const textBox = document.createElement('textarea');
  const button = document.createElement('button');
  button.appendChild(document.createTextNode('Submit'));

  button.onclick = () => {
    saveMarker(lat, lng, textBox.value);
    createMarkerDisplay(lat, lng, textBox.value);
    
    marker.setMap(null);
  };

  const container = document.createElement('div');
  container.appendChild(textBox);
  container.appendChild(document.createElement('br'));
  container.appendChild(button);

  return container;
}