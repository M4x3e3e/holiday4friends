package maxundmax.holiday4friends.Business;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Max on 18.04.2018.
 */

public class MediaObject {

    private static final String TAG = "MediaObject";
    private static final String MEDIA_COLLECTION = "media";
    private HashMap mediaMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    private String name;
    private Bitmap image;
    private String holiday_id;
    private String imagepath;
    private String description;
    private String id;

    public MediaObject(){}

    public MediaObject(Bitmap image, String holiday_id){
        this.image = image; this.holiday_id = holiday_id;
    }

    public void uploadMediaToFirebase(Uri path, Activity context) {
        this.imagepath = FirebaseMethods.uploadImageToFirebase(path, context);
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        String id = mFirestore.collection(MEDIA_COLLECTION).document().getId();
        this.setId(id);


        Task<Void> voidTask = mFirestore.collection(MEDIA_COLLECTION)
                .document(id)
                .set(getMediaMap())
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



    public Map getMediaMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id); //Set the user as Story Owner
        map.put("holiday_id", this.holiday_id); //Set the user as Story Owner
        map.put("name", this.name);
        map.put("description", this.description);
        map.put("timestamp", Calendar.getInstance().getTime());
        map.put("imagepath", this.imagepath);
        return map;
    }
}
