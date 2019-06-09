console.log("Testing. Does this Work!");


var map = L.map('map').setView([ 34.7517595, -92.329416], 8);
map.zoomControl.setPosition("bottomleft");

var mapboxlink = "https://api.mapbox.com/styles/v1/robertkaciradpt/cjjrecba50sae2snpvqcw8ylq/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1Ijoicm9iZXJ0a2FjaXJhZHB0IiwiYSI6ImNqZ3BoODQ2NTAwM20ycXJ1OWpkZnh1emkifQ.MBfZdxZljkG8_JeivKerxw";
var parklessStreetBasemap = L.tileLayer(mapboxlink).addTo(map);
var Esri_WorldImagery = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});



var stateparkslayer = L.esri.dynamicMapLayer({
    url: "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_MapService_2017/MapServer"
});
var parkPolygon = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_scratch/FeatureServer/1",
    where : "type = 'funded park'",
    style : {fillColor : "#003300", stroke : false, fillOpacity : 1}
}).bindPopup(function(layer){
    return layer.feature.properties.currentNam;
}).addTo(map);

var parkIcon = L.icon({
    iconUrl : "/img/greenpark.png",
    iconSize : [12,12],
    iconAnchor : [6,6]
});
var grantPoint = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_scratch/FeatureServer/0",
    pointToLayer : function(feature, latlng){
        return L.marker(latlng, {icon : parkIcon});
    }
}).addTo(map);
var stateProjectBoundary = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/25",
    where : "type = 'state'",
    style : {fill : false, stroke : true, opacity : 1.0, color : "#FF0000", weight : 4.0}
});
var federalProjectBoundary = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/26",
    where : "type = 'federal'",
    style : {fill : false, stroke : true, opacity : 1.0, color : "#ffff00", weight : 4.0}
});
var conversionpolygons = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/24",
    style : function(feature) {
        if (feature.properties.type == "past conversion"){
            return {color : "#FF0090" , fillColor : "#FF0090" , fillOpacity : 1, weight : 4.0};
        }
        else if (feature.properties.type == "replacement"){
            return {color : "#99ff33" , fillColor : "#99ff33" , fillOpacity : 1, weight : 4.0};
        } else if (feature.properties.type == "converted replacement"){
            return {color : "#99ff33" , fillColor : "#FF0090" , fillOpacity : 1, weight : 4.0};
        } else {
            return {fill : false, stroke : false};
        };
    }
});
conversionpolygons.bindPopup(function(layer){
    var conversionDescription = "<h4>" + layer.feature.properties.yearoccur + "-" + layer.feature.properties.label +
        ": </h4> <br> <p> " + layer.feature.properties.note1 + layer.feature.properties.note1 + "</p>";
    return conversionDescription;
});

var houseDistricts = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/15"})
    .bindTooltip(function(layer){
        return "District " + layer.feature.properties.ndistrict + ": " + layer.feature.properties.name;
});
var senateDistricts = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/34"})
    .bindTooltip(function(layer){
        return "District " + layer.feature.properties.ndistrict + ": " + layer.feature.properties.name;
});
var regions = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/52",
    style : function(feature){
        if (feature.properties.region2017 == "region 1"){
            return {fillColor : "pink", color : "white", fillOpacity : 0.9};
        } else if (feature.properties.region2017 == "region 2"){
            return {fillColor : "blue" , color : "white", fillOpacity : 0.9};
        } else if (feature.properties.region2017 == "region 3"){
            return {fillColor : "green" , color : "white", fillOpacity : 0.9};
        } else {
            return {fill: false , stroke : false};
        }
    }
});



var overlayMaps = {"State Parks" : stateparkslayer ,
    "Park Polygon" : parkPolygon,
    "Grant Point" : grantPoint,
    "State Project Boundary" : stateProjectBoundary,
    "Federal Project Boundary" : federalProjectBoundary,
    "Conversion Polygon" : conversionpolygons,
    "House Districts" : houseDistricts,
    "Senate Districts" : senateDistricts,
    "Project Officier Regions" : regions
};
var baseMaps = { "Streets" : parklessStreetBasemap, "Aerial" : Esri_WorldImagery};
L.control.layers(baseMaps, overlayMaps).addTo(map);

L.Control.geocoder({position : "topleft"}).addTo(map);