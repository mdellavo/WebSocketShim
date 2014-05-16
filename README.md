WebSocketShim
=============

A WebSocket shim for WebView on older Androids

Example
=======

public class MainActivity extends Activity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView)findViewById(R.id.web_view);
        WebSocketShim.apply(mWebView);
    }
}


