/**
 * Copyright (C) 2013, Magnet Systems Inc.  All Rights Reserved.
 */
package com.magnet.android.mms.async.constraint;

import java.io.Serializable;

import com.google.android.gms.location.LocationRequest;
import com.magnet.android.mms.utils.logger.Log;

import android.content.Context;
import android.location.Location;

/**
 * This constraint gates for polygon geo-fence.  This class requires Google
 * Play Service and {@link LocationReceiver}
 * <br>
 * Caller must specify a broadcast receiver
 * {@link com.magnet.android.mms.async.constraint.LocationReceiver} in 
 * AndroidManifest.xml to drain any queued requests when the application is not
 * running.
 * @see com.magnet.android.mms.async.constraint.LocationReceiver
 */
public class GeoRegionConstraint implements Constraint, Serializable {
  private static final long serialVersionUID = 9134773120011606309L;
  private final static String TAG = "GeoRegionConstraint";
  private final static long UPDATE_INTERVAL = 60 * 1000L; // 1 min

  private String mId;
  private Point[] mRegion;
  private boolean mIn;
  
  public static class Point implements Serializable {
    private static final long serialVersionUID = -7941549396654571438L;
    double lat;
    double lng;

    public Point(double lat, double lng) {
      this.lat = lat;
      this.lng = lng;
    }
  }
  
  private boolean IsPointInPolygon(Point[] poly, Point point) {
    boolean c = false;
    for (int i = 0, j = poly.length - 1; i < poly.length; j = i++) {
      Point ipoly = poly[i];
      Point jpoly = poly[j];
      if ((((ipoly.lat <= point.lat) && (point.lat < jpoly.lat)) ||
          ((jpoly.lat <= point.lat) && (point.lat < ipoly.lat))) &&
          (point.lng < ((jpoly.lng - ipoly.lng) * (point.lat - ipoly.lat) / 
              (jpoly.lat - ipoly.lat) + ipoly.lng))) {
          c = !c;
      }
    }
    return c;
  }

  /**
   * Default Constructor.
   * @param id A unique name for this region.
   * @param region A region represented by a polygon
   * @param in True for inside the region; false for outside the region.
   */
  public GeoRegionConstraint(Context appContext, String id, Point[] region,
                              boolean in) {
    mId = id;
    mRegion = region;
    mIn = in;
    
    // Connect to Google Play Location Service.
    LocationReceiver.init(appContext, 5000L);
  }

  @Override
  public boolean isAllowed(Context appContext) {
    Location loc = LocationReceiver.getLastLocation(appContext);
    if (Log.isLoggable(Log.DEBUG))
      Log.d(TAG, "getLastLocation() loc="+loc);
    if (loc == null) {
      return false;
    }
    Point point = new Point(loc.getLatitude(), loc.getLongitude());
    boolean allowed = (mIn == IsPointInPolygon(mRegion, point));
    
    if (Log.isLoggable(Log.DEBUG))
      Log.d(TAG, "isAllowed() returns "+allowed);
    return allowed;
  }

  @Override
  public void stopInBackground(Context appContext) {
  }
  
  @Override
  public void startInBackground(Context appContext) {
    if (Log.isLoggable(Log.DEBUG)) {
      Log.d(TAG, "startInBackground() id="+mId);
    }
    
    // Set up a location update request to Google Play Service.
    LocationRequest request = LocationRequest.create();
    request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    request.setInterval(UPDATE_INTERVAL);
    LocationReceiver.requestLocationUpdates(appContext, request);
  }
}
