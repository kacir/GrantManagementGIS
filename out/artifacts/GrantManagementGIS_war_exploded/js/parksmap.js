console.log("Testing. Does this Work!");


var map = L.map('map', {minZoom : 7, maxBounds : [ [ 30.232947, -98.151799], [ 38.872223, -87.048198] ]})
    .setView([ 34.7517595, -92.329416], 7);
map.zoomControl.setPosition("bottomleft");

var mapboxlink = "https://api.mapbox.com/styles/v1/robertkaciradpt/cjjrecba50sae2snpvqcw8ylq/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1Ijoicm9iZXJ0a2FjaXJhZHB0IiwiYSI6ImNqZ3BoODQ2NTAwM20ycXJ1OWpkZnh1emkifQ.MBfZdxZljkG8_JeivKerxw";
var parklessStreetBasemap = L.tileLayer(mapboxlink).addTo(map);
var Esri_WorldImagery = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});



var stateparkslayer = L.esri.dynamicMapLayer({
    url: "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_MapService_2017/MapServer"
});
var parkPolygon = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38",
    where : "type = 'funded park'",
    style : {fillColor : "#003300", stroke : false, fillOpacity : 1}
}).bindPopup(function(layer){
    console.log(layer.feature.properties);
    var popupText = "<h3>" + layer.feature.properties.currentNam + "</h3><table>" +
        "<tr><td>Previously:</td><td>" +layer.feature.properties.pastName + "</td></tr>" +
        "<tr><td>Sponsor:</td><td>" +layer.feature.properties.sponsorshi + "</td></tr>" +
        "<tr><td>Inspection Date:</td><td>" + new Date(layer.feature.properties.inspDate).toDateString()  + "</td></tr>" +
        "<tr><td>LWCF Area:</td><td>50 Acres</td></tr>" +
        "<tr><td>State Area:</td><td>60 Acres</td></tr>" +
        "<tr><td>Total Park Area:</td><td>" + parseFloat(layer.feature.properties.calc_acre).toFixed(2) + " Acres</td></tr>" +
        "</table>" +
        "<div class='container'><div class='row'><div class='col popup-button'><span>ind Grant in Park</span></div><a href='" + layer.feature.properties.boxlink + "'><div class='col popup-button'><span>Doc Scans</span></div></a><a href='" + layer.feature.properties.googleLink + "'><div class='col popup-button'><span>Driving Directions</span></div></a></div></div>";
    return popupText;
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
    var conversionDescription = "<h3>" + layer.feature.properties.yearoccur + "-" + layer.feature.properties.label +
        ": </h3> <br> <p> " + layer.feature.properties.note1 + layer.feature.properties.note1 + "</p>";
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
    "Park Point" : grantPoint,
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


var legendControl = L.control({position : "bottomright"});
legendControl.onAdd = function (map) {
    this._div = L.DomUtil.create('div', 'legend-control'); // create a div with a class "legend-control"
    this.update();
    return this._div;
};
// method that we will use to update the control based on feature properties passed
legendControl.update = function (props) {

    if (map.hasLayer(parkPolygon)){
        var parkPolygonClass = "";
    } else {
        var parkPolygonClass = "hidden"
    }

    if (map.hasLayer(grantPoint)){
        var grantPointClass = "";
    } else {
        var grantPointClass = "hidden"
    }

    if (map.hasLayer(stateProjectBoundary)){
        var stateProjectBoundaryClass = "";
    } else {
        var stateProjectBoundaryClass = "hidden"
    }

    if (map.hasLayer(federalProjectBoundary)){
        var federalProjectClass = "";
    } else {
        var federalProjectClass = "hidden"
    }

    if (map.hasLayer(conversionpolygons)){
        var conversionPolygonClass = "";
    } else {
        var conversionPolygonClass = "hidden"
    }

    if (map.hasLayer(houseDistricts)){
        var houseDistrictClass = "";
    } else {
        var houseDistrictClass = "hidden"
    }

    if (map.hasLayer(senateDistricts)){
        var senateDistrictClass = "";
    } else {
        var senateDistrictClass = "hidden"
    }

    if (map.hasLayer(regions)){
        var regionsClass = "";
    } else {
        var regionsClass = "hidden"
    }

    this._div.innerHTML = "<h4>Legend</h4><div class='" + parkPolygonClass + " '><span>Funded Park</span><svg width='25' height='25'><rect width='25' height='25' style='fill:green;stroke:green;stroke-width:3;fill-opacity:0.9'></rect></svg></div>" +
        "<div class='" + grantPointClass + "'><span>Park Point</span><img src='img/greenpark.png' width='25' height='25' /></div>" +
        "<div class='" + stateProjectBoundaryClass + "'><span>State Project Boundary</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:none;stroke:red;stroke-width:3'></rect></svg> </div>" +
        "<div class='" + federalProjectClass + "' ><span>Federal Project Boundary</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:none;stroke:yellow;stroke-width:3'></rect></svg> </div>" +
        "<div class='" + conversionPolygonClass + "' ><span>Converted Area</span><svg width='25' height='25'><rect width='25' height='25' style='fill:deeppink;stroke:deeppink;stroke-width:3'></rect></svg></div> " +
        "<div class='" + conversionPolygonClass + "' ><span>Replacement Property</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:lawngreen;stroke:lawngreen;stroke-width:3'></rect></svg></div>" +
        "<div class='" + conversionPolygonClass + "' ><span>Converted Replacement Property</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:deeppink;stroke:lawngreen;stroke-width:3'></rect></svg></div>" +
        "<div class='" + houseDistrictClass + "' ><span>House District</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:blue;stroke:blue;stroke-width:3;fill-opacity:0.5'></rect></svg></div>" +
        "<div class='" + senateDistrictClass + "' ><span>Senate District</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:blue;stroke:blue;stroke-width:3;fill-opacity:0.5'></rect></svg></div>" +
        "<div class='" + regionsClass + "' ><span>Northwest Region</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:blue;stroke:white;stroke-width:3;fill-opacity:0.9'></rect></svg></div>" +
        "<div class='" + regionsClass + "' ><span>Northeast Region</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:deeppink;stroke:white;stroke-width:3;fill-opacity:0.9'></rect></svg></div>" +
        "<div class='" + regionsClass + "' ><span>Southern Region</span> <svg width='25' height='25'><rect width='25' height='25' style='fill:green;stroke:white;stroke-width:3;fill-opacity:0.9'></rect></svg></div>";
};
legendControl.addTo(map);

map.on("overlayadd overlayremove", function(eo){
    legendControl.update();
    console.log("updating legend contents");
});