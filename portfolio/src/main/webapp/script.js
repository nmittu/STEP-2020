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

/**
 * Adds a random greeting to the page.
 */
function addRandomFact() {
  const facts =
      ['I played trumpet in High School.', 'I am a physics minor.',
       'I\'m vegetarian', 'I don\'t have any pets :('];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}


function onLoad() {
  setUpImagePopups();
  fetchData();
}


var popup;
var popup_img;

function showPopUp() {
  popup.style.display = "block";
  popup_img.src = this.src
}

function setUpImagePopups() {
  popup = document.getElementsByClassName("image-popup-container")[0];
  popup_img = document.getElementsByClassName("image-popup")[0];

  var images = document.getElementsByTagName("img");


  for(image of images) {
    image.onclick = showPopUp;
  }

  var close_button = document.getElementsByClassName("close")[0];

  close_button.onclick = function() {
    popup.style.display = "none";
  }
}

function fetchData() {
    fetch("/data").then(response => response.json()).then(comments => {
      let commentsContainer = document.getElementById("servlet-response")
      for (let comment of comments) {
        let template = document.getElementById("comment-template");
        let copy = template.content.cloneNode(true);

        copy.getElementById("display-name").innerText = comment.displayName;
        copy.getElementById("comment").innerText = comment.comment;

        commentsContainer.appendChild(copy);
      }
    });
}
