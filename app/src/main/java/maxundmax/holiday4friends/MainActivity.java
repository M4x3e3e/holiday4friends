package maxundmax.holiday4friends;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maxundmax.holiday4friends.Business.Adapter.ListViewFeedObjectAdapter;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.Adapter.ListViewHolidayObjectAdapter;
import maxundmax.holiday4friends.Business.MediaObject;
import maxundmax.holiday4friends.Business.SubscriptionObject;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    private static final int RC_SIGN_IN = 123;
    private static final int CREATE_HOLIDAY = 12;
    private static final int MYHOLIDAYS_VIEW = 13;
    private static final int HOLIDAY_OVERVIEW = 14;
    private static final int SUBSCRIPTION_VIEW = 15;

    private FirebaseFirestore mFirestore;
    private Context mCtx;

    private static final String TAG = "MainActivity";
    private static final String MEDIA_COLLECTION = "media";
    private static final String HOLIDAY_COLLECTION = "holiday";
    private static final String SUBSCRIPTION_COLLECTION = "subscription";

    private ProgressBar progressBar;
    private RelativeLayout splashScreen;
    private static ArrayList<SubscriptionObject> subscriptionObjects = new ArrayList<>();
    private static ArrayList<HolidayObject> holidayObjects = new ArrayList<>();
    private static ArrayList<MediaObject> mediaObjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Setze ContentView

        mFirestore = FirebaseFirestore.getInstance();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Login();

        splashScreen = findViewById(R.id.splashLogo);
        splashScreen.setVisibility(View.VISIBLE);

        findViewById(R.id.footerBtnAddHoliday).setOnClickListener(this);
        findViewById(R.id.footerBtnHolidays).setOnClickListener(this);
        findViewById(R.id.footerBtnSubscriptions).setOnClickListener(this);


    }

    private void Initialize(){

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 500);
        animation.setDuration(3000); // in milliseconds
        animation.setRepeatMode(ValueAnimator.RESTART);
        animation.setRepeatCount(ValueAnimator.INFINITE);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        mediaObjects.clear();
        subscriptionObjects.clear();
        holidayObjects.clear();


        getSubscriptions();
    }

    private void Login() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
               );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }



    private void getSubscriptions() {
        mFirestore.collection(SUBSCRIPTION_COLLECTION).whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        subscriptionObjects.clear();
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                            EndInitialize();
                        } else {
                            List<SubscriptionObject> subscriptionObjectList = documentSnapshots.toObjects(SubscriptionObject.class);
                            subscriptionObjects.addAll(subscriptionObjectList);
                            getHoliday(subscriptionObjectList);
                        }

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

    private void getHoliday(final List<SubscriptionObject> subs) {


        mFirestore.collection(HOLIDAY_COLLECTION).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        holidayObjects.clear();
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                            EndInitialize();
                        } else {
                            List<HolidayObject> holidayObjectList = documentSnapshots.toObjects(HolidayObject.class);

                            for (HolidayObject o : holidayObjectList) {
                                for (SubscriptionObject s : subs) {
                                    if (o.getId().equals(s.getHoliday_id())) {
                                        holidayObjects.add(o);
                                    }
                                }
                            }
                            getMedia();
                        }

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

    private void getMedia() {

        mFirestore.collection(MEDIA_COLLECTION).orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot documentSnapshots) {
            mediaObjects.clear();
            if (documentSnapshots.isEmpty()) {
                Log.d(TAG, "onSuccess: LIST EMPTY");
            } else {
                List<MediaObject> mediaObjectList = documentSnapshots.toObjects(MediaObject.class);

                for (MediaObject o : mediaObjectList) {
                    for (HolidayObject s : holidayObjects) {
                        if (o.getHoliday_id().equals(s.getId())) {
                            mediaObjects.add(o);
                        }
                    }
                }
            }
            EndInitialize();
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

    private void EndInitialize() {
        if(mediaObjects.size() <= 0)
        {
            ListView emptyView = findViewById(R.id.newsfeedListView);
            TextView txtView = (TextView) findViewById(R.id.newsfeedTextView);
            txtView.setText("Hier siehst du Beiträge von abonnierten Urlauben. Bisher keine Beiträge vorhanden.");
            emptyView.setEmptyView(txtView);
        }

        final ListView listView = (ListView) findViewById(R.id.newsfeedListView);
        ListViewFeedObjectAdapter adap = new ListViewFeedObjectAdapter(mediaObjects,holidayObjects, this);
       /*  listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });*/
        listView.setAdapter(adap);

        progressBar.clearAnimation();
        progressBar.setVisibility(View.GONE);
        splashScreen.setVisibility(View.GONE);
    }



    private void OpenSubscriptionView() {
        Intent intent = new Intent(this, subscription_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, SUBSCRIPTION_VIEW);
    }

    private void OpenMyHolidaysView() {
        Intent intent = new Intent(this, myHolidays_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, MYHOLIDAYS_VIEW);
    }

    private void CreateHoliday() {
        Intent intent = new Intent(this, create_holiday.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, CREATE_HOLIDAY);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Initialize();

                // ...
            } else {
                // Sign in failed, check response for error code
                Login();
            }
        } else if (requestCode == CREATE_HOLIDAY) {

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                // getData();
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
            }

        }
        else if (requestCode == SUBSCRIPTION_VIEW) {
                Initialize();

        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_create_activity) {
            CreateHoliday();

        }
        if (id == R.id.nav_subscription_activity) {
            OpenSubscriptionView();

        }
        if (id == R.id.nav_myHolidays_activity) {
            OpenMyHolidaysView();

        }
        if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNavigationViewListner() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    public void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Erfolgreich ausgelogt.", Toast.LENGTH_LONG).show();
                        Login();
                    }
                });


    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.footerBtnAddHoliday) {
            CreateHoliday();

        }
        if (view.getId()  == R.id.footerBtnSubscriptions) {
            OpenSubscriptionView();

        }
        if (view.getId()  == R.id.footerBtnHolidays) {
            OpenMyHolidaysView();

        }
    }
}

/*
TODO:

        Foto Cache implementieren !
        Splash Screen --> LOGO !
        Neuigkeiten --> Tage angezeigt werden !
        Statistiken zu den Urlauben (Abbos)
        Löschen aller Objekte !
        Subscription zurück nehmen !
        Strings in Resource File
        Kommentare im Code LEL
*/

