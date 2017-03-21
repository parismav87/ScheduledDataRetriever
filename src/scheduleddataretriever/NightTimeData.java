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
public class NightTimeData {
    private String UUID;
    private Utilities ut;
    
    public NightTimeData(String UUID){
        this.UUID = UUID;
        this.ut = new Utilities();
    }
    
    public void getNightTimeData(long yesterdayNightMillis, long thisMorningMillis) throws IOException, JSONException{
        String activityIntensityDataDailyNight = this.ut.getMeasurement(UUID, "ActivityIntensity", yesterdayNightMillis, thisMorningMillis);
        this.ut.getAverage(activityIntensityDataDailyNight); // [data] trim off the first array
        String stepsPerMinuteDataDailyNight = this.ut.getMeasurement(UUID, "ActivityStepsPerMinute", yesterdayNightMillis, thisMorningMillis);
        this.ut.getTotal(stepsPerMinuteDataDailyNight);
    }
}
