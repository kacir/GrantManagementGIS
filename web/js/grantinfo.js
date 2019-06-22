var formatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2
});

//bind and event to the auto suggestion to get results
//bind an event to the search button and enter key for these results

//make a function that populates the results panel with information
function makeResults (sponsorcode){
    //make a json request to the backend for the info
    var sponsorsummary = $("#sponsor-summary");
    var resultsElement = $("#grant-results-box");
    var accordionApplied = false;

    var sponsorSearchinputBox = $("#sponsor-search");


    function displayGrantDetails (sponsor, projectNumbers){
        //change the url parameters based on if its a request of a particular sponsor or a particular project number set
        var parameters = {};
        if (sponsor === null || sponsor === undefined){
            parameters.projectNumbers = projectNumbers;
        } else {
            parameters.sponsor = sponsor;
        };

        $.getJSON("/api/grantdetails?sponsor=" + sponsor , function(data){
            console.log("Grant Results back from server are: ");
            console.log(data);

            //if the accordion exists then get rid of it and all of the contents of the div element
            if (!accordionApplied === false){
                resultsElement.accordion("destroy");
                $(".park-tooltip").tooltip("destroy");
            };
            resultsElement.html("");

            for (var i = 0; i < data.length; i++){
                var grant = data[i];

                //in the grant is withdrawn include that info in the title of the accordion
                var withdrawnHeader = "";
                if (grant.hasOwnProperty("status")){
                    if (grant.status.toUpperCase() == "WITHDRAWN"){
                        withdrawnHeader = "<span class='withdrawn'>WITHDRAWN</span>";
                    } else if (grant.status.toUpperCase() == "TRANSFERED"){
                        withdrawnHeader = "<span class='transfered'><strong>TRANSFERED</strong></span>";
                    };
                } else {
                    grant.status = "Unknown";
                };
                if (!grant.hasOwnProperty("projecttype")){
                    grant.projecttype = "Unknown";
                };
                if (grant.hasOwnProperty("awardamount")){
                    grant.awardamount = formatter.format(Number(grant.awardamount));
                } else {
                    grant.awardamount = "Unknown";
                };


                var grantContent = "<h3>" + grant.projectnum + "-" + grant.year.slice(2,4) + " " + withdrawnHeader  + "</h3>";
                grantContent += "<div><div class='container'>" +
                    "<div class='row'><div class='col-12'><strong>Project Name: </strong><span>" + grant.projectname + "</span></div></div>" +
                    "<div class='row'><div class='col-12'><strong>Park Name: </strong><div id='park-hover-for-" + grant.projectnum + "'></div></div></div>" +
                    "<div class='row'><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Award Amount: </strong><span>" + grant.awardamount + "</span></div><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Project Status: </strong><span>" + grant.status + "</span></div><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Project Type: </strong><span>" + grant.projecttype + "</span></div></div>";

                //depending on the completion status or withrawn status. grant may not have certain properties. do not include in table if they are not needed
                if (grant.hasOwnProperty("itemsapplication")){
                    if (grant.hasOwnProperty("itemscompleted")){
                        if (grant.itemsapplication === grant.itemscompleted){
                            grant.itemscompleted = "Same as application";
                        };
                        grantContent += "<div class='row'><div class='col-12'><strong>Items Completed</strong><p>" + grant.itemscompleted + "</p></div></div>";
                    };
                    grantContent += "<div class='row'><div class='col-12'><strong>Items on Application</strong><p>" + grant.itemsapplication + "</p></div></div>";
                };

                grantContent += "</div></div>";
                resultsElement.append(grantContent);//closes the containing and content div
                console.log("looping through results to make div elements");

                insertParkNames(grant.projectnum);

            };
            resultsElement.accordion({heightStyle : "content"});
            accordionApplied = true;



        });
    };

    function insertParkNames (projectnum){
        //for the intersted element make a ESRI query which get the park name and park number and adds to the park span of the accordion
        L.esri.query({"url": "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/28"})
            .where("projectNum = '" + projectnum + "'")
            .run(function(error, gPointfeatureCollection, response){
                //loop through each gpoint for a grant
                for (var x = 0; x < gPointfeatureCollection.features.length; x++){
                    var point = gPointfeatureCollection.features[x].geometry;
                    L.esri.query({"url": "http://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38"})
                        .intersects(point)
                        .run(function(error, parkFeatureCollection, response){
                            console.log("parks returned from backend for grant " + projectnum);
                            console.log(parkFeatureCollection);
                            var park = parkFeatureCollection.features[0];
                            if (park.properties.hasOwnProperty("pastName") && !(park.properties.pastName === " " || park.properties.pastName === "")){
                                var title = "Previously called: " + park.properties.pastName;
                            } else {
                                var title = "Park has only had one name";
                            };

                            $("#park-hover-for-" + projectnum).append("<span __parknum='" + park.properties.parkNum + "' __projectnum='" + projectnum +  "' title='" + title  + "' OBJECTID='" + park.properties.OBJECTID + "' class='park-tooltip'> " + park.properties.currentNam + " </span>");
                            //bind the tooltip at this point because we can't time something after all the ESRI query's finish


                            var parkSelector = $("span[__parknum='" + park.properties.parkNum + "'][__projectnum='" + projectnum + "']")
                            parkSelector.tooltip();
                            parkmap.parkHover(parkSelector);
                            //bind a function to zoom into the right part of the map on click
                            //bind function which heights on hover, and hover gives grant details
                        });
                };
            });
    };


    sponsorSearchinputBox.on("keypress", function(event){
       if (event.key === "Enter"){
           displayGrantDetails(sponsorSearchinputBox.val(), null);
       };
    });

    var grantinfosearchButton = $("#grant-info-search-button");
    grantinfosearchButton.on("click", function(){
        displayGrantDetails(sponsorSearchinputBox.val(), null);
    });

    sponsorSearchinputBox.autocomplete({autoFocus : true, minLength: 2, source :
            function(request, response){
                $.getJSON("/api/sponsorsearch?searchterm=" + request.term, function(data){
                    console.log(data);
                    var maxResults = 10;
                    if (data.length > maxResults){
                        data = data.slice(0, maxResults -1);
                    };

                    var i;
                    for (i = 0; i < data.length; i++){
                        if (data[i].type === "sponsor"){
                            data[i].label = data[i].sponsor;
                            data[i].value = data[i].sponsor;
                        } else {
                            data[i].label = data[i].projectnum + "-" + data[i].year.slice(2,4) + "  -  " + data[i].sponsor;
                            data[i].value = data[i].projectnum;
                        };

                    };
                    response(data);
                });
            }});
};

//run the contents of the mod
makeResults();