package maxundmax.holiday4friends.Business;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by Max on 26.04.2018.
 */

public class LocalPhotoCache {

    public static HashMap<String, Bitmap> photoCache = new HashMap<>();

    /**
     * Gibt zurück ob Bild mit dem Bildpfad im Cache exisitert
     * @param imagePath Bildpfad
     * @return True wenn Bild existiert , False wenn nicht
     */
    public static Boolean ImageExists(String imagePath){
       Bitmap i =  photoCache.get(imagePath);
        if(i == null)
            return false;
        else
            return true;
    }

    /**
     * Fügt das übergebene Bild dem LocalPhoto Cache hinzu
     * @param imagepath der Bildpfad als
     * @param image Das Bild
     */
    public static void AddImage(String imagepath,Bitmap image){

        photoCache.put(imagepath,image);
    }

    /**
     * Löscht das Bild aus dem Local Cache
     * @param imagepath Bildpfad
     */
    public static void RemoveImage( String imagepath){
        photoCache.remove(imagepath);
    }

    /**
     * Gibt das Bild mit dem Pfad zurück
     * @param imagePath Bildpfad
     * @return Bitmap
     */
    public static Bitmap GetImage(String imagePath){
        return photoCache.get(imagePath);
    }

}
