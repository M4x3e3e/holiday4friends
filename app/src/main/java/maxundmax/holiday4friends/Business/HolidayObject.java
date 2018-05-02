package maxundmax.holiday4friends.Business;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.MutableInt;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maximilianprohl on 14.04.18.
 */

public class HolidayObject extends UploadebleObject {

    //Firebase Collection Names
    private final String SUBSCRIPTION_COLLECTION = "subscription";
    private final String HOLIDAY_COLLECTION = "holiday";

    //TAG
    private final String TAG = "HolidayObject";

    //Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String ownerId) {
        this.owner_id = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagePath) {
        this.imagepath = imagePath;
    }

    public int getSubscribeCount() { return subscriptionCount; }


    public void setSubscribeCount(int subscriptionCount) { this.subscriptionCount = subscriptionCount; }

    public ArrayList<MediaObject> getMediaList() {
        return mediaList;
    }

    public void setMediaList(ArrayList<MediaObject> mediaList) {
        this.mediaList = mediaList;
    }

    private ArrayList<MediaObject> mediaList;

    public SubscriptionObject getSubscriptionObject() {
        return subscriptionObject;
    }

    public void setSubscriptionObject(SubscriptionObject subscriptionObject) {
        this.subscriptionObject = subscriptionObject;
    }

    private SubscriptionObject subscriptionObject;

    private int subscriptionCount;

    private String id;
    private String owner_id;
    private String name;
    private String description;
    private Date startdate;
    private Date enddate;
    private Boolean isPublic;
    private Date timestamp;

    private String imagepath;


    /**
     * Konstruktor
     */
    public HolidayObject() {
        mediaList = new ArrayList<>();
        this.subscriptionCount = 0;
    }


    /**
     * Gibt eine HashMap mit den Eigenschaften des Holiday Objektes zurück
     * @return HashMap mit allen Eigenschaften
     */
    @Override
    public Map getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id); //Set the user as Story Owner
        map.put("owner_id", this.owner_id); //Set the user as Story Owner
        map.put("name", this.name);
        map.put("description", this.description);
        map.put("public", this.isPublic);
        map.put("startdate", this.startdate);
        map.put("enddate", this.enddate);
        map.put("timestamp", Calendar.getInstance().getTime());
        map.put("imagepath", this.imagepath);
        return map;
    }

    /**
     * Löscht den Urlaub aus der Firebase DB und die dazu gehörigen Subscriptions und Media Datensätze
     */
    public void deleteHolidayFromFirebase() {
        FirebaseMethods.deleteImageFromFirebase(this.imagepath);
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        for (MediaObject mediaObject : mediaList) {
            mediaObject.deleteMedieFromFirebase();
        }
        DeleteAllSubscriptions();
        delete();
    }

    /**
     * Löscht alle Subscriptions zu diesem Holiday Objekt aus der Firebase DB
     */
    private void DeleteAllSubscriptions() {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(SUBSCRIPTION_COLLECTION).whereEqualTo("holiday_id", this.getId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            List<SubscriptionObject> subscriptionObjectList = documentSnapshots.toObjects(SubscriptionObject.class);
                            for (SubscriptionObject sub: subscriptionObjectList) {
                                sub.delete();
                            }
                        }

                        return;
                    }
                });


    }

    /**
     * Löscht das Holiday Objekt aus der Firebase DB
     */
    private void delete(){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(HOLIDAY_COLLECTION).document(this.id).delete().
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

    }

}
