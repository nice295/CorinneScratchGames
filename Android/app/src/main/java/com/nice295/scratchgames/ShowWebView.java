package com.nice295.scratchgames;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import io.paperdb.Paper;


public class ShowWebView extends BaseActivity {

    private static final String TAG = "ShowWebView";

    private boolean mLike = false;

    private LinkedList<String> mListLikes;
    private String mId;

    private FirebaseAnalytics mFirebaseAnalytics;

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    //private Button button;
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //This will not show title bar 
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.show_web_view);

        Intent intent = getIntent();
        mId = intent.getExtras().getString("id");
        String user = intent.getExtras().getString("user");
        String name = intent.getExtras().getString("name");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(name);
        toolbar.setSubtitle(user);

        //Get webview
        webView = (WebView) findViewById(R.id.webView1);
        if (haveNetworkConnection()) {
            startWebView("https://nice295.github.io/scratchgames/app.html?id="+mId+"&turbo=false&full-screen=true");
        } else {
            Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            finish(); //khlee
            webView.loadUrl("file:///android_asset/error.html");
        }

        mListLikes = Paper.book().read("likes", new LinkedList<String>());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);

        if ( mListLikes.contains(mId) ) {
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
            mLike = true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.like) {
            if(mLike){
                //change your view and sort it by Alphabet
                //item.setIcon(icon1)
                item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
                mLike = false;

                mListLikes.remove(mId);
            }else{
                //change your view and sort it by Date of Birth
                //item.setIcon(icon2)
                item.setIcon(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
                mLike = true;

                mListLikes.add(mId);

                String androidId = Settings.Secure.getString(this.getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                Bundle params = new Bundle();
                params.putString("id", mId);
                params.putString("userId", androidId);
                mFirebaseAnalytics.logEvent("likes", params);
            }
            Paper.book().write("likes", mListLikes);
            Log.d(TAG, "Like count: " + mListLikes.size());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startWebView(String url) {

        //Create new webview Client to show progress dialog
        //When opening a url or click on link

        webView.setWebViewClient(new WebViewClient() {
            //If you will not use this method url links are opeen in new brower not in webview
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            /*
            //If url has "tel:245678" , on clicking the number it will directly call to inbuilt calling feature of phone  
            public boolean shouldOverrideUrlLoading(WebView view ,String url){
			    	
		 	    	if(url.startsWith("tel:")){
			    		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			    		startActivity(intent);
			    	} else {
			    		
			    		view.loadUrl(url);
			    		
			    	}
        }
        */

            //Show loader on url load
            public void onLoadResource(WebView view, String url) {
            }

            public void onPageFinished(WebView view, String url) {
                try {
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

        });

        // Javascript inabled on webview
        webView.getSettings().setJavaScriptEnabled(true);

        // Other webview options
        /*
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.getSettings().setBuiltInZoomControls(true);
        		//Additional Webview Properties 
        	        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		        webView.getSettings().setDatabaseEnabled(true);
		        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
		        webView.getSettings().setAppCacheEnabled(true);
		        webView.getSettings().setLayoutAlgorithm(webView.getSettings().getLayoutAlgorithm().NORMAL);
		        webView.getSettings().setLoadWithOverviewMode(true);
		        webView.getSettings().setUseWideViewPort(false);
		        webView.setSoundEffectsEnabled(true);
		        webView.setHorizontalFadingEdgeEnabled(false);
		        webView.setKeepScreenOn(true);
		        webView.setScrollbarFadingEnabled(true);
		        webView.setVerticalFadingEdgeEnabled(false);
	
        
        
        
        
        
        */
         
        /*
         String summary = "<html><body>You scored <b>192</b> points.</body></html>";
         webview.loadData(summary, "text/html", null);
         */

        //Load url in webview
        webView.loadUrl(url);


    }


    // Open previous opened link from history on webview when back button pressed

    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }

        closeWebView();



        finish();
    }

    private void closeWebView() {
        //webView.clearHistory();
        //webView.clearCache(true);
        webView.loadUrl("about:blank");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeWebView();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeWebView();
        finish();
    }
}
