package com.imt.musiclamp;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by dtw on 15/5/26.
 */
public class IMTInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imtinfolayout);
//        InputStream inputStream = getResources().openRawResource(R.raw.info);
//        InputStreamReader inputStreamReader = null;
//
//        inputStreamReader = new InputStreamReader(inputStream);
//        BufferedReader reader = new BufferedReader(inputStreamReader);
//        StringBuffer sb = new StringBuffer("");
//        String line;
//        try {
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//                sb.append("\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        ((TextView) findViewById(R.id.imtinfo)).setText(sb);
//        ((TextView) findViewById(R.id.imtinfo)).setMovementMethod(new ScrollingMovementMethod());

        WebView webView=(WebView)findViewById(R.id.webView);
        webView.loadUrl("http://112.74.105.77:8080/phone/servicePage.htm");

    }
}
