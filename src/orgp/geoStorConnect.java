package orgp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class geoStorConnect {

    public static String connectionUrl = "jdbc:sqlserver://db.geostor.org:1433;databaseName=asdi;user=adpt;password=Kv3Dc2katEmCBS4tARuo";


    public static void main(String args[]){
        System.out.println("testing!");
        geoStorConnect test = new geoStorConnect();
        JSONArray tempArray = new JSONArray();

        tempArray = test.searchParks("City Park", tempArray, 10);

        System.out.println(tempArray.toString());
    }

    public JSONArray searchParks(String term,  JSONArray resultArray, int searchLimit){



        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        //build the sql request for parks matching a specific name
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            String SQL = "SELECT TOP (" + String.valueOf(searchLimit)  + ") [OBJECTID],[parkNum],[currentNam],[pastName], [county] ,[Shape].STCentroid().ToString() AS centroid FROM [asdi].[adpt].[OGPARKFOOTPRINTS]";
            SQL += " WHERE OBJECTID = " + term + " AND [type] = 'funded park' ORDER BY [currentNam] ASC;";
            System.out.println("moving through MS SQL code");
            ResultSet rs = stmt.executeQuery(SQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                JSONObject item = new JSONObject();

                item.put("OBJECTID", rs.getString("OBJECTID"));
                item.put("parkNum", rs.getString("parkNum"));
                item.put("currentNam", rs.getString("currentNam"));
                item.put("pastName", rs.getString("pastName"));
                item.put("county", rs.getString("county"));
                item.put("centroid", rs.getString("centroid"));
                item.put("type", "park");

                resultArray.put(item);
            }
        }
        // Handle any errors that may have occurred.
        catch (SQLException | JSONException e) {
            e.printStackTrace();
        }

        System.out.println("finished main portion of script");

        return resultArray;
    }

}
