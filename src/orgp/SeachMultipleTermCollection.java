package orgp;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.sql.ResultSet;
import java.util.HashMap;

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
        String[] testString = {"Little Rock", "Riverfront Park" } ;

        SeachMultipleTermCollection testResult = new SeachMultipleTermCollection(testString);
        testResult.printInternalLists();
        testResult.printRecommendedResult();

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

    private HashMap<String, String> searchPark (String identifier){

        HashMap parkReturn = new HashMap<String, String>();

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        try (Connection con = DriverManager.getConnection(geoStorConnect.connectionUrl); Statement stmt = con.createStatement();) {
            String parkSQL = "SELECT * FROM [asdi].[adpt].[OGPARKFOOTPRINTS] WHERE [OBJECTID] = " + identifier + ";";

            ResultSet parkAttr = stmt.executeQuery(parkSQL);
            while (parkAttr.next()){
                parkReturn.put("city", parkAttr.getString("city").toUpperCase().trim());
                parkReturn.put("county", parkAttr.getString("county").toUpperCase().trim());
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return parkReturn;
    }

    private HashMap<String, String> searchCityOrCounty(String identifier){

        HashMap cityReturn = new HashMap<String, String>();

        ResultSet cityAttr = dbutil.queryDB("SELECT sponsor, county FROM sponsor WHERE sponsorcode = " + identifier  + ";");

        try {
            while (cityAttr.next()){
                cityReturn.put("city" , cityAttr.getString("sponsor").toUpperCase().trim());
                cityReturn.put("county" , cityAttr.getString("county").toUpperCase().trim());
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return cityReturn;

    }

    public SeachMultipleTermCollection (String[] termArray){

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
            for (SearchItem item : singleTermResult.getTopResults()){
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
                                HashMap countyAttributes = searchCityOrCounty(county.getIdentifier());
                                HashMap cityAttributes = searchCityOrCounty(city.getIdentifier());
                                HashMap parkAttributes = searchPark(park.getIdentifier());

                                //if all agree on the county location
                                if (countyAttributes.get("county").equals(cityAttributes.get("county"))){

                                    if (cityAttributes.get("city").equals(parkAttributes.get("city"))){
                                        SearchItem temp = park.deepCopy();
                                        temp.setMergedScore((park.getScore() + county.getScore() + city.getScore()) / this.resultArray.length );
                                        temp.setMergedConflicts(false);
                                        this.recommendedResult.add(temp);
                                    } else {
                                        SearchItem temp = city.deepCopy();
                                        temp.setMergedScore( (county.getScore() + city.getScore()) / this.resultArray.length );
                                        temp.setMergedConflicts(true);
                                        this.recommendedResult.add(temp);
                                    }
                                } else {
                                    if (countyAttributes.get("county").equals(parkAttributes.get("county"))){
                                        SearchItem temp = park.deepCopy();
                                        temp.setMergedScore((park.getScore() + county.getScore()) / this.resultArray.length);
                                        temp.setMergedConflicts(true);
                                        this.recommendedResult.add(temp);
                                    } else {
                                        if (cityAttributes.get("city").equals(parkAttributes.get("city"))){
                                            SearchItem temp = park.deepCopy();
                                            temp.setMergedScore((park.getScore() + city.getScore()) / this.resultArray.length);
                                            temp.setMergedConflicts(true);
                                            this.recommendedResult.add(temp);
                                        } else {
                                            // none of results match, pick whichever has the highest score by adding to list and sorting by score
                                            ArrayList<SearchItem> iterationCombo = new ArrayList<SearchItem>();
                                            iterationCombo.add(park);
                                            iterationCombo.add(county);
                                            iterationCombo.add(city);
                                            Collections.sort(iterationCombo);
                                            Collections.reverse(iterationCombo);

                                            SearchItem temp = iterationCombo.get(0).deepCopy();
                                            temp.setMergedScore( temp.getScore() / this.resultArray.length );
                                            temp.setMergedConflicts(true);
                                            this.recommendedResult.add(temp);
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
                        HashMap countyAttributes = searchCityOrCounty(county.getIdentifier());
                        HashMap parkAttributes = searchPark(park.getIdentifier());

                        if (countyAttributes.get("county").equals(parkAttributes.get("county"))){
                            SearchItem temp = park.deepCopy();
                            temp.setMergedScore((temp.getScore() + county.getScore()) / this.resultArray.length );
                            temp.setMergedConflicts(false);
                            this.recommendedResult.add(temp);
                        } else {
                            if (park.getScore() > county.getScore()){
                                SearchItem temp = park.deepCopy();
                                temp.setMergedScore(temp.getScore() / this.resultArray.length);
                                temp.setMergedConflicts(true);
                                this.recommendedResult.add(temp);
                            } else {
                                SearchItem temp = county.deepCopy();
                                temp.setMergedScore(temp.getScore() / this.resultArray.length);
                                temp.setMergedConflicts(true);
                                this.recommendedResult.add(temp);
                            }
                        }
                    }
                }

            } else {
                SearchItem temp = this.counties.get(0).deepCopy();
                temp.setMergedScore(temp.getScore() / this.resultArray.length);
                temp.setMergedConflicts(false);
                this.recommendedResult.add(temp);
            }

        } else if (this.cities.size() > 0){
            if (this.parks.size() > 0){

                for (SearchItem city : this.cities){
                    for (SearchItem park : this.parks){

                        HashMap parkAttributes = searchPark(park.getIdentifier());
                        HashMap cityAttributes = searchCityOrCounty(city.getIdentifier());

                        if (parkAttributes.get("city").equals("") || cityAttributes.get("city").equals("")){
                            //pick the park as the result and calculate a low cumulative score
                        } else if (parkAttributes.get("city").equals("Unincorporated Area".toUpperCase())){
                            //choose a city or park based on which has the higher score
                        } else if (parkAttributes.get("city").equals(cityAttributes.get("city"))) {
                            //if everything lines up then the city and park match, pick the city
                            SearchItem temp = park.deepCopy();
                            temp.setMergedScore((temp.getScore() + city.getScore()) / this.resultArray.length);
                            temp.setMergedConflicts(false);
                            recommendedResult.add(temp);


                        } else if (!parkAttributes.get("city").equals(cityAttributes.get("city"))){
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



            } else {
                SearchItem temp = this.cities.get(0).deepCopy();
                temp.setMergedScore(temp.getScore() / this.resultArray.length);
                temp.setMergedConflicts(false);
                recommendedResult.add(temp);
            }

            //if the only found result were a parks
        } else if (this.parks.size() > 0){
            SearchItem temp = this.parks.get(0).deepCopy();
            temp.setMergedScore(temp.getScore() / this.resultArray.length);
            temp.setMergedConflicts(false);
            recommendedResult.add(temp);
        }

        Collections.sort(recommendedResult);
        Collections.reverse(recommendedResult);

    }
}
