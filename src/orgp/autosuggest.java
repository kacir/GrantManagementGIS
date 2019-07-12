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
import java.sql.*;

@WebServlet("/api/autosuggest")
public class autosuggest extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //find all relevant sponsors in list an return them
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONArray list = new JSONArray();
        DBUtility dbutil = new DBUtility();

        //getting the important autosuggestion info for sponsors
        String manjorSponsorSQL = "SELECT sponsor, type, displayname, county FROM sponsor WHERE type = 'City' OR type = 'County' OR type = 'State' ORDER BY type ASC, sponsor ASC;";
        ResultSet sponsors =  dbutil.queryDB(manjorSponsorSQL);

        try {
            while (sponsors.next()) {
                JSONObject temp = new JSONObject();
                temp.put("sponsor", sponsors.getString("sponsor") );
                temp.put("sponsorType", sponsors.getString("type") );
                temp.put("displayname", sponsors.getString("displayname") );
                temp.put("county", sponsors.getString("county") );
                temp.put("type" , "sponsor");

                list.put(temp);
            }
        } catch (SQLException | JSONException e){
            e.printStackTrace();
        }

        System.out.println("length after sponsor is " + list.length() );

        //getting the important autosuggestion info for parks
        String connectionUrl = "jdbc:sqlserver://db.geostor.org:1433;databaseName=asdi;user=adpt;password=Kv3Dc2katEmCBS4tARuo";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        //build the sql request for parks matching a specific name
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            String SQL = "SELECT [currentNam],[pastName], [county], [sponsorshi] FROM [asdi].[adpt].[OGPARKFOOTPRINTS] WHERE [type] = 'funded park' ORDER BY [currentNam] ASC;";
            System.out.println("moving through MS SQL code");
            ResultSet rs = stmt.executeQuery(SQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                JSONObject item = new JSONObject();

                item.put("currentNam", rs.getString("currentNam"));
                item.put("pastName", rs.getString("pastName"));
                item.put("county", rs.getString("county"));
                item.put("sponsorshi", rs.getString("sponsorshi"));
                item.put("type", "park");

                list.put(item);
            }
        } catch (SQLException | JSONException b) {
            b.printStackTrace();
        }

        System.out.println("length after park is " + list.length() );


        String minorSponsorSQL = "SELECT sponsor, type, displayname, county FROM sponsor WHERE type = 'Community' ORDER BY sponsor ASC;";
        ResultSet minorSponsors =  dbutil.queryDB(minorSponsorSQL);

        try {
            while (minorSponsors.next()) {
                JSONObject temp = new JSONObject();
                temp.put("sponsor", minorSponsors.getString("sponsor") );
                temp.put("sponsorType", minorSponsors.getString("type") );
                temp.put("displayname", minorSponsors.getString("displayname") );
                temp.put("county", minorSponsors.getString("county") );
                temp.put("type" , "sponsor");

                list.put(temp);
            }
        } catch (SQLException | JSONException c){
            c.printStackTrace();
        }
        response.getWriter().write(list.toString());
    }
}
