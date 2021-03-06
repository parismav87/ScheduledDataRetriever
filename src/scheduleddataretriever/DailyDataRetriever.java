/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONException;

/**
 *
 * @author Paris
 */
public class DailyDataRetriever {

    private static final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(5);

    //constructor
    public DailyDataRetriever() {

    }

    public static void run() throws IOException, JSONException, ParseException {
        boolean debugMode = true;
        BasicConfigurator.configure(); //soemthing for jena?

        DailyDataRetriever ddr = new DailyDataRetriever();

        Calendar now = Calendar.getInstance();
        Calendar thisMorning = Calendar.getInstance();
        thisMorning.set(Calendar.HOUR_OF_DAY, 7);
        thisMorning.set(Calendar.MINUTE, 0);
        thisMorning.set(Calendar.SECOND, 0);

        long thisMorningMillis = thisMorning.getTimeInMillis();
        String UUID = "6ea6995e-f011-4277-92ce-d1342ca7c121";
//        ddr.getDailyData(debugMode, thisMorningMillis, UUID);
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
        
        int delay = 60 * 60 * 24; //24 hours   
        long initialDelay = 0;
        if (!debugMode) {
            initialDelay = thisMorning.getTimeInMillis() - now.getTimeInMillis();
            if (initialDelay < 0) { //if this morning has already passed, it will be a negative number
                initialDelay = (1000 * 60 * 60 * 24) - initialDelay;
            }
        }

        scheduler.scheduleWithFixedDelay(yourRunnable, initialDelay, delay, TimeUnit.SECONDS);

    }
    
    

    public void getDailyData(boolean debugMode, long thisMorningMillis, String UUID) throws IOException, JSONException, ParseException {

        DailyData dd = new DailyData(debugMode, thisMorningMillis, UUID);
        MonthlyData md = new MonthlyData(thisMorningMillis, UUID);
        
        if (dd.checkIfMonthlyData()) { //if it's the 1st day of the month 00:00:01
            md.getMonthlyData();
        }
        
        dd.getDailyData();
    }


    

}
