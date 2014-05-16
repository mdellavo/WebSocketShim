package org.quuux.websocketshim.websocket_shim;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class WebSocketShim {

    private static final String TAG = "WebSocketShim";

    private List<WebSocketConnection> mSockets = new ArrayList<WebSocketConnection>();
    private WeakReference<WebView> mView;

    public WebSocketShim(final WebView view) {
        attach(view);
    }

    private void attach(final WebView view) {
        mView = new WeakReference<WebView>(view);
    }

    private WebView getView() {
        return mView != null ? mView.get() : null;
    }

    private WebSocketInterface getInterface() {
        return new WebSocketInterface(this);
    }

    private static String sandbox(final String script) {
        return String.format("javascript:(function() { %s })();", script);
    }

    private static void eval(final WebView view, final String script) {
        if (view != null) {
            final String sandboxedScript = sandbox(script);
            log("eval(%s", sandboxedScript);
            view.loadUrl(sandboxedScript);
        }
    }

    private void eval(final String script, final Object... args) {
        final WebView view = getView();
        if (view != null)
            eval(view, args.length > 0 ? String.format(script, args) : script);
    }


    private static void logException(final String fmt, final Throwable e, Object... args) {
        Log.e(TAG, args.length > 0 ? String.format(fmt, args) : fmt, e);
    }

    private static void log(final String fmt, Object... args) {
        Log.d(TAG, args.length > 0 ? String.format(fmt, args) : fmt);
    }

    public WebSocketConnection getSocket(final int socket) {
        return mSockets.get(socket);
    }

    private int getSocketIndex(final WebSocketConnection socket) {
        return mSockets.indexOf(socket);
    }

    public int newSocket() {
        final WebSocketConnection sock = new WebSocketConnection();
        mSockets.add(sock);
        return getSocketIndex(sock);
    }

    private void close(final int socket) {
        final WebSocketConnection sock = mSockets.get(socket);
        if (sock != null) {
            log("close() @ %s", socket);
            sock.disconnect();
        }
    }

    private void send(final int socket, final String data) {
        final WebSocketConnection sock = mSockets.get(socket);
        if (sock != null) {
            log("send(data=%s) @ %s", data, socket);
            sock.sendTextMessage(data);
        }
    }

    private void connect(final int socket, final String uri) {
        final WebSocketConnection sock = getSocket(socket);
        final int socketIndex = getSocketIndex(sock);

        try {
            sock.connect(uri, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    log("open(uri=%s) @ %s", uri, socketIndex);
                    eval("WebSocketShim.onOpen(%s, \"%s\")", socketIndex, uri);
                }

                @Override
                public void onTextMessage(final String data) {
                    log("message(data=%s) @ %s", data, socketIndex);
                    eval("WebSocketShim.onMessage(%s, \"%s\")", socketIndex, data);
                }

                @Override
                public void onClose(final int code, final String reason) {
                    log("close(code=%s, reason=%s) @ %s", code, reason, socketIndex);
                    eval("WebSocketShim.onClose(%s, %s, \"%s\", %s)", socketIndex, code, reason, null);
                }
            });
        } catch (WebSocketException e) {
            logException("web socket exception @ %s", e, socketIndex);
        }
    }


    private static class WebSocketInterface {

        private final WebSocketShim mShim;

        public WebSocketInterface(final WebSocketShim shim) {
            mShim = shim;
        }

        @JavascriptInterface
        public int newSocket() {
            return mShim.newSocket();
        }

        @JavascriptInterface
        public void connect(final int socket, final String uri) {
            mShim.connect(socket, uri);
        }

        @JavascriptInterface
        public void send(final int socket, final String data) {
            mShim.send(socket, data);
        }

        @JavascriptInterface
        public void close(final int socket) {
            mShim.close(socket);
        }
    }


    public static String loadLibrary(final Context context) {
        final AssetManager assetManager = context.getAssets();
        try {
            final BufferedInputStream in = new BufferedInputStream(assetManager.open("WebSocketShim.js"));
            final StringBuilder sb = new StringBuilder();

            final byte[] buffer = new byte[4096];
            int count;
            while((count = in.read(buffer)) >= 0) {
                sb.append(new String(buffer, 0, count));
            }

            return sb.toString();
        } catch (final IOException e) {
            logException("error loading shim library", e);
        }
        return null;
    }

    public static void apply(final WebView view) {

        final String library = loadLibrary(view.getContext());
        if (library == null) {
            log("error loading library");
            return;
        }

        eval(view, library);

        final WebSocketShim shim = new WebSocketShim(view);
        view.addJavascriptInterface(shim.getInterface(), "WebSocketInterface");
    }

}
