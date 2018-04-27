package maxundmax.holiday4friends;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import maxundmax.holiday4friends.Business.FirebaseMethods;
import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.LocalPhotoCache;

public class modify_holiday extends AppCompatActivity {

    private HolidayObject hObj;
    private static final String HOLIDAY_COLLECTION = "holiday";
    private static final String TAG = "ModifyActivity";
    private ProgressDialog progressDialog;
    private final int PICK_IMAGE_REQUEST = 7;
    private Uri filePath;

    private FirebaseFirestore mFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private EditText nameTbx;
    private EditText descriptionTbx;
    private ImageView imageView;
    private ImageButton btnImageSelector;
    private Button btnSave;

    public modify_holiday() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_holiday);

        progressDialog = new ProgressDialog(modify_holiday.this);

        // FireBase Objects
        mFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        nameTbx = findViewById(R.id.modifyHolidayName);
        descriptionTbx = findViewById(R.id.modifyHolidayDescription);
        imageView = findViewById(R.id.modifyHolidayImageViewer);
        btnImageSelector = findViewById(R.id.modifyHolidayBtnImageSelektor);
        btnSave = findViewById(R.id.modifyHolidayBtnSave);

        String id = getIntent().getStringExtra("id");
        LoadHolidayFromDB(id);
    }

    private void Initialize(){
        nameTbx.setText(hObj.getName());
        descriptionTbx.setText(hObj.getDescription());
        FirebaseMethods.downloadImageIntoImageView(imageView,hObj);

        btnImageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onImageSelectorClicked();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWriteClicked();
            }
        });
    }

    private void onWriteClicked() {
        //Check

        hObj.setName(nameTbx.getText().toString());
        hObj.setDescription(descriptionTbx.getText().toString());

        if (TextUtils.isEmpty(hObj.getName())) {
            Toast.makeText(this, "Bitte geben Sie einen Namen an.", Toast.LENGTH_LONG).show();
            return;
        }
        if (hObj.getName().length() > 50) {
            Toast.makeText(this, "Der Name darf maximal 50 Zeichen lang sein. ", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(hObj.getDescription())) {
            Toast.makeText(this, "Bitte geben Sie eine kurze Beschreibung an.", Toast.LENGTH_LONG).show();
            return;
        }


        progressDialog.setTitle("Hochladen...");
        progressDialog.show();
        if(filePath != null) {
            FirebaseMethods.deleteImageFromFirebase(hObj.getImagepath());
            hObj.setImagepath(uploadImageToFirebase(filePath, this));
        }
        else {
            UploadHoliday();
        }

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


    private void onImageSelectorClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String uploadImageToFirebase(Uri filePath, Context context) {
        if (filePath != null) {


            try {
                String path = "images/" + UUID.randomUUID().toString();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filePath);
                LocalPhotoCache.AddImage(path, bitmap);

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                float ratio = (float) height / width;

                int newWidth = 1920;
                int newHeight = (int) ((float) newWidth * ratio);


                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

                byte[] data = byteArrayOutputStream.toByteArray();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference ref = storage.getReference().child(path);
                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                UploadHoliday();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //progressDialog.dismiss();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                //progressDialog.setMessage("Hochgeladen " + (int) progress + "%");
                            }
                        });

                return ref.getPath();
            } catch (IOException ex) {

            }
        }
        return "";

    }

    private void UploadHoliday() {
        Task<Void> voidTask = mFirestore.collection(HOLIDAY_COLLECTION)
                .document(hObj.getId())
                .set(hObj.getDataMap())
                .addOnCompleteListener(modify_holiday.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "write:onComplete");
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "write:onComplete:failed", task.getException());
                        }
                        else
                        {
                            progressDialog.cancel();
                            finishThis(RESULT_OK);
                        }
                    }
                });
    }

    private void finishThis(int resCode){
        setResult(resCode);

        finish();
    }

}
