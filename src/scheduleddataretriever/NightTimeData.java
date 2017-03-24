/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.IOException;
import java.util.Calendar;
import org.json.JSONException;

/**
 *
 * @author Paris
 */
public class NightTimeData {
    private String UUID;
    private Utilities ut;
    
    public NightTimeData(String UUID){
        this.UUID = UUID;
        this.ut = new Utilities();
    }
    
    public void getNightTimeData(long yesterdayNightMillis, long thisMorningMillis) throws IOException, JSONException{
        String activityIntensityDataDailyNight = this.ut.getMeasurement(UUID, "ActivityIntensity", yesterdayNightMillis, thisMorningMillis);
        double activityIntensity = this.ut.getAverage(activityIntensityDataDailyNight); // [data] trim off the first array
        
        TurtleTemplate tt = new TurtleTemplate();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(thisMorningMillis);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        String monthString = "";
        if (month<10){
            monthString = "0"+String.valueOf(month);
        } else{
            monthString = String.valueOf(month);
        }
        int year = cal.get(Calendar.YEAR);
        String date = year+"-"+monthString+"-"+day;
        System.out.println(date);
        String temp = tt.prepareValueTemplate("ActivityIntensityStats", date, "ObservationMethods_NOCTURNAL", "ObservationMethods_AVERAGE", String.valueOf(activityIntensity), UUID);
        this.ut.sendPOST("http://inlife-1.inab.certh.gr:8080/inlife/api/data/ActivityIntensityStats", temp);
        
        
        String stepsPerMinuteDataDailyNight = this.ut.getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayNightMillis, thisMorningMillis);
        double totalSteps = this.ut.getTotal(stepsPerMinuteDataDailyNight);
        TurtleTemplate tt2 = new TurtleTemplate();
        String temp2 = tt2.prepareValueTemplate("Steps", date, "ObservationMethods_NOCTURNAL", "ObservationMethods_SUM", String.valueOf(totalSteps), UUID);
        this.ut.sendPOST("http://inlife-1.inab.certh.gr:8080/inlife/api/data/Steps", temp2);
    }
}
