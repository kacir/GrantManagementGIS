L.Control.Geocoder.CustomGeocoder = L.Class.extend({
    options: {
        serviceUrl: '/geocode'
    },

    initialize: function(options) {
        L.setOptions(this, options);
    },

    geocode: function(query, cb, context) {

        $.getJSON("/api/geocode?searchterm=" + query, function(data){
            var i;
            for (i = 0; i < data.length; i++){
                data[i].name = data[i].sponsor;

                data[i].center = L.latLng( data[i].lat, data[i].lon);
                var pointGeoJSON = turf.point([data[i].lon, data[i].lat]);
                //create an artifical bounding box for the area
                var bufferedPoint = turf.buffer(pointGeoJSON, 5, {units: "miles"});
                var bufferLayer = L.geoJSON(bufferedPoint);
                data[i].bbox = bufferLayer.getBounds();
            };

            //if some kind of result was taken then just return whats in the places dataset
            if (data.length > 0){
                cb.call(context, data);
            } else {
                console.log("running thing that will search ESRI");
                //if nothing was taken then search the ESRI rest service for a park name
                L.esri.query({"url" : "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38" })
                    .where("currentNam LIKE '%" + query + "%'")
                    .limit(5)
                    .fields(["currentNam", "pastName "])
                    .run(function(error, featureCollection, response){
                        console.log(featureCollection);
                        var results = [];
                        var a;
                        for (a = 0; a < featureCollection.features.length; a++){
                            console.log("making it through the loop");
                            var suggestion = {};
                            if (featureCollection.features[a].properties.pastName === " " || featureCollection.features[a].properties.pastName === "" || featureCollection.features[a].properties.pastName === undefined || featureCollection.features[a].properties.pastName === null){
                                suggestion.name = featureCollection.features[a].properties.currentNam;
                            } else {
                                suggestion.name = featureCollection.features[a].properties.currentNam + "(Prev " + featureCollection.features[a].properties.pastName + ")";
                            };
                            var turCenter = turf.centerOfMass(featureCollection.features[a]);
                            suggestion.center = L.latLng( turCenter.geometry.coordinates[1], turCenter.geometry.coordinates[0]);
                            suggestion.bbox = L.geoJSON(featureCollection.features[a]).getBounds();
                            results.push(suggestion);
                        };
                        console.log(results);
                        console.log(a);
                        cb.call(context, results);
                    });
            };
        });
    },

    suggest: function(query, cb, context) {
        return this.geocode(query, cb, context);
    }
});
