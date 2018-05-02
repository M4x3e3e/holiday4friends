package maxundmax.holiday4friends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maxundmax.holiday4friends.Business.Adapter.ListViewHolidayObjectAdapter;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.SubscriptionObject;


public class subscription_activity extends AppCompatActivity {

    // static Vars
    private static final String TAG = "subscription_activity";
    private static final String HOLIDAY_COLLECTION = "holiday";

    private static final int HOLIDAY_OVERVIEW = 13;
    private static final String SUBSCRIPTION_COLLECTION = "subscription";
    private Context mCtx;
    private static ArrayList<HolidayObject> mArrayList = new ArrayList<>();
    FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        mFirestore = FirebaseFirestore.getInstance();
        mCtx = this;
        getHolidayCollection();
    }

    //Holt alle Holiday Objekte aus der Firebase DB
    private void getHolidayCollection() {
        mFirestore.collection(HOLIDAY_COLLECTION).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");

                            return;
                        } else {
                            mArrayList.clear();
                            List<HolidayObject> holidayObjects = documentSnapshots.toObjects(HolidayObject.class);
                            for (HolidayObject holidayObject : holidayObjects) {
                                if (!holidayObject.getOwner_id().equals(FirebaseAuth.getInstance().getUid())) {
                                    mArrayList.add(holidayObject);
                                }
                            }
                            if (mArrayList.size() <= 0) {
                                ListView emptyView = findViewById(R.id.holidaySubscriptionListView);
                                TextView txtView = (TextView) findViewById(R.id.holidaySubscriptionTextView);
                                txtView.setText("Keine Urlaube zum Abonnieren vorhanden.");
                                emptyView.setEmptyView(txtView);
                            }
                            getSubscriptionData();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error getting data!!!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Holt alle Subscription Objekte aus der Firebase DB , welche die UserID des aktuellen Nutzer haben
    private void getSubscriptionData() {
        mFirestore.collection(SUBSCRIPTION_COLLECTION).whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");

                        } else {
                            List<SubscriptionObject> acObjs = documentSnapshots.toObjects(SubscriptionObject.class);
                            for (SubscriptionObject ob : acObjs) {
                                for (HolidayObject hob : mArrayList) {
                                    if (hob.getId().equals(ob.getHoliday_id())) {
                                        hob.setSubscriptionObject(ob);
                                    }
                                }
                            }
                        }
                        LoadSubscriptionCounts();
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

    //Holt alle Subscriptions und berechnet die Anzahl
    private void LoadSubscriptionCounts() {
        mFirestore.collection(SUBSCRIPTION_COLLECTION).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                        } else {
                            List<SubscriptionObject> subscriptionObjects = documentSnapshots.toObjects(SubscriptionObject.class);
                            for (HolidayObject h : mArrayList) {
                                for (SubscriptionObject s : subscriptionObjects) {
                                    if (s.getHoliday_id().equals(h.getId())) {
                                        h.setSubscribeCount(h.getSubscribeCount() + 1);
                                    }
                                }
                            }
                        }

                        renderActivites();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error getting data!!!", Toast.LENGTH_LONG).show();
                    }
                });

    }


    //Nach dem Daten Holen wird die UI Entsprechend gebindet
    private void renderActivites() {
        final ListView listView = (ListView) findViewById(R.id.holidaySubscriptionListView);
        ListViewHolidayObjectAdapter adap = new ListViewHolidayObjectAdapter(mArrayList, this, false);
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


