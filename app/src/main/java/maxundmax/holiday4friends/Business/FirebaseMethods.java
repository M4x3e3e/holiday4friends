package maxundmax.holiday4friends.Business;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Max on 17.04.2018.
 */

public class FirebaseMethods {
    private static final String HOLIDAY_COLLECTION = "activity";

    /**
     * Downloaded ein Bild vom Firebase Storage und setzt es beim übergebenen ImageView als Image
     * @param v ImageView
     * @param obj Media Objekt vom Image
     */
    public static void downloadImageIntoImageView(final ImageView v, final MediaObject obj) {
        if (!LocalPhotoCache.ImageExists(obj.getImagepath()))// Reference to an image file in Firebase Storage
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference(obj.getImagepath());
            final long five_MEGABYTE = 1024 * 1024 * 5;

            storageReference.getBytes(five_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    obj.setImage(bitmap);
                    v.setImageBitmap(bitmap);
                    LocalPhotoCache.AddImage(obj.getImagepath(), bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } else {
            Bitmap localMap = LocalPhotoCache.GetImage(obj.getImagepath());
            obj.setImage(localMap);
            v.setImageBitmap(localMap);
        }
    }

    /**
     * Downloaded ein Bild vom Firebase Storage und setzt es beim übergebenen ImageView als Image
     * @param v ImageView
     * @param obj Holiday Objekt vom Image
     */
    public static void downloadImageIntoImageView(final ImageView v, final HolidayObject obj) {
        if (!LocalPhotoCache.ImageExists(obj.getImagepath())) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference(obj.getImagepath());

            final long five_MEGABYTE = 1024 * 1024 * 5;

            storageReference.getBytes(five_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    v.setImageBitmap(bitmap);
                    LocalPhotoCache.AddImage(obj.getImagepath(), bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } else {
            Bitmap localMap = LocalPhotoCache.GetImage(obj.getImagepath());
            v.setImageBitmap(localMap);
        }
    }

    /**
     * Löscht das Bild aus dem Firebase Storage
     * @param imagePath Firebase Imagepath
     */
    public static void deleteImageFromFirebase(String imagePath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child(imagePath);
        LocalPhotoCache.RemoveImage(imagePath);
        ref.delete();
    }

    /**
     * Lädt das übergebene Bild in den Firebase Storage Hoch
     * @param filePath Lokaler Bildpfad
     * @param context Context der aufrufenden Activity
     * @return
     */
    public static String uploadImageToFirebase(Uri filePath, Context context) {
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
                ref.putBytes(data);

                return ref.getPath();
            } catch (IOException ex) {

            }
        }
        return "";

    }

}
