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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /** Reads the pin from the link and asks Waze to drive there, using Waze's documented link format. */
    private void navigateWithWaze(String url) {
        Matcher m = LATLON.matcher(Uri.decode(url));
        if (!m.find()) { Toast.makeText(this, "No location for this place", Toast.LENGTH_SHORT).show(); return; }
        String link = "https://waze.com/ul?ll=" + m.group(1) + "," + m.group(2) + "&navigate=yes&zoom=17";
        Intent waze = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        waze.setPackage("com.waze");
        waze.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(waze);
        } catch (ActivityNotFoundException e) {
            open(link); // Waze not installed: the web page offers to install it
        }
    }

    private static final Pattern LATLON = Pattern.compile("ll=(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)");

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
