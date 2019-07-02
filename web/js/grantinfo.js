//formatter is used to change the grant award amount from the float into a money string
var formatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2
});

var grantInfoWindow = {};

//make a function that populates the results panel with information
grantInfoWindow.makeResults =  function (){
    //make a json request to the backend for the info
    var resultsElement = $("#grant-results-box");
    var accordionApplied = false;

    var sponsorSearchinputBox = $("#sponsor-search");

    grantInfoWindow.displayGrantDetails = function  (sponsor, projectNumbers, searchTitle){
        //change the url parameters based on if its a request of a particular sponsor or a particular project number set. need to deal with missing or null inputs to function
        var requestURL;
        if (!(sponsor === null || sponsor === undefined || sponsor === "" || sponsor === " ")){
            requestURL = "/api/grantdetails?term=" + sponsor;
        }
        if (!(projectNumbers === null || projectNumbers === undefined || projectNumbers === "" || projectNumbers === " ")){
            requestURL = "/api/grantdetails?projectnumbers=" + projectNumbers;
        }
        if (searchTitle === null || searchTitle === undefined || searchTitle === "" || searchTitle === " "){
            searchTitle = "<h3>Unknown Search</h3>"
        }

        $("#search-title").html(searchTitle);
        sponsorSearchinputBox.val("");

        //if the accordion exists then get rid of it and all of the contents of the div element
        if (!accordionApplied === false){
            resultsElement.accordion("destroy");
            $(".park-tooltip").tooltip("destroy");
        }

        //insert a loading giff while the data from the backend loads
        resultsElement.html("<img src='img/loading_large.gif' width='60%'/>");


        $.getJSON(requestURL , function(fullData){
            console.log("Grant Results back from server are: ");
            console.log(fullData);
            var data = fullData.grants;

            $("#sponsor-mini-map").addClass("hidden");
            resultsElement.html("");

            //If there are no results then display text explaining that to the end user
            if (data.length > 0){
                $("#no-results-text").addClass("hidden");
            } else {
                $("#no-results-text").removeClass("hidden");
            }

            //fill in the sponsor details box
            if (fullData.hasOwnProperty("sponsorDetails")) {
                parkmap.zoomToSponsor(fullData.sponsorDetails);
                var sponsorContent = "<table class='sponsor-summary-details'>";
                if (fullData.sponsorDetails.hasOwnProperty("county")){
                    sponsorContent += "<tr><td><strong>County: </strong></td><td>" + fullData.sponsorDetails.county + "</td></tr>";
                }
                if (fullData.sponsorDetails.hasOwnProperty("website")){
                    if (!(fullData.sponsorDetails.website.includes("http://") || fullData.sponsorDetails.website.includes("https://"))){
                        fullData.sponsorDetails.website = "http://" + fullData.sponsorDetails.website;
                        sponsorContent += "<tr><td><strong>Website</strong></td><td><a target='_blank' href='" + fullData.sponsorDetails.website + "'>" + fullData.sponsorDetails.website + "</a></td></tr>";
                    }
                }
                if (fullData.sponsorDetails.hasOwnProperty("projcount")){
                    sponsorContent += "<tr><td><strong>Grants Awarded </strong></td><td>" + parseInt(fullData.sponsorDetails.projcount).toLocaleString() + "</td></tr>";
                } else {
                    sponsorContent += "<tr><td><strong>Grants Awarded </strong></td><td>0</td></tr>";
                }

                if (fullData.sponsorDetails.hasOwnProperty("awardsum")){
                    sponsorContent += "<tr><td><strong>Sum of Grant Awards </strong></td><td>" + formatter.format(Number(fullData.sponsorDetails.awardsum)) + "</td></tr>";
                }

                if (fullData.sponsorDetails.hasOwnProperty("municipallink")){
                    sponsorContent += "<tr><td><strong>Mayor </strong></td><td><span id='sponsor-details-mayorname'><img height='20' src='img/loading.gif' /></span></td></tr>";
                    sponsorContent += "<tr><td><strong>Population </strong></td><td><span id='sponsor-details-population'><img height='20' src='img/loading.gif' /></span></td></tr>";
                    sponsorContent += "<tr><td><strong>Phone </strong></td><td><span id='sponsor-details-phone'><img height='20' src='img/loading.gif' /></span></td></tr>";
                    sponsorContent += "<tr><td><strong>Fax </strong></td><td><span id='sponsor-details-fax'><img height='20' src='img/loading.gif' /></span></td></tr>";
                    sponsorContent += "<tr><td><strong>Address </strong></td><td><span id='sponsor-details-address'><img height='20' src='img/loading.gif' /></span></td></tr>";
                }

                sponsorContent += "</table>";

                if (fullData.sponsorDetails.hasOwnProperty("lat") && fullData.sponsorDetails.hasOwnProperty("lon") ){
                    regionSVG.buildSVG("#sponsor-mini-map");
                    regionSVG.plotPoint("#sponsor-mini-map", [fullData.sponsorDetails.lon, fullData.sponsorDetails.lat] );
                    $("#sponsor-mini-map").removeClass("hidden")
                }

                //fill in the grant sponsor details at the top of the results panel
                $("#sponsor-summary").html(sponsorContent);

                if (fullData.sponsorDetails.hasOwnProperty("municipallink")){
                    console.log("requesting remote data from municipal league");
                    $.post("/api/remoteinfo", {url : fullData.sponsorDetails.municipallink, type : "city"}, function(remoteData){
                        console.log("remote data found is: ");
                        console.log(remoteData);
                        $("#sponsor-details-mayorname").html(remoteData.mayorname);
                        $("#sponsor-details-population").html(remoteData.population);
                        $("#sponsor-details-phone").html(remoteData.phone);
                        $("#sponsor-details-fax").html(remoteData.fax);
                        $("#sponsor-details-address").html(remoteData.address.replace(new RegExp(",", "g"), ",<br/>"));

                    }).fail(function(jqxhr, textStatus, error){
                        var err = textStatus + ", " + error;
                        console.log( "Request Failed: " + err );
                    });
                }


            } else {
                $("#sponsor-summary").html("");
            }

            for (var i = 0; i < data.length; i++){
                var grant = data[i];

                //in the grant is withdrawn include that info in the title of the accordion
                var withdrawnHeader = "";
                if (grant.hasOwnProperty("status")){
                    if (grant.status.toUpperCase() === "WITHDRAWN"){
                        withdrawnHeader = "<span class='withdrawn'>WITHDRAWN</span>";
                    } else if (grant.status.toUpperCase() === "TRANSFERED"){
                        withdrawnHeader = "<span class='transfered'><strong>TRANSFERED</strong></span>";
                    }
                } else {
                    grant.status = "Unknown";
                }
                if (!grant.hasOwnProperty("projecttype")){
                    grant.projecttype = "Unknown";
                }
                if (grant.hasOwnProperty("awardamount")){
                    grant.awardamount = formatter.format(Number(grant.awardamount));
                } else {
                    grant.awardamount = "Unknown";
                }


                var grantContent = "<h3>" + grant.projectnum + "-" + grant.year.slice(2,4) + " " + withdrawnHeader  + "</h3>";
                grantContent += "<div><div class='container'>" +
                    "<div class='row'><div class='col-12'><strong>Project Name: </strong><span>" + grant.projectname + "</span></div></div>";

                grantContent += "<div class='row'><div class='col'><strong>Sponsors: </strong>";
                //for each of the joint sponsor on a project, add them to the list
                for (var u = 0; u < grant.sponsorList.length; u++){
                    grantContent += "<span class='join-sponsors' displayname='" + grant.sponsorList[u].displayname + "' sponsorcode='" + grant.sponsorList[u].sponsorcode +"'>" + grant.sponsorList[u].sponsor + "</span>";
                }
                grantContent += "</div></div>";

                grantContent += "<div class='row'><div class='col-12'><strong>Park Name: </strong><div class='park-elements-div' id='park-hover-for-" + grant.projectnum + "'></div></div></div>" +
                    "<div class='row'><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Award Amount: </strong><span>" + grant.awardamount + "</span></div><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Project Status: </strong><span>" + grant.status + "</span></div><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Project Type: </strong><span>" + grant.projecttype + "</span></div></div>";

                //depending on the completion status or withrawn status. grant may not have certain properties. do not include in table if they are not needed
                if (grant.hasOwnProperty("itemsapplication")){
                    if (grant.hasOwnProperty("itemscompleted")){
                        if (grant.itemsapplication === grant.itemscompleted){
                            grant.itemscompleted = "Same as application";
                        }
                        grantContent += "<div class='row'><div class='col-12'><strong>Items Completed</strong><p>" + grant.itemscompleted + "</p></div></div>";
                    }
                    grantContent += "<div class='row'><div class='col-12'><strong>Items on Application</strong><p>" + grant.itemsapplication + "</p></div></div>";
                }

                grantContent += "</div></div>";//close out the accordon div and boostrap container class
                resultsElement.append(grantContent);//closes the containing and content div
                console.log("looping through results to make div elements");

                insertParkNames(grant.projectnum);

            }
            resultsElement.accordion({heightStyle : "content"});
            //this attribute is needed because, if the accordion is destroyed before its created it
            // launches an exception. This variable keeps track of if it exists so the exception does not occur
            accordionApplied = true;

            //function called when a sponsor button is click on in the details of an individual grant
            $(".join-sponsors").on("click", function(){
                var sponsorcode = $(this).attr("sponsorcode");
                var displayname = $(this).attr("displayname");
                grantInfoWindow.displayGrantDetails(displayname, null, "<p>Grants sponsored by</p><h3>" + displayname +  "</h3>")
            });



        });
    };

    function insertParkNames (projectnum){
        //for the selected grant element make a ESRI query which get the park name and park numbers related to the grant and adds to the park span of the grant details parks field.
        L.esri.query({"url": "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/28"})
            .where("projectNum = '" + projectnum + "'")
            .run(function(error, gPointfeatureCollection, response){
                //loop through each gpoint for a grant
                for (var x = 0; x < gPointfeatureCollection.features.length; x++){
                    var point = gPointfeatureCollection.features[x].geometry;
                    L.esri.query({"url": "https://gis.arkansas.gov/arcgis/rest/services/ADPT/ADPT_ORGP_MASTER2/MapServer/38"})
                        .intersects(point)
                        .run(function(error, parkFeatureCollection, response){
                            console.log("parks returned from backend for grant " + projectnum);
                            console.log(parkFeatureCollection);
                            var park = parkFeatureCollection.features[0];
                            if (park.properties.hasOwnProperty("pastName") && !(park.properties.pastName === " " || park.properties.pastName === "")){
                                var title = "Previously called: " + park.properties.pastName;
                            } else {
                                var title = "Park has only had one name";
                            }

                            $("#park-hover-for-" + projectnum).append("<span __parknum='" + park.properties.parkNum + "' __projectnum='" + projectnum +  "' title='" + title  + "' OBJECTID='" + park.properties.OBJECTID + "' class='park-tooltip'> " + park.properties.currentNam + " </span>");
                            //bind the tooltip at this point because we can't time something after all the ESRI query's finish


                            var parkSelector = $("span[__parknum='" + park.properties.parkNum + "'][__projectnum='" + projectnum + "']")
                            parkSelector.tooltip();
                            parkmap.parkHover(parkSelector);
                            //bind a function to zoom into the right part of the map on click
                            //bind function which heights on hover, and hover gives grant details
                        });
                }
            });
    }


    sponsorSearchinputBox.on("keypress", function(event){
       if (event.key === "Enter"){
           grantInfoWindow.displayGrantDetails(sponsorSearchinputBox.val(), null, "<p>Grants sponsored by</p><h3>" + sponsorSearchinputBox.val() + "</h3>");
       }
    });

    var grantinfosearchButton = $("#grant-info-search-button");
    grantinfosearchButton.on("click", function(){
        grantInfoWindow.displayGrantDetails(sponsorSearchinputBox.val(), null, "<p>Grants sponsored by</p><h3>" + sponsorSearchinputBox.val() + "</h3>");
    });

    sponsorSearchinputBox.autocomplete({autoFocus : true, html: true, minLength: 2, source :
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
                            if (data[i].hasOwnProperty("projcount")){
                                data[i].label = "<strong>" + data[i].displayname + "</strong> " + data[i].projcount + " Grants";
                            } else {
                                data[i].label = "<strong>" + data[i].displayname + "</strong>" + " 0 Grants";
                            }

                            data[i].value = data[i].displayname;
                        } else {
                            data[i].label = "<strong>" + data[i].projectnum + "-" + data[i].year.slice(2,4) + "</strong>  -  " + data[i].displayname;
                            data[i].value = data[i].projectnum;
                        }

                    }
                    response(data);
                });
            }});
};

//run the contents of the mod
grantInfoWindow.makeResults();