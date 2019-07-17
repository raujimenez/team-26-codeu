let map, infoWindow;
/* Editable marker that displays when a user clicks in the map. */
let editMarker;
const urlParameters = new URLSearchParams(window.location.search);
const pUsername = urlParameters.get('user');
function createMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 38.5949, lng: -94.8923},
        zoom: 5
    });

    fetchMarkers();


    //Get user location and show an open window with information
    infoWindow = new google.maps.InfoWindow;
    // Try HTML5 geolocation.
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            var pos = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };

            infoWindow.setPosition(pos);
            infoWindow.setContent('You are here');
            infoWindow.open(map);
            map.setCenter(pos);
        }, function() {
            handleLocationError(true, infoWindow, map.getCenter());
        });
    } else {
        // Browser doesn't support Geolocation
        handleLocationError(false, infoWindow, map.getCenter());
    }


// Create the search box and link it to the UI element.
    var input = document.getElementById('pac-input');
    var searchBox = new google.maps.places.SearchBox(input);
    map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

    // Bias the SearchBox results towards current map's viewport.
    map.addListener('bounds_changed', function() {
        searchBox.setBounds(map.getBounds());
    });

    var markers = [];
    // Listen for the event fired when the user selects a prediction and retrieve
    // more details for that place.
    searchBox.addListener('places_changed', function() {
        var places = searchBox.getPlaces();

        if (places.length == 0) {
            return;
        }

        // Clear out the old markers.
        markers.forEach(function(marker) {
            marker.setMap(null);
        });
        markers = [];

        // For each place, get the icon, name and location.
        var bounds = new google.maps.LatLngBounds();
        places.forEach(function(place) {
            if (!place.geometry) {
                console.log("Returned place contains no geometry");
                return;
            }
            var icon = {
                url: place.icon,
                size: new google.maps.Size(71, 71),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(17, 34),
                scaledSize: new google.maps.Size(25, 25)
            };

            // Create a marker for each place.
            markers.push(new google.maps.Marker({
                map: map,
                icon: icon,
                title: place.name,
                position: place.geometry.location
            }));

            if (place.geometry.viewport) {
                // Only geocodes have viewport.
                bounds.union(place.geometry.viewport);
            } else {
                bounds.extend(place.geometry.location);
            }
        });
        map.fitBounds(bounds);
    });

}

/**Handle error if cannot find user's location*/
function handleLocationError(browserHasGeolocation, infoWindow, pos) {
    infoWindow.setPosition(pos);
    infoWindow.setContent(browserHasGeolocation ?
        'Error: The Geolocation service failed.' :
        'Error: Your browser doesn\'t support geolocation.');
    infoWindow.open(map);
}

/** Fetches markers from the backend and adds them to the map. */
function fetchMarkers(){
    const url = '/userPageMarkers?user=' + pUsername;
    fetch(url).then((response) => {
        return response.json();
    }).then((markers) => {
        markers.forEach((marker) => {
            createMarkerForDisplay(marker.lat, marker.lng, marker.content, marker.markerid)
        });
    });
}
/** Creates a marker that shows a read-only info window when clicked. */
function createMarkerForDisplay(lat, lng, content, markerid){
    const marker = new google.maps.Marker({
        position: {lat: lat, lng: lng},
        map: map
    });
    var infoWindow = new google.maps.InfoWindow({
        content:"<div>"+content+"<br><button onclick=\"\location.href = 'viewListing.html?id="+markerid+"';\"\ >To The Post</button></div>"

    });
    marker.addListener('click', () => {
       infoWindow.open(map, marker);
    });
}
