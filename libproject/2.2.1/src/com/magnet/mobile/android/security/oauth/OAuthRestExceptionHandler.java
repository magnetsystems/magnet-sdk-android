/*
 * Copyright (c) 2013.  Magnet Systems, Inc.  All rights reserved.
 */

package com.magnet.mobile.android.security.oauth;

import javax.inject.Singleton;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.magnet.core.NanoServices;
import com.magnet.rest.client.api.MagnetRestExceptionHandler;
import com.magnet.security.api.oauth.OAuthLoginException;
// DO NOT REMOVE: import generated Android template app R class
//@IMPORT_R_CLASS@

/**
 * The OAuth implementation of the exception handler.
 * This class is included in the generated Android template app.
 */
@Singleton
public class OAuthRestExceptionHandler implements MagnetRestExceptionHandler {
  private Context mContext = null;
  private static final String NOTIFICATION_TAG = OAuthRestExceptionHandler.class.getName();
  
  public synchronized void handleException(Throwable ex) {
    if (ex instanceof OAuthLoginException) {
      if (mContext == null) {
        mContext = ((Context) NanoServices.getGlobalInstance()
            .lookupFirst(Context.class)).getApplicationContext();
      }
      OAuthLoginException oex = (OAuthLoginException) ex;
      Uri data = Uri.parse(oex.getCodeRequestUri());
      Intent intent = new Intent(mContext, OAuthFlowActivity.class);
      intent.setData(data);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      PendingIntent pendingIntent = 
          PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
      
      NotificationManager notificationMgr = 
          (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
      
      //cancel any existing notifications
      notificationMgr.cancel(NOTIFICATION_TAG, 0);
      
      Notification.Builder noteBuilder = new Notification.Builder(mContext);
      noteBuilder
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_statusbar_action_required)
        .setContentTitle(mContext.getText(R.string.oauth_notification_title))
        .setContentText(mContext.getText(R.string.oauth_notification_text))
        .setContentIntent(pendingIntent);
      notificationMgr.notify(NOTIFICATION_TAG, 0, noteBuilder.getNotification());
    }
  }
}
