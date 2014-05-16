package org.quuux.websocketshimtest.websocketshimtest;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.quuux.websocketshim.websocket_shim.WebSocketShim;

public class MainActivity extends Activity {

    private static final String TAG = "WebSocketShimTest";
    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebView = new TestWebView(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new TestWebChromeClient());
        WebSocketShim.apply(mWebView);
        setContentView(mWebView);
    }

    class TestWebView extends WebView {

        public TestWebView(final Context context) {
            super(context);
        }

        public TestWebView(final Context context, final AttributeSet attrs) {
            super(context, attrs);
        }

        public TestWebView(final Context context, final AttributeSet attrs, final int defStyle) {
            super(context, attrs, defStyle);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public TestWebView(final Context context, final AttributeSet attrs, final int defStyle, final boolean privateBrowsing) {
            super(context, attrs, defStyle, privateBrowsing);
        }
    }

    class TestWebChromeClient extends WebChromeClient {
        @Override
        public void onConsoleMessage(final String message, final int lineNumber, final String sourceID) {
            super.onConsoleMessage(message, lineNumber, sourceID);
            Log.d(TAG, String.format("onConsoleMessage(message=%s, lineNumber=%s, sourceId=%s)", message, lineNumber, sourceID));
        }

        @Override
        public boolean onConsoleMessage(final ConsoleMessage consoleMessage) {
            Log.d(TAG, String.format("onConsoleMessage(consoleMessage=%s)", consoleMessage));
            return super.onConsoleMessage(consoleMessage);
        }
    }
}
