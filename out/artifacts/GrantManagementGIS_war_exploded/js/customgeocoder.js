L.Control.Geocoder.CustomGeocoder = L.Class.extend({
    options: {
        serviceUrl: '/geocode'
    },

    initialize: function(options) {
        L.setOptions(this, options);
    },

    geocode: function(query, cb, context) {

        //Generate a result object as an example and send to rest of function.
        var results = [];
        var bounds = L.latLngBounds([
            [ 34.714890, -92.369638],
            [ 34.773560, -92.294150]
        ]);
        var centerpoint = L.latLng( 34.758854, -92.326194);

        /*use this geocoding engine if the homebase one does not return results
        https://nominatim.openstreetmap.org/search?q=little&countrycodes=us&state=arkansas&format=json
         */

        results[0] = {
            name : "Town",
            bbox : bounds,
            center : centerpoint,
            properties : {text: "text associated" , address : "Bullfrog lane, Clay County"}
        };

        cb.call(context, results);

    },

    suggest: function(query, cb, context) {
        return this.geocode(query, cb, context);
    }
});
