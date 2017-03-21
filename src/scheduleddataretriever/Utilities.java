/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Paris
 */
public class Utilities {
    
    
    public Utilities(){
        
    }
    
    
    String getMeasurement(String UUID, String measurement, long start, long end) throws IOException {

        String URL = "http://inlife-1.inab.certh.gr:8080/inlife/api/data/Patient/"
                + UUID
                + "/" + measurement + "/"
                + "TimeSeries?start="
                + start
                + "&end="
                + end;

        if (measurement == "HeartRate") {
            URL += "&filter=true";
        }

//        System.out.println(URL);
        InputStream response = sendGET(URL, false);
        StringWriter writer = new StringWriter();
        IOUtils.copy(response, writer, "UTF-8");
        String responseString = writer.toString();
//        System.out.println(response);
//        System.out.println(responseString);
        return responseString.substring(1, responseString.length() - 1);
    }
    
    
    public void getAverage(String response) throws JSONException {
        double sum = 0;

        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        int count = series.length();

        for (int i = 0; i < series.length(); i++) {
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            sum += Double.valueOf(value);
        }
        System.out.println("average");
        System.out.println(sum / count);
    }

    public void getAverageMonthly(float[] percentageWorn, String response) throws JSONException {
        double sumDay = 0;
        double sumNight = 0;
        int countDay = 0;
        int countNight = 0;

        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");

        for (int i = 0; i < series.length(); i++) {
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            long timestampLong = Long.parseLong(timestamp);
            Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(timestampLong);
            int hourOfDay = moment.get(Calendar.HOUR_OF_DAY);

            if (hourOfDay >= 7 && hourOfDay < 22) {
                sumDay += Double.parseDouble(value);
                countDay += 1;
            } else {
                sumNight += Double.parseDouble(value);
                countNight += 1;
            }
        }
        System.out.println("average monthly");
        System.out.println(sumDay / countDay);
        System.out.println(sumNight / countNight);
    }

    public void getTotal(String response) throws JSONException {
 
        if(response.trim().equals("")){
            System.out.println("no data");
            return ;
        }
        double sum = 0;
 
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");

        for (int i = 0; i < series.length(); i++) {
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            sum += Double.valueOf(value);
        }
        System.out.println("total");
        System.out.println(sum);
    }

    public void getTotalMonthly(float[] percentageWorn, String response) throws JSONException {
        double sumDay = 0;
        double sumNight = 0;

        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");

        for (int i = 0; i < series.length(); i++) {
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            long timestampLong = Long.parseLong(timestamp);
            Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(timestampLong);
            int hourOfDay = moment.get(Calendar.HOUR_OF_DAY);

            if (hourOfDay >= 7 && hourOfDay < 22) {
                sumDay += Double.parseDouble(value);
            } else {
                sumNight += Double.parseDouble(value);
            }
        }
        System.out.println("total Monthly");
        System.out.println(sumDay);
        System.out.println(sumNight);
    }
    
    
    public static InputStream sendGET(String url, boolean isTurtle) throws IOException {
        System.out.println("request to:    " + url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Basic d2Vsazp3ZWxr");
        if (!isTurtle) {
            con.setRequestProperty("Accept", "Application/json");
        }
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    con.getInputStream()));
//            String inputLine;

            // print result
            return con.getInputStream();
        } else {
            String error = "GET request error";
            InputStream errorStream = new ByteArrayInputStream(error.getBytes(StandardCharsets.UTF_8));
            return errorStream;
        }

    }
}
