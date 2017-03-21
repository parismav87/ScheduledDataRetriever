/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.IOException;
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
    this.ut.getAverage(activityIntensityDataDailyDay); // [data] trim off the first array
    String stepsPerMinuteDataDailyDay = this.ut.getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayMorningMillis, yesterdayNightMillis);
    this.ut.getTotal(stepsPerMinuteDataDailyDay);
}
    
    
}
