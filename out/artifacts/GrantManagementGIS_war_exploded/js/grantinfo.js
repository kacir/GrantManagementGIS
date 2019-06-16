

//bind and event to the auto suggestion to get results
//bind an event to the search button and enter key for these results

//make a function that populates the results panel with information
function makeResults (sponsorcode){
    //make a json request to the backend for the info
    var sponsorsummary = $("#sponsor-summary");
    var resultsElement = $("#grant-results-box");

    $("#sponsor-search").autocomplete({autoFocus : true, minLength: 2, source :
            function(request, response){
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