package maxundmax.holiday4friends.Business;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Max on 19.04.2018.
 */

public class SubscriptionObject extends UploadebleObject {

    private final String SUBSCRIPTION_COLLECTION = "subscription";
    private final String TAG = "SubscriptionObject";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHoliday_id() {
        return holiday_id;
    }

    public void setHoliday_id(String holiday_id) {
        this.holiday_id = holiday_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    private String id;
    private String holiday_id;
    private String user_id;
    private Date timestamp;

    public SubscriptionObject() {
    }


    public void uploadSubScriptionToFirebase(Activity context) {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        String id = mFirestore.collection(SUBSCRIPTION_COLLECTION).document().getId();
        this.setId(id);


        Task<Void> voidTask = mFirestore.collection(SUBSCRIPTION_COLLECTION)
                .document(id)
                .set(getDataMap())
                .addOnCompleteListener(context, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "write:onComplete:failed", task.getException());
                        } else {

                        }
                    }
                });
    }

    @Override
    public Map getDataMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", this.id);
        map.put("user_id", this.user_id);
        map.put("holiday_id", this.holiday_id);
        map.put("timestamp", Calendar.getInstance().getTime());

        return map;
    }
}


