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
  setUpCommentForm();
}

function setUpCommentForm() {
  fetch('/user-status').then(resp => resp.json()).then(user => {
    if (user.loggedIn) {
      document.getElementById("display-name").value = user.displayName;

      fetch('/image-url').then(resp => resp.text()).then(url => {
        const form = document.getElementById("comments-form");
        form.action = url;
        form.classList.remove('hidden');
      })
    } else {
      const loginOut = document.getElementById("login-out");
      loginOut.href = "/login"
      loginOut.innerText = "Login";
    }
  })
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


let page = 1;

function nextPage() {
  page++;
  document.getElementById("pageNum").innerText = page;
  refreshComments();
}

function prevPage() {
  page = Math.max(page-1, 1);
  document.getElementById("pageNum").innerText = page;
  refreshComments();
}


function refreshComments() {
  let commentsContainer = document.getElementById("comments-container");
  commentsContainer.querySelectorAll(".comment").forEach(ele => ele.remove());

  fetchData();
}

function fetchData() {
    let limit = document.getElementById("limit").value;
    let targetLang = document.getElementById("targetLang").value;

    fetch(`/data?limit=${limit}&targetLang=${targetLang}&page=${page}`)
      .then(response => response.json()).then(comments => {
        let commentsContainer = document.getElementById("comments-container");
        for (let comment of comments) {
          let template = document.getElementById("comment-template");
          let copy = template.content.cloneNode(true).querySelector(".comment");

          copy.querySelector(".display-name").innerText = comment.displayName;
          let commentElement = copy.querySelector(".comment-content");
          commentElement.innerText = comment.comment;

          
          let sentimentElement = copy.querySelector('.sentiment');
          sentimentElement.innerText = getSentimentEmoji(comment.sentiment);

          let commentImage = copy.querySelector(".comment-image");
          if (comment.imageUrl != null) {
            commentImage.classList.remove("hidden");
            commentImage.src = comment.imageUrl
          }

          copy.style.visibility = "hidden";
          commentsContainer.appendChild(copy);

          let showMoreButton = copy.querySelector(".show-more");

          if (commentElement.clientHeight === commentElement.scrollHeight) {
            showMoreButton.style.display = "none";
          }

          let deleteButton = copy.querySelector(".delete-comment");
          if (comment.isOwner){
            deleteButton.addEventListener('click', () => {
              deleteComment(comment.id);

              refreshComments();
            });
          } else {
            deleteButton.remove();
          }

          copy.style.visibility = "";
        }
      });
}

function getSentimentEmoji(score) {
  if (score >= 0.7) {
    return String.fromCodePoint(0x1F601);
  } else if (score >= 0.4) {
    return String.fromCodePoint(0x1F600);
  } else if (score >= 0.2) {
    return String.fromCodePoint(0x1F610);
  } else if (score >= -0.4) {
    return String.fromCodePoint(0x1F611);
  } else if (score >= -0.6) {
    return String.fromCodePoint(0x1F620);
  } else {
    return String.fromCodePoint(0x1F621);
  }
}


function toggleShowMore() {
  let commentElement = event.srcElement.parentElement.getElementsByClassName("comment-content")[0];

  if (commentElement.style.maxHeight != "none") {
    commentElement.style.maxHeight = "none";
    event.srcElement.innerText = "Hide";
  } else {
    commentElement.style.maxHeight = null;
    event.srcElement.innerText = "Show more";
  }
}

function deleteComment(id) {
  let request = new Request(`/delete-data?id=${id}`, {method: 'POST'});
  fetch(request);
}
