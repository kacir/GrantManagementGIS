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

        String sponsor = request.getParameter("sponsor");
        DBUtility dbutil = new DBUtility();

        String sql = "SELECT * FROM grantdisplay WHERE UPPER(sponsor) = UPPER('" + sponsor +"') ORDER BY year DESC;";
        System.out.println("Follwing is SQL code submitted " + sql);
        ResultSet res = dbutil.queryDB(sql);

        //the output is going to be json so a JSONArray object will be used to hold the output given to the response object
        JSONArray list = new JSONArray();

        try {
            while (res.next()) {
                JSONObject suggestion = new JSONObject();
                suggestion.put("projectnum" , res.getString("projectnum"));
                suggestion.put("sponsor" , res.getString("sponsor"));
                suggestion.put("sponsortype" , res.getString("sponsortype"));
                suggestion.put("displayname" , res.getString("displayname"));
                suggestion.put("year" , res.getString("year"));
                suggestion.put("awardamount" , res.getString("awardamount"));
                suggestion.put("projecttype" , res.getString("projecttype"));
                suggestion.put("county" , res.getString("county"));
                suggestion.put("projectname" , res.getString("projectname"));
                suggestion.put("granttype" , res.getString("granttype"));
                suggestion.put("status" , res.getString("status"));
                suggestion.put("itemscompleted" , res.getString("itemscompleted"));
                suggestion.put("itemsapplication" , res.getString("itemsapplication"));
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
