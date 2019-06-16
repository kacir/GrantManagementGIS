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
