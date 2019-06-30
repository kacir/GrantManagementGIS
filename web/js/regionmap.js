var regionSVG = {};

regionSVG.dissolve =  function(){
    turf.dissolve(regionSVG.json, {propertyName : "region"});
};

regionSVG.findRegions = function(){
    L.esri.query({url : "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/51"})
        .returnGeometry(false)
        .run(function(error, featureCollection, response){
            for (var i=0; i < featureCollection.features.length; i++){
                var esriFeature = featureCollection.features[i];
                //loop through simplified JSON
                for (var u=0; u <regionSVG.json.features.length; u++){
                    var simpleFeature = featureCollection.features[u];
                    if (esriFeature.properties.county === simpleFeature.properties.County_Nam){
                        simpleFeature.properties.region = esriFeature.properties.region2017;
                    }
                }
            }
            regionSVG.dissolved = turf.dissolve(regionSVG.json, {propertyName : "region"});
        });
};

regionSVG.launch =  function(){
    $.getJSON("data/county.json", function(data){
        regionSVG.json = data;

    });
};


regionSVG.buildSVG = function(cssSelector){
    var projection = d3.geoConicConformal()
        .scale(300)
        .center([0,0])
        .translate([469, 248])
        .rotate([90, -55, 9]);

    var geoGenerator = d3.geoPath()
        .projection(projection);

    d3.select(cssSelector)
        .selectAll("path")
        .data(regionSVG.dissolved.features)
        .enter()
        .append("path")
        .attr("d", geoGenerator);

};