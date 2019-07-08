const urlParams = new URLSearchParams(window.location.search);
const parameterID = urlParams.get("id");

/* Pass in the listings object from fetchListings to set the title */
function setPageTitle(listing) {
  document.title = listing.title;
}

function fetchListings() {
  const url = "/listings?id=" + parameterID;
  fetch(url)
    .then(response => {
      return response.json();
    })
    .then(listings => {
      const listingsContainer = document.getElementById("listing-container");
      if (listings.length == 0) {
        listingsContainer.innerHTML = "<p>The post with id = " + parameterID + "</p>";
      } else {
        listingsContainer.innerHTML = "";
      }
      listings.forEach(listing => {
        setPageTitle(listing);
        const listingDiv = buildListingsDiv(listing);
        listingsContainer.appendChild(listingDiv);
      });
    });
}

/**
 * Builds an element that displays the Listing.
 * @param {Listing} listing
 * @return {Element}
 */
function buildListingsDiv(listing) {
  
  /* contains image */
  const pictureDiv = document.createElement("img");
  pictureDiv.classList.add("col-lg-6");
  pictureDiv.classList.add("col-md-6");
  pictureDiv.classList.add("col-sm");
  pictureDiv.src = "http://newnation.sg/wp-content/uploads/random-pic-internet-07.jpg";
  pictureDiv.alt = "seller image";

  /* create card-header for title and timestamp */
  const cardHeader = document.createElement("div");
  cardHeader.classList.add("card-header");
  
  /* formatted using table to better align items */
  const tableDiv = document.createElement("table");
  tableDiv.classList.add("table");
  tableDiv.classList.add("table-borderless");
  tableDiv.setAttribute("style", "margin: 0");
  
  const tableBody = document.createElement("tbody");
  const tableRow = document.createElement("tr");
  
  const listingTitle = document.createElement("td");
  listingTitle.innerHTML = '<font size=+1.5>' + listing.title + '</font>';
  const listingDate = document.createElement("td");
  listingDate.classList.add("text-right");
  listingDate.innerHTML = '<font class="text-right text-muted" size="-1">' + new Date(listing.timestamp).toLocaleTimeString('en-US') + '</font>';

  tableRow.appendChild(listingTitle);
  tableRow.appendChild(listingDate);

  tableBody.appendChild(tableRow);
  tableDiv.appendChild(tableBody);
  
  cardHeader.appendChild(tableDiv);

  /* contains description of listing */
  const cardBody = document.createElement("div");
  cardBody.classList.add("card-body");
  cardBody.innerHTML = listing.text;

  const cardFooter = document.createElement("div");
  cardFooter.classList.add("card-footer");
  cardFooter.classList.add("text-center");

  /* Creates contact information */
  const contactDiv = document.createElement("a");
  contactDiv.classList.add("btn");
  contactDiv.classList.add("btn-primary");
  contactDiv.href = "mailto:" + listing.user;
  contactDiv.innerHTML = "Send them an Email";
  
  cardFooter.appendChild(contactDiv);

  /* Container that formats title, description, and contact information of listing */
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("card");
  infoDiv.classList.add("h-100");
  infoDiv.appendChild(cardHeader);
  infoDiv.appendChild(cardBody);
  infoDiv.appendChild(cardFooter);
  
  /* needed to make listing info same size as image */
  const cardContainer = document.createElement("div");
  cardContainer.classList.add("col-lg-6");
  cardContainer.classList.add("col-md-6");
  cardContainer.classList.add("col-sm");

  cardContainer.appendChild(infoDiv);

  /* contains all information in a listing */
  const listingDiv = document.createElement("div");
  listingDiv.classList.add("card-deck");
  listingDiv.appendChild(pictureDiv);
  listingDiv.appendChild(cardContainer);

  return listingDiv;
}

/** Fetches data and populates the UI of the page. */
function buildUI() {
  fetchListings();
  addLoginOrLogoutLinkToNavigation();
}