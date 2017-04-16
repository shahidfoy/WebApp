package com.shahidfoy.webapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.view.Window;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

    //private SwipeRefreshLayout swipeRefreshLayout;

    private WebView webView;
    // private Bundle webViewBundle;
    private Context context;

    // allow image upload.
    ProgressBar progressBar;

    private ValueCallback<Uri> message;
    private ValueCallback<Uri[]> uriMessage;
    private static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(requestCode == REQUEST_SELECT_FILE) {
                if(uriMessage == null) return;
                uriMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uriMessage = null;
            }
        }
        else if(requestCode==FILECHOOSER_RESULTCODE) {
            if(null == message) return;
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            message.onReceiveValue(result);
            message = null;
        }
        else {
            Toast.makeText(this, "Failed to Upload Image", Toast.LENGTH_LONG).show();
        }
    }

    /*
    public class GeoWebChromeClient extends WebChromeClient {

    }
    */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);

        //webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);



        context = getApplicationContext();
        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());






        webView.setWebChromeClient(new android.webkit.WebChromeClient() {







            // geolocation
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                //Log.i(TAG, "onGeolocationPermissionsShowPrompt()");
                /*
                final boolean remember = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Locations");
                builder.setMessage("Would like to use your Current Location ")
                        .setCancelable(true).setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // origin, allow, remember
                        callback.invoke(origin, true, remember);
                        // PackageManager.PERMISSION_GRANTED;
                    }
                }).setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // origin, allow, remember
                        callback.invoke(origin, false, remember);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                */

            }

            // sets external links
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg)
            {
                WebView.HitTestResult result = view.getHitTestResult();
                String data = result.getExtra();
                //Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                String testData;
                String locationData;
                if(data.length() >= 46) {
                    testData = data.substring(0, 46);
                    if(testData.equals("https://maps.googleapis.com/maps/api/staticmap")) {
                        locationData = data.substring(132);
                    }
                    else {
                        locationData = data;
                    }
                }
                else {
                    testData = data;
                    locationData = data;
                }

                // Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();

                if(testData.equals("https://maps.googleapis.com/maps/api/staticmap")) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q="+locationData+""));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                    return true;
                } else {
                    Context contextWindow = view.getContext();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                    contextWindow.startActivity(browserIntent);

                    return false;

                }


            }


            // upload images for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                message = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"),
                        FILECHOOSER_RESULTCODE);


            }


            // android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                message = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);

            }


            // android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                message = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FILECHOOSER_RESULTCODE);
            }

            // android 5.0+
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if(uriMessage != null) {
                    uriMessage.onReceiveValue(null);
                    uriMessage = null;
                }

                uriMessage = filePathCallback;

                //Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                //i.addCategory(Intent.CATEGORY_OPENABLE);
                //i.setType("image/*");

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                    //startActivityForResult(i, REQUEST_SELECT_FILE);

                } catch(ActivityNotFoundException e) {
                    uriMessage = null;
                    //Toast.makeText(this, "Sorry but you are not connected to a network. This app requires access to the internet", Toast.LENGTH_LONG).show();;
                    return false;
                }

                return true;
            }

        });


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSupportZoom(false);

        // geolocation
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // webView.getSettings().setGeolocationDatabasePath(context.getFilesDir().getPath());


        // external links
        webView.getSettings().setSupportMultipleWindows(true);





        if(isNetWorkAvailable(context)) {
            // Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(this, "Sorry but you are not connected to a network. This app requires access to the internet", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));


        }

        if(savedInstanceState == null) {
            webView.loadUrl("https://automotivemech.com");
        }




        setContentView(webView);


        // allows geolocation permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!canAccessCoarseLocation() || !canAccessFineLocation()){
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }


        SwipeRefreshLayout swipeLayout = new SwipeRefreshLayout(this);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Insert your code here
                webView.reload(); // refreshes the WebView
            }
        });




    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }



    @Override
    public void onStart() {
        // First call the "offical" version of this method
        super.onStart();


        if(isNetWorkAvailable(context)) {
            // Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(this, "Sorry but you are not connected to a network. This app requires access to the internet", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));


        }

        // webView.reload();

        //Toast.makeText(this, "In onStart", Toast.LENGTH_SHORT).show();
        //Log.i("info", "In onStart");

    }

    @Override
    public void onResume() {
        // First call the "official" version of this method
        super.onResume();

        //webView.restoreState(webViewBundle);

        webView.onResume();
        webView.resumeTimers();

        //Toast.makeText(this, "In onResume", Toast.LENGTH_SHORT).show();
        //Log.i("info", "In onResume");
    }

    @Override
    public void onPause() {
        // First call the "official" version of this method
        super.onPause();

        webView.onPause();
        webView.pauseTimers();

        //Toast.makeText(this, "In on Pause", Toast.LENGTH_SHORT).show();
        //Log.i("info", "In onPause");
    }

    @Override
    public void onStop() {
        // First call the "official" version of this method
        super.onStop();

        //Toast.makeText(this, "In onStop", Toast.LENGTH_SHORT).show();
        //Log.i("info", "In onStop");
    }

    @Override
    public void onDestroy() {
        // First call the "offical" version of this method
        super.onDestroy();


        webView.stopLoading();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.destroy();
        webView = null;

        //Toast.makeText(this, "In onDestroy", Toast.LENGTH_SHORT).show();
        //Log.i("info", "In onDestroy");
    }



    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    public boolean isNetWorkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    // allows geolocation permissions
    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessFineLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessCoarseLocation() {
        return(hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }



}
