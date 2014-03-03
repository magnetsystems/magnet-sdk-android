/*
 * Copyright (c) 2013.  Magnet Systems, Inc.  All rights reserved.
 */
package com.magnet.mobile.android.security.oauth;

import android.content.Context;

import com.magnet.inject.Contract;

/**
 * Handler for the OAuthFlowActivity callback.
 * This class is included in the Android template app
 */
@Contract
public interface OAuthFlowCompleteHandler {
  /**
   * Called when the OAuth flow activity completes.
   * The OAuth exchange may or may not have been completed
   * successfully
   * 
   * @param context
   * @param isCompleted true if the flow completed successfully or false
   * if the activity was closed before completed.
   */
  public void onOAuthFlowComplete(Context context, boolean isCompleted);
}
