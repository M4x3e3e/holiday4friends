package maxundmax.holiday4friends.Business;

import android.util.Log;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Max on 17.04.2018.
 */

public class ParserHelper {

    public static Date StringToDate(String str){

        SimpleDateFormat parser =  new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
        Date date = null;
        try {
            date  = parser.parse(str);
        }catch (ParseException pe){
            Log.d("ParserHelper", pe.getMessage());
        }
        return date;

    }

    public static String ParsStringToFeed(Date timestamp) {
        Date today = Calendar.getInstance().getTime();
        int todayDay = new DateTime(today).getDayOfYear();
        int year = new DateTime(today).getYear();
        int timeStampDay = new DateTime(timestamp).getDayOfYear();
        int timeStampDayOfWeek = new DateTime(timestamp).getDayOfWeek();
        int timeStampYear = new DateTime(timestamp).getYear();

        if (todayDay == timeStampDay && year == timeStampYear) {
            return "Heute";
        } else if (todayDay - 1 == timeStampDay) {
            return "Gestern";
        } else if (todayDay - 2 == timeStampDay) {
            return "Vorgestern";
        }
        else if (todayDay +7 <= timeStampDay) {
            switch (timeStampDayOfWeek) {
                case 1: {
                    return "Montag";
                }
                case 2: {
                    return "Dienstag";
                }
                case 3: {
                    return "Mittwoch";
                }
                case 4: {
                    return "Donnerstag";
                }
                case 5: {
                    return "Freitag";
                }
                case 6: {
                    return "Samstag";
                }
                case 7: {
                    return "Sonntag";
                }

            }

        }else{
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY");


            return sdf.format(timestamp);
        }

        return "";
    }
}
