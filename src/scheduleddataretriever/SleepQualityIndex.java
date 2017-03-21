/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Paris
 */
public class SleepQualityIndex {
    private String UUID;
    
    public SleepQualityIndex(String UUID){
        this.UUID = UUID;
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
        
        float criteriaPercentage = (float)sleepQualityCriteria/4;
        System.out.println(criteriaPercentage);

    }
}
