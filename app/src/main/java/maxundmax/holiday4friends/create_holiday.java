package maxundmax.holiday4friends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.zxing.common.StringUtils;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.spec.ECField;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class create_holiday extends AppCompatActivity
        implements View.OnClickListener {

    private static final String ACTIVITY_COLLECTION = "activity";
    private static final String TAG = "CreateActivity";
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 7;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

        private EditText holiday_name;
        private EditText activity_description;
        private Button buttonSave;
        private Button chooseImage;
        private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_holiday);
        mFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

       holiday_name = (EditText) findViewById(R.id.activity_name);
       activity_description = (EditText) findViewById(R.id.activity_description);
       buttonSave = findViewById(R.id.button_write);
       chooseImage = findViewById(R.id.btn_image_selector);
       imageView = findViewById(R.id.image_viewer);

        buttonSave.setOnClickListener(this);
        chooseImage.setOnClickListener(this);
    }

    private void onWriteClicked() {
        //Check

        String holidayName = holiday_name.getText().toString();
        String holidayDescription = activity_description.getText().toString();

        if (TextUtils.isEmpty(holidayName)) {
            Toast.makeText(this, "Bitte geben Sie einen Namen an.", Toast.LENGTH_LONG).show();
            return;
        }
        if (holidayName.length() > 50) {
            Toast.makeText(this, "Der Name darf maximal 50 Zeichen lang sein. ", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(holidayDescription)) {
            Toast.makeText(this, "Bitte geben Sie eine kurze Beschreibung an.", Toast.LENGTH_LONG).show();
            return;
        }

        if(filePath == null){
            Toast.makeText(this, "Bitte geben Sie ein Titelbild an.", Toast.LENGTH_LONG).show();
            return;

        }
        String path = uploadImageToFirebase();

        Boolean isPublic = true; // TODO: Am Anfang alle Stories Offen, später private möglich

        Map<String, Object> map = new HashMap<>();
        map.put("owner_id", FirebaseAuth.getInstance().getUid()); //Set the user as Story Owner
        map.put("name", holidayName);
        map.put("description", holidayDescription);
        map.put("public", isPublic);
        map.put("startdate", Calendar.getInstance().getTime());
        map.put("enddate", null);
        map.put("timestamp", Calendar.getInstance().getTime());
        map.put("imagepath" ,path);

        Task<Void> voidTask = mFirestore.collection(ACTIVITY_COLLECTION)
                .document()
                .set(map)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "write:onComplete");
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "write:onComplete:failed", task.getException());
                        }
                    }
                });

    }

    private String uploadImageToFirebase() {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Hochladen...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(create_holiday.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(create_holiday.this, "Fehlgeschlagen "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Hochgeladen "+(int)progress+"%");
                        }
                    });
            return ref.getPath();
        }
        return "";
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_write) {
            onWriteClicked();
        }

        if (v.getId() == R.id.btn_image_selector) {
            onImageSelectorClicked();
        }
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
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
