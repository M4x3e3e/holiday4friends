package maxundmax.holiday4friends.Business;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maximilianprohl on 14.04.18.
 */

public class ActivityObject {

    private static final String ACTIVITY_COLLECTION = "activity";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    private String id;
    private String ownerId;
    private String name;
    private String description;
    private Date startdate;
    private Date enddate;
    private Boolean isPublic;
    private Date timestamp;

    private String imagePath;

    public ActivityObject(String _id, String _ownerId, String _name, String _description, Date _startdate, Date _enddate, Boolean _isPublic, Date _timestamp, String _imagePath) {
        this.id = _id;
        this.ownerId = _ownerId;
        this.name = _name;
        this.description = _description;
        this.startdate = _startdate;
        this.enddate = _enddate;
        this.isPublic = _isPublic;
        this.timestamp = _timestamp;
        this.imagePath = _imagePath;

    }

    public ActivityObject() {
    }


    public Map getActivityMap() {


        Map<String, Object> map = new HashMap<>();
        map.put("owner_id", this.ownerId); //Set the user as Story Owner
        map.put("name", this.name);
        map.put("description", this.description);
        map.put("public", this.isPublic);
        map.put("startdate", this.startdate);
        map.put("enddate", this.enddate);
        map.put("timestamp", Calendar.getInstance().getTime());
        map.put("imagepath", this.imagePath);
        return map;
    }

}
