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
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

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
        setContentView(R.layout.activity_main);
        //Setze ContentView

        //Get Firestore getInstance()
        mFirestore = FirebaseFirestore.getInstance();

        //Setzen der Events für die Menüs
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Aufrufen der Login Methode
        Login();

        //Anzeigen des Splashscreen (Logo h4f)
        splashScreen = findViewById(R.id.splashLogo);
        splashScreen.setVisibility(View.VISIBLE);

        //Click Event Footer Buttons
        findViewById(R.id.footerBtnAddHoliday).setOnClickListener(this);
        findViewById(R.id.footerBtnHolidays).setOnClickListener(this);
        findViewById(R.id.footerBtnSubscriptions).setOnClickListener(this);
    }

    private void Initialize() {
        //Prograssbar Starten
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 500);
        animation.setDuration(3000); // in milliseconds
        animation.setRepeatMode(ValueAnimator.RESTART);
        animation.setRepeatCount(ValueAnimator.INFINITE);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        //Objekt Listen leeren
        mediaObjects.clear();
        subscriptionObjects.clear();
        holidayObjects.clear();

        //Hole alle Subscriptions von Firebase
        getSubscriptions();
    }

    private void Login() {
        // Festlegen von Authentifikations Provider
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
        );

        // Erstellen und Aufrufen des Firebase Login Intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }


    //Methode zum Abrufen der Subscription Datensätze von Firebase
    //Sollte eigentlich in eine Extra Klasse ausgelagert sein, aber aufgrund der aktuellen Datenstruktur und Nutzung hier implementiert
    private void getSubscriptions() {
        //
        mFirestore.collection(SUBSCRIPTION_COLLECTION).whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {

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



    //Methode zum Abrufen der Holiday Datensätze von Firebase
    //Sollte eigentlich in eine Extra Klasse ausgelagert sein, aber aufgrund der aktuellen Datenstruktur und Nutzung hier implementiert
    private void getHoliday(final List<SubscriptionObject> subs) {
        mFirestore.collection(HOLIDAY_COLLECTION).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {

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

    //Methode zum Abrufen der Media Datensätze von Firebase
    //Sollte eigentlich in eine Extra Klasse ausgelagert sein, aber aufgrund der aktuellen Datenstruktur und Nutzung hier implementiert
    private void getMedia() {
        mFirestore.collection(MEDIA_COLLECTION).orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                        } else {
                            List<MediaObject> mediaObjectList = documentSnapshots.toObjects(MediaObject.class);

                            //
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
        ListView listView = findViewById(R.id.newsfeedListView);
        if (mediaObjects.size() <= 0) {
            TextView txtView = findViewById(R.id.newsfeedTextView);
            txtView.setText(R.string.KeineEintraegeMain);
            listView.setEmptyView(txtView);
        } else {
            ListViewFeedObjectAdapter adapt = new ListViewFeedObjectAdapter(mediaObjects, holidayObjects, this);
            listView.setAdapter(adapt);
        }
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

        } else if (requestCode == SUBSCRIPTION_VIEW) {
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
        if (view.getId() == R.id.footerBtnSubscriptions) {
            OpenSubscriptionView();

        }
        if (view.getId() == R.id.footerBtnHolidays) {
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

