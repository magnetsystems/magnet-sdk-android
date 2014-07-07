/**
 * Copyright (C) 2013 Magnet Systems Inc.  All Rights Reserved.
 */
package com.magnet.android.mms.async.constraint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.magnet.android.mms.utils.logger.Log;

//import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * A utility class for geo-location.
 */
public class GeoUtil {
    private final static String TAG = "GeoUtil";
    private final static double EARTH_RADIUS_IN_METERS = 6371 * 1000;
    
    /**
     * Get the first found longitude and latitude of a postal address.
     * @param context
     * @param postal A postal address.
     * @return A geo-location, or null if IOException occurred.
     */
    public static Address getGeoFromPostal( Context context, String postal ) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
    
        try {
            address = coder.getFromLocationName(postal, 1);
            if (address == null || address.isEmpty()) {
                return null;
            }
            Address location = address.get(0);
            return location;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Convert from geo-location to GeoPoint for Google Map or GMS apps.
     * @param location A geo-location.
     * @return A GeoPoint object.
     */
    /*
    public static GeoPoint addressToGeoPoint( Address location ) {
      if (location == null)
        return null;
      return new GeoPoint((int) (location.getLatitude() * 1E6),
                           (int) (location.getLongitude() * 1E6));
    }
    */
    
    /**
     * Convert from geo-location to "latitude, longitude" string format.
     * @param location A geo-location.
     * @return A string in "latitude, longitude" format.
     */
    public static String addressToString( Address location ) {
      if (location == null)
        return null;
      return String.valueOf(location.getLatitude())+", "+
              String.valueOf(location.getLongitude());
    }
    
    /**
     * Convert a "latitude, longitude" string to a geo-location.
     * @param geoLocation
     * @return An Address with latitude and longitude.
     */
    public static Address parseGeoLocation( String geoLocation ) {
      if (geoLocation == null)
        return null;
      String[] tokens = geoLocation.split(",");
      Address location = new Address(null);
      location.setLatitude(Double.parseDouble(tokens[0].trim()));
      location.setLongitude(Double.parseDouble(tokens[1].trim()));
      return location;
    }
    
    /*
     * Distance in meters between two points on Earth using the "haversine"
     * formula.
     * @param lat1 latitude of point1
     * @param lng1 longitude of point1
     * @param lat2 latitude of point2
     * @param lng2 longitude of point2
     * @return The distance in meters.
     */
    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
      double dLat = Math.toRadians(lat2-lat1);
      double dLng = Math.toRadians(lng2-lng1);
      double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                  Math.sin(dLng/2) * Math.sin(dLng/2) *
                  Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      double dist = EARTH_RADIUS_IN_METERS * c;

      return (float) dist;
  }
  
  /**
   * Check if the location is within the circle of a geo-fence.
   * @param loc A location to be tested.
   * @param fence A center of a geo-fence.
   * @param radius The radius in meters of the geo-fence.
   * @return true if within the geo-fence circle; otherwise, false.
   */
  public static boolean inCircle(Address loc, Address fence, float radius ) {
    float dist = distFrom(loc.getLatitude(), loc.getLongitude(), 
                          fence.getLatitude(), fence.getLongitude());
    return (dist <= radius);
  }
  
  /**
   * Check if the location is within the circule of a geo-fence.
   * @param loc A location to be tested.
   * @param fence A center of a gen-fence.
   * @param radius The radius in meters of the geo-fence.
   * @return true if within the geo-fence circle; otherwise, false.
   */
  public static boolean inCircle(Location loc, Location fence, float radius) {
    float dist = distFrom(loc.getLatitude(), loc.getLongitude(), 
        fence.getLatitude(), fence.getLongitude());
    return (dist <= radius);
  }
  
  /**
   * Get the last known location based on the passive provider.
   * @param context
   * @return null if not available, or the last known location.
   */
  public static Address getCurrentLocation(Context context) {
    Address curAddr = null;
    LocationManager locMgr = (LocationManager) context.getSystemService(
        Context.LOCATION_SERVICE);
    String[] providers = { // LocationManager.GPS_PROVIDER, 
        // LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER };
    for (String provider : providers) {
      try {
        Location curLoc = locMgr.getLastKnownLocation(provider);
        Log.d(TAG, "provider="+provider+" => loc="+curLoc);
        if (curLoc != null) {
          curAddr = new Address(Locale.getDefault());
          curAddr.setLatitude(curLoc.getLatitude());
          curAddr.setLongitude(curLoc.getLongitude());
          return curAddr;
        }
      } catch (Exception e) {
        Log.e(TAG, "getLastKnownLocation() error", e);
      }
    }
    return null;
  }
  
  private static LocationListener sLocListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location arg0) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onProviderDisabled(String provider) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      // TODO Auto-generated method stub
    }
  };
  
  /**
   * 
   * @param context
   * @param minTime Update in milliseconds
   * @param minDist Update in meters
   * @param accuracy {@link Criteria#ACCURACY_FINE} or {@value Criteria#ACCURACY_COARSE}
   * @param power {@link Criteria#POWER_HIGH}, {@link Criteria#POWER_LOW}, or {@link Criteria#POWER_MEDIUM}
   */
  public static void updateCurrentLocation(Context context, long minTime, 
      float minDist, int accuracy, int power) {
    LocationManager locMgr = (LocationManager) context.getSystemService(
        Context.LOCATION_SERVICE);
    Criteria criteria = new Criteria();
    criteria.setAccuracy(accuracy);
    criteria.setPowerRequirement(power);
    locMgr.requestLocationUpdates(minTime, minTime, criteria, sLocListener, null);
  }  
}
