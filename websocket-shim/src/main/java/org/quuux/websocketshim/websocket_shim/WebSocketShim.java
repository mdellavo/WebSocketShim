package org.quuux.websocketshim.websocket_shim;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.List;

public class WebSocketShim {

    private List<Integer>=


    private static class WebSocketInterface {


        private final WebSocketShim mShim;

        public WebSocketInterface(final WebSocketShim shim) {
            mShim = shim;
        }

        @JavascriptInterface
        public int openSocket() {
            return -1;
        }


    }

    public static void apply(final WebView view) {

        final WebSocketShim shim = new WebSocketShim();

        view.addJavascriptInterface(new WebSocketInterface(shim), "WebSocket");
    }

}
