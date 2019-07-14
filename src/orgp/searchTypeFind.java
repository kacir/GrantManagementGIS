package orgp;


import java.sql.*;
import java.util.ArrayList;

public class searchTypeFind {

    public static void main(String[] args) {
        try {
            ArrayList<SearchItem> test = searchTypeFind.find("55");

            System.out.println("Array Length is " + test.size());

            //Collections.sort(test, SeachItemSorting);
            test.sort(SearchItem.arraySorter);

            for (int i = 0; i < test.size(); i++ ){
                SearchItem temp = test.get(i);
                System.out.println("Index " + i);
                temp.printAttributes();

            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }


    public static ArrayList find(String searchTerm) throws Exception{

        //remove white space from beinging and end of the string
        searchTerm = searchTerm.trim().toUpperCase();
        if (searchTerm.length() == 0){
            throw new Exception("Term argument has no length");
        }

        //remove all of the double spaces in the word or word set
        while (searchTerm.contains("  ")){
            searchTerm = searchTerm.replace("  ", " ");
        }


        ArrayList<SearchItem> result = new ArrayList();

        DBUtility dbutil = new DBUtility();
        //search through the sponsors table
        String sponsorSQL = "SELECT sponsorcode, sponsor, displayname, county, type ";
        sponsorSQL += "FROM sponsor WHERE (type = 'Community' OR type = 'City' OR type = 'County' OR type = 'State') AND UPPER(displayname) LIKE UPPER('%" + searchTerm + "%');";
        ResultSet sponsors = dbutil.queryDB(sponsorSQL);

        while (sponsors.next()){
            String sponsorCode = sponsors.getString("sponsorCode");
            String displayname = sponsors.getString("displayname").trim().toUpperCase();
            String type = sponsors.getString("type");
            Double score = 0.00;
            Boolean strictMatch = false;

            if (displayname.equals(searchTerm)){
                score = 1.0;
                strictMatch = true;
            } else if (displayname.contains(searchTerm)){
                score = Double.valueOf(searchTerm.length()) / Double.valueOf(displayname.length());
                System.out.println("Calculated Score of sponsor is " + score);
                if (displayname.indexOf(searchTerm) == 0){
                    strictMatch = true;
                } else {
                    strictMatch = false;
                }
            }
            //add the result to the end product list
            try {
                SearchItem imp = new SearchItem(sponsorCode, type, strictMatch, score);
                result.add(imp);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //score each result

        //search through the parks Feature Class
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        //build the sql request for parks matching a specific name
        try (Connection con = DriverManager.getConnection(geoStorConnect.connectionUrl); Statement stmt = con.createStatement()) {
            String parkSQL = "SELECT [OBJECTID],[currentNam],[pastName] FROM [asdi].[adpt].[OGPARKFOOTPRINTS] WHERE [type] = 'funded park' AND (UPPER([currentNam]) LIKE UPPER('%" + searchTerm + "%') OR UPPER([pastName]) LIKE UPPER('%" + searchTerm + "%'))  ORDER BY [currentNam] ASC;";

            ResultSet rs = stmt.executeQuery(parkSQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                //score each result
                String currentName = rs.getString("currentNam").trim().toUpperCase();
                String pastNames;
                if (rs.getString("pastName") == null){
                    pastNames = "";
                } else {
                    pastNames = rs.getString("pastName").trim().toUpperCase();
                }
                String objectid = rs.getString("OBJECTID");
                Double score = 0.00;
                Boolean strictMatch = false;

                //search through and rank the current park name
                if (currentName.equals(searchTerm)){
                    score = 1.0;
                    strictMatch = true;
                } else if (currentName.contains(searchTerm)){
                    score = Double.valueOf(searchTerm.length()) / Double.valueOf((currentName.length()));
                    if (currentName.indexOf(searchTerm) == 0){
                        strictMatch = true;
                    }
                }

                //check through the past park names list for matches
                if (pastNames.length() > 0 && !pastNames.equals(" ") && !pastNames.equals(", ")) {
                    String[] pastNamesArray = pastNames.split(", ");
                    for (String pastName: pastNamesArray){
                        if (pastName.contains(searchTerm) && pastName.length() > 0){
                           double possibleScore =  Double.valueOf(searchTerm.length())   / Double.valueOf(pastName.length());
                           if (possibleScore > score){
                               score = possibleScore;
                               if (pastName.indexOf(searchTerm) == 0){
                                   strictMatch = true;
                               } else {
                                   strictMatch = false;
                               }
                           }
                        }
                    }
                }

                //create the scored object
                try {
                    SearchItem matchingItem = new SearchItem(objectid, "park", strictMatch, score);
                    result.add(matchingItem);
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        } catch (SQLException b) {
            b.printStackTrace();
        }

        String projectSQL = "SELECT projectnumber FROM grants WHERE UPPER(projectnumber) LIKE UPPER('%" + searchTerm + "%');";
        ResultSet projects = dbutil.queryDB(projectSQL);

        while (projects.next()){
            String projectnumber = projects.getString("projectnumber");
            projectnumber = projectnumber.trim().toUpperCase();
            Double score = 0.00;
            Boolean strictMatch = false;

            if (projectnumber.equals(searchTerm)){
                strictMatch = true;
                score = 1.0;
            } else if (projectnumber.contains(searchTerm)) {
                score = Double.valueOf(searchTerm.length()) / Double.valueOf(projectnumber.length());
                if (projectnumber.indexOf(searchTerm) == 0){
                    strictMatch = true;
                } else {
                    strictMatch = false;
                }
            }

            SearchItem emp = new SearchItem(projectnumber, "Project", strictMatch,score);
            result.add(emp);

        }


        //search through the grants database
        //score each result


        return result;
    }

}
