/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Paris
 */
public class DailyDataRetriever {
    
    private static final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(5);
    
    //constructor
    public DailyDataRetriever(){
        
    }
    
    public static void run() throws IOException, JSONException, ParseException {
        
        boolean debugMode = true;
        DailyDataRetriever ddr = new DailyDataRetriever();
        Calendar now = Calendar.getInstance();
        Calendar thisMorning = Calendar.getInstance();
        thisMorning.set(Calendar.HOUR_OF_DAY, 7);
        thisMorning.set(Calendar.MINUTE, 0);
        thisMorning.set(Calendar.SECOND, 0);
        
        long thisMorningMillis = thisMorning.getTimeInMillis();
        String UUID = "d4135cc8-6c9a-422d-9ba7-910db1e0fe5a";
        
        Runnable yourRunnable = new Runnable() {
            @Override
            public void run() {
                try {
//                    wd.retrieve(URL);
                    ddr.getDailyData(debugMode, thisMorningMillis, UUID);
                    
                    // Implement your Code here!
                } catch (IOException ex) {
                    Logger.getLogger(DailyDataRetriever.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(DailyDataRetriever.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(DailyDataRetriever.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

                //add logic to calculate millis until next 7 oclock
                //if initialDelay <0 then it should be MillisOf24Hours - initialDelay for next morning
//        long initialDelay = thisMorning.getTimeInMillis() - now.getTimeInMillis();
        int delay = 60* 60 * 24; //24 hours   
        long initialDelay = 0;
        if (!debugMode){
            initialDelay = thisMorning.getTimeInMillis() - now.getTimeInMillis();
            if(initialDelay <0){ //if this morning has already passed, it will be a negative number
                initialDelay = (1000*60*60*24) - initialDelay;
            }
        }
        

        scheduler.scheduleWithFixedDelay(yourRunnable, initialDelay, delay, TimeUnit.SECONDS);

    }
    
    public void getDailyData(boolean debugMode, long thisMorningMillis, String UUID) throws IOException, JSONException, ParseException {
        
        Calendar today = Calendar.getInstance();
        int monthDay = today.get(Calendar.DAY_OF_MONTH);
        int dayToCompare;
        if (debugMode){
            dayToCompare = monthDay;
        } else {
            dayToCompare = 1;
        }
        
        
        if(monthDay == dayToCompare){ //if it's the 1st day of the month 00:00:01
            getMonthlyData(today, thisMorningMillis, UUID);
        }
        
        Calendar yesterdayMorning = Calendar.getInstance();
        yesterdayMorning.add(Calendar.DATE, -1);
        yesterdayMorning.set(Calendar.HOUR_OF_DAY, 7);
        yesterdayMorning.set(Calendar.MINUTE, 0);
        yesterdayMorning.set(Calendar.SECOND, 1);
        long yesterdayMorningMillis = yesterdayMorning.getTimeInMillis();
//        System.out.println(yesterdayMorningMillis + "----" + thisMorningMillis);
        
        Calendar yesterdayNight = Calendar.getInstance();
        yesterdayNight.add(Calendar.DATE, -1);
        yesterdayNight.set(Calendar.HOUR_OF_DAY, 22);
        yesterdayNight.set(Calendar.MINUTE, 0);
        yesterdayNight.set(Calendar.SECOND, 0);
        long yesterdayNightMillis = yesterdayNight.getTimeInMillis();
        
        String minutesWornDailyDay = getMeasurement(UUID, "ActivityRawType", yesterdayMorningMillis, yesterdayNightMillis);
        String minutesWornDailyNight = getMeasurement(UUID, "ActivityRawType", yesterdayNightMillis, thisMorningMillis);
        float percentageWornDailyDay = getMinutesWorn(minutesWornDailyDay);
        float percentageWornDailyNight = getMinutesWorn(minutesWornDailyNight);
        
        if(percentageWornDailyDay >= 0.75){ //get day data
            String activityIntensityDataDailyDay = getMeasurement(UUID, "ActivityIntensity", yesterdayMorningMillis, yesterdayNightMillis);
            getAverage(activityIntensityDataDailyDay); // [data] trim off the first array
            String stepsPerMinuteDataDailyDay = getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayNightMillis, thisMorningMillis);
            getTotal(stepsPerMinuteDataDailyDay);
        }
        
        if(percentageWornDailyNight >= 0.75){ //get night data
            String activityIntensityDataDailyNight = getMeasurement(UUID, "ActivityIntensity", yesterdayNightMillis, thisMorningMillis);
            getAverage(activityIntensityDataDailyNight); // [data] trim off the first array
            String stepsPerMinuteDataDailyDay = getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayNightMillis, thisMorningMillis);
            getTotal(stepsPerMinuteDataDailyDay);
        }

        
    }
    
    String getMeasurement(String UUID, String measurement, long start, long end) throws IOException{
        
        String URL = "http://inlife-1.inab.certh.gr:8080/inlife/api/data/Patient/"
                + UUID
                + "/"+measurement+"/"
                + "TimeSeries?start="
                + start
                + "&end="
                + end;
        
        if(measurement == "HeartRate"){
            URL+= "&filter=true";
        }
        
//        System.out.println(URL);
        String response = sendGET(URL);
//        System.out.println(response);
        return response.substring(1, response.length()-1);
    }
    
    public float[] getMinutesWornMonthly(String response) throws JSONException{
//        System.out.println(response);
        int sumDay = 0;
        int sumNight = 0;
        int countDay = 0;
        int countNight = 0;
        int sumNonCountDay = 0;
        int sumNonCountNight = 0;
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        
//        System.out.println(count);
        for(int i=0; i<series.length(); i++){
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            long timestampLong = Long.parseLong(timestamp);
            Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(timestampLong);
            int hourOfDay = moment.get(Calendar.HOUR_OF_DAY);
            int valueInt = (int)Float.parseFloat(value);
//            System.out.println(valueInt);
            if(valueInt!=3 && valueInt<6){
                if(hourOfDay>=7 && hourOfDay<22){ //daytime
                    sumDay+= 1;
                    countDay +=1;
                } else { //nighttime
                    sumNight +=1;
                    countNight +=1;
                }
            } else if(valueInt ==3 || valueInt == 6){
                if(hourOfDay>=7 && hourOfDay<22){
                    countDay +=1;
                } else {
                    countNight +=1;
                }
            } else{
                if(hourOfDay>=7 && hourOfDay<22){
                    sumNonCountDay +=1;
                } else {
                    sumNonCountNight +=1;
                }
            }
        }
        //dont count values that dont represent anything useful
        countDay = countDay - sumNonCountDay;
        countNight = countNight - sumNonCountNight;
        
        float[] percentageWorn = new float[2];
        percentageWorn[0] = (float)sumDay/ (float)countDay;
        percentageWorn[1] = (float)sumNight/ (float)countNight;
        
        return percentageWorn;
    }
    
    public float getMinutesWorn(String response) throws JSONException{
//        System.out.println(response);
        int sum = 0;
        int sumNonCount = 0;
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        int count = series.length();
        
//        System.out.println(count);
        for(int i=0; i<series.length(); i++){
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            
            int valueInt = (int)Float.parseFloat(value);
//            System.out.println(valueInt);
            if(valueInt!=3 && valueInt<6){
                sum+=1;
            } else if (valueInt>6){
                sumNonCount += 1;
            }
        }
        //dont count values that dont represent anything useful
        count = count-sumNonCount;
        
        float percentageWorn = (float)sum/ (float)count;
        if(percentageWorn>=0.75){
            System.out.println("worn long enough  " + percentageWorn);
        } else {
            System.out.println("not enough  " + percentageWorn);
        }
        
        return percentageWorn;
    }
    
    public void getAverage(String response) throws JSONException{
        double sum = 0;
        
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        int count = series.length();
        
        for(int i=0; i<series.length(); i++){
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            sum+= Double.valueOf(value);
        }
        System.out.println("average");
        System.out.println(sum/count);
    }
    
    public void getAverageMonthly(float[] percentageWorn, String response) throws JSONException{
        double sumDay = 0;
        double sumNight = 0;
        int countDay = 0;
        int countNight = 0;
        
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        
        for(int i=0; i<series.length(); i++){
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            long timestampLong = Long.parseLong(timestamp);
            Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(timestampLong);
            int hourOfDay = moment.get(Calendar.HOUR_OF_DAY);
            
            if(hourOfDay>=7 && hourOfDay<22){
                sumDay += Double.parseDouble(value);
                countDay +=1;
            } else {
                sumNight += Double.parseDouble(value);
                countNight +=1;
            }
        }
        System.out.println("average monthly");
        System.out.println(sumDay/countDay);
        System.out.println(sumNight/countNight);
    }
    
    public void getTotal(String response) throws JSONException{
        double sum = 0;
        
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        
        for(int i=0; i<series.length(); i++){
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            sum+= Double.valueOf(value);
        }
        System.out.println("total");
        System.out.println(sum);
    }
    
    public void getTotalMonthly(float[] percentageWorn, String response) throws JSONException{
        double sumDay = 0;
        double sumNight = 0;
        
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        
        for(int i=0; i<series.length(); i++){
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            long timestampLong = Long.parseLong(timestamp);
            Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(timestampLong);
            int hourOfDay = moment.get(Calendar.HOUR_OF_DAY);
            
            if(hourOfDay>=7 && hourOfDay<22){
                sumDay += Double.parseDouble(value);
            } else {
                sumNight += Double.parseDouble(value);
            }
        }
        System.out.println("total Monthly");
        System.out.println(sumDay);
        System.out.println(sumNight);
    }
    

    public void getMonthlyData(Calendar today, long midnightMillis, String UUID) throws IOException, JSONException{
        
        today.set(Calendar.HOUR_OF_DAY, 7);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        long thisMorningMillis = today.getTimeInMillis();
        
        Calendar startOfMonth = Calendar.getInstance();
        startOfMonth.add(Calendar.MONTH, -1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 7);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 1); 
       long startOfMonthMillis = startOfMonth.getTimeInMillis();
//        System.out.println(yesterdayMorningMillis + "----" + thisMorningMillis);
       
        
        
        String minutesWornMonthly = getMeasurement(UUID, "ActivityRawType", startOfMonthMillis, thisMorningMillis);
        float[] percentageWornMonthly = getMinutesWornMonthly(minutesWornMonthly);
        
        String activityIntensityDataMonthly = getMeasurement(UUID, "ActivityIntensity", startOfMonthMillis, thisMorningMillis);
        getAverageMonthly(percentageWornMonthly, activityIntensityDataMonthly); // [data] trim off the first array
        String stepsPerMinuteDataMonthly = getMeasurement(UUID, "ActivityStepsPerMinute", startOfMonthMillis, thisMorningMillis);
        getTotalMonthly(percentageWornMonthly, stepsPerMinuteDataMonthly);
        
        
    }
    
    
    
    public static String sendGET(String url) throws IOException {
//        System.out.println(url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Basic d2Vsazp3ZWxr");
        con.setRequestProperty("Accept", "Application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            return response.toString();
        } else {
            return "GET request error";
        }

    }
    
}
