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

            //if the accordion exists then get rid of it and all of the contents of the div element
            if (!accordionApplied === false){
                resultsElement.accordion("destroy");
            }
            resultsElement.html("");

            for (var i = 0; i < data.length; i++){
                var grant = data[i];
                resultsElement.append("<h3>" + grant.projectnum + "-" + grant.year.slice(2,4)  + "</h3><div><div class='container'>" +
                    "<div class='row'><div class='col-12'><strong>Project Name: </strong><span>" + grant.projectname + "</span></div></div>" +
                    "<div class='row'><div class='col-12'><strong>Park Name: </strong><span>Insert Park names here</span></div></div>" +
                    "<div class='row'><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Award Amount: </strong><span>" + formatter.format(Number(grant.awardamount)) + "</span></div><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Project Status: </strong><span>" + grant.status + "</span></div><div class='col-md-6 col-sm-12 col-lg-6 col-xl-4'><strong>Project Type: </strong><span>" + grant.projecttype + "</span></div></div>" +
                    "<div class='row'><div class='col-12'><strong>Items Completed</strong><p>" + grant.itemscompleted + "</p></div></div>" +
                    "<div class='row'><div class='col-12'><strong>Items on Application</strong><p>" + grant.itemsapplication + "</p></div></div>" +
                    "</div></div>");//closes the containing and content div

                //"<label>Project Name: </label><span>" + grant.projectname + "</span><label>Park Name: </label><span>Insert Park names here</span>" +
                //"<label>Award Amount: </label><span>" + grant.awardamount + "</span><label>Project Status: </label><span>" + grant.status + "</span>" +
                //"<label>Project Type: </label><span>" + grant.projecttype + "</span><label>Items Completed</label><p>" + grant.itemscompleted + "</p>" +
                //"<label>Items on Application</label><p>" + grant.itemsapplication +"</p><button>Edit Grant</button>

                console.log("looping through results to make div elements");
            };
            resultsElement.accordion();
            accordionApplied = true;

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

                    var maxResults = 10
                    if (data.length > maxResults){
                        data = data.slice(0, maxResults -1);
                    }

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