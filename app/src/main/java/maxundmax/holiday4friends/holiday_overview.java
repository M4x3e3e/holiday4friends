package maxundmax.holiday4friends;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import maxundmax.holiday4friends.Business.Adapter.ListViewHolidayObjectAdapter;
import maxundmax.holiday4friends.Business.Adapter.ListViewMediaObjectAdapter;
import maxundmax.holiday4friends.Business.FirebaseMethods;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.MediaObject;
import maxundmax.holiday4friends.Business.UploadebleObject;

public class holiday_overview extends AppCompatActivity
        implements View.OnClickListener {

    private HolidayObject hObj;
    private static final String MEDIA_COLLECTION = "media";
    private static final String HOLIDAY_COLLECTION = "holiday";
    private final int PICK_IMAGE_REQUEST = 7;
    private final int MODIFY_HOLIDAY = 13;

    TextView nameTbx;
    TextView descriptionTbx;
    ImageView imageView;
    ImageButton btnAddImageToHoliday;
    ImageButton btnModifyHoliday;
    ListViewMediaObjectAdapter lvmoAdapater;
    String hId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_overview);

        nameTbx = findViewById(R.id.holidayOverviewTextViewName);
        descriptionTbx = findViewById(R.id.holidayOverviewTextViewDescription);
        imageView = findViewById(R.id.holidayOverviewImageView);
        btnModifyHoliday = findViewById(R.id.overviewHolidayBtnModify);
        btnAddImageToHoliday = findViewById(R.id.btn_AddImageToHoliday);
        btnAddImageToHoliday.setOnClickListener(this);

        hId =getIntent().getStringExtra("id");
        LoadHolidayFromDB(hId);
    }

    private void Initialize() {
        nameTbx.setText(hObj.getName());
        descriptionTbx.setText(hObj.getDescription());
        FirebaseMethods.downloadImageIntoImageView(imageView, hObj);
        boolean isOwnerView = hObj.getOwner_id().equals(FirebaseAuth.getInstance().getUid());
        ImageButton btnDelete = findViewById(R.id.overviewHolidayBtnDelete);
        if (isOwnerView) {
            btnModifyHoliday.setVisibility(View.VISIBLE);
            btnModifyHoliday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.getId() == R.id.overviewHolidayBtnModify){
                        Intent intent = new Intent(holiday_overview.this, modify_holiday.class);
                        intent.putExtra("id", hObj.getId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, MODIFY_HOLIDAY);
                    }
                }
            });
            btnAddImageToHoliday.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (view.getId() == R.id.overviewHolidayBtnDelete) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(holiday_overview.this);
                                alertDialogBuilder
                                        .setTitle("Möchten Sie den Urlaub wirklich Löschen?")
                                        .setCancelable(false)
                                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // Do something with parameter.
                                                deleteHoliday(hObj);

                                            }
                                        })
                                        .setNegativeButton("Nein",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }
                    });

        }

        final ListView mediaListView = findViewById(R.id.mediaListView);
        lvmoAdapater = new ListViewMediaObjectAdapter(hObj.getMediaList(), this, isOwnerView);
        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MediaObject selectedAC = (MediaObject) mediaListView.getItemAtPosition(i);

            }


            public void onNothingSelected(AdapterView parentView) {

            }
        });
        mediaListView.setAdapter(lvmoAdapater);

    }

    private void deleteHoliday(HolidayObject holidayObject) {
        holidayObject.deleteHolidayFromFirebase();
        setResult(42);
        finish();
    }


    public void LoadHolidayFromDB(String id) {
        // FirebaseMethods.GetDataFromFirebase(this.hObj);

        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(HOLIDAY_COLLECTION).whereEqualTo("id", id).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            return;
                        } else {
                            List<HolidayObject> acObjs = documentSnapshots.toObjects(HolidayObject.class);
                            hObj = (HolidayObject) acObjs.get(0);
                            LoadMediaFromDB(hObj);
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

    public void LoadMediaFromDB(final HolidayObject hOj) {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection(MEDIA_COLLECTION).orderBy("timestamp", Query.Direction.DESCENDING).whereEqualTo("holiday_id", hOj.getId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            ListView emptyView = findViewById(R.id.mediaListView);
                            TextView txtView = (TextView) findViewById(R.id.holidayOverviewEmptyTextView);
                            if (hOj.getOwner_id().equals(FirebaseAuth.getInstance().getUid())) {
                                txtView.setText("Bisher sind keine Medien vorhanden. Füge direkt welche hinzu! :)");
                            } else {
                                txtView.setText("Bisher wurden keine Medien hinzugefügt.");
                            }
                            emptyView.setEmptyView(txtView);
                        } else {
                            List<MediaObject> mObjs = documentSnapshots.toObjects(MediaObject.class);
                            hObj.getMediaList().addAll(mObjs);

                        }
                        Initialize();
                        return;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(holiday_overview.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("Message", e.getMessage());
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_AddImageToHoliday) {
            AddPicturesToHoliday();
        }
    }

    private void AddPicturesToHoliday() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                MediaObject mObj = new MediaObject(bitmap, this.hObj.getId());
                mObj.uploadMediaToFirebase(filePath, this);
                this.hObj.getMediaList().add(0,mObj);
                lvmoAdapater.notifyDataSetChanged();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == MODIFY_HOLIDAY) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                LoadHolidayFromDB(hId);

            } else {

            }
        }
    }


}
