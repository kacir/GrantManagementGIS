console.log("Testing. Does this Work!");


var map = L.map('map').setView([ 34.7517595, -92.329416], 8);
var mapboxlink = "https://api.mapbox.com/styles/v1/robertkaciradpt/cjjrecba50sae2snpvqcw8ylq/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1Ijoicm9iZXJ0a2FjaXJhZHB0IiwiYSI6ImNqZ3BoODQ2NTAwM20ycXJ1OWpkZnh1emkifQ.MBfZdxZljkG8_JeivKerxw";
L.tileLayer(mapboxlink).addTo(map);

L.esri.dynamicMapLayer({
    url: "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer"
}).addTo(map);