/*
 * Copyright (c) 2013.  Magnet Systems, Inc.  All rights reserved.
 */
package com.magnet.mobile.android.security.oauth;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.magnet.common.api.Reference;
import com.magnet.connection.restlike.MagnetRestLikeConnectionService;
import com.magnet.mobile.android.Log;
import com.magnet.mobile.android.MagnetActivity;
// DO NOT REMOVE: import generated Android template app R class
//@IMPORT_R_CLASS@

/**
 * The activity that handles the an OAuth WebView flow.
 * This class is will be included in generated Android template app
 */
public class OAuthFlowActivity extends MagnetActivity {
  private static final String TAG = OAuthFlowActivity.class.getSimpleName();
  private WebView mOAuthWebView = null;
  private static final String OAUTH_REDIRECT_URI_PARAMETER = "redirect_uri";
  private static final String OAUTH_DONE_URI_PARAMETER = "X-OAuth-Done-Uri";
  private static final String CLOSE_REDIRECT_URL = "http://close";
  private URL mRedirectUrl = null;
  private HashMap<String, String> mHeaders = new HashMap<String, String>();

  @Inject
  private Reference<MagnetRestLikeConnectionService> mConnectionService;

  @Inject
  private List<Reference<OAuthFlowCompleteHandler>> mOAuthCompletionHandlers;

  private boolean mIsCompleted = false;

  private WebViewClient mWebViewClient = new WebViewClient() {

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      Log.w(TAG, "Ignoring this SSL Error:" + error.toString());
      handler.proceed();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      try {
        URL urlObject = new URL(url);
        if (url.startsWith(CLOSE_REDIRECT_URL)) {
          Log.d(TAG, "shouldOverrideUrlLoading(): found the close url, closing the flow");
          OAuthFlowActivity.this.finish();
          mIsCompleted = true;
          return true;
        } else if (isLocalRedirectUrl(urlObject)) {
          //this is the redirect from the OAuth provider that we specified initially
          //we need to append the session id
          String sessionId = mConnectionService.get().getSessionIdForUrl(urlObject);
          Log.d(TAG, "shouldOverrideUrlLoading(): session id found=" + (sessionId != null));
          url = buildLocalRedirectUrlWithClose(url, sessionId);

          mHeaders.put("jsessionid", sessionId);
          mHeaders.put(OAUTH_DONE_URI_PARAMETER, CLOSE_REDIRECT_URL);

          view.loadUrl(url, mHeaders);
          return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(e);
      }
    }
  };

  private WebSettings mOAuthWebViewSettings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Get the uri from the intent.
    Intent callingIntent = getIntent();
    Uri uri = callingIntent.getData();
    try {
      mRedirectUrl = new URL(uri.getQueryParameter(OAUTH_REDIRECT_URI_PARAMETER));
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }

    setContentView(R.layout.oauth_webview);
    mOAuthWebView = (WebView) findViewById(R.id.oauth_webview);
    mOAuthWebView.getSettings().setJavaScriptEnabled(true);
    mOAuthWebViewSettings = mOAuthWebView.getSettings();
    mOAuthWebView.setWebViewClient(mWebViewClient);
    mOAuthWebViewSettings.setSavePassword(false);
    mOAuthWebViewSettings.setSaveFormData(false);
    mOAuthWebView.loadUrl(uri.toString());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy: clearing webview cookies");
    CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(this);
    CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.removeAllCookie();

    for (Reference<OAuthFlowCompleteHandler> handlerRef : mOAuthCompletionHandlers) {
      try {
        OAuthFlowCompleteHandler handler = handlerRef.get();
        Log.d(TAG, "onDestroy: calling OAuthFlowCompleteHandler: " + handler);
        handler.onOAuthFlowComplete(this, mIsCompleted);
      } catch (Exception ex) {
        Log.e(TAG, "onDestroy: caught exception when calling OAuthFlowCompleteHandler", ex);
      }
    }
  }

  private boolean isLocalRedirectUrl(URL url) {
    //if this the redirect_uri pulled from the initial url matches this one
    if (url.getHost().equals(mRedirectUrl.getHost())) {
      return true;
    }
    return false;
  }

  private String buildLocalRedirectUrlWithClose(String url, String sessionId) {
    StringBuilder sb = new StringBuilder(url);
    if (sb.indexOf("?") == -1) {
      sb.append('?');
    } else {
      sb.append('&');
    }
    sb.append(OAUTH_DONE_URI_PARAMETER).append('=');
    try {
      sb.append(URLEncoder.encode(CLOSE_REDIRECT_URL, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      //coding error
      throw new RuntimeException(e);
    }

    if (sessionId != null) {
      sb.append('&').append(MagnetRestLikeConnectionService.SESSION_ID_NAME).append('=').append(sessionId);
    }
    return sb.toString();
  }
}
