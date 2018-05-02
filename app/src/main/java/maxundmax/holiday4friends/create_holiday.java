package maxundmax.holiday4friends;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import maxundmax.holiday4friends.Business.HolidayObject;
import maxundmax.holiday4friends.Business.LocalPhotoCache;


public class create_holiday extends AppCompatActivity
        implements View.OnClickListener {

    // Firebase Collection Name
    private static final String HOLIDAY_COLLECTION = "holiday";
    private static final String TAG = "CreateActivity";

    // Locale Private Variablen
    private ProgressDialog progressDialog;
    private final int PICK_IMAGE_REQUEST = 7;
    private Uri filePath;


    //Firebase Objekte
    private FirebaseFirestore mFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    // UI Elemente
    private Button buttonSave;
    private ImageButton chooseImage;
    private ImageView imageView;
    EditText holiday_name;
    EditText activity_description;


    private HolidayObject actualActivity;

    /**
     * On Create Methode
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_holiday);
        progressDialog = new ProgressDialog(create_holiday.this);

        // FireBase Objects
        mFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Activity Object
        this.actualActivity = new HolidayObject();

        // UI Objects
        holiday_name = (EditText) findViewById(R.id.activity_name);
        activity_description = (EditText) findViewById(R.id.activity_description);

        buttonSave = findViewById(R.id.button_write);
        chooseImage = findViewById(R.id.btn_image_selector);
        imageView = findViewById(R.id.image_viewer);

        // Set Click Events to UI Objects
        buttonSave.setOnClickListener(this);
        chooseImage.setOnClickListener(this);
    }

    /**
     * Speichert den neuen Urlaub
     */
    private void SaveNewHoliday() {
        //Check

        actualActivity.setName(holiday_name.getText().toString());
        actualActivity.setDescription(activity_description.getText().toString());

        if (TextUtils.isEmpty(actualActivity.getName())) {
            Toast.makeText(this, "Bitte geben Sie einen Namen an.", Toast.LENGTH_LONG).show();
            return;
        }
        if (actualActivity.getName().length() > 50) {
            Toast.makeText(this, "Der Name darf maximal 50 Zeichen lang sein. ", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(actualActivity.getDescription())) {
            Toast.makeText(this, "Bitte geben Sie eine kurze Beschreibung an.", Toast.LENGTH_LONG).show();
            return;
        }

        if (filePath == null) {
            Toast.makeText(this, "Bitte geben Sie ein Titelbild an.", Toast.LENGTH_LONG).show();
            return;

        }

        progressDialog.setTitle("Hochladen...");
        progressDialog.show();

        // Lädt das Bild in den Firebase Storage Hoch
        actualActivity.setImagepath(uploadImageToFirebase(filePath,this));
        actualActivity.setPublic(true);// TODO: Am Anfang alle Stories Offen, später private möglich
        actualActivity.setStartdate(Calendar.getInstance().getTime());
        actualActivity.setOwner_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String id = mFirestore.collection(HOLIDAY_COLLECTION).document().getId();
        actualActivity.setId(id);
    }

    /**
     * Beendet die Create Holiday Activity
     * @param resCode Der Result Code
     */
    private void finishThis(int resCode){
        setResult(resCode);

        finish();
    }


    /**
     * OnClick Event für die Activity
     * @param v Sendende View
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_write) {
            SaveNewHoliday();
        }

        if (v.getId() == R.id.btn_image_selector) {
            onImageSelectorClicked();
        }
    }

    /**
     * Startet einen Intent zum auswählen eines Bildes
     */
    private void onImageSelectorClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE_REQUEST);
    }


    /**
     * Activity Result Methode
     * @param requestCode Der Empfangene RequestCode
     * @param resultCode Der Empfangene ResultCode
     * @param data Daten die Von der Activity
     */
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

    /**
     * Lädt ein Bild vom lokalen Gerät in den Firebase Storage hoch. Das Bild wird dabei komprimiert und verkleinert.
     * @param filePath Der lokale Bildpfad
     * @param context Context der aufrufenden Activity
     * @return Firebase Storage Filepath
     */
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
            } catch (Exception ex) {
                Toast.makeText(context, "Beim Hochladen ist leider ein Fehler aufgetreten. " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return "";

    }

    /**
     * Lädt den Holiday Datensatz in die Firebase Cloud Datenbank hoch
     */
    private void UploadHoliday() {
        Task<Void> voidTask = mFirestore.collection(HOLIDAY_COLLECTION)
                .document(actualActivity.getId())
                .set(actualActivity.getDataMap())
                .addOnCompleteListener(create_holiday.this, new OnCompleteListener<Void>() {
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

}
