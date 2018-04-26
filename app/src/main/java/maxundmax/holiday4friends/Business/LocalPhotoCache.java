package maxundmax.holiday4friends.Business;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by Max on 26.04.2018.
 */

public class LocalPhotoCache {

    public static HashMap<String, Bitmap> photoCache = new HashMap<>();

    public static Boolean ImageExists(String imagePath){
       Bitmap i =  photoCache.get(imagePath);
        if(i == null)
            return false;
        else
            return true;
    }

    public static void AddImage(String imagepath,Bitmap image){
        photoCache.put(imagepath,image);
    }

    public static void RemoveImage( String imagepath,Bitmap image){
        photoCache.remove(imagepath);
    }

    public static Bitmap GetImage(String imagePath){
        return photoCache.get(imagePath);
    }

}
