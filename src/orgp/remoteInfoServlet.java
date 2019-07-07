package orgp;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/remoteinfo")
public class remoteInfoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getParameter("url");
        System.out.println("URL recieved is " + url);
        String type = request.getParameter("type");
        System.out.println("The type of URL request is" + type);

        JSONObject result = new JSONObject();

        try {
            if (type.equals("city")){
                System.out.println("requesting data for municipal");
                result = new getSponsorRemoteInfo().municipal(url);
            }
            if (type.equals("county")){
                System.out.println("requesting data for county");
                result = new getSponsorRemoteInfo().judge(url);
            }

            type.equals("county");

        } catch (JSONException e){
            e.printStackTrace();
        }

        response.getWriter().write(result.toString());

    }
}
