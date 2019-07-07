const urlParams = new URLSearchParams(window.location.search);
const parameterID = urlParams.get("id");

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
  const headerDiv = document.createElement("div");
  headerDiv.classList.add("message-header");
  headerDiv.classList.add("padded");
  headerDiv.classList.add("border-bottom");

  headerDiv.appendChild(
    document.createTextNode(listing.user + " - " + new Date(listing.timestamp))
  );

  const bodyDiv = document.createElement("div");
  bodyDiv.classList.add("message-body");
  bodyDiv.classList.add("padded");
  bodyDiv.classList.add("border-left");
  bodyDiv.classList.add("border-bottom");
  bodyDiv.classList.add("border-right");
  bodyDiv.innerHTML =  listing.title + listing.text;

  const listingDiv = document.createElement("div");
  listingDiv.classList.add("message-div");
  listingDiv.classList.add("rounded");
  listingDiv.classList.add("panel");
  listingDiv.appendChild(headerDiv);
  listingDiv.appendChild(bodyDiv);

  return listingDiv;
}

/** Fetches data and populates the UI of the page. */
function buildUI() {
  fetchListings();
  addLoginOrLogoutLinkToNavigation();
}