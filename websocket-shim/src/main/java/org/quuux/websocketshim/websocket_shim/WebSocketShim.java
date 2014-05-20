package org.quuux.websocketshim.websocket_shim;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WebSocketShim {

    private static final String TAG = "WebSocketShim";

    private List<WebSocketClient> mSockets = new ArrayList<WebSocketClient>();
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
        return String.format("javascript: (function() { %s })();", script.replace("\n", ""));
    }

    private static void eval(final WebView view, final String script) {
        if (view != null) {
            final String sandboxedScript = sandbox(script);
            log(sandboxedScript);
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

    public WebSocketClient getSocket(final int socket) {
        return mSockets.get(socket);
    }

    private int getSocketIndex(final WebSocketClient socket) {
        return mSockets.indexOf(socket);
    }

    private void close(final int socket) {
        final WebSocketClient sock = mSockets.get(socket);
        if (sock != null) {
            log("close() @ %s", socket);
            sock.close();
        }
    }

    private void send(final int socket, final String data) {
        final WebSocketClient sock = mSockets.get(socket);
        if (sock != null) {
            log("send(data=%s) @ %s", data, socket);
            sock.send(data);
        }
    }

    private int connect(final String uri) {


        final URI u;
        try {
            u = new URI(uri);
        } catch (URISyntaxException e) {
            Log.e(TAG, "uri error", e);
            return -1;
        }

        final WebSocketClient sock = new WebSocketClient(u) {

            private int getSocketIndex() {
                return mSockets.indexOf(this);
            }

            @Override
            public void onOpen(final ServerHandshake handshakedata) {
                final int socketIndex = getSocketIndex();
                log("open(uri=%s) @ %s", uri, socketIndex);
                eval("WebSocketShim.onOpen(%s, \"%s\")", socketIndex, uri);
            }

            @Override
            public void onMessage(final String message) {
                final int socketIndex = getSocketIndex();
                log("message(message=%s) @ %s", message, socketIndex);
                eval("WebSocketShim.onMessage(%s, \"%s\")", socketIndex, message);
            }

            @Override
            public void onClose(final int code, final String reason, final boolean remote) {
                final int socketIndex = getSocketIndex();
                log("close(code=%s, reason=%s, remote=%s) @ %s", code, reason, remote, socketIndex);
                eval("WebSocketShim.onClose(%s, %s, \"%s\", %s)", socketIndex, code, reason, remote);
            }

            @Override
            public void onError(final Exception ex) {
                final int socketIndex = getSocketIndex();
                logException("error(exception=%s) @ %s", ex, ex, socketIndex);
                eval("WebSocketShim.onError(%s, \"%s\")", socketIndex, ex);
            }
        };
        sock.connect();
        mSockets.add(sock);

        return sock != null ? mSockets.indexOf(sock) : -1;
    }


    private static class WebSocketInterface {

        private final WebSocketShim mShim;

        public WebSocketInterface(final WebSocketShim shim) {
            mShim = shim;
        }

        @JavascriptInterface
        public int connect(final String uri) {
            return mShim.connect(uri);
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

        final WebSocketShim shim = new WebSocketShim(view);
        view.addJavascriptInterface(shim.getInterface(), "WebSocketInterface");

        final String library = loadLibrary(view.getContext());
        if (library == null) {
            log("error loading library");
            return;
        }

        eval(view, library);

    }

}
