/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.IOException;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Paris
 */
public class MonthlyData {
    
    private Calendar today;
    private String UUID;
    private long thisMorningMillis;
    private Utilities ut;
    
    public MonthlyData(long thisMorningMillis, String UUID){
        this.UUID = UUID;
        this.thisMorningMillis = thisMorningMillis;
        this.today = Calendar.getInstance();
        this.ut = new Utilities();
    }
    

    public void getMonthlyData() throws IOException, JSONException {

        Calendar startOfMonth = Calendar.getInstance();
        startOfMonth.add(Calendar.MONTH, -1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 7);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 1);
        long startOfMonthMillis = startOfMonth.getTimeInMillis();
//        System.out.println(yesterdayMorningMillis + "----" + thisMorningMillis);

        String minutesWornMonthly = this.ut.getMeasurement(UUID, "ActivityRawType", startOfMonthMillis, this.thisMorningMillis);
        float[] percentageWornMonthly = getMinutesWornMonthly(minutesWornMonthly);

        String activityIntensityDataMonthly = this.ut.getMeasurement(UUID, "ActivityIntensity", startOfMonthMillis, this.thisMorningMillis);
        this.ut.getAverageMonthly(percentageWornMonthly, activityIntensityDataMonthly); // [data] trim off the first array
        String stepsPerMinuteDataMonthly = this.ut.getMeasurement(UUID, "ActivityStepsPerMinute", startOfMonthMillis, this.thisMorningMillis);
        this.ut.getTotalMonthly(percentageWornMonthly, stepsPerMinuteDataMonthly);

    }
    
    
    
    public float[] getMinutesWornMonthly(String response) throws JSONException {
        if(response.trim().equals("")){
            System.out.println("no data");
            float[] noData = {};
            return noData;
        }
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
        for (int i = 0; i < series.length(); i++) {
            JSONObject measurement = series.getJSONObject(i);
            String value = measurement.getString("value");
            String timestamp = measurement.getString("timestamp");
            long timestampLong = Long.parseLong(timestamp);
            Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(timestampLong);
            int hourOfDay = moment.get(Calendar.HOUR_OF_DAY);
            int valueInt = (int) Float.parseFloat(value);
//            System.out.println(valueInt);
            if (valueInt != 3 && valueInt < 6) {
                if (hourOfDay >= 7 && hourOfDay < 22) { //daytime
                    sumDay += 1;
                    countDay += 1;
                } else { //nighttime
                    sumNight += 1;
                    countNight += 1;
                }
            } else if (valueInt == 3 || valueInt == 6) {
                if (hourOfDay >= 7 && hourOfDay < 22) {
                    countDay += 1;
                } else {
                    countNight += 1;
                }
            } else if (hourOfDay >= 7 && hourOfDay < 22) {
                sumNonCountDay += 1;
            } else {
                sumNonCountNight += 1;
            }
        }
        //dont count values that dont represent anything useful
        countDay = countDay - sumNonCountDay;
        countNight = countNight - sumNonCountNight;

        float[] percentageWorn = new float[2];
        percentageWorn[0] = (float) sumDay / (float) countDay;
        percentageWorn[1] = (float) sumNight / (float) countNight;

        return percentageWorn;
    }
    
    
    
}
