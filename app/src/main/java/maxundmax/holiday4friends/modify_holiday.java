package maxundmax.holiday4friends;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import maxundmax.holiday4friends.Business.FirebaseMethods;
import maxundmax.holiday4friends.Business.HolidayObject;

public class modify_holiday extends AppCompatActivity {

    private HolidayObject hObj;
    private static final String HOLIDAY_COLLECTION = "holiday";


    EditText nameTbx;
    EditText descriptionTbx;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_holiday);

        nameTbx = findViewById(R.id.modifyHolidayName);
        descriptionTbx = findViewById(R.id.modifyHolidayDescription);
        imageView = findViewById(R.id.modifyHolidayImageView);

        String id = getIntent().getStringExtra("id");
        LoadHolidayFromDB(id);
    }

    private void Initialize(){

        nameTbx.setText(hObj.getName());
        descriptionTbx.setText(hObj.getDescription());
        FirebaseMethods.downloadImageIntoImageView(imageView,hObj);
    }



    public void LoadHolidayFromDB(String id) {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(HOLIDAY_COLLECTION).whereEqualTo("id", id).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {

                            return;
                        } else {
                            List<HolidayObject> acObjs = documentSnapshots.toObjects(HolidayObject.class);
                            hObj = (HolidayObject)acObjs.get(0);
                            Initialize();
                            return;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }
}
