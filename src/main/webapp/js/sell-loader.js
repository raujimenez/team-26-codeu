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

/**
 * Shows the message form if the user is logged in and viewing their own page.
 */
function showMessageFormIfLoggedIn() {
    fetch('/login-status')
        .then((response) => {
            return response.json();
        })
        .then((loginStatus) => {
            if (loginStatus.isLoggedIn) {
                const messageForm = document.getElementById('message-container');
                messageForm.innerHTML = '<div id="message-container">Loading...</div>';
                fetchBlobstoreUrlAndShowForm();
            }
        });
}

/**
 * Builds the rich text editor using CKEditor 5
 */
function buildTextBox() {
    ClassicEditor.create(document.getElementById('message-input'), {
        removePlugins: ['MediaEmbed', 'ImageToolbar', 'ImageUpload']
    });
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
            messageContainer= document.getElementById('message-container');
            messageContainer.innerHTML = '';
        });
}


/** Fetches data and populates the UI of the page. */
function buildUI() {
    addLoginOrLogoutLinkToNavigation();
    showMessageFormIfLoggedIn();
    buildTextBox();
}