// Fetch messages and add them to the page.
function fetchMessages() {
    const url = '/allListings';
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

        cardDeck = buildDeck();

        messages.forEach((message) => {
            const messageDiv = buildMessageDiv(message);
            cardDeck.appendChild(messageDiv);
        });
        messageContainer.appendChild(cardDeck);
    });

    addLoginOrLogoutLinkToNavigation();
}

function buildDeck() {
    cardDeckDiv = document.createElement('div');
    cardDeckDiv.classList.add('card-deck');

    return cardDeckDiv;
}

// Builds an element that displays the message.
function buildMessageDiv(message) {

    /*
    const timeDiv = document.createElement('div');
    timeDiv.classList.add('right-align');
    timeDiv.appendChild(document.createTextNode(new Date(message.timestamp)));
    */ 

    const cardTitle = document.createElement('h5');
    cardTitle.classList.add('card-title');
    cardTitle.appendChild(document.createTextNode(message.title));

    const cardPrice = document.createElement('h6');
    cardPrice.classList.add('card-subtitle');
    cardPrice.classList.add('mb-2');
    cardPrice.classList.add('text-muted');
  //  cardPrice.appendChild(document.createTextNode(message.price));
    cardPrice.innerHTML = '<span class="badge badge-success"> $' + message.price.toFixed(2) + '</span>' + '</font>';

    const cardImage = document.createElement('img');
    cardImage.classList.add('card-img-top');
    if (message.imageUrl != '') {
        cardImage.src = message.imageUrl;
    }
    else {
        cardImage.src = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/No_image_available_600_x_450.svg/600px-No_image_available_600_x_450.svg.png';
    }

    const cardText = document.createElement('div');
    cardText.classList.add('card-text');
    cardText.innerHTML = message.text; 

    const cardBody = document.createElement('div');
    cardBody.classList.add('card-body');
    cardBody.appendChild(cardTitle);
    cardBody.appendChild(cardPrice);
    cardBody.appendChild(cardText);

    const cardBtn = document.createElement('a');
    cardBtn.classList.add('btn');
    cardBtn.classList.add('btn-primary');
    cardBtn.href = '/viewListing.html?id=' + message.id;
    cardBtn.innerHTML = 'See more details';

    const cardFooter = document.createElement('div');
    cardFooter.classList.add('card-footer');
    cardFooter.appendChild(cardBtn);


    const cardDiv = document.createElement('div');
    cardDiv.classList.add('card');
    cardDiv.style.cssText = 'width: 18rem;';
    cardDiv.appendChild(cardImage);
    cardDiv.appendChild(cardBody);
    cardDiv.appendChild(cardFooter);

    return cardDiv;
}

// Fetch data and populate the UI of the page.
function buildUI() {
    fetchMessages();
}