package maxundmax.holiday4friends;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

    TextView nameTbx;
    TextView descriptionTbx;
    ImageView imageView;
    Button btnAddImageToHoliday;

    ListViewMediaObjectAdapter lvmoAdapater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_overview);

        nameTbx = findViewById(R.id.holidayOverviewTextViewName);
        descriptionTbx = findViewById(R.id.holidayOverviewTextViewDescription);
        imageView = findViewById(R.id.holidayOverviewImageView);

        btnAddImageToHoliday = findViewById(R.id.btn_AddImageToHoliday);
        btnAddImageToHoliday.setOnClickListener(this);

        String id = getIntent().getStringExtra("id");
        LoadHolidayFromDB(id);
    }

    private void Initialize() {
        nameTbx.setText(hObj.getName());
        descriptionTbx.setText(hObj.getDescription());
        FirebaseMethods.downloadImageIntoImageView(imageView, hObj);
        boolean isOwnerView = hObj.getOwner_id().equals(FirebaseAuth.getInstance().getUid());
        if (isOwnerView) {
            btnAddImageToHoliday.setVisibility(View.VISIBLE);
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
        mFirestore.collection(MEDIA_COLLECTION).whereEqualTo("holiday_id", hOj.getId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            ListView emptyView = findViewById(R.id.mediaListView);
                            TextView txtView = (TextView) findViewById(R.id.holidayOverviewEmptyTextView);
                            if (hOj.getOwner_id().equals(FirebaseAuth.getInstance().getUid())) {
                                txtView.setText("Bisher sind keine Medien vorhanden. Füge direkt welche hinzu! :)");
                            }else{
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
                this.hObj.getMediaList().add(mObj);
                lvmoAdapater.notifyDataSetChanged();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
