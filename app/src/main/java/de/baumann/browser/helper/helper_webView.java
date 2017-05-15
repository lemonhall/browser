/*
    This file is part of the Browser WebApp.

    Browser WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Browser WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Browser webview app.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.browser.helper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Patterns;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Locale;

import de.baumann.browser.R;
import de.baumann.browser.databases.DbAdapter_History;
import de.baumann.browser.utils.Utils_AdClient;
import de.baumann.browser.utils.Utils_UserAgent;

import static android.content.ContentValues.TAG;
import static android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;


public class helper_webView {

    private static class GetScript extends AsyncTask<Void, Integer, String> {
        // you may separate this or combined to caller class.
        public interface AsyncResponse {
            void processFinish(String output,WebView view);
        }

        public AsyncResponse delegate = null;
        public WebView view = null;

        public GetScript(AsyncResponse delegate,WebView view){
            this.delegate = delegate;
            this.view     = view;
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                jcifs.Config.registerSmbURLHandler();
                String s_url = "smb://192.168.1.110/Public/test.txt";
                SmbFile smbfile = new SmbFile(s_url);
                SmbFileInputStream in;

                try {
//                                in = new SmbFileInputStream(smbfile);
//                                byte[] b = new byte[8192];
//                                int n;
//                                while(( n = in.read( b )) > 0 ) {
//                                    Log.d("lemonhall", b.toString());
//                                }
                    in = new SmbFileInputStream(smbfile);
                    BufferedReader bufferedFileReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String line = null;
                    StringBuilder f_content = new StringBuilder();

                    try {
                        while ((line = bufferedFileReader.readLine()) != null) {
                            if (!line.trim().isEmpty()) {
                                f_content.append(line);
                            }
                        }
                        //Log.d("lemonhall", f_content.toString());
                        return f_content.toString();
                    } finally {
                        bufferedFileReader.close();
                    }

                }catch (SmbException e) {
                    Log.d("lemonhall", "new SmbFileInputStream",e);
                    return e.toString();
                }


            }catch(Exception e){
                Log.d("lemonhall", "onPageFinished()", e);
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result,view);
        }
    }

    public static String getTitle (WebView webview) {

        return  webview.getTitle().replace("'", "\\'");
    }


    @SuppressLint("SetJavaScriptEnabled")
    public static void webView_Settings(final Activity from, final WebView webView) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
        String fontSizeST = sharedPref.getString("font", "500");
        int fontSize = Integer.parseInt(fontSizeST);

        webView.getSettings().setAppCachePath(from.getApplicationContext().getCacheDir().getAbsolutePath());
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setMixedContentMode(MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setTextZoom(fontSize);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        //webView.setInitialScale(20);

        from.registerForContextMenu(webView);

        if (sharedPref.getString ("cookie", "1").equals("2") || sharedPref.getString ("cookie", "1").equals("3")){
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView,true);
        } else {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView,false);
        }

        if (sharedPref.getBoolean ("java", false)){
            webView.getSettings().setJavaScriptEnabled(true);
            sharedPref.edit().putString("java_string", from.getString(R.string.app_yes)).apply();
        } else {
            webView.getSettings().setJavaScriptEnabled(false);
            sharedPref.edit().putString("java_string", from.getString(R.string.app_no)).apply();
        }

        if (sharedPref.getBoolean ("pictures", false)){
            webView.getSettings().setLoadsImagesAutomatically(true);
            sharedPref.edit().putString("pictures_string", from.getString(R.string.app_yes)).apply();
        } else {
            webView.getSettings().setLoadsImagesAutomatically(false);
            sharedPref.edit().putString("pictures_string", from.getString(R.string.app_no)).apply();
        }

        if (sharedPref.getBoolean ("loc", false)){
            webView.getSettings().setGeolocationEnabled(true);
            helper_main.grantPermissionsLoc(from);
            sharedPref.edit().putString("loc_string", from.getString(R.string.app_yes)).apply();
        } else {
            webView.getSettings().setGeolocationEnabled(false);
            sharedPref.edit().putString("loc_string", from.getString(R.string.app_no)).apply();
        }

        if (sharedPref.getString ("cookie", "1").equals("1") || sharedPref.getString ("cookie", "1").equals("3")){
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            sharedPref.edit().putString("cookie_string", from.getString(R.string.app_yes)).apply();
        } else {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(false);
            sharedPref.edit().putString("cookie_string", from.getString(R.string.app_no)).apply();
        }

        if (sharedPref.getBoolean ("blockads_bo", false)){
            sharedPref.edit().putString("blockads_string", from.getString(R.string.app_yes)).apply();
        } else {
            sharedPref.edit().putString("blockads_string", from.getString(R.string.app_no)).apply();
        }

        Utils_UserAgent myUserAgent= new Utils_UserAgent();

        if (sharedPref.getBoolean ("request_bo", false)){
            sharedPref.edit().putString("request_string", from.getString(R.string.app_yes)).apply();
            myUserAgent.setUserAgent(from, webView, true, webView.getUrl());
        } else {
            sharedPref.edit().putString("request_string", from.getString(R.string.app_no)).apply();
            myUserAgent.setUserAgent(from, webView, false, webView.getUrl());
        }
    }

    public static void webView_WebViewClient (final Activity from, final SwipeRefreshLayout swipeRefreshLayout,
                                              final WebView webView, final TextView urlBar) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);


        // crude if-else just to get the functionality in, feel free to make this more concise if you like
        if (sharedPref.getString("blockads_string", "").equals(from.getString(R.string.app_yes))) {
            webView.setWebViewClient(new Utils_AdClient() {

                public void onPageFinished(WebView view, String url) {

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
                    view.setInitialScale(300);

                    super.onPageFinished(view, url);
                    swipeRefreshLayout.setRefreshing(false);
                    urlBar.setText(webView.getTitle());
                    sharedPref.edit().putString("openURL", "").apply();

                    if (webView.getTitle() != null && !webView.getTitle().equals("about:blank")  && !webView.getTitle().isEmpty()) {

                        DbAdapter_History db = new DbAdapter_History(from);
                        db.open();
                        db.deleteDouble(webView.getUrl());

                        if(db.isExist(helper_main.createDateSecond())){
                            Log.i(TAG, "Entry exists" + webView.getUrl());
                        }else{
                            if (helper_webView.getTitle (webView).contains("'")) {
                                String title = helper_webView.getTitle (webView).replace("'", "");
                                db.insert(title, webView.getUrl(), "", "", helper_main.createDateSecond());

                            } else {
                                db.insert(helper_webView.getTitle (webView), webView.getUrl(), "", "", helper_main.createDateSecond());
                            }
                        }
                    }
                    if(url.equals("https://www.douban.com/")){
                        AsyncTask<Void, Integer, String> asyncGetScript =new GetScript(new GetScript.AsyncResponse(){
                            @Override
                            public void processFinish(String output,WebView view){
                                //Here you will receive the result fired from async class
                                //of onPostExecute(result) method.
                                Log.d("lemonhall", "Hi I got from AsyncTask");
                                Log.d("lemonhall", output);
                                view.loadUrl("javascript:" + output);
                            }
                        },view).execute();
                    }

                }

                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    final Uri uri = Uri.parse(url);
                    return handleUri(uri);
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    final Uri uri = request.getUrl();
                    return handleUri(uri);
                }

                private boolean handleUri(final Uri uri) {

                    Log.i(TAG, "Uri =" + uri);
                    final String url = uri.toString();
                    // Based on some condition you need to determine if you are going to load the url
                    // in your web view itself or in a browser.
                    // You can use `host` or `scheme` or any part of the `uri` to decide.

                    if (url.startsWith("http")) return false;//open web links as usual
                    //try to find browse activity to handle uri
                    Uri parsedUri = Uri.parse(url);
                    PackageManager packageManager = from.getPackageManager();
                    Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
                    if (browseIntent.resolveActivity(packageManager) != null) {
                        from.startActivity(browseIntent);
                        return true;
                    }
                    //if not activity found, try to parse intent://
                    if (url.startsWith("intent:")) {
                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            if (intent.resolveActivity(from.getPackageManager()) != null) {
                                try {
                                    from.startActivity(intent);
                                } catch (Exception e) {
                                    Snackbar.make(webView, R.string.toast_error, Snackbar.LENGTH_SHORT).show();
                                }

                                return true;
                            }
                            //try to find fallback url
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            if (fallbackUrl != null) {
                                webView.loadUrl(fallbackUrl);
                                return true;
                            }
                            //invite to install
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                                    Uri.parse("market://details?id=" + intent.getPackage()));
                            if (marketIntent.resolveActivity(packageManager) != null) {
                                from.startActivity(marketIntent);
                                return true;
                            }
                        } catch (URISyntaxException e) {
                            //not an intent uri
                        }
                    }
                    return true;//do nothing in other cases
                }

            });
        } else{
            webView.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
                    super.onPageFinished(view, url);
                    swipeRefreshLayout.setRefreshing(false);
                    urlBar.setText(webView.getTitle());
                    sharedPref.edit().putString("openURL", "").apply();

                    if (webView.getTitle() != null && !webView.getTitle().equals("about:blank")  && !webView.getTitle().isEmpty()) {

                        DbAdapter_History db = new DbAdapter_History(from);
                        db.open();
                        db.deleteDouble(webView.getUrl());

                        if(db.isExist(helper_main.createDateSecond())){
                            Log.i(TAG, "Entry exists" + webView.getUrl());
                        }else{
                            if (helper_webView.getTitle (webView).contains("'")) {
                                String title = helper_webView.getTitle (webView).replace("'", "");
                                db.insert(title, webView.getUrl(), "", "", helper_main.createDateSecond());

                            } else {
                                db.insert(helper_webView.getTitle (webView), webView.getUrl(), "", "", helper_main.createDateSecond());
                            }
                        }
                    }
                }

                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    final Uri uri = Uri.parse(url);
                    return handleUri(uri);
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    final Uri uri = request.getUrl();
                    return handleUri(uri);
                }

                private boolean handleUri(final Uri uri) {

                    Log.i(TAG, "Uri =" + uri);
                    final String url = uri.toString();
                    // Based on some condition you need to determine if you are going to load the url
                    // in your web view itself or in a browser.
                    // You can use `host` or `scheme` or any part of the `uri` to decide.

                    if (url.startsWith("http")) return false;//open web links as usual
                    //try to find browse activity to handle uri
                    Uri parsedUri = Uri.parse(url);
                    PackageManager packageManager = from.getPackageManager();
                    Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(parsedUri);
                    if (browseIntent.resolveActivity(packageManager) != null) {
                        from.startActivity(browseIntent);
                        return true;
                    }
                    //if not activity found, try to parse intent://
                    if (url.startsWith("intent:")) {
                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            if (intent.resolveActivity(from.getPackageManager()) != null) {
                                try {
                                    from.startActivity(intent);
                                } catch (Exception e) {
                                    Snackbar.make(webView, R.string.toast_error, Snackbar.LENGTH_SHORT).show();
                                }

                                return true;
                            }
                            //try to find fallback url
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            if (fallbackUrl != null) {
                                webView.loadUrl(fallbackUrl);
                                return true;
                            }
                            //invite to install
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(
                                    Uri.parse("market://details?id=" + intent.getPackage()));
                            if (marketIntent.resolveActivity(packageManager) != null) {
                                from.startActivity(marketIntent);
                                return true;
                            }
                        } catch (URISyntaxException e) {
                            //not an intent uri
                        }
                    }
                    return true;//do nothing in other cases
                }

            });

        }
    }

    public static void closeWebView (Activity from, WebView webView) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
        if (sharedPref.getBoolean ("clearCookies", false)){
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
        }

        if (sharedPref.getBoolean ("clearCache", false)){
            webView.clearCache(true);
        }

        if (sharedPref.getBoolean ("clearForm", false)){
            webView.clearFormData();
        }

        if (sharedPref.getBoolean ("history", false)){
            from.deleteDatabase("history_DB_v01.db");
            webView.clearHistory();
        }
        sharedPref.edit().putString("started", "").apply();
    }


    public static void openURL (Activity from, WebView mWebView, EditText editText) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
        String text = editText.getText().toString();
        String searchEngine = sharedPref.getString("searchEngine", "https://duckduckgo.com/?q=");
        String wikiLang = sharedPref.getString("wikiLang", "en");

        if (text.startsWith("http")) {
            mWebView.loadUrl(text);
        } else if (text.startsWith("www.")) {
            mWebView.loadUrl("https://" + text);
        } else if (Patterns.WEB_URL.matcher(text).matches()) {
            mWebView.loadUrl("https://" + text);
        } else {

            String subStr = null;

            if (text.length() > 3) {
                subStr=text.substring(3);
            }

            if (text.contains(".w ")) {
                mWebView.loadUrl("https://" + wikiLang + ".wikipedia.org/wiki/Spezial:Suche?search=" + subStr);
            } else if (text.startsWith(".f ")) {
                mWebView.loadUrl("https://www.flickr.com/search/?advanced=1&license=2%2C3%2C4%2C5%2C6%2C9&text=" + subStr);
            } else  if (text.startsWith(".m ")) {
                mWebView.loadUrl("https://metager.de/meta/meta.ger3?focus=web&eingabe=" + subStr);
            } else if (text.startsWith(".g ")) {
                mWebView.loadUrl("https://github.com/search?utf8=✓&q=" + subStr);
            } else  if (text.startsWith(".s ")) {
                if (Locale.getDefault().getLanguage().contentEquals("de")){
                    mWebView.loadUrl("https://startpage.com/do/search?query=" + subStr + "&lui=deutsch&l=deutsch");
                } else {
                    mWebView.loadUrl("https://startpage.com/do/search?query=" + subStr);
                }
            } else if (text.startsWith(".G ")) {
                if (Locale.getDefault().getLanguage().contentEquals("de")){
                    mWebView.loadUrl("https://www.google.de/search?&q=" + subStr);
                } else {
                    mWebView.loadUrl("https://www.google.com/search?&q=" + subStr);
                }
            } else  if (text.startsWith(".y ")) {
                if (Locale.getDefault().getLanguage().contentEquals("de")){
                    mWebView.loadUrl("https://www.youtube.com/results?hl=de&gl=DE&search_query=" + subStr);
                } else {
                    mWebView.loadUrl("https://www.youtube.com/results?search_query=" + subStr);
                }
            } else  if (text.startsWith(".d ")) {
                if (Locale.getDefault().getLanguage().contentEquals("de")){
                    mWebView.loadUrl("https://duckduckgo.com/?q=" + subStr + "&kl=de-de&kad=de_DE&k1=-1&kaj=m&kam=osm&kp=-1&kak=-1&kd=1&t=h_&ia=web");
                } else {
                    mWebView.loadUrl("https://duckduckgo.com/?q=" + subStr);
                }
            } else {
                if (searchEngine.contains("https://duckduckgo.com/?q=")) {
                    if (Locale.getDefault().getLanguage().contentEquals("de")){
                        mWebView.loadUrl("https://duckduckgo.com/?q=" + text + "&kl=de-de&kad=de_DE&k1=-1&kaj=m&kam=osm&kp=-1&kak=-1&kd=1&t=h_&ia=web");
                    } else {
                        mWebView.loadUrl("https://duckduckgo.com/?q=" + text);
                    }
                } else if (searchEngine.contains("https://metager.de/meta/meta.ger3?focus=web&eingabe=")) {
                    if (Locale.getDefault().getLanguage().contentEquals("de")){
                        mWebView.loadUrl("https://metager.de/meta/meta.ger3?focus=web&eingabe=" + text);
                    } else {
                        mWebView.loadUrl("https://metager.de/meta/meta.ger3?focus=web&eingabe=" + text +"&focus=web&encoding=utf8&lang=eng");
                    }
                } else if (searchEngine.contains("https://startpage.com/do/search?query=")) {
                    if (Locale.getDefault().getLanguage().contentEquals("de")){
                        mWebView.loadUrl("https://startpage.com/do/search?query=" + text + "&lui=deutsch&l=deutsch");
                    } else {
                        mWebView.loadUrl("https://startpage.com/do/search?query=" + text);
                    }
                }else {
                    mWebView.loadUrl(searchEngine + text);
                }
            }
        }
    }
}