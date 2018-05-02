package maxundmax.holiday4friends.Business;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSAdapter {
    MyLocationListener listener = new MyLocationListener();
    private static double latitude;
    /**
     * @return the latitude
     */
    public static double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public static void setLatitude(double l) {
        latitude = l;
    }

    private static double longitude;
    /**
     * @return the longitude
     */
    public static double getLongitude() {
        return longitude;
    }

    /**
     * @param d the longitude to set
     */
    public static void setLongitude(double d) {
        longitude = d;
    }
}


class MyLocationListener implements LocationListener {
    public void onLocationChanged(Location location) {

        GPSAdapter.setLongitude(location.getLongitude());
        GPSAdapter.setLatitude(location.getLatitude());
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}