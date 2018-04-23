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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maximilianprohl on 14.04.18.
 */

public class HolidayObject {


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

    public MutableInt getSubscribeCount() {

        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection("subscription").whereEqualTo("holiday_id", this.getId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {

                            return;
                        } else {
                            List<SubscriptionObject> acObjs = documentSnapshots.toObjects(SubscriptionObject.class);
                            subCount.value = acObjs.size();
                            return;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        return subCount;

    }


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

    private MutableInt subCount;

    private String id;
    private String owner_id;
    private String name;
    private String description;
    private Date startdate;
    private Date enddate;
    private Boolean isPublic;
    private Date timestamp;

    private String imagepath;

    public HolidayObject(String _id, String _ownerId, String _name, String _description, Date _startdate, Date _enddate, Boolean _isPublic, Date _timestamp, String _imagePath) {
        super();
        this.id = _id;
        this.owner_id = _ownerId;
        this.name = _name;
        this.description = _description;
        this.startdate = _startdate;
        this.enddate = _enddate;
        this.isPublic = _isPublic;
        this.timestamp = _timestamp;
        this.imagepath = _imagePath;

    }

    public HolidayObject() {
        mediaList = new ArrayList<>();

        this.subCount = new MutableInt(0);
    }


    public Map getActivityMap() {
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


}
