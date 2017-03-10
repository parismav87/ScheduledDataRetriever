/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Paris
 */
public class DailyData {
    
    private long thisMorningMillis;
    private boolean debugMode;
    private String UUID;
    private Utilities ut;
    
    public DailyData(boolean debugMode, long thisMorningMillis, String UUID) throws IOException, JSONException, ParseException{
        this.thisMorningMillis = thisMorningMillis;
        this.debugMode = debugMode;
        this.UUID = UUID;
        this.ut = new Utilities();
    }
    
    
    public boolean checkIfMonthlyData(){
        Calendar today = Calendar.getInstance();
        int monthDay = today.get(Calendar.DAY_OF_MONTH);
        int dayToCompare;
        if (this.debugMode) {
            dayToCompare = monthDay;
        } else {
            dayToCompare = 1;
        }
        if (monthDay == dayToCompare) { //if it's the 1st day of the month 00:00:01
            return true;
        } else {
            return false;
        }
    }
    
    
    public void getDailyData() throws IOException, JSONException{
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

        String minutesWornDailyDay = this.ut.getMeasurement(UUID, "ActivityRawType", yesterdayMorningMillis, yesterdayNightMillis);
        String minutesWornDailyNight = this.ut.getMeasurement(UUID, "ActivityRawType", yesterdayNightMillis, thisMorningMillis);
        float percentageWornDailyDay = getMinutesWorn(minutesWornDailyDay);
        float percentageWornDailyNight = getMinutesWorn(minutesWornDailyNight);

        if (percentageWornDailyDay >= 0.75) { //get day data
            getDayTimeData(yesterdayMorningMillis, yesterdayNightMillis);
        }

        if (percentageWornDailyNight >= 0.75) { //get night data
            getNightTimeData(yesterdayNightMillis);
        }

        // get patient questionnaire answers every day.
        InputStream questionnaires = this.ut.getQuestionnaires(UUID, yesterdayMorningMillis);
        this.ut.parseQuestionnaires(questionnaires);

        String activityRawType = this.ut.getMeasurement(UUID, "ActivityRawType", yesterdayNightMillis, thisMorningMillis);
        getSleepQualityIndex(activityRawType);
    }
    
    
    public void getNightTimeData(long yesterdayNightMillis) throws IOException, JSONException{
        String activityIntensityDataDailyNight = this.ut.getMeasurement(UUID, "ActivityIntensity", yesterdayNightMillis, thisMorningMillis);
        this.ut.getAverage(activityIntensityDataDailyNight); // [data] trim off the first array
        String stepsPerMinuteDataDailyNight = this.ut.getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayNightMillis, thisMorningMillis);
        this.ut.getTotal(stepsPerMinuteDataDailyNight);
    }
    
    public void getDayTimeData(long yesterdayMorningMillis, long yesterdayNightMillis) throws IOException, JSONException{
        String activityIntensityDataDailyDay = this.ut.getMeasurement(UUID, "ActivityIntensity", yesterdayMorningMillis, yesterdayNightMillis);
        this.ut.getAverage(activityIntensityDataDailyDay); // [data] trim off the first array
        String stepsPerMinuteDataDailyDay = this.ut.getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayNightMillis, thisMorningMillis);
        this.ut.getTotal(stepsPerMinuteDataDailyDay);
    }
    
    
    
    public void getSleepQualityIndex(String activityRawType) throws JSONException {
        if(activityRawType.trim().equals("")){
            System.out.println("no data");
            return;
        }
        int sleepQualityCriteria = 0;
        JSONObject JSONouter = new JSONObject(activityRawType);
        JSONArray series = JSONouter.getJSONArray("series");
        ArrayList<Integer> sleepTime = new ArrayList<Integer>();
        int layOnBedTime = -1;
        int minsNotAsleep = 0;
        int minsNotAsleepSinceLayDown = 0;
        int timesWoke = 0;

        //sleepTime(0) is the index of the first measurement of REM/NREM, and the last element is the index of the last measurement respectively
        for (int i = 0; i < series.length(); i++) {
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            if (value.equals("4.0") || value.equals("5.0")) {
                sleepTime.add(i);
            }
        }

        System.out.println(sleepTime.get(0) + "..." + sleepTime.get(sleepTime.size() - 1));

        //get last non silent measurement. That+1 is the index where the patient went to bed.
        for (int j = sleepTime.get(0); j >= 0; j--) {
//            System.out.println(series.getJSONObject(0).getString("value"));
            if (!series.getJSONObject(j).getString("value").equals("4.0") && !series.getJSONObject(j).getString("value").equals("5.0") && !series.getJSONObject(j).getString("value").equals("0.0")) {
                layOnBedTime = j + 1;
                break;
            }
        }

//        System.out.println(sleepTime.get(0) - layOnBedTime);
        if ((sleepTime.get(0) - layOnBedTime) <= 30) { //if it took <= 30 mins to fall asleep
            sleepQualityCriteria += 1;
        }

        //check the whole sleep span for awakenings
        for (int k = sleepTime.get(0); k <= sleepTime.get(sleepTime.size() - 1); k++) {
//            System.out.println(k);
            if (!series.getJSONObject(k).getString("value").equals("4.0") && !series.getJSONObject(k).getString("value").equals("5.0")) {
                minsNotAsleep += 1;
//                System.out.println(k);
                if (series.getJSONObject(k - 1).getString("value").equals("4.0") || series.getJSONObject(k - 1).getString("value").equals("5.0")) {
                    timesWoke += 1;
                }
            }
        }
//        System.out.println(timesWoke);
//        System.out.println(minsNotAsleep);
        if (minsNotAsleep <= 20) {
            sleepQualityCriteria += 1;
        }
        if (timesWoke <= 1) {
            sleepQualityCriteria += 1;
        }

        for (int m = layOnBedTime; m <= sleepTime.get(sleepTime.size() - 1); m++) {
            if (!series.getJSONObject(m).getString("value").equals("4.0") && !series.getJSONObject(m).getString("value").equals("5.0")) {
                minsNotAsleepSinceLayDown += 1;
//                System.out.println(k);
            }
        }
//        System.out.println(minsNotAsleepSinceLayDown);
        float sleepToWakeRatio = (float) minsNotAsleepSinceLayDown / (sleepTime.get(sleepTime.size() - 1) - layOnBedTime);
//        System.out.println(sleepToWakeRatio);

        if (sleepToWakeRatio <= 0.15) {
            sleepQualityCriteria += 1;
        }

        System.out.println(sleepQualityCriteria);

    }
    
    
    public float getMinutesWorn(String response) throws JSONException {
        if(response.trim().equals("")){
            System.out.println("no data");
            return 0;
        }
//        System.out.println("-------"+response);
        int sum = 0;
        int sumNonCount = 0;
        JSONObject JSONouter = new JSONObject(response);
        JSONArray series = JSONouter.getJSONArray("series");
        int count = series.length();

//        System.out.println(count);
        for (int i = 0; i < series.length(); i++) {
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");

            int valueInt = (int) Float.parseFloat(value);
//            System.out.println(valueInt);
            if (valueInt != 3 && valueInt < 6) {
                sum += 1;
            } else if (valueInt > 6) {
                sumNonCount += 1;
            }
        }
        //dont count values that dont represent anything useful 
        count = count - sumNonCount;

        float percentageWorn = (float) sum / (float) count;
        if (percentageWorn >= 0.75) {
            System.out.println("worn long enough  " + percentageWorn);
        } else {
            System.out.println("not enough  " + percentageWorn);
        }

        return percentageWorn;
    }
    
    
    
    
}
