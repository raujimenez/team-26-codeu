// Fetch messages and add them to the page.
function fetchMessages() {
    const url = '/feed';
    fetch(url).then((response) => {
        return response.json();
    }).then((messages) => {
        const messageContainer = document.getElementById('message-container');
        if (messages.length == 0) {
            messageContainer.innerHTML = '<p>There are no posts yet.</p>';
        }
        else {
            messageContainer.innerHTML = '';
        }
        messages.forEach((message) => {
            const messageDiv = buildMessageDiv(message);
            messageContainer.appendChild(messageDiv);
        });
    });

    addLoginOrLogoutLinkToNavigation();
}

// Builds an element that displays the message.
function buildMessageDiv(message) {
    const usernameDiv = document.createElement('div');
    usernameDiv.classList.add("left-align");
    usernameDiv.appendChild(document.createTextNode(message.user));

    const timeDiv = document.createElement('div');
    timeDiv.classList.add('right-align');
    timeDiv.appendChild(document.createTextNode(new Date(message.timestamp)));

    const headerDiv = document.createElement('div');
    headerDiv.classList.add('message-header');
    headerDiv.classList.add('padded');
    headerDiv.classList.add('border-bottom');
    headerDiv.appendChild(usernameDiv);
    headerDiv.appendChild(timeDiv);

    const bodyDiv = document.createElement('div');
    bodyDiv.classList.add('message-body');
    bodyDiv.classList.add('padded');
    bodyDiv.classList.add('border-left');
    bodyDiv.classList.add('border-bottom');
    bodyDiv.classList.add('border-right');
    bodyDiv.innerHTML = message.text; 
    
    const messageDiv = document.createElement('div');
    messageDiv.classList.add("message-div");
    messageDiv.classList.add('rounded');
    messageDiv.classList.add('panel');
    messageDiv.appendChild(headerDiv);
    messageDiv.appendChild(bodyDiv);

    return messageDiv;
}

// Fetch data and populate the UI of the page.
function buildUI() {
    fetchMessages();
}