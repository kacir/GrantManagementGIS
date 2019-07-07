package orgp;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class getSponsorRemoteInfo {

    public static void main(String args[]){
        String myURL = "https://www.arcounties.org/counties/crittenden/";

        getSponsorRemoteInfo example = new getSponsorRemoteInfo();
        try {
            example.judge(myURL);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    protected JSONObject judge(String countyURL) throws IOException {

        if (!countyURL.contains("https://" ) && countyURL.contains("http://")){
            countyURL = countyURL.replace("http://", "https://");
        }

        String line = "";
        String documentString = "";

        URL url = new URL(countyURL);
        InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null){
            documentString += line;
        }

        System.out.println("Finished getting from web data");
        br.close();
        is.close();
        System.out.println("HTML taken from the County Association Server");

        JSONObject result = new JSONObject();

        //finding the judges name on the page
        Document doc = Jsoup.parse(documentString);
        Elements judgeParagraph = doc.select("p:contains(County Judge)");
        String[] detailsList = judgeParagraph.html().split("<br>");

        String judgeEmail = judgeParagraph.select("a").text();
        String judgeName = detailsList[1];
        String phoneNumber = detailsList[2].replace("Phone: ", "");
        String faxNumber = detailsList[3].replace("Fax: ", "");
        String mailingAddress = detailsList[4].replace("Address: ", "");


        System.out.println("judges Email is " + judgeEmail);
        System.out.println("judge name is " + judgeName);
        System.out.println("Phone number is " + phoneNumber);
        System.out.println("Fax number is " + faxNumber);
        System.out.println("Mailing Address " + mailingAddress);

        try {
            result.put("judgename" , judgeName);
            result.put("phone" , phoneNumber);
            result.put("fax" , faxNumber);
            result.put("address" , mailingAddress);
            result.put("email", judgeEmail);

        } catch (JSONException e){
            e.printStackTrace();
        }

        return result;

    }

    protected JSONObject municipal(String muniStringURL) throws IOException, JSONException {

        if (!muniStringURL.contains("https://" ) && muniStringURL.contains("http://")){
            muniStringURL = muniStringURL.replace("http://", "https://");
        }

        String line = "";
        String documentString = "";

        URL url = new URL(muniStringURL);
        InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null){
            documentString += line;
        }

        System.out.println("Finished getting from web data");
        br.close();
        is.close();
        System.out.print("HTML taken from the Municipal League Server");
        //System.out.print(documentString);

        JSONObject result = new JSONObject();

        //finding the mayor's name on the page
        Document doc = Jsoup.parse(documentString);
        Elements tables = doc.getElementsByTag("table");
        Elements mayorRow = tables.select("tr:contains(Mayor)");
        Elements tableDataElements = mayorRow.select("td");
        Elements sibling = tableDataElements.eq(1);

        String mayorname = sibling.html().trim();
        result.put("mayorname", mayorname);

        //looking for population numbers
        Elements paragraphPop = doc.select("p:contains(Population:)");

        String cityPopulation = paragraphPop.text().replace("Population: ", "").trim();
        result.put("population", cityPopulation);

        Elements paragraphAddress = doc.select("P:contains(Address:)");
        String address  = paragraphAddress.text().replace("Address: ", "").trim();
        result.put("address", address);

        Elements paragraphPhone = doc.select("P:contains(Phone: )");
        String phone = paragraphPhone.text().replace("Phone: ", "").trim();
        result.put("phone", phone);

        Elements paragraphFax = doc.select("P:contains(Fax: )");
        String fax = paragraphFax.text().replace("Fax: ", "").trim();
        result.put("fax", fax);

        System.out.println(result.toString());
        return result;

        //might be good to put together a county version of this for the judges info
    }
}
