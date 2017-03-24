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
public class DayTimeData {
    private Utilities ut;
    private String UUID;
    
public DayTimeData(String UUID){
    this.ut = new Utilities();
    this.UUID = UUID;
}  

public void getDayTimeData(long yesterdayMorningMillis, long yesterdayNightMillis) throws IOException, JSONException{
    
    String activityIntensityDataDailyDay = this.ut.getMeasurement(UUID, "ActivityIntensity", yesterdayMorningMillis, yesterdayNightMillis);
    double averageActivityIntensity = this.ut.getAverage(activityIntensityDataDailyDay); // [data] trim off the first array
    
    TurtleTemplate tt = new TurtleTemplate();
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(yesterdayNightMillis);
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
//    System.out.println(date);
    String temp = tt.prepareValueTemplate("ActivityIntensityStats", date, "ObservationMethods_DIURNAL", "ObservationMethods_AVERAGE", String.valueOf(averageActivityIntensity), UUID);
    this.ut.sendPOST("http://inlife-1.inab.certh.gr:8080/inlife/api/data/ActivityIntensityStats", temp);
    
    
    
    String stepsPerMinuteDataDailyDay = this.ut.getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayMorningMillis, yesterdayNightMillis);
    double totalSteps = this.ut.getTotal(stepsPerMinuteDataDailyDay);
    
    TurtleTemplate tt2 = new TurtleTemplate();
    String temp2 = tt2.prepareValueTemplate("Steps", date, "ObservationMethods_DIURNAL", "ObservationMethods_SUM", String.valueOf(totalSteps), UUID);
    this.ut.sendPOST("http://inlife-1.inab.certh.gr:8080/inlife/api/data/Steps", temp2);
    
}
    
    
}
