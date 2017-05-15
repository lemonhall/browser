1、加入smb文件支持
================
http://search.maven.org/
http://stackoverflow.com/questions/39320338/accessing-files-using-java-with-samba-jcifs


2、搜索库
========
https://mvnrepository.com/search?q=jcifs

找到一个版本：
https://mvnrepository.com/artifact/org.samba.jcifs/jcifs/1.3.18-kohsuke-1

地址是：
// https://mvnrepository.com/artifact/org.samba.jcifs/jcifs


3、加入
========
将以下依赖加入到build.gradle之中去
注意是模块app的build文件

    compile 'org.samba.jcifs:jcifs:1.3.18-kohsuke-1'

4、build会失败
=============
因为我们没加maven的仓库

现在加
注意页面上会写：
https://mvnrepository.com/artifact/org.samba.jcifs/jcifs/1.3.18-kohsuke-1

Note: this artifact it located at Jenkins Releases repository (http://repo.jenkins-ci.org/releases/)

好的，我们打开根目录下的build.gradle

OK，这回没有问题了；

5、添加代码
========

在helper_webView里加，先放到这里，之后再优化：

```
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

try{
	jcifs.Config.registerSmbURLHandler();
    String user = "";
    String pass ="";
    String sharedFolder="Public";

    String url = "smb://192.168.1.110/" + sharedFolder + "/Risa.jpg";
    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
                        null, user, pass);
    SmbFile sfile = new SmbFile(url, auth);
}catch(Exception e){
    
}
```

6、权限
======

```
AndroidManisfest.xml permissions

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```


7、例子代码
=========

```
    try{
        jcifs.Config.registerSmbURLHandler();
        String user = "";
        String pass ="";
        String sharedFolder="Public";

        String s_url = "smb://192.168.1.110/" + sharedFolder + "/test.txt";
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
                null, user, pass);
        SmbFile sfile = new SmbFile(s_url, auth);
        String f_content = sfile.getContent().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("alert('"+f_content+"')");
        view.loadUrl("javascript:" + sb.toString());

    }catch(Exception e){
        Log.d("lemonhall", "onPageFinished()", e);
    }
```

8、调试技巧
=========
https://developer.android.com/studio/profile/am-basics.html#displaying

在最下面，找到
Android Monitor
然后就可以看到logcat了

然后搞一个过滤器出来，先过滤包名


9、其它例子
==========
http://stackoverflow.com/questions/7771888/access-to-file-using-java-with-samba-jcifs
http://www.javased.com/index.php?api=jcifs.smb.SmbFile
```
                byte[] b = new byte[8192];
                int n;
                while(( n = in.read( b )) > 0 ) {
                    System.out.write( b, 0, n );
                }
```

10、Crash的原因：
===============
http://stackoverflow.com/questions/12064156/android-smb-file-access-crashing

```
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err: java.lang.ExceptionInInitializerError
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.netbios.NbtAddress.getWINSAddress(NbtAddress.java:533)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.UniAddress.<clinit>(UniAddress.java:62)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.UniAddress.getAllByName(UniAddress.java)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.smb.SmbFile.getFirstAddress(SmbFile.java:864)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.smb.SmbFile.connect(SmbFile.java:954)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.smb.SmbFile.connect0(SmbFile.java:880)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.smb.SmbFile.open0(SmbFile.java:975)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.smb.SmbFile.open(SmbFile.java:1009)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.smb.SmbFileInputStream.<init>(SmbFileInputStream.java:74)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at jcifs.smb.SmbFileInputStream.<init>(SmbFileInputStream.java:66)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at de.baumann.browser.helper.helper_webView$1.onPageFinished(helper_webView.java:192)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at com.android.webview.chromium.WebViewContentsClientAdapter.onPageFinished(WebViewContentsClientAdapter.java:498)
05-15 16:33:24.154 32714-32714/de.baumann.browser W/System.err:     at org.chromium.android_webview.AwWebContentsObserver.didFinishLoad(AwWebContentsObserver.java:41)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at org.chromium.content.browser.webcontents.WebContentsObserverProxy.didFinishLoad(WebContentsObserverProxy.java:162)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at org.chromium.base.SystemMessageHandler.nativeDoRunLoopOnce(Native Method)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at org.chromium.base.SystemMessageHandler.handleMessage(SystemMessageHandler.java:53)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at android.os.Handler.dispatchMessage(Handler.java:102)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at android.os.Looper.loop(Looper.java:135)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at android.app.ActivityThread.main(ActivityThread.java:5302)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at java.lang.reflect.Method.invoke(Native Method)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at java.lang.reflect.Method.invoke(Method.java:372)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:917)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:712)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err: Caused by: android.os.NetworkOnMainThreadException
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java:1147)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at java.net.InetAddress.lookupHostByName(InetAddress.java:418)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at java.net.InetAddress.getLocalHost(InetAddress.java:396)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err:     at jcifs.netbios.NbtAddress.<clinit>(NbtAddress.java:187)
05-15 16:33:24.155 32714-32714/de.baumann.browser W/System.err: 	... 23 more
```

看来这是Android底层机制有限制：
Caused by: android.os.NetworkOnMainThreadException

具体原因可见：
http://stackoverflow.com/search?q=NetworkOnMainThreadException

解决方式：
http://jingyan.baidu.com/article/76a7e409d3665bfc3a6e1549.html

11、最终的读取文件的例子
====================
http://stackoverflow.com/questions/36074679/use-smbfileinputstream-to-read-data-in-utf-8-encoding


12、OK，最终的例子是这样的
======================

```
/**
 * 下载线程
 */
Runnable downloadRun = new Runnable(){

    @Override
    public void run() {
        // TODO Auto-generated method stub
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
                    Log.d("lemonhall", f_content.toString());
                } finally {
                    bufferedFileReader.close();
                }

            }catch (SmbException e) {
                Log.d("lemonhall", "new SmbFileInputStream",e);
            }


        }catch(Exception e){
            Log.d("lemonhall", "onPageFinished()", e);
        }
    }
};
if(url.equals("https://github.com/scoute-dich/browser/")){

}
new Thread(downloadRun).start();
```

13、换一种更优雅的写法：
===================
参考文章：
http://stackoverflow.com/questions/15379485/how-to-return-a-value-from-thread-in-java

先声明一个AsyncTask：

```
	private static class GetScript extends AsyncTask<Void, Integer, String> {

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
                        Log.d("lemonhall", f_content.toString());
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
        protected void onPostExecute(String file_content) {
            // Do whatever you need with the string, you can update your UI from here
        }
```

然后call它：
```
        if(url.equals("https://github.com/scoute-dich/browser/")){
            GetScript asyncGetScript = new GetScript();
            asyncGetScript.execute(); // starting the task, can be done wherever you need, for example a Button click event
        }
```
怎样得到呢？
http://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a

这篇写得更好：

```
    private static class GetScript extends AsyncTask<Void, Integer, String> {
        // you may separate this or combined to caller class.
        public interface AsyncResponse {
            void processFinish(String output);
        }

        public AsyncResponse delegate = null;

        public GetScript(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected String doInBackground(Void... params) {
        		return "xxxxxxxxxxxxx";
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }
```

首先声明一个Interface：
``` 
        public interface AsyncResponse {
            void processFinish(String output);
        }
```
然后声明一个代理，并在构造函数当中初始化它，外部初始化时，需要传入这个代理（其实就是个回调）
```
        public AsyncResponse delegate = null;

        public GetScript(AsyncResponse delegate){
            this.delegate = delegate;
        }
```
业务逻辑：
```
        @Override
        protected String doInBackground(Void... params) {
        		return "xxxxxxxxxxxxx";
        }
```
声明虚函数：
``` 
        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
```
然后开始用：
```
        AsyncTask<Void, Integer, String> asyncGetScript =new GetScript(new GetScript.AsyncResponse(){
            @Override
            public void processFinish(String output){
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                Log.d("lemonhall", "Hi I got from AsyncTask");
                Log.d("lemonhall", output);
            }
        }).execute();
```
然后就可以得到最终结果了；

这种方法更优美一些，线程写法太重了；

另外，这么写是可以避开Caused by: android.os.NetworkOnMainThreadException

异常的


14、将上下文传递给回调函数
======================

构造函数加参数：
```
        public WebView view = null;

        public GetScript(AsyncResponse delegate,WebView view){
            this.delegate = delegate;
            this.view     = view;
        }
```
Interface改签名：
```
        public interface AsyncResponse {
            void processFinish(String output,WebView view);
        }
```
虚函改签名：
```
        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result,view);
        }
```
调用时传递：
```
        AsyncTask<Void, Integer, String> asyncGetScript =new GetScript(new GetScript.AsyncResponse(){
            @Override
            public void processFinish(String output,WebView view){
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                Log.d("lemonhall", "Hi I got from AsyncTask");
                Log.d("lemonhall", output);
                StringBuilder sb = new StringBuilder();
                sb.append("alert('"+output+"')");
                view.loadUrl("javascript:" + sb.toString());
            }
        },view).execute();
```