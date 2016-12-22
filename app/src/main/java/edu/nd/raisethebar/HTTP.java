package edu.nd.raisethebar;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by jack1 on 11/2/2016.
 */

public class HTTP {
    private static final String TAG = "RTB-HTTP";
    enum Method {
        GET(false), POST(true);

        boolean output;

        Method(boolean output) {
            this.output = output;
        }
    }

    public static class AsyncCall extends AsyncTask<Void, Void, String> {
        interface StringRunnable{
            void run(String s);
        }
        URL url;
        HashMap<String, String> parameters;
        Method m;
        private StringRunnable runnable;

        AsyncCall(Method m, URL url, HashMap<String, String> params, StringRunnable runnable) {
            this.m = m;
            this.url = url;
            this.parameters = params;
            this.runnable = runnable;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Uri.Builder builder = new Uri.Builder().scheme(url.getProtocol()).authority(url.getAuthority()).path(url.getPath());//adds path to builder
                for (String key : parameters.keySet()) {
                    builder.appendQueryParameter(key,parameters.get(key));//adds params
                }
                if (!m.output) { //modify root url with params if needed
                    Log.d(TAG, builder.build().toString());
                    url = new URI(builder.build().toString()).toURL();//if params in url, update url
                }
                Log.d(TAG, url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod(m.name());
                conn.setDoInput(true);


                if (m.output) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(builder.build().getEncodedQuery());

                    writer.flush();
                    writer.close();
                    os.close();
                }
                int responseCode = conn.getResponseCode();
                Log.d(TAG,"Response code: "+responseCode);

                StringBuilder result = new StringBuilder();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        Log.d(TAG,"Reading...");
                        String line;
                        while ((line = in.readLine()) != null) {
                            result.append(line);
                            result.append('\n');
                        }
                    } finally {
                        conn.disconnect();
                    }
                }
                Log.d(TAG,result.toString());
                return result.toString();
            } catch (IOException | URISyntaxException e) {
                Log.e(TAG,"HTTP Error",e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            runnable.run(s);
        }
    }
}
