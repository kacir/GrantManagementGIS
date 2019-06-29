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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        System.out.println("request for data has been recived!");

        //get the parameter information
        //give the parameter a default value if the url parameter was not used in the request
        String splitchar = " ";
        if (!(request.getParameter("splitchar") == null)){
            splitchar = request.getParameter("splitchar");
            System.out.println("split character of " + splitchar + " has been found");
        }

        String term = "";
        if (!(request.getParameter("term") == null)){
            term = request.getParameter("term");
            System.out.println("term of " + term + " has been found");
        }

        String[] projectNumbersArray = {};
        if (!(request.getParameter("projectnumbers") == null)){
            //check to see if the array can actually be split if it contains the split character
            if (request.getParameter("projectnumbers").contains(splitchar)){
                projectNumbersArray = request.getParameter("projectnumbers").split(splitchar);
            }
        }

        //send the data back to the end user
        JSONObject fullDataset = processRequest(term, projectNumbersArray);
        response.getWriter().write(fullDataset.toString());
    }

    //function does the main legwork for constructing sql strings based on the post processed serlvet parameters
    protected  JSONObject processRequest(String projectnumberOrSponsor, String[] projectNumbersArray){
        JSONObject fullDataset = new JSONObject();

        DBUtility dbutil = new DBUtility();
        //regardless of how the project grant details are found they will be appended to this JSON list
        JSONArray projectsDetailsList = new JSONArray();

        //string used to search useing the search term assuming the input is a sponsor name
        String sqlGrantDetailsViaSponsor = "SELECT * FROM grantdisplay WHERE UPPER(displayname) = UPPER('" + projectnumberOrSponsor +"') ORDER BY year DESC;";
        //string used to search using the search term assuming the input is a project number
        String sqlGrantDetailsViaProjectNumber = "SELECT * FROM grantdisplay WHERE UPPER(projectnum) = UPPER('" + projectnumberOrSponsor + "') ORDER BY year DESC;";
        try {
            projectsDetailsList = processGrantDetailsQuery(projectsDetailsList, sqlGrantDetailsViaSponsor, dbutil);
            //embed sponsor details to the fullDataset Object if a sponsor is clear

            //String sqlSponsorDetail = "SELECT * FROM sponsor WHERE Upper(displayname) = UPPER('" + projectnumberOrSponsor + "') LIMIT 1;";
            String sqlSponsorDetail = " SELECT sp.sponsor, sp.type, sp.displayname, sp.pop2010, sp.website, sp.folder, sp.lat, sp.lon, sp.city_fips_ , ct.projcount ";
            sqlSponsorDetail += " FROM sponsor AS sp LEFT JOIN ";
            sqlSponsorDetail += " (SELECT  sponsorcode, COUNT(projectnum) AS projcount FROM sponsor_to_grant GROUP BY sponsorcode) AS ct ";
            sqlSponsorDetail += " ON sp.sponsorcode = ct.sponsorcode ";
            sqlSponsorDetail += " WHERE Upper(displayname) = UPPER('" + projectnumberOrSponsor + "') LIMIT 1; ";


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
                sponsorDetails.put("projcount", res.getString("projcount"));
                fullDataset.put("sponsorDetails", sponsorDetails);
            }

            projectsDetailsList = processGrantDetailsQuery(projectsDetailsList, sqlGrantDetailsViaProjectNumber, dbutil);

            for (int i = 1; i < projectNumbersArray.length; i++){
                String sqlArrayItem = "SELECT * FROM grantdisplay WHERE UPPER(projectnum) = UPPER('" + projectNumbersArray[i] + "') ORDER BY year DESC;";
                projectsDetailsList = processGrantDetailsQuery(projectsDetailsList, sqlArrayItem, dbutil);
            }

            fullDataset.put("grants", projectsDetailsList);
        } catch (SQLException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        return fullDataset;
    }


    //This method is used three times - using a different query SQL but the same results field and table - the times are
    //individual grant #, and sponsor name
    protected  JSONArray processGrantDetailsQuery (JSONArray projectsDetailsList, String sql, DBUtility db) throws SQLException, JSONException{
        System.out.println("following is SQL code submitted " + sql);

        ResultSet res = db.queryDB(sql);
        while (res.next()){
            JSONObject grant = new JSONObject();
            grant.put("projectnum" , res.getString("projectnum"));
            grant.put("sponsor" , res.getString("sponsor"));
            grant.put("sponsortype" , res.getString("sponsortype"));
            grant.put("displayname" , res.getString("displayname"));
            grant.put("year" , res.getString("year"));
            grant.put("awardamount" , res.getString("awardamount"));
            grant.put("projecttype" , res.getString("projecttype"));
            grant.put("county" , res.getString("county"));
            grant.put("projectname" , res.getString("projectname"));
            grant.put("granttype" , res.getString("granttype"));
            grant.put("status" , res.getString("status"));
            grant.put("itemscompleted" , res.getString("itemscompleted"));
            grant.put("itemsapplication" , res.getString("itemsapplication"));

            //check to see how many sponsors there are of this particular project and append list to the gramt JSON object
            JSONArray sponsorList = new JSONArray();
            String projectnum = res.getString("projectnum");
            String sqlSponsorsSearch = "SELECT sponsor.sponsorcode, sponsor.sponsor, sponsor.type, sponsor.displayname FROM sponsor_to_grant AS imp JOIN sponsor ON imp.sponsorcode=sponsor.sponsorcode WHERE imp.projectnum = '" + projectnum + "';";
            ResultSet sponsorRes = db.queryDB(sqlSponsorsSearch);
            while (sponsorRes.next()){
                JSONObject sponsor = new JSONObject();
                sponsor.put("sponsor", sponsorRes.getString("sponsor"));
                sponsor.put("type", sponsorRes.getString("type"));
                sponsor.put("displayname", sponsorRes.getString("displayname"));
                sponsor.put("sponsorcode", sponsorRes.getString("sponsorcode"));
                sponsorList.put(sponsor);
            }
            grant.put("sponsorList" , sponsorList);


            projectsDetailsList.put(grant);
        }

        return  projectsDetailsList;
    }
}
