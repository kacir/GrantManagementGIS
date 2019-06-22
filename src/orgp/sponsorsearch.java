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

@WebServlet("/api/sponsorsearch")
public class sponsorsearch extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String searchterm = request.getParameter("searchterm");
        DBUtility dbutil = new DBUtility();

        String sql = "SELECT sponsor, displayname, sponsorcode FROM sponsor WHERE UPPER(sponsor) LIKE UPPER('%" + searchterm + "%') ORDER BY sponsor ASC;";

        ResultSet res = dbutil.queryDB(sql);

        //the output is going to be json so a JSONArray object will be used to hold the output given to the response object
        JSONArray list = new JSONArray();

        try {
            while (res.next()) {
                JSONObject suggestion = new JSONObject();
                suggestion.put("sponsor" , res.getString("sponsor"));
                suggestion.put("displayname" , res.getString("displayname"));
                suggestion.put("sponsorcode" , res.getString("sponsorcode"));
                suggestion.put("type", "sponsor");
                list.put(suggestion);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        //try and get a list of project numbers to that fit the search term
        String ProjectNumSQL = "SELECT projectnum, year, sponsor FROM grantdisplay WHERE UPPER(projectnum) LIKE UPPER('%" + searchterm  + "%');";

        ResultSet projectRes = dbutil.queryDB(ProjectNumSQL);
        System.out.println("about to search grants");
        try {
            while (projectRes.next()) {
                System.out.println("found a single grant suggestion");
                JSONObject suggestion = new JSONObject();
                suggestion.put("sponsor", projectRes.getString("sponsor"));
                suggestion.put("year", projectRes.getString("year"));
                suggestion.put("projectnum", projectRes.getString("projectnum"));
                suggestion.put("type", "grant");
                list.put(suggestion);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }

        response.getWriter().write(list.toString());

    }
}
