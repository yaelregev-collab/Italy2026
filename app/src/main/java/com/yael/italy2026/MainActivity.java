package com.yael.italy2026;

import android.app.Activity;
import android.app.AlertDialog;
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

    private static final String[] WAZE_OPTIONS = {
        "1. Search the address in Waze",
        "2. Navigate to the map pin",
        "3. Open the pin as a location",
        "4. Share the address to Waze"
    };

    /** Test version: lets you pick how to hand the place to Waze, to find the way that works on this phone. */
    private void navigateWithWaze(String url) {
        Uri u = Uri.parse(url.replaceFirst("^waze:/*", "waze://x/"));
        final String q = u.getQueryParameter("q");
        final String ll = u.getQueryParameter("ll");
        new AlertDialog.Builder(this)
            .setTitle(q)
            .setItems(WAZE_OPTIONS, (dialog, which) -> launchWaze(which, q, ll))
            .show();
    }

    private void launchWaze(int which, String q, String ll) {
        Intent i;
        if (which == 0) {
            i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://waze.com/ul?q=" + Uri.encode(q)));
        } else if (which == 1) {
            i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://waze.com/ul?ll=" + ll + "&navigate=yes"));
        } else if (which == 2) {
            i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + ll + "?q=" + Uri.encode(ll + "(" + q + ")")));
        } else {
            i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, q);
        }
        i.setPackage("com.waze");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Waze can't open option " + (which + 1), Toast.LENGTH_LONG).show();
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
