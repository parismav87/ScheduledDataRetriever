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
    private boolean debugMode; //to speed up testing
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
        
        DayTimeData dtd = new DayTimeData(UUID);
        if (percentageWornDailyDay >= 0.75) { //get day data
            dtd.getDayTimeData(yesterdayMorningMillis, yesterdayNightMillis);
        } else {
            System.out.println("daytime data not sufficient");
        }
        
        NightTimeData ntd = new NightTimeData(UUID);
        if (percentageWornDailyNight >= 0.75) { //get night data
            ntd.getNightTimeData(yesterdayNightMillis, thisMorningMillis);
        } else {
            System.out.println("nighttime data not sufficient");
        }

        QuestionnaireData qd = new QuestionnaireData(UUID);
        qd.getAbnormalQuestionnaireAnswers(yesterdayMorningMillis);
        // get patient questionnaire answers every day.

        SleepQualityIndex sqi = new SleepQualityIndex(UUID, thisMorningMillis);
        String activityRawType = this.ut.getMeasurement(UUID, "ActivityRawType", yesterdayNightMillis, thisMorningMillis);
        sqi.getSleepQualityIndex(activityRawType);
        
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
//        if (percentageWorn >= 0.75) {
//            System.out.println("worn long enough  " + percentageWorn);
//        } else {
//            System.out.println("not enough  " + percentageWorn);
//        }

        return percentageWorn;
    }
    
    
    
    
}
