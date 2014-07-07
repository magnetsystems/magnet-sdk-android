/**
 * Copyright (c) 2014 Magnet Systems, Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.magnet.android.mms.async.constraint;

import android.content.Context;

/**
 * This composite constraint uses WWANConstraint and GeoRegionConstraint.
 * This class requires {@link NetworkStateReceiver} and
 * {@link LocationReceiver}
 */
public class WWANAndGeoRegionConstraint extends GeoRegionConstraint {
  private static final long serialVersionUID = 942734022271023843L;
  private WWANConstraint mWWANConstraint;

  public WWANAndGeoRegionConstraint(Context appContext, String id, 
                                    Point[] region, boolean in) {
    super(appContext, id, region, in);
    mWWANConstraint = new WWANConstraint();
  }
  
  @Override
  public boolean isAllowed(Context appContext) {
    if (!mWWANConstraint.isAllowed(appContext))
      return false;
    return super.isAllowed(appContext);
  }
  
  @Override
  public void startInBackground(Context appContext) {
    super.startInBackground(appContext);
    mWWANConstraint.startInBackground(appContext);
  }
  
  @Override
  public void stopInBackground(Context appContext) {
    super.stopInBackground(appContext);
    mWWANConstraint.stopInBackground(appContext);
  }
}
