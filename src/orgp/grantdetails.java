package orgp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/grantdetails")
public class grantdetails extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        //get the parameter information
        String splitchar = request.getParameter("splitchar");
        String sponsor = request.getParameter("sponsor");
        String[] projectNumbersArray = request.getParameter("projectNumbers").split(splitchar);

        //send the data back to the end user
        JSONObject fullDataset = processRequest(sponsor, projectNumbersArray);
        response.getWriter().write(fullDataset.toString());
    }

    protected  JSONObject processRequest(String projectnumberOrSponsor, String[] projectNumbersArray){
        JSONObject fullDataset = new JSONObject();

        DBUtility dbutil = new DBUtility();
        //regardless of how the project grant details are found they will be appended to this JSON list
        JSONArray projectsDetailsList = new JSONArray();


        String sqlGrantDetailsViaSponsor = "SELECT * FROM grantdisplay WHERE UPPER(sponsor) = UPPER('" + projectnumberOrSponsor +"') ORDER BY year DESC;";
        String sqlGrantDetailsViaProjectNumber = "SELECT * FROM grantdisplay WHERE UPPER(projectnum) = UPPER('" + projectnumberOrSponsor + "') ORDER BY year DESC;";
        try {
            projectsDetailsList = processGrantDetailsQuery(projectsDetailsList, sqlGrantDetailsViaSponsor, dbutil);
            //embed sponsor details to the fullDataset Object if a sponsor is clear
            if (projectsDetailsList.length() > 0){
                String sqlSponsorDetail = "SELECT * FROM sponsor WHERE Upper(sponsor) = UPPER('" + projectnumberOrSponsor + "') LIMIT 1;";
                ResultSet res = dbutil.queryDB(sqlSponsorDetail);
                while (res.next()){
                    JSONObject sponsorDetails = new JSONObject();
                    sponsorDetails.put("sponsor", res.getString("sponsor"));
                    sponsorDetails.put("type", res.getString("type"));
                    sponsorDetails.put("displayname", res.getString("displayname"));
                    sponsorDetails.put("pop2010", res.getString("pop2010"));
                    sponsorDetails.put("website", res.getString("website"));
                    sponsorDetails.put("folder", res.getString("folder"));
                    sponsorDetails.put("lat", res.getString("lon"));
                    sponsorDetails.put("city_fips_", res.getString("city_fips_"));
                    fullDataset.put("sponsorDetails", sponsorDetails);

                }
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
