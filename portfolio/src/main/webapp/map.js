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

let markerEdit;

let markers = [];

/** Creates a map and adds it to the page. */
function createMap() {
  map = new google.maps.Map(
    document.getElementById('map'),
    {center: {lat: 41.8317, lng: -95.9914}, zoom: 4});
  
  fetch('/user-status').then(resp => resp.json()).then(user => {
    if (user.loggedIn) {
      map.addListener('click', (event) => {
        createMarkerEdit(event.latLng.lat(), event.latLng.lng());
      });
    } else {
      const loginOut = document.getElementById("login-out");
      loginOut.href = "/login"
      loginOut.innerText = "Login";
    }
  });

  fetchMarkers();
}

function fetchMarkers() {
  fetch('/markers').then(resp => resp.json()).then(markers => {
    markers.forEach(marker => {
      createMarkerDisplay(marker.lat, marker.lng, marker.desc, marker.id, marker.isOwner);
    });
  })
}

function deleteMarker(id) {
  let params = new URLSearchParams();
  params.append('id', id);
  
  fetch('/delete-marker', {method: 'POST', body: params});
}

function createMarkerDisplay(lat, lng, desc, id, isOwner) {
  const marker = new google.maps.Marker({
    position: {
      lat: lat,
      lng: lng
    },
    map: map
  });

  const template = document.getElementById("display-marker-template");
  const copy = template.content.cloneNode(true).querySelector(".display-marker");

  const descElement = copy.querySelector(".marker-description");
  descElement.innerText = desc;

  if (isOwner) {
    const deleteButton = copy.querySelector('.delete-marker');
    deleteButton.classList.remove("hidden");
  

    deleteButton.onclick = () => {
      marker.setMap(null);
      
      deleteMarker(id);
    }
  }


  const infoWindow = new google.maps.InfoWindow({content: copy});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

function createMarkerEdit(lat, lng) {
	if(markerEdit) {
    markerEdit.setMap(null);
  }

  markerEdit = new google.maps.Marker({
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
    markerEdit.setMap(null);
  });

  infoWindow.open(map, markerEdit);
}

function saveMarker(lat, lng, desc, onId) {
  const params = new URLSearchParams();
  params.append("lat", lat);
  params.append("lng", lng);
  params.append("desc", desc);

  fetch("/markers", {method: "POST", body: params}).then(resp => resp.json()).then(marker => {
    onId(marker.id);
  });
}

function buildInfoWindow(lat, lng) {
  const template = document.getElementById("edit-marker-template");
  const copy = template.content.cloneNode(true).querySelector(".edit-marker");

  const button = copy.querySelector(".submit-marker");
  const textBox = copy.querySelector(".marker-desc-box")

  button.onclick = () => {
    saveMarker(lat, lng, textBox.value, id => {
      createMarkerDisplay(lat, lng, textBox.value, id, true);
    
    	markerEdit.setMap(null);
    });
  };

  return copy;
}