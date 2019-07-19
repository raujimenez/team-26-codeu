/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Get ?user=XYZ parameter value
const urlParams = new URLSearchParams(window.location.search);
const parameterUsername = urlParams.get('user');

// URL must include ?user=XYZ parameter. If not, redirect to homepage.
if (!parameterUsername) {
  window.location.replace('/');
}

/** Sets the page title based on the URL parameter username. */
function setPageTitle() {
  document.getElementById('page-title').innerText = parameterUsername;
  document.title = parameterUsername + ' - User Page';
}

/**
 * Shows the message form if the user is logged in and viewing their own page.
 */
/*
function showMessageFormIfViewingSelf() {
  fetch('/login-status')
      .then((response) => {
        return response.json();
      })
      .then((loginStatus) => {
        if (loginStatus.isLoggedIn &&
            loginStatus.username == parameterUsername) {
          const messageForm = document.getElementById('message-form');
          messageForm.classList.remove('hidden');
        }
      });
}
*/
/** Fetches messages and add them to the page. */
function fetchMessages() {
  const url = '/messages?user=' + parameterUsername;
  fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((messages) => {
        const messagesContainer = document.getElementById('message-container');
        if (messages.length == 0) {
          messagesContainer.innerHTML = '<p>This user has no posts yet.</p>';
        } else {
          messagesContainer.innerHTML = '';
        }
        unorderedList = createList();
        messages.forEach((message) => {
          if(message.title != "marker_in_map") {
            // Temporary solution: create card only if it is a listing and not a marker
          const messageDiv = buildMessageDiv(message); 
          unorderedList.appendChild(messageDiv);
          }
        });
        messageCard = createCard(unorderedList);
        messagesContainer.appendChild(messageCard);
      });
}

function createList() {
  const creatingList = document.createElement("ul");
  creatingList.classList.add('list-group');
  creatingList.classList.add('list-group-flush');

  return creatingList;
}

function createCard(cardList) {
  const createDiv = document.createElement("div");
  createDiv.classList.add("card");
  createDiv.classList.add('bg-info');
  createDiv.classList.add('mb-3');

  createDiv.appendChild(cardList);

  return createDiv;
}

/**
 * Builds an element that displays the message.
 * @param {Message} message
 * @return {Element}
 */
function buildMessageDiv(message) {
  const cardBtn = document.createElement('a');
  cardBtn.classList.add('btn');
  cardBtn.classList.add('btn-primary');
  cardBtn.classList.add('float-right');
  cardBtn.href = '/viewListing.html?id=' + message.id;
  cardBtn.innerHTML = 'See more details';

  const cardPrice = document.createElement('span');
  cardPrice.classList.add('badge');
  cardPrice.classList.add('badge-success');
  cardPrice.style = 'margin-left: 0.5em';
  cardPrice.innerHTML = ' $' + message.price.toFixed(2) + '</font>';

  const listItem = document.createElement('li'); 
  listItem.classList.add('list-group-item');
  listItem.classList.add('bg-light');
  listItem.innerHTML = '<b>' + message.title + '</b>';
  listItem.appendChild(cardPrice);
  listItem.appendChild(cardBtn);

  return listItem;
}

/**
 * Builds the rich text editor using CKEditor 5
 */
function buildTextBox() {
  ClassicEditor.create( document.getElementById('message-input') );
}

function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url')
    .then((response) => {
      return response.text();
    })
    .then((imageUploadUrl) => {
      const messageForm = document.getElementById('message-form');
      messageForm.action = imageUploadUrl;
      messageForm.classList.remove('hidden');
    });
}

/** Fetches data and populates the UI of the page. */
function buildUI() {
  fetchBlobstoreUrlAndShowForm();
  setPageTitle();
  buildTextBox();
  fetchMessages();
  addLoginOrLogoutLinkToNavigation();
}