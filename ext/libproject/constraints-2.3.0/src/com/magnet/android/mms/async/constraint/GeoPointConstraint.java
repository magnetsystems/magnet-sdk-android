/**
 * Copyright (C) 2013-2014, Magnet Systems Inc.  All Rights Reserved.
 */
package com.magnet.android.mms.async.constraint;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.location.Geofence;
import com.magnet.android.mms.utils.logger.Log;

import android.content.Context;
import android.location.Location;

/**
 * This constraint gates for a circular geo-fence.  This class requires
 * Google Play Service and {@link LocationReceiver}
 * <br>
 * Caller must specify a broadcast receiver
 * {@link com.magnet.android.mms.async.constraint.LocationReceiver} in 
 * AndroidManifest.xml to drain any queued requests when the application is not
 * running.
 * @see com.magnet.android.mms.async.constraint.LocationReceiver
 */
public class GeoPointConstraint implements Constraint, Serializable {
  private static final long serialVersionUID = 1315207650834154735L;
  private final static String TAG = "GeoPointConstraint";

  private String mId;
  private double mLat;
  private double mLng;
  private float mRadius;
  private long mDuration;
  private boolean mIn;

  /**
   * Default Constructor.  The duration becomes effective only after the request
   * was queued.
   * @param id A unique name (e.g. call ID or timestamp) for each call.
   * @param location Latitude/longitude
   * @param radius The radius in meters
   * @param duration {@link Geofence#NEVER_EXPIRE} or duration in milliseconds.
   * @param in True for inside the fence; false for outside the fence.
   */
  public GeoPointConstraint(Context appContext, String id, double lat, double lng,
                        float radius, long duration, boolean in) {
    mId = id;
    mLat = lat;
    mLng = lng;
    mRadius = radius;
    mDuration = duration;
    mIn = in;
    
    // Connect to Google Play Location Service.
    LocationReceiver.init(appContext, 5000L);
  }
    
  @Override
  public boolean isAllowed(Context appContext) {
    Location loc = LocationReceiver.getLastLocation(appContext);
    if (Log.isLoggable(Log.DEBUG)) {
      Log.d(TAG, "isAllowed(): getLastLocation() loc="+loc);
    }
    if (loc == null)
      return false;
    Location fence = new Location("");
    fence.setLatitude(mLat);
    fence.setLongitude(mLng);
    if (Log.isLoggable(Log.DEBUG)) {
      Log.d(TAG, "isAllowed(): distance from loc="+loc+" to fence="+fence+" is "+
        GeoUtil.distFrom(loc.getLatitude(), loc.getLongitude(), 
            fence.getLatitude(), fence.getLongitude()));
    }
    return (mIn == GeoUtil.inCircle(loc, fence, mRadius));
  }
    
  @Override
  public void stopInBackground(Context appContext) {
    if (Log.isLoggable(Log.DEBUG)) {
      Log.d(TAG, "stopInBackground() id="+mId);
    }
    LocationReceiver.removeGeofences(appContext, Arrays.asList(new String[] { mId })); 
  }
  
  @Override
  public void startInBackground(Context appContext) {
    if (Log.isLoggable(Log.DEBUG)) {
      Log.d(TAG, "startInBackground()");
    }
    
    // Use Google Play Location Service to monitor the geo-fence transition.
    Geofence[] fences = { new Geofence.Builder()
      .setRequestId(mId)
      .setCircularRegion(mLat, mLng, mRadius)
      .setExpirationDuration(mDuration)
      .setTransitionTypes(mIn ? Geofence.GEOFENCE_TRANSITION_ENTER : 
                                Geofence.GEOFENCE_TRANSITION_EXIT)
      .build()
    };
    List<Geofence> list = Arrays.asList(fences);
    LocationReceiver.addGeofences(appContext, list);
  }
}
