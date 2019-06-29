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

        JSONObject result = new JSONObject();

        try {
            result = new getSponsorRemoteInfo().get(url, type);
        } catch (JSONException e){
            e.printStackTrace();
        }

        response.getWriter().write(result.toString());

    }
}
