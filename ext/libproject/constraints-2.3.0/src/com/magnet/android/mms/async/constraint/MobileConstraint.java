/**
 * Copyright (C) 2013-2014, Magnet Systems Inc.  All Rights Reserved.
 */
package com.magnet.android.mms.async.constraint;

import java.io.Serializable;

import android.content.Context;

/**
 * This composite constraint uses WWANConstraint and WifiConstraint to gate 
 * WiMax/3G/4G or WiFi connectivity.  This class requires 
 * {@link NetworkStateReceiver}.

 * Caller must specify a broadcast receiver
 * {@link com.magnet.android.async.NetworkStateReceiver} in AndroidManifest.xml
 * to drain any queued requests when the application is not running.
 * @see com.magnet.android.async.NetworkStateReceiver
 */
public class MobileConstraint extends WWANConstraint implements Serializable {
  private static final long serialVersionUID = 9152714061699760814L;
  private final static String TAG = "MobileConstraint";
  private WifiConstraint mWifiConstraint;
  
  /**
   * Default constructor disallowing roaming.
   */
  public MobileConstraint() {
    super();
    mWifiConstraint = new WifiConstraint();
  }
  
  @Deprecated
  public MobileConstraint(Context context) {
    this();
  }
  
  /**
   * Constructor allowing roaming.
   * @param allowRoaming True to allow roaming; false to disallow roaming.
   */
  public MobileConstraint(boolean allowRoaming) {
    super(allowRoaming);
    mWifiConstraint = new WifiConstraint();
  }
  
  /**
   * Check if the constraint should be lifted when there is WiFi or WiMax/3G/4G
   * mobile connectivity.
   * @return true if connected; otherwise, false.
   */
  @Override
  public boolean isAllowed(Context context) {
    if (super.isAllowed(context))
      return true;
    return mWifiConstraint.isAllowed(context);
  }
}
