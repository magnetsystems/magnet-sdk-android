/**
 * Copyright (C) 2013 Magnet Systems Inc.  All Rights Reserved.
 */
package com.magnet.android.mms.async.constraint;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.magnet.android.mms.async.Call;
import com.magnet.android.mms.async.CallManager;
import com.magnet.android.mms.utils.logger.Log;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * This receiver monitors the geo-fencing activities or location updates for
 * for reliable asynchronous calls using Google Play Service.  If a circular 
 * geo-fencing criteria or a polygon geo-fencing (enter or exit) is met, a
 * pending requests will be processed from the queues. Developer must bundle 
 * the Google Play Service library in the apk.  Also, developer should include
 * this extra constraints as a libproject.
 * 
 * In the AndroidManifest.xml, developer must specify a meta-data tag for Google
 * Play Service, the permission, receiver and AsyncIntentService:
 * <p>
 * <pre>
 *   &lt;uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/&gt;
 *   &lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/&gt;
 *   ...
 *   &lt;application&gt;
 *     &lt;meta-data android:name="com.google.android.gms.version" 
 *        android:value="@integer/google_play_services_version" /&gt;
 *     ...
 *     &lt;receiver android:name="com.magnet.android.mms.async.constraint.LocationReceiver"
 *          android:enabled="true"&gt;
 *       &lt;intent-filter&gt;
 *         &lt;action android:name="com.magnet.android.action.LOCATION_CHANGE"/&gt;
 *         &lt;action android:name="com.magnet.android.action.NO_ASYNC_PENDING_REQUESTS"/&gt;
 *       &lt;/intent-filter&gt;
 *     &lt;/receiver&gt;
 * 
 *     &lt;service
 *       android:name="com.magnet.android.mms.async.AsyncIntentService"
 *       android:exported="false"&gt;
 *       &lt;intent-filter&gt;
 *         &lt;action android:name="com.magnet.android.action.RUN_ASYNC" /&gt;
 *       &lt;/intent-filter&gt;
 *     &lt;/service&gt;
 *     ...
 *   &lt;/application&gt;
 * </pre>
 * 
 */
public class LocationReceiver extends BroadcastReceiver {
  /**
   * The action of an intent that geo-fence transition, or location has been
   * changed.
   */
  public final static String ACTION_LOCATION_CHANGE = "com.magnet.android.action.LOCATION_CHANGE";
  public final static String EXTRA_IS_TRANSITION = "transition";

  private final static String TAG = "LocationReceiver";
  private final static String[] TRANSITION_TYPES = { "NONE", "ENTER", "EXIT", "ENTER|EXIT" };
  private final static String NOT_READY = "Not connected to LocationClient yet; try again.";
  private final static int GEOFENCE_UPDATE_REQUEST = 301;
  private final static int LOCATION_UPDATE_REQUEST = 302;
  
  private static LocationClient sLocClient;
  private static PendingIntent sPendingIntent;
  private static OnAddGeofencesResultListener sOnAddResultListener = new 
    OnAddGeofencesResultListener() {
      @Override
      public void onAddGeofencesResult(int statusCode, String[] ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
          sb.append(id).append(',');
        }
        Log.d(TAG, "onAddGeofencesResult() status=" + statusCode + ", ids="+
              sb.toString());
      }
    };
  
  private static OnRemoveGeofencesResultListener sOnRemoveResultListener = new
    OnRemoveGeofencesResultListener() {
      @Override
      public void onRemoveGeofencesByPendingIntentResult(
          int statusCode, PendingIntent pendingIntent) {
        Log.d(TAG, "onRemoveGeofencesByPendingIntentResult() stat="+statusCode+
              ", pendIntent="+pendingIntent);
      }
  
      @Override
      public void onRemoveGeofencesByRequestIdsResult(
          int statusCode, String[] ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
          sb.append(id).append(',');
        }
        Log.d(TAG, "onRemoveGeofencesByRequestIdsResult() stat="+statusCode+
              ", ids="+sb.toString());
      }
    };

  private static ConnectionCallbacks sConnectionCallbacks = new 
    ConnectionCallbacks() {
      @Override
      public void onConnected(Bundle bundle) {
        synchronized(sLocClient) {
          sLocClient.notifyAll();
        }
        Log.d(TAG, "onConnected() bundle="+bundle);
      }

      @Override
      public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");
        sLocClient = null;
      }
    };
    
  private static OnConnectionFailedListener sFailedListener = new 
    OnConnectionFailedListener() {
      @Override
      public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed: result=" + result);
        synchronized(sLocClient) {
          sLocClient.notifyAll();
        }
      }
    };

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Log.isLoggable(Log.DEBUG))
    Log.d(TAG, "onReceive intent="+intent);
    
    String action = intent.getAction();
    if (Call.ACTION_NO_ASYNC_PENDING_REQUESTS.equals(action)) {
      LocationReceiver.done(context.getApplicationContext());
    } else if (ACTION_LOCATION_CHANGE.equals(action)) {
      if (intent.getBooleanExtra(EXTRA_IS_TRANSITION, false)) {
        if (Log.isLoggable(Log.DEBUG)) {
          StringBuilder sb = new StringBuilder();
          int transition = LocationClient.getGeofenceTransition(intent);
          List<Geofence> fences = LocationClient.getTriggeringGeofences(intent);
          for (Geofence fence : fences) {
            sb.append(fence.getRequestId()).append(':')
              .append(TRANSITION_TYPES[transition])
              .append(',');
          }
          Toast.makeText(context.getApplicationContext(),
              "Geofence: "+sb.toString(), Toast.LENGTH_SHORT).show();
        }
      }
      
      // Evaluate the constraint using the geo-fencing intent or GeoRegionConstraint
      CallManager.getInstance(context.getApplicationContext()).run();
    }
  }

  /**
   * Initialize this receiver when there is at least one pending request.  It
   * will connect to the Google Play Service for location update or geo-fence
   * transition change.
   * @param context The application context.
   * @param timeout Connection timeout in milliseconds.
   * @return true if connected; false if not connected.
   */
  public static boolean init(Context context, long timeout ) {
    int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    if (status != ConnectionResult.SUCCESS) {
      String[] googlePlaySvcStat = { "SUCCESS", "SERVICE MISSING", 
          "SERVICE VERSION UPDATE REQUIRED", "SERVICE DISABLED", 
          "SIGN_IN_REQUIRED", "INVALID ACCOUNT", "RESOLUTION REQUIRED",
          "NETWORK ERROR", "INTERNAL ERROR", "SERVICE_INVALID",
          "DEVELOPER_ERROR", "LICENSE_CHECK_FAILED", "DATE INVALID" };
      Log.e(TAG, "Google Play Service status: "+googlePlaySvcStat[status]);
      return false;
    }
    
    if (sLocClient == null) {
      synchronized (sConnectionCallbacks) {
        if (sLocClient == null) {
          sLocClient = new LocationClient(context, sConnectionCallbacks,
                                          sFailedListener);
          sLocClient.connect();
        }
      }
    }
    synchronized (sLocClient) {
      if (sLocClient.isConnected()) {
        return true;
      }
      try {
        if (Log.isLoggable(Log.DEBUG)) {
          Log.d(TAG, "Connecting to Location Client... wait("+timeout+")");
        }
        if (timeout > 0L) {
          sLocClient.wait(timeout);
        } else {
          sLocClient.wait();
        }
      } catch (InterruptedException e) {
        // Ignored.
      }
    }
    return sLocClient.isConnected();
  }
  
  /**
   * Done with this receiver.  It will disconnect from Google Play Service when
   * there are no more pending requests.  If there is a request of location
   * updates, it will be removed.
   * @param context
   */
  public static void done(Context context) {
    if (Log.isLoggable(Log.DEBUG))
      Log.d(TAG, "Removing location updates listener: no-op="+(sPendingIntent==null));
    if (sPendingIntent != null) {
      if (sLocClient != null) {
        sLocClient.removeLocationUpdates(sPendingIntent);
      }
      sPendingIntent = null;
    }
    
    if (Log.isLoggable(Log.DEBUG))
      Log.d(TAG, "Disconnec from Location Client="+sLocClient);
    if (sLocClient != null) {
      sLocClient.disconnect();
      sLocClient = null;
    }
  }
  
  /**
   * Get the last known location using Google Play Location Service.
   * @param context The application context.
   * @return null if Location Service is not available; otherwise, a location.
   */
  public static Location getLastLocation(Context context) {
    try {
      boolean inited = true;
      if (sLocClient == null || !sLocClient.isConnected()) {
        inited = LocationReceiver.init(context, 10000L);
      }
      if (inited) {
        return sLocClient.getLastLocation();
      } else {
        return null;
      }
    } catch (Throwable e) {
      Log.e(TAG, "Unable to get last location", e);
      return null;
    }
  }
  
  /**
   * Get the last known location using Google Play Location Service or Android
   * Location Service via passive provider.
   * @param context The application context.
   * @return The last known location, or null.
   */
  public static Location getLastKnownLocation(Context context) {
    try {
      boolean inited = true;
      if (sLocClient == null || !sLocClient.isConnected()) {
        inited = LocationReceiver.init(context, 10000L);
      }
      if (inited) {
        return sLocClient.getLastLocation();
      } else {
        Log.w(TAG, "Use passive provider from Android Location Service for last location");
        LocationManager locMgr = (LocationManager) context.getSystemService(
            Context.LOCATION_SERVICE);
        return locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
      }
    } catch (Throwable e) {
      Log.e(TAG, "Unable to get last known location", e);
      return null;
    }
  }

  /**
   * Add one or more points as geo-fences.  Each geo-fence must have an ID.
   * @param context The application context.
   * @param list A list of geo-fences.
   */
  public static void addGeofences(Context context, List<Geofence> list) {
    boolean inited = true;
    if (sLocClient == null || !sLocClient.isConnected()) {
      inited = LocationReceiver.init(context, 10000L);
    }
    if (!inited) {
      Log.e(TAG, "addGeofences failed: "+NOT_READY);
    } else {
      Log.d(TAG, "addGeofences()");
      Intent intent = (new Intent(LocationReceiver.ACTION_LOCATION_CHANGE))
          .setPackage(context.getPackageName())
          .addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
          .putExtra(EXTRA_IS_TRANSITION, true);
      
      PendingIntent pendingIntent = PendingIntent.getBroadcast(
        context.getApplicationContext(), GEOFENCE_UPDATE_REQUEST, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);
      
      sLocClient.addGeofences(list, pendingIntent, sOnAddResultListener);
    }
  }
  
  /**
   * Remove the geofences by their ID's.
   * @param context The application context.
   * @param ids A list of geo-fence ID.
   */
  public static void removeGeofences(Context context, List<String> ids) {
    boolean inited = true;
    if (sLocClient == null || !sLocClient.isConnected()) {
      inited = LocationReceiver.init(context, 10000L);
    }
    if (!inited) {
      Log.e(TAG, "removeGeofences failed: "+NOT_READY);
    } else {
      sLocClient.removeGeofences(ids, sOnRemoveResultListener);
    }
  }
  
  /**
   * Specify the quality of service for location updates from Google Play 
   * Service.  Any existing QoS will be overwritten.
   * @param context The application context.
   * @param qosRqt A non-null QoS request.
   * @return true for QoS set successfully; false for failure.
   */
  public static boolean setQos(Context context, LocationRequest qosRqt ) {
    boolean inited = true;
    if (sLocClient == null || !sLocClient.isConnected()) {
      inited = LocationReceiver.init(context, 10000L);
    }
    if (!inited) {
      Log.e(TAG, "Google Play Service: "+NOT_READY);
      return false;
    }
    Log.d(TAG, "Enabling requestLocationUpdates()");
    Intent intent = (new Intent(LocationReceiver.ACTION_LOCATION_CHANGE))
        .setPackage(context.getPackageName())
        .addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
        .putExtra(EXTRA_IS_TRANSITION, false);
    sPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
        LOCATION_UPDATE_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    sLocClient.requestLocationUpdates(qosRqt, sPendingIntent);
    return true;
  }
  
  /**
   * Check if the QoS for location updates is set.
   * @param context The application context.
   * @return true if location updates QoS is set; otherwise, false.
   */
  public static boolean isQosSet(Context context) {
    return sPendingIntent != null;
  }
}
