package com.yael.italy2026;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


/** Shows the trip list (assets/index.html) and handles the Waze and Copy buttons. */
public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setAllowFileAccess(false);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return handle(req.getUrl().toString());
            }
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handle(url);
            }
        });
        if (state != null) web.restoreState(state);
        else web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }

    private boolean handle(String url) {
        if (url.startsWith("file:///android_asset/")) return false;
        if (url.startsWith("copy:")) {
            String text = Uri.decode(url.substring(5));
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("Address", text));
            if (Build.VERSION.SDK_INT < 33) Toast.makeText(this, "Address copied", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (url.startsWith("waze:")) {
            navigateWithWaze(url);
            return true;
        }
        open(url);
        return true;
    }

    /** Sends the place's address to Waze as a search, the same as pasting it into Waze, and starts navigation. */
    private void navigateWithWaze(String url) {
        String address = Uri.parse(url.replaceFirst("^waze:/*", "waze://x/")).getQueryParameter("q");
        if (address == null || address.isEmpty()) { Toast.makeText(this, "No address for this place", Toast.LENGTH_SHORT).show(); return; }
        String link = "https://waze.com/ul?q=" + Uri.encode(address) + "&navigate=yes";
        Intent waze = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        waze.setPackage("com.waze");
        waze.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(waze);
        } catch (ActivityNotFoundException e) {
            open(link); // Waze not installed: the web page offers to install it
        }
    }

    private void open(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (ActivityNotFoundException ignored) { }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }
}
