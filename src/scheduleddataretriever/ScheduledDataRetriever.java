/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.IOException;
import java.text.ParseException;
import org.json.JSONException;

/**
 *
 * @author Paris Mavromoustakos
 */
public class ScheduledDataRetriever {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, JSONException, ParseException{
        // TODO code application logic here
        DailyDataRetriever.run();
    }
    
}
