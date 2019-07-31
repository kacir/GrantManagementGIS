package orgp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/grantdetails")
public class grantdetails extends HttpServlet {

    private DBUtility dbutil = new DBUtility();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        System.out.println("request for data has been recived!");

        //municipal the parameter information
        //give the parameter a default value if the url parameter was not used in the request
        String splitchar = " ";
        if (!(request.getParameter("splitchar") == null)){
            splitchar = request.getParameter("splitchar");
            System.out.println("split character of " + splitchar + " has been found");
        } else {
            System.out.println("Split Character was missing");
        }

        String term = "";
        if (!(request.getParameter("term") == null)){
            term = request.getParameter("term");
            System.out.println("term of " + term + " has been found");
        } else {
            System.out.println("Search Term was null!");
        }

        String[] projectNumbersArray = {};
        if (!(request.getParameter("projectnumbers") == null)){
            //check to see if the array can actually be split if it contains the split character
            if (request.getParameter("projectnumbers").contains(splitchar)){
                projectNumbersArray = request.getParameter("projectnumbers").split(splitchar);
                System.out.println("length of the split array is: ");
                System.out.println(projectNumbersArray.length);
            } else {
                projectNumbersArray = new String[] {request.getParameter("projectnumbers")};
            }
        } else {
            System.out.println("Project numbers list was missing from request");
        }

        JSONObject fullDataset = new JSONObject();

        //perform a term search
        if (projectNumbersArray.length > 0){
            fullDataset = processProjectNumbersRequest(projectNumbersArray);
        } else {
            SearchItem recommendedItem = SearchTypeFind.findMaster(term);
            if (! (recommendedItem == null)){
                if (recommendedItem.getType().equals("City") || recommendedItem.getType().equals("County") ||  recommendedItem.getType().equals("State") || recommendedItem.getType().equals("Community") ){
                    fullDataset = processSponsorRequest(recommendedItem.getIdentifier());
                } else if (recommendedItem.getType().equals("Park")) {
                    fullDataset = processParkRequest(recommendedItem.getIdentifier());
                } else if (recommendedItem.getType().equals("Project")){
                    fullDataset = processProjectNumberRequest(term);
                }

            }
        }

        //send the data back to the end user

        response.getWriter().write(fullDataset.toString());
    }

    //function does the main legwork for constructing sql strings based on the post processed serlvet parameters
    protected  JSONObject processSponsorRequest(String uniqueIdentifier){
        JSONObject fullDataset = new JSONObject();

        //regardless of how the project grant details are found they will be appended to this JSON list
        JSONArray projectsDetailsList = new JSONArray();

        //string used to search the search term assuming the input is a sponsor name
        String sqlGrantDetailsViaSponsor = "SELECT * FROM grantdisplay WHERE sponsorcode = '" + uniqueIdentifier +"' ORDER BY year DESC;";

        try {
            projectsDetailsList = processGrantDetailsQuery(projectsDetailsList, sqlGrantDetailsViaSponsor, dbutil);
            //embed sponsor details to the fullDataset Object if a sponsor is clear

            String sqlSponsorDetail = " SELECT sp.sponsor, sp.type, sp.displayname, sp.pop2010, sp.website, sp.folder, sp.lat, sp.lon, sp.city_fips_ , sp.county, sp.municipallink, sp.judgelink, ct.projcount, ct.awardsum ";
            sqlSponsorDetail += " FROM sponsor AS sp LEFT JOIN ";
            sqlSponsorDetail += " (SELECT imp.sponsorcode, COUNT(imp.projectnum) AS projcount, SUM(awardamount) AS awardsum FROM sponsor_to_grant AS imp JOIN grants ON imp.projectnum = grants.projectnumber GROUP BY imp.sponsorcode) AS ct ";
            sqlSponsorDetail += " ON sp.sponsorcode = ct.sponsorcode ";
            sqlSponsorDetail += " WHERE sp.sponsorcode = '" + uniqueIdentifier + "' LIMIT 1; ";


            ResultSet res = dbutil.queryDB(sqlSponsorDetail);
            while (res.next()){
                JSONObject sponsorDetails = new JSONObject();
                sponsorDetails.put("sponsor", res.getString("sponsor"));
                sponsorDetails.put("type", res.getString("type"));
                sponsorDetails.put("displayname", res.getString("displayname"));
                sponsorDetails.put("pop2010", res.getString("pop2010"));
                sponsorDetails.put("website", res.getString("website"));
                sponsorDetails.put("folder", res.getString("folder"));
                sponsorDetails.put("lat", res.getString("lat"));
                sponsorDetails.put("lon", res.getString("lon"));
                sponsorDetails.put("city_fips_", res.getString("city_fips_"));
                sponsorDetails.put("county", res.getString("county"));
                sponsorDetails.put("projcount", res.getString("projcount"));
                sponsorDetails.put("awardsum", res.getString("awardsum"));
                sponsorDetails.put("municipallink", res.getString("municipallink"));
                sponsorDetails.put("judgelink", res.getString("judgelink"));
                fullDataset.put("sponsorDetails", sponsorDetails);
            }

            fullDataset.put("grants" , projectsDetailsList);

        } catch (SQLException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        return fullDataset;
    }

    protected  JSONObject processProjectNumberRequest(String projectNumber){
        JSONObject fullDataset = new JSONObject();
        JSONArray projectsDetailsList = new JSONArray();

        String sqlGrantDetailsViaProjectNumber = "SELECT * FROM grantdisplay WHERE UPPER(projectnum) = UPPER('" + projectNumber + "') ORDER BY year DESC;";


        projectsDetailsList = processGrantDetailsQuery(projectsDetailsList, sqlGrantDetailsViaProjectNumber, dbutil);

        try {
            fullDataset.put("grants", projectsDetailsList);
        } catch (JSONException e){
            e.printStackTrace();
        }


        return fullDataset;

    }

    protected  JSONObject processParkRequest(String identifier){

        JSONObject fullDataset = new JSONObject();
        JSONArray projectsDetailsList = new JSONArray();
        JSONArray parkResult = new JSONArray();


        geoStorConnect parkSearch = new geoStorConnect();
        parkResult = parkSearch.searchParks(identifier, parkResult, 1);
        try {
            if (parkResult.length() > 0){
                JSONObject park = parkResult.getJSONObject(0);
                fullDataset.put("park" , park);
                fullDataset.put("grants", projectsDetailsList);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return fullDataset;

    }

    protected JSONObject processProjectNumbersRequest(String[] projectNumbersArray) {

        JSONObject fullDataset = new JSONObject();
        JSONArray projectsDetailsList = new JSONArray();

        String combinedSQL = "SELECT *, projectnumber AS projectnum FROM grants WHERE ";
        for (int i = 0; i < projectNumbersArray.length; i++){
            System.out.println("Project Number that is addressed by search is '" + projectNumbersArray[i] + "'");
            String orContainer;
            if (i == 0){
                orContainer = "";
            } else {
                orContainer = " OR ";
            }

            String itemClause = orContainer + " ( UPPER(projectnumber) = UPPER('" + projectNumbersArray[i] + "'))";
            combinedSQL += itemClause;
        }
        if (projectNumbersArray.length > 0){
            combinedSQL += " ORDER BY year DESC;";
            System.out.println("combined SQL");
            System.out.println(combinedSQL);
            projectsDetailsList = processGrantDetailsQuery(projectsDetailsList, combinedSQL, dbutil);
        }

        try {
            fullDataset.put("grants", projectsDetailsList);
        } catch (JSONException e){
            e.printStackTrace();
        }


        return fullDataset;
    }


    //This method is used three times - using a different query SQL but the same results field and table - the times are
    //individual grant #, and sponsor name
    protected  JSONArray processGrantDetailsQuery (JSONArray projectsDetailsList, String sql, DBUtility db) {
        System.out.println("following is SQL code submitted " + sql);

        try {
            ResultSet res = db.queryDB(sql);
            while (res.next()) {
                JSONObject grant = new JSONObject();
                grant.put("projectnum", res.getString("projectnum"));
                grant.put("year", res.getString("year"));
                grant.put("awardamount", res.getString("awardamount"));
                grant.put("projecttype", res.getString("projecttype"));
                grant.put("county", res.getString("county"));
                grant.put("projectname", res.getString("projectname"));
                grant.put("granttype", res.getString("granttype"));
                grant.put("status", res.getString("status"));
                grant.put("itemscompleted", res.getString("itemscompleted"));
                grant.put("itemsapplication", res.getString("itemsapplication"));
                grant.put("phonelog", res.getString("phonelog"));
                grant.put("boxlink", res.getString("boxlink"));

                //check to see how many sponsors there are of this particular project and append list to the gramt JSON object
                JSONArray sponsorList = new JSONArray();
                String projectnum = res.getString("projectnum");
                String sqlSponsorsSearch = "SELECT sponsor.sponsorcode, sponsor.sponsor, sponsor.type, sponsor.displayname FROM sponsor_to_grant AS imp JOIN sponsor ON imp.sponsorcode=sponsor.sponsorcode WHERE imp.projectnum = '" + projectnum + "';";
                ResultSet sponsorRes = db.queryDB(sqlSponsorsSearch);
                while (sponsorRes.next()) {
                    JSONObject sponsor = new JSONObject();
                    sponsor.put("sponsor", sponsorRes.getString("sponsor"));
                    sponsor.put("type", sponsorRes.getString("type"));
                    sponsor.put("displayname", sponsorRes.getString("displayname"));
                    sponsor.put("sponsorcode", sponsorRes.getString("sponsorcode"));
                    sponsorList.put(sponsor);
                }
                grant.put("sponsorList", sponsorList);


                projectsDetailsList.put(grant);
            }
        } catch (SQLException | JSONException e){
            e.printStackTrace();
        }

        return  projectsDetailsList;
    }
}
