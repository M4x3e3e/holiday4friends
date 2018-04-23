package maxundmax.holiday4friends.Business;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Max on 17.04.2018.
 */

public class FirebaseMethods {
    private static final String HOLIDAY_COLLECTION = "activity";

    public static void downloadImageIntoImageView(ImageView v, final MediaObject obj) {
        // Reference to an image file in Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference(obj.getImagepath());

        final ImageView view = v;
        final long five_MEGABYTE = 1024 * 1024 * 5;

        storageReference.getBytes(five_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                obj.setImage(bitmap);
                view.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

    }

    public static void downloadImageIntoImageView(ImageView v, final HolidayObject obj) {
        // Reference to an image file in Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference(obj.getImagepath());

        final ImageView view = v;
        final long five_MEGABYTE = 1024 * 1024 * 5;

        storageReference.getBytes(five_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                view.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

    }

    public static String uploadImageToFirebase(Uri filePath, Context context) {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Hochladen...");
            progressDialog.show();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filePath);

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                float ratio = (float) height/width;

                int newWidth = 1920;
                int newHeight = (int)((float)newWidth*ratio);


                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);


                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);



                byte[] data = byteArrayOutputStream.toByteArray();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference ref = storage.getReference().child("images/" + UUID.randomUUID().toString());
                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Hochgeladen " + (int) progress + "%");
                            }
                        });
                return ref.getPath();
            }catch(IOException ex){}
        }
        return "";
    }

   /* private void downloadFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("<your_bucket>");
        StorageReference  islandRef = storageRef.child("file.txt");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "file_name");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath,"imageName.txt");

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +localFile.toString());
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
    }*/
}
