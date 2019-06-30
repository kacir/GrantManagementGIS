parkmap = {};

//an initializer function that sets up the map and most of events and event listeners
parkmap.start = function(){

parkmap.map = L.map('map', {minZoom : 7, maxBounds : [ [ 30.232947, -98.151799], [ 38.872223, -87.048198] ]})
    .setView([ 34.7517595, -92.329416], 7);
parkmap.map.zoomControl.setPosition("bottomleft");

//load the basemap information
var mapboxlink = "https://api.mapbox.com/styles/v1/robertkaciradpt/cjjrecba50sae2snpvqcw8ylq/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1Ijoicm9iZXJ0a2FjaXJhZHB0IiwiYSI6ImNqZ3BoODQ2NTAwM20ycXJ1OWpkZnh1emkifQ.MBfZdxZljkG8_JeivKerxw";
var parklessStreetBasemap = L.tileLayer(mapboxlink).addTo(parkmap.map);
var Esri_WorldImagery = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});


//the ADPT state parks master layer maintained by Darin
var stateparkslayer = L.esri.dynamicMapLayer({
    url: "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_MapService_2017/MapServer"
});

//function feeds both the park point and park polygon popup construction.
function parkPopupBuild (feature) {
    var popupText = "<h3>" + feature.properties.currentNam + "</h3><table>";
    if (!(feature.properties.pastName === undefined || feature.properties.pastName === null || feature.properties.pastName === "" || feature.properties.pastName === " ")){
        popupText = popupText + "<tr><td>Previously:</td><td>" +feature.properties.pastName + "</td></tr>";
    };
    popupText = popupText + "<tr><td>Sponsor:</td><td>" +feature.properties.sponsorshi + "</td></tr>" +
        "<tr><td>Inspection Date:</td><td>" + new Date(feature.properties.inspDate).toDateString()  + "</td></tr>";

    //Some features lack federal boundary. Do not include the table row if its not needed.
    if (feature.properties.fed6f3Stat === "has 6(f)3 boundary") {
        if (!feature.properties.hasOwnProperty("fedeprojectarea")){
            feature.properties.fedeprojectarea = "<img height='20' src='img/loading.gif' />";

            L.esri.query({ url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/25"})
                .where("type = 'federal'")
                .intersects(feature.geometry)
                .run(function(error, featureCollection, response){
                    if (featureCollection.features.legnth == 0){
                        alert("Query returned no features!");
                    }
                    var selectedGeometry = featureCollection.features[0].geometry;
                    var squaremetersArea = turf.area(selectedGeometry);
                    var acresOfProjectArea = turf.convertArea(squaremetersArea, "meters", "acres").toFixed(2);
                    feature.properties.fedeprojectarea = acresOfProjectArea;
                    $("#lwcf-acres").text(acresOfProjectArea);
                });
        }
        popupText = popupText + "<tr><td>LWCF Area:</td><td><span id='lwcf-acres'>" + feature.properties.fedeprojectarea + "</span> Acres</td></tr>";
    }

    //Some features lack state boundary. Do not include the table row if its not needed.
    if (feature.properties.state6f3St === "has 6(f)3 boundary"){
        if (!feature.properties.hasOwnProperty("stateprojectarea")){
            feature.properties.stateprojectarea = "\"<img height='20' src='img/loading.gif' />\"";

            L.esri.query({ url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/25"})
                .where("type = 'state'")
                .intersects(feature.geometry)
                .run(function(error, featureCollection, response){
                    if (featureCollection.features.legnth == 0){
                        alert("Query returned no features!");
                    }
                    var selectedGeometry = featureCollection.features[0].geometry;
                    var squaremetersArea = turf.area(selectedGeometry);
                    var acresOfProjectArea = turf.convertArea(squaremetersArea, "meters", "acres").toFixed(2);
                    feature.properties.stateprojectarea = acresOfProjectArea;
                    $("#anrc-acres").text(acresOfProjectArea);
                });
        }
        popupText = popupText + "<tr><td>State Area:</td><td><span id='anrc-acres'>" + feature.properties.stateprojectarea + "</span> Acres</td></tr>";


    };
    popupText = popupText + "<tr><td>Total Park Area:</td><td>" + parseFloat(feature.properties.calc_acre).toFixed(2) + " Acres</td></tr>" +
        "</table>" +
        "<div class='container'><div class='row'><div id='cross-reference-park-to-grant' onclick='parkmap.crossReferenceParkToGrant()' OBJECTID='" + feature.properties.OBJECTID + "' class='col popup-button'><span>Grants in Park</span></div><a target='_blank' href='" + feature.properties.boxlink + "'><div class='col popup-button'><span>Doc Scans</span></div></a><a target='_blank' href='" + feature.properties.googleLink + "'><div class='col popup-button'><span>Driving Directions</span></div></a></div></div>";
        //the on click function is very important. When the button is pressed, the grant info window searches for the grants related to the park that the popup represents
    return popupText;
};

//on click function referenced in the park popup
parkmap.crossReferenceParkToGrant = function(){
    var OBJECTID = $("#cross-reference-park-to-grant").attr("OBJECTID");
    var selectedPark = parkmap.parkPolygon.getFeature(OBJECTID);
    //find each of the related grant numbers
    L.esri.query({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/28"})
        .intersects(selectedPark)
        .run(function(error, featureCollection, response){
            var stringNumerList = "";//the backend can't directly handel array objects but it can handel strings. The backend splits the string into an array using the space character
            for (var t = 0; t < featureCollection.features.length; t++){
                stringNumerList += featureCollection.features[t].properties.projectNum + " ";
            }
            //make request to the grant window to search for the grants related to the park.
            grantInfoWindow.displayGrantDetails(null, stringNumerList, "<p>given on this park</p><h3>" + selectedPark.feature.properties.currentNam + "</h3>");

        });
};
//park polygon style needed to be refferended by a few functions that change layer symbology based on hover affects.
//These hover effects need to be result by referencing the style that first created the layer
parkmap.parkPolygonStyle = {fillColor : "#008000", stroke : false, fillOpacity : 1};
parkmap.parkPolygon = L.esri.featureLayer({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38",
    where : "type = 'funded park'",
    style : parkmap.parkPolygonStyle
}).bindPopup(function(layer){
    var feature = layer.feature;
    return parkPopupBuild(feature);
}).addTo(parkmap.map);

//the park icon needed to be referenced by another function so hover effects on the markers can be reset.
parkmap.parkIcon = L.icon({
    iconUrl : "/img/greenpark.png",
    iconSize : [12,12],
    iconAnchor : [6,6]
});

parkmap.parkCentroidLayer = L.geoJSON(null, {pointToLayer : function(feature, latlng){
        return L.marker(latlng, {icon : parkmap.parkIcon});
    }}).bindPopup(function(layer){
        //query for the park footprint below the point
        L.esri.query({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38"})
            .where("type = 'funded park'")
            .intersects(layer.feature.geometry)
            .run(function(error, featureCollection, response){
                if (featureCollection.features.length === 0){

                    //buffer the centroid out if it did not catch the footprint because the center of the park is actually outside of the park
                    var bufferedPoint = turf.buffer(layer.feature.geometry, 0.04572, {units : "kilometers"});
                    L.esri.query({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38"})
                        .where("type = 'funded park'")
                        .intersects(bufferedPoint)
                        .run(function(error, secondFeatureCollection, response){
                            var parkfootprintFeature = secondFeatureCollection.features[0];
                            //send that footprint to the popupgenerator function
                            var popupcontents = parkPopupBuild(parkfootprintFeature);
                            $("#park-point-popup").html(popupcontents);
                        });
                } else {
                    var parkfootprintFeature = featureCollection.features[0];
                    //send that footprint to the popupgenerator function
                    var popupcontents = parkPopupBuild(parkfootprintFeature);
                    $("#park-point-popup").html(popupcontents);
                }
            });
        return "<div id='park-point-popup'> Loading! </div>";//there is a time delay because of the chained ESRI query requests to the parks layer. This is a placeholder until the information is loaded
});
parkmap.parkCentroidLayer.addTo(parkmap.map);

//attempt to find the centorid of all funded parks and then add all those park centers to the park points layer
// could not use the grantpoint ESRI layer because it was not flexible enough to use the same popup as the park polygon
// The speed is reasonable based on the fact there are less than 1k of point features
L.esri.query({url : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38"})
    .where("type = 'funded park'")
    .run(function(error, featureCollection, response){
        featureCollection.features.forEach(function(feature){
            var pointFeature = turf.centerOfMass(feature.geometry);
            pointFeature.properties.pastName = feature.properties.pastName;
            pointFeature.properties.parkNum = feature.properties.parkNum;
            pointFeature.properties.OBJECTID = feature.properties.OBJECTID;
            pointFeature.properties.currentNam = feature.properties.currentNam;
            pointFeature.properties.sponsorshi = feature.properties.sponsorshi;
            pointFeature.properties.fed6f3Stat = feature.properties.fed6f3Stat;
            pointFeature.properties.state6f3St = feature.properties.state6f3St;
            pointFeature.properties.calc_acre = feature.properties.calc_acre;

            parkmap.parkCentroidLayer.addData(pointFeature);
        });
    });

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



var overlayMaps = {"Park Polygon" : parkmap.parkPolygon,
    "Park Point" : parkmap.parkCentroidLayer,
    "State Project Boundary" : stateProjectBoundary,
    "Federal Project Boundary" : federalProjectBoundary,
    "Project Officier Regions" : regions,
    "Conversion Polygon" : conversionpolygons,
    "House Districts" : houseDistricts,
    "Senate Districts" : senateDistricts,
    "State Parks" : stateparkslayer
};
var baseMaps = { "Streets" : parklessStreetBasemap, "Aerial" : Esri_WorldImagery};
L.control.layers(baseMaps, overlayMaps).addTo(parkmap.map);

//implemented a custom geocoder for this project which uses custom grant sponsor information for arkansas.
var myCoderEngine = new L.Control.Geocoder.CustomGeocoder();
L.Control.geocoder({position : "topleft", geocoder : myCoderEngine, placeholder : "Town, Park, or Street Address"}).addTo(parkmap.map);


var legendControl = L.control({position : "bottomright"});
legendControl.onAdd = function (map) {
    this._div = L.DomUtil.create('div', 'legend-control'); // create a div with a class "legend-control"
    this.update();
    return this._div;
};
// method that we will use to update the control based on feature properties passed
legendControl.update = function (props) {

    if (parkmap.map.hasLayer(parkmap.parkPolygon)){
        var parkPolygonClass = "";
    } else {
        var parkPolygonClass = "hidden"
    }

    if (parkmap.map.hasLayer(parkmap.parkCentroidLayer)){
        var grantPointClass = "";
    } else {
        var grantPointClass = "hidden"
    }

    if (parkmap.map.hasLayer(stateProjectBoundary)){
        var stateProjectBoundaryClass = "";
    } else {
        var stateProjectBoundaryClass = "hidden"
    }

    if (parkmap.map.hasLayer(federalProjectBoundary)){
        var federalProjectClass = "";
    } else {
        var federalProjectClass = "hidden"
    }

    if (parkmap.map.hasLayer(conversionpolygons)){
        var conversionPolygonClass = "";
    } else {
        var conversionPolygonClass = "hidden"
    }

    if (parkmap.map.hasLayer(houseDistricts)){
        var houseDistrictClass = "";
    } else {
        var houseDistrictClass = "hidden"
    }

    if (parkmap.map.hasLayer(senateDistricts)){
        var senateDistrictClass = "";
    } else {
        var senateDistrictClass = "hidden"
    }

    if (parkmap.map.hasLayer(regions)){
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
legendControl.addTo(parkmap.map);

parkmap.map.on("overlayadd overlayremove", function(eo){
    legendControl.update();
    console.log("updating legend contents");
});

};

//function binds event listeners to the park span elements listed in the grant info window when results are shown
parkmap.parkHover = function(parkSelector){
    var OBJECTID = parkSelector.attr("OBJECTID");
    var parkNum = parkSelector.attr("__parknum");

    //zoom to the park when it is click on
    parkSelector.on("click", function(event){
        var selectedPark = parkmap.parkPolygon.getFeature(OBJECTID);
        var parkBounds = selectedPark.getBounds();
        parkmap.map.flyToBounds(parkBounds);
    });

    //change opacity and size of selected park on hover
    parkSelector.on("mouseover", function(event){
        parkmap.parkCentroidLayer.eachLayer(function(layer){
            if (layer.feature.properties.parkNum === parkNum){
                layer.setOpacity(1.0);
                //Change size of icon temporarily
                var multiplyfactor = 3;
                var largeParkIcon = L.icon({iconSize : [parkmap.parkIcon.options.iconSize[0]*multiplyfactor ,parkmap.parkIcon.options.iconSize[1]*multiplyfactor],
                    iconAnchor : [parkmap.parkIcon.options.iconAnchor[0]*multiplyfactor, parkmap.parkIcon.options.iconAnchor[1]*multiplyfactor],
                    iconUrl : parkmap.parkIcon.options.iconUrl
                });
                layer.setIcon(largeParkIcon);
            } else {
                layer.setOpacity(0.1);
            };
        });
        parkmap.parkPolygon.eachActiveFeature(function(layer){
            if (layer.feature.properties.parkNum == parkNum){
                layer.setStyle({
                    color : "#003D02",
                    fillColor : "#003D02",
                    stroke : true,
                    weight : 3,
                    fillOpacity : 1.0,
                    opacity : 1.0
                });
            } else {
                layer.setStyle({opacity : 0.3, fillOpacity : 0.3});
            };
        });
    });

    //revert the opacity, size, and color settings for the park point and park polygon layers
    parkSelector.on("mouseleave", function(event){
        parkmap.parkPolygon.resetStyle(OBJECTID);
        parkmap.parkCentroidLayer.eachLayer(function(layer){
            layer.setOpacity(1.0);
            layer.setIcon(parkmap.parkIcon);
        });
        parkmap.parkPolygon.eachActiveFeature(function(layer){
            layer.setStyle(parkmap.parkPolygonStyle);
        });
    });

};

//zooms to the sponsor function. called when grant search window stuff is launched
parkmap.zoomToSponsor = function(sponsorDetails){
    var sponsorLocation = L.latLng(sponsorDetails.lat, sponsorDetails.lon);

    if (sponsorDetails.hasOwnProperty("city_fips_")){
        L.esri.query({url : "http://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/41"})
            .where("city_fips = " + sponsorDetails.city_fips_)
            .run(function(error, featureCollection, response){
                if (featureCollection.features.length > 0){
                    var municipalExtent = L.geoJSON(featureCollection).getBounds();
                    parkmap.map.flyToBounds(municipalExtent);
                } else {
                    parkmap.map.flyTo(sponsorLocation);
                }
            });
    } else {
        if (sponsorDetails.type === "County"){
            L.esri.query({url : "https://gis.arkansas.gov/arcgis/rest/services/FEATURESERVICES/Boundaries/FeatureServer/8"})
                .intersects(sponsorLocation)
                .run(function(error, featureCollection, response){
                    if (featureCollection.features.length > 0){
                        var countyExtent = L.geoJSON(featureCollection).getBounds();
                        parkmap.map.flyToBounds(countyExtent);
                    } else {
                        parkmap.map.setZoomAround(sponsorLocation, 12);
                    }
                });
        } else if (sponsorDetails.type === "State"){
            //zoom to the full extent of the state
            parkmap.map.setView([ 34.7517595, -92.329416], 7);
        } else {
            parkmap.map.setZoomAround(sponsorLocation, 12);
        }
    }
};

//run the contents of the script within a function rather than global scope
parkmap.start();