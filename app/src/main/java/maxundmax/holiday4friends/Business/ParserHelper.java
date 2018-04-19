package maxundmax.holiday4friends.Business;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
}
