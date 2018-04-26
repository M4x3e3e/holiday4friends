package maxundmax.holiday4friends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import maxundmax.holiday4friends.Business.Adapter.ListViewHolidayObjectAdapter;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.SubscriptionObject;


public class myHolidays_activity extends AppCompatActivity {


    private static final String TAG = "subscription_activity";
    private static final String HOLIDAY_COLLECTION = "holiday";

    private static final int HOLIDAY_OVERVIEW = 13;
    private Context mCtx;
    private static ArrayList<HolidayObject> mArrayList = new ArrayList<>();
    FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myholidays);
        mFirestore = FirebaseFirestore.getInstance();
        mCtx = this;
        getHolidayCollection();
    }


    private void getHolidayCollection() {
        mFirestore.collection(HOLIDAY_COLLECTION).whereEqualTo("owner_id", FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        mArrayList.clear();
                        if (documentSnapshots.isEmpty()) {

                            ListView emptyView = findViewById(R.id.myHolidayListView);
                            TextView txtView = (TextView)findViewById(R.id.myHolidayTextView);
                            txtView.setText("Du hast keine Urlaube angelegt.");
                            emptyView.setEmptyView(txtView);
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            List<HolidayObject> acObjs = documentSnapshots.toObjects(HolidayObject.class);
                            mArrayList.addAll(acObjs);
                        }
                        Initialize();
                        return;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error getting data!!!", Toast.LENGTH_LONG).show();
                    }
                });
    }




    private void Initialize() {
        final ListView listView = (ListView) findViewById(R.id.myHolidayListView);
        ListViewHolidayObjectAdapter adap = new ListViewHolidayObjectAdapter(mArrayList, this, true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HolidayObject selectedAC = (HolidayObject) listView.getItemAtPosition(i);

                Intent intent = new Intent(mCtx, holiday_overview.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, HOLIDAY_OVERVIEW);
                intent.putExtra("id", selectedAC.getId());
                startActivity(intent);
            }


            public void onNothingSelected(AdapterView parentView) {

            }
        });

        listView.setAdapter(adap);
    }


}


