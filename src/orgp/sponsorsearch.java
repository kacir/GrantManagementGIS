package orgp;

import com.microsoft.sqlserver.jdbc.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.internal.StringUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/sponsorsearch")
public class sponsorsearch extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }
    protected JSONArray sponsorTypeSearch(JSONArray list, String sponsorType, String searchterm, DBUtility dbutil, int limit){

        String sql = " SELECT sp.sponsor, sp.displayname, sp.sponsorcode, ct.projcount ";
        sql += " FROM sponsor AS sp LEFT JOIN (SELECT  sponsorcode, COUNT(projectnum) AS projcount FROM sponsor_to_grant GROUP BY sponsorcode) AS ct ON sp.sponsorcode = ct.sponsorcode ";
        sql += " WHERE UPPER(sp.displayname) LIKE UPPER('" + searchterm + "%') AND sp.type = '" + sponsorType + "' ORDER BY sp.displayname ASC LIMIT " + Integer.toString(limit) + " ;";

        System.out.println(sql);

        ResultSet res = dbutil.queryDB(sql);

        try {
            while (res.next()) {
                JSONObject suggestion = new JSONObject();
                suggestion.put("sponsor" , res.getString("sponsor"));
                suggestion.put("displayname" , res.getString("displayname"));
                suggestion.put("sponsorcode" , res.getString("sponsorcode"));
                suggestion.put("projcount" , res.getString("projcount"));
                suggestion.put("type", "sponsor");
                list.put(suggestion);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        return list;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        int limit;
        if (request.getParameter("limit") == null){
            limit = 10;
        } else {
            if(StringUtils.isNumeric(request.getParameter("limit"))){
                limit = Integer.parseInt(request.getParameter("limit"));
            } else  {
                limit = 10;
            }
        }

        String searchterm = request.getParameter("searchterm");
        //remove wierd things in the search term that will mess with the search
        searchterm = searchterm.trim();
        searchterm = searchterm.replace("  ", " ");

        DBUtility dbutil = new DBUtility();

        //the output is going to be json so a JSONArray object will be used to hold the output given to the response object
        JSONArray list = new JSONArray();

        list = sponsorTypeSearch(list, "City", searchterm, dbutil, limit);
        //search for each type of sponsor in priority order so results can be in the preferred order
        if (list.length() < limit) {
            int runningLimit = limit - list.length();
            list = sponsorTypeSearch(list, "County", searchterm, dbutil, runningLimit);
        }

        if (list.length() < limit){
            //search for the park name accordingly
            int runningLimit = limit - list.length();
            geoStorConnect parkSearch = new geoStorConnect();
            list = parkSearch.searchParks( searchterm, list, runningLimit);
        }

        if (list.length() < limit) {
            int runningLimit = limit - list.length();
            list = sponsorTypeSearch(list, "Community", searchterm, dbutil, runningLimit);
        }

        if (list.length() < limit) {
            int runningLimit = limit - list.length();
            list = sponsorTypeSearch(list, "School District", searchterm, dbutil, runningLimit);
        }

        if (list.length() < limit) {
            int runningLimit = limit - list.length();
            //try and a list of project numbers to that fit the search term
            String ProjectNumSQL = "SELECT projectnum, year, sponsor, displayname FROM grantdisplay WHERE UPPER(projectnum) LIKE UPPER('%" + searchterm + "%') ORDER BY year DESC LIMIT " + Integer.toString(runningLimit) + " ;";

            ResultSet projectRes = dbutil.queryDB(ProjectNumSQL);
            System.out.println("about to search grants");
            try {
                while (projectRes.next()) {
                    System.out.println("found a single grant suggestion");
                    JSONObject suggestion = new JSONObject();
                    suggestion.put("sponsor", projectRes.getString("sponsor"));
                    suggestion.put("displayname", projectRes.getString("displayname"));
                    suggestion.put("year", projectRes.getString("year"));
                    suggestion.put("projectnum", projectRes.getString("projectnum"));
                    suggestion.put("type", "grant");
                    list.put(suggestion);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        response.getWriter().write(list.toString());

    }
}