

//bind and event to the auto suggestion to get results
//bind an event to the search button and enter key for these results

//make a function that populates the results panel with information
function makeResults (sponsorcode){
    //make a json request to the backend for the info
    var sponsorsummary = $("#sponsor-summary");
    var resultsElement = d3.select("#grant-results-box");

    var sponsorSearchinputBox = $("#sponsor-search");


    function displayGrantDetails (sponsor, projectNumbers){
        console.log("going to search for grants related to - " + sponsor);
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

            //empty it from previous searches
            resultsElement.html("")
                .selectAll(".individual-grant")
                .data(data)
                .enter()
                .append("div")
                .attr("class" , ".individual-grant")
                .html(function(d){
                    var grantContent = "<h3>" + d.projectnum + "-" + d.year.slice(2,4)  +  "</h3>" +
                        "<label>Grant Type: </label><span>" + d.granttype + "</span>";
                    return grantContent;
                });



        });
    };


    sponsorSearchinputBox.on("keypress", function(event){
        console.log("some kind of key was pressed!");
       if (event.key === "Enter"){
           displayGrantDetails(sponsorSearchinputBox.val(), null);
           console.log("enter key has been pressed!");
       };
    });

    var grantinfosearchButton = $("#grant-info-search-button");
    grantinfosearchButton.on("click", function(){
        displayGrantDetails(sponsorSearchinputBox.val(), null);
        console.log("search has been clicked on!");
    });

    sponsorSearchinputBox.autocomplete({autoFocus : true, minLength: 2, source :
            function(request, response){
                console.log("request for autocomplete has been made");
                $.getJSON("/api/sponsorsearch?searchterm=" + request.term, function(data){
                    var i;
                    for (i = 0; i < data.length; i++){
                        data[i].label = data[i].sponsor;
                        data[i].value = data[i].sponsor;
                    };
                    console.log("sending data into the autocomplete for use!");
                    console.log(data);
                    response(data);
                });
            }});
};

//run the contents of the mod
makeResults();