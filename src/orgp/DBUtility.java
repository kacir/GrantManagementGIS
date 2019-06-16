package orgp;


import java.sql.*;

public class DBUtility {
    private static final String Driver = "org.postgresql.Driver";
    private static final String ConnUrl = "jdbc:postgresql://localhost:5433/grants";
    private static final String Username = "postgres";
    private static final String Password = "admin";

    // This is a constructor
    public DBUtility() {
    }

    // create a Connection to the database
    private Connection connectDB() {
        Connection conn = null;
        try {

            Class.forName(Driver);

            conn = DriverManager.getConnection(ConnUrl, Username, Password);
            return conn;
        } catch (Exception e) {
            System.out.println("This is where connection error starts");
            e.printStackTrace();
            System.out.println("This is where connection error ends");
        }
        return conn;
    }

    // execute a sql query (e.g. SELECT) and return a ResultSet
    public ResultSet queryDB(String sql) {
        Connection conn = connectDB();
        ResultSet res = null;
        try {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                res = stmt.executeQuery(sql);
                conn.close();

            } else {
                System.out.println("Connection was not successful for a query");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    // execute a sql query (e.g. INSERT) to modify the database;
    // no return value needed
    public void modifyDB(String sql) {
        Connection conn = connectDB();
        try {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();
                conn.close();

            }
        } catch (Exception e) {
            System.out.println("Connection was not successful for a modification");
            e.printStackTrace();
        }
    }

    /**
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        // You can test the methods you created here
        DBUtility util = new DBUtility();


        // 2. query the database
        ResultSet res = util.queryDB("select * from structures ");
        while (res.next()) {
            System.out.println("label: " + res.getString("label"));
        }

    }

}