function createMap(){
    const map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 37.3352, lng: -121.8811},
        zoom: 14
    });
    const trexMarker = new google.maps.Marker({
        position: {lat: 37.337444, lng: -121.878080},
        map: map,
        title: 'My Home'
    });
    var trexInfoWindow = new google.maps.InfoWindow({
        content: 'Garage Sale at Sunday 10AM!!!'
    });
    trexInfoWindow.open(map, trexMarker);
}