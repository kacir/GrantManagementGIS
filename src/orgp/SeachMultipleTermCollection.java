package orgp;

import java.util.*;


public class SeachMultipleTermCollection {

    private static String[] overlyGeneralTerms = {"City", "County", "Park"};
    private String[] termArray;
    private SearchSingleTerm[] resultArray;

    private ArrayList<SearchItem> counties = new ArrayList<>();
    private ArrayList<SearchItem> cities = new ArrayList<>();
    private ArrayList<SearchItem> parks = new ArrayList<>();

    //stats on finalized score;
    private Double score;
    private Boolean resultConflicts;
    private ArrayList<SearchItem> recommendedResult = new ArrayList<>();

    public static void main (String[] args){
        String[] testString = {"Saint Francis ", "City Park"} ;

        try {
            SeachMultipleTermCollection testResult = new SeachMultipleTermCollection(testString);
            testResult.printInternalLists();
            testResult.printRecommendedResult();
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    public SearchItem getRecommendedResult() {
        return this.recommendedResult.get(0);
    }

    public void printRecommendedResult(){
        System.out.println("Recommended Word Combo Results are..........................................................................");
        for (SearchItem item : this.recommendedResult){
            item.printAttributes();
        }
    }

    private void printInternalLists(){
        System.out.println("Counties in search are...........................");
        for (SearchItem county : this.counties){
            county.printAttributes();
        }
        System.out.println("Cities in search are..............................");
        for (SearchItem city : this.cities){
            city.printAttributes();
        }
        System.out.println("Parks in search are...............................");
        for (SearchItem park : this.parks){
            park.printAttributes();
        }

    }

    private DBUtility dbutil = new DBUtility();



    private void addRecommendedItem(SearchItem item){

        boolean holder = false;

        for (SearchItem y : this.recommendedResult){
            if (y.getIdentifier().equals(item.getIdentifier()) && y.getType().equals(item.getType())  ){
                if (y.getMergedScore().equals(item.getMergedScore()) ){
                    holder = true;
                }
            }
        }

        //if not in list then add it
        if (!holder){
            this.recommendedResult.add(item);
        }

    }

    public SeachMultipleTermCollection (String[] termArray) throws Exception{

        ArrayList<String> termListArray = new ArrayList(Arrays.asList(termArray));

        //remove overly general terms
        //remove spaces and such
        for (String generalTerm : overlyGeneralTerms){
            for (var i = 0; i < termListArray.size(); i++){
                if (generalTerm.toUpperCase().replaceAll("  ", " ").equals(termListArray.get(i).toUpperCase().replaceAll("  ", " "))){
                    termListArray.remove(i);
                    i--;//make sure the next time the loop moves forward, go over the same
                    //index again because its referring to a different item in the list when an item is remove
                    //and index's are reassigned;
                }
            }
        }

        //set the collection of strings in the sequence for use later on
        termArray = new String[termListArray.size()];
        termArray = termListArray.toArray(termArray);
        this.termArray = termArray;


        //create SearchSingleTerm results for each term in the array
        this.resultArray = new SearchSingleTerm [termArray.length];

        //generate a result array
        for (int a = 0; a < this.termArray.length; a++){
            System.out.println("Seaching term for.... " + termArray[a]);
            try {
                this.resultArray[a] = new SearchSingleTerm(termArray[a]);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //classify each term into a grouping
        for (SearchSingleTerm singleTermResult : this.resultArray){
            System.out.println( "Single Term in result Array :'" + singleTermResult.getSearchTerm() + "'" );
            for (SearchItem item : singleTermResult.getAllresults()){
                System.out.println("Single Term Item type is" + item.getType() );

                switch (item.getType()){
                    case "Park":
                        this.parks.add(item);
                        break;
                    case "County":
                        this.counties.add(item);
                        break;
                    case "City":
                        this.cities.add(item);
                        break;
                        default:
                            break;
                }
            }
        }

        //sort all of the created lists according to their respective scores
        Collections.sort(this.counties);
        Collections.reverse(this.counties);
        Collections.sort(this.cities);
        Collections.reverse(this.cities);
        Collections.sort(this.parks);
        Collections.reverse(this.parks);



        //after all results are classified its them to reconcile them against each other to see what makes logical sense.
        if (this.counties.size() > 0){
            if (this.cities.size() > 0){
                if (this.parks.size() > 0){
                    //3X nested loop
                    //get results for all three
                    //compare all three

                    for (SearchItem county : this.counties){
                        for (SearchItem city : this.cities){
                            for (SearchItem park : this.parks){

                                //if all agree on the county location
                                if ( county.sameCounty(city) ){

                                    if ( city.sameCity(park) ){
                                        //condition if they all share the same term
                                        if (park.originTermSame(county) && county.originTermSame(city)){
                                            //all three items are sharing the same term, use only the highest scoring one for the final recommendation
                                            SearchItem temp;
                                            if (park.getScore() >= county.getScore() && park.getScore() >= city.getScore()){
                                                temp = park.deepCopy();
                                                temp.setMergedScore(park.getScore() / Double.valueOf(this.resultArray.length) );
                                            } else if (county.getScore() >= park.getScore() && county.getScore() >= city.getScore()){
                                                temp = county.deepCopy();
                                                temp.setMergedScore(county.getScore() / Double.valueOf(this.resultArray.length));
                                            } else if (city.getScore() >= park.getScore() && city.getScore() >= county.getScore()) {
                                                temp = city.deepCopy();
                                                temp.setMergedScore(city.getScore() / Double.valueOf(this.resultArray.length));
                                            } else {
                                                throw new Exception("there should not be an error here");
                                            }
                                            temp.setMergedConflicts(false);
                                            addRecommendedItem(temp);

                                        } else if (park.originTermSame(county)){
                                            //park and county share term
                                            SearchItem temp;
                                            if (park.getScore() > county.getScore()){
                                                temp = park.deepCopy();
                                                temp.setMergedScore((park.getScore() + city.getScore()) / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(false);
                                            } else {
                                                temp = county.deepCopy();
                                                temp.setMergedConflicts(false);
                                                temp.setMergedScore((county.getScore() + city.getScore()) / Double.valueOf(this.resultArray.length));
                                            }
                                            addRecommendedItem(temp);

                                        } else  if (county.originTermSame(city)){
                                            //county and city share term
                                            SearchItem temp;
                                            if (county.getScore() > city.getScore()){
                                                temp = county.deepCopy();
                                                temp.setMergedConflicts(false);
                                                temp.setMergedScore((park.getScore() + county.getScore()) / Double.valueOf(this.resultArray.length));
                                            } else {
                                                temp = city.deepCopy();
                                                temp.setMergedScore((park.getScore() + city.getScore()) / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(false);
                                            }
                                            addRecommendedItem(temp);

                                        } else if (city.originTermSame(park)) {
                                            SearchItem temp;
                                            if (city.getScore() > park.getScore()){
                                                temp = city.deepCopy();
                                                temp.setMergedScore((city.getScore() + county.getScore()) / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(false);
                                            } else {
                                                temp = park.deepCopy();
                                                temp.setMergedScore((park.getScore() + county.getScore()) / Double.valueOf(this.resultArray.length));
                                            }
                                            addRecommendedItem(temp);

                                        } else {
                                            //nothing shares a term, each park, city, and county SearchItem is tied to a unique searchTerm
                                            SearchItem temp = park.deepCopy();
                                            temp.setMergedScore((park.getScore() + county.getScore() + city.getScore()) / Double.valueOf(this.resultArray.length) );
                                            temp.setMergedConflicts(false);
                                            addRecommendedItem(temp);
                                        }

                                    } else {
                                        //check if city and county have the same term
                                        SearchItem temp;
                                        if (city.originTermSame(county)){
                                            if (city.getScore() > county.getScore()){
                                                temp = city.deepCopy();
                                                temp.setMergedScore(city.getScore() / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(true);
                                            } else {
                                                temp = county.deepCopy();
                                                temp.setMergedScore(county.getScore() / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(true);
                                            }
                                        } else {
                                            temp = city.deepCopy();
                                            temp.setMergedScore( (county.getScore() + city.getScore()) / Double.valueOf(this.resultArray.length) );
                                            temp.setMergedConflicts(true);
                                        }
                                        addRecommendedItem(temp);

                                    }
                                } else {
                                    if (county.sameCounty(park) ){
                                        SearchItem temp;
                                        if (park.originTermSame(park)){
                                            if (county.getScore() > park.getScore()){
                                                temp = county.deepCopy();
                                                temp.setMergedScore(county.getScore() / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(true);
                                            } else {
                                                temp = park.deepCopy();
                                                temp.setMergedScore(park.getScore() / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(true);
                                            }
                                        } else {
                                            temp = park.deepCopy();
                                            temp.setMergedScore((park.getScore() + county.getScore()) / Double.valueOf(this.resultArray.length));
                                            temp.setMergedConflicts(true);
                                        }
                                        addRecommendedItem(temp);

                                    } else {
                                        if (city.sameCity(park)){
                                            SearchItem temp;
                                            if (city.originTermSame(park)){
                                                if (city.getScore() > park.getScore()){
                                                    temp = city.deepCopy();
                                                    temp.setMergedScore(city.getScore() / Double.valueOf(this.resultArray.length));
                                                    temp.setMergedConflicts(true);
                                                } else {
                                                    temp = park.deepCopy();
                                                    temp.setMergedScore(park.getScore() / Double.valueOf(this.resultArray.length));
                                                    temp.setMergedConflicts(true);
                                                }
                                            } else {
                                                temp = park.deepCopy();
                                                temp.setMergedScore((park.getScore() + city.getScore()) / Double.valueOf(this.resultArray.length));
                                                temp.setMergedConflicts(true);

                                            }
                                            addRecommendedItem(temp);

                                        } else {
                                            // none of results match, pick whichever has the highest score by adding to list and sorting by score
                                            ArrayList<SearchItem> iterationCombo = new ArrayList<SearchItem>();
                                            iterationCombo.add(park);
                                            iterationCombo.add(county);
                                            iterationCombo.add(city);
                                            Collections.sort(iterationCombo);
                                            Collections.reverse(iterationCombo);

                                            SearchItem temp = iterationCombo.get(0).deepCopy();
                                            temp.setMergedScore( temp.getScore() / Double.valueOf(this.resultArray.length) );
                                            temp.setMergedConflicts(true);
                                            addRecommendedItem(temp);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            } else if (this.parks.size() > 0){
                for (SearchItem county : this.counties){
                    for (SearchItem park : this.parks){
                        if (county.originTermSame(park)){
                            SearchItem temp;
                            if (county.getScore() > park.getScore()){
                                temp = county.deepCopy();
                                temp.setMergedScore(county.getScore() / Double.valueOf(this.resultArray.length));
                                temp.setMergedConflicts(false);
                            } else {
                                temp = park.deepCopy();
                                temp.setMergedScore(park.getScore() / Double.valueOf(this.resultArray.length));
                                temp.setMergedConflicts(false);
                            }
                            addRecommendedItem(temp);

                        } else {
                            if (county.sameCounty(park) ){
                                SearchItem temp = park.deepCopy();
                                temp.setMergedScore((temp.getScore() + county.getScore()) / Double.valueOf(this.resultArray.length) );
                                temp.setMergedConflicts(false);
                                addRecommendedItem(temp);
                            } else {
                                if (park.getScore() > county.getScore()){
                                    SearchItem temp = park.deepCopy();
                                    temp.setMergedScore(temp.getScore() / Double.valueOf(this.resultArray.length));
                                    temp.setMergedConflicts(true);
                                    addRecommendedItem(temp);
                                } else {
                                    //check to see if the county was already part of the array.
                                    //if its already in there then do not double add it.
                                    SearchItem temp = county.deepCopy();
                                    temp.setMergedScore(temp.getScore() / Double.valueOf(this.resultArray.length) );
                                    temp.setMergedConflicts(true);
                                    addRecommendedItem(temp);

                                }
                            }
                        }
                    }
                }

            } else {
                for (SearchItem county : counties){
                    SearchItem temp = county.deepCopy();
                    temp.setMergedScore(temp.getScore() / this.resultArray.length);
                    temp.setMergedConflicts(false);
                    this.recommendedResult.add(temp);
                }
            }

        } else if (this.cities.size() > 0){
            if (this.parks.size() > 0){

                for (SearchItem city : this.cities){
                    for (SearchItem park : this.parks){

                        if (park.originTermSame(city)){
                            SearchItem temp;
                            if (park.getScore() > city.getScore()){
                                temp = park.deepCopy();
                                temp.setMergedScore(park.getScore() / Double.valueOf(this.resultArray.length));
                                temp.setMergedConflicts(false);
                            } else {
                                temp = city.deepCopy();
                                temp.setMergedScore(city.getScore() / Double.valueOf(this.resultArray.length));
                                temp.setMergedConflicts(false);
                            }
                            recommendedResult.add(temp);
                        } else {
                            if (park.getCity().equals("") || city.getCity().equals("")){
                                //pick the park as the result and calculate a low cumulative score
                            } else if (park.getCity().equals("Unincorporated Area".toUpperCase())){
                                //choose a city or park based on which has the higher score
                            } else if (park.sameCity(city) ) {
                                //if everything lines up then the city and park match, pick the city
                                SearchItem temp = park.deepCopy();
                                temp.setMergedScore((temp.getScore() + city.getScore()) / this.resultArray.length);
                                temp.setMergedConflicts(false);
                                recommendedResult.add(temp);


                            } else if ( !park.sameCity(city) ){
                                //the park and city do not match then choose whichever has the highest score
                                if (park.getScore() > city.getScore()){
                                    SearchItem temp = park.deepCopy();
                                    temp.setMergedScore(temp.getScore() / this.resultArray.length);
                                    temp.setMergedConflicts(false);
                                    recommendedResult.add(temp);
                                }
                            }
                        }
                    }
                }



            } else {
                for (SearchItem city : cities){
                    SearchItem temp = city.deepCopy();
                    temp.setMergedScore(temp.getScore() / this.resultArray.length);
                    temp.setMergedConflicts(false);
                    recommendedResult.add(temp);
                }
            }

            //if the only found result were a parks
        } else if (this.parks.size() > 0){
            for (SearchItem park : parks){
                SearchItem temp = park.deepCopy();
                temp.setMergedScore(temp.getScore() / this.resultArray.length);
                temp.setMergedConflicts(false);
                recommendedResult.add(temp);
            }
        }

        //sort everything according to be merged scores
        Collections.sort(recommendedResult, new Comparator<SearchItem>() {
            @Override
            public int compare(SearchItem o1, SearchItem o2) {
                Double temp = o1.getMergedScore() - o2.getMergedScore();
                if (temp.equals(0.00)){
                    return 0;
                } else if (temp > 0){
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        Collections.reverse(recommendedResult);

    }
}
