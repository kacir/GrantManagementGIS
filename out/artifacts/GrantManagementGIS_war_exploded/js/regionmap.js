var regionSVG = {};

regionSVG.example = function(){
    //example code that can be eliminated later on
    regionSVG.buildSVG("#example");
    regionSVG.plotPoint("#example", [-92.300207, 34.742863]);
};

regionSVG.launch =  function(){
    $.getJSON("data/region.json", function(data){
        regionSVG.json = data;

        console.log("simplified region geometry");
        console.log(data);
    });
};
//Long lat list - in that order
regionSVG.plotPoint = function(cssSelector, coordinatePair){
    console.log("plot mini map point called");

    d3.select(cssSelector)
        .select("circle")
        .remove();

    d3.select(cssSelector)
        .append("circle")
        .attr("cx", regionSVG.projection(coordinatePair)[0])
        .attr("cy", regionSVG.projection(coordinatePair)[1])
        .attr("r", "5px")
        .attr("fill", "green");
};

regionSVG.buildSVG = function(cssSelector){

    var width = 200;
    var height = 200;

    regionSVG.projection = d3.geoAlbers()
        .rotate([95.55, 0, 0])
        .parallels([22.91, 55.02])
        .scale(1573.74)
        .translate( [0 ,  0])
        .fitSize([width, height], regionSVG.json);

    regionSVG.geoGenerator = d3.geoPath()
        .projection(regionSVG.projection);

    d3.select(cssSelector)
        .attr("width", width)
        .attr("height", height)
        .selectAll("path")
        .data(regionSVG.json.features)
        .enter()
        .append("path")
        .attr("fill", "#DCDCDC")
        .attr("stroke", "black")
        .attr("stroke-width" , 1)
        .attr("d", regionSVG.geoGenerator);
};

/*
        .center([0,0])
        .translate([469, 248])
        .rotate([90, -55, 9])
 */

regionSVG.launch();
console.log("everything in SVG builder has started");