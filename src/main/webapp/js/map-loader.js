function createMap(){
    const map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 37.3352, lng: -121.8811},
        zoom: 14
    });
}