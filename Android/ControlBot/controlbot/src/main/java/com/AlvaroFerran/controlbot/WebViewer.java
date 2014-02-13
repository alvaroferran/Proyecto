package com.AlvaroFerran.controlbot;

/**
 * Created by alvaro on 1/10/14.
 */
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewer extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}