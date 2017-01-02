package com.tford.testlocations;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by tford on 12/30/16.
 */

public class MainActivity extends AppCompatActivity {
    private OkHttpClient insecureClient;
    private String requestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getToken(View view) {
        System.out.printf("Inside MainActivity.getToken, got called...");
        System.out.printf("Here is where we need to make a request for a token!!!");
        Log.d("MainActivity.getToken", "Got called...Here is where we should make a request for the token...");
        GetTokenTask getTokenTask = new GetTokenTask();
        getTokenTask.execute();
    }

    public void getLocations(View view) {
        System.out.printf("Inside MainActivity.getLocations, got called...");
        GetLocationsTask getLocationsTask = new GetLocationsTask(requestToken);
        getLocationsTask.execute();
    }

    private class GetTokenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            Log.d("MainActivity.GetTokenTask.doInBackground", "Inside GetTokenTask.doInBackground, got called!!!");
            String token = GetTokenCommand.execute(getInsecureClient());
            return token;
        }

        @Override
        protected void onPostExecute(String token) {
            if (token != null) {
                requestToken = token;
                TextView tokenResponseTextView = (TextView) findViewById(R.id.token_response);
                tokenResponseTextView.setText(requestToken);
            }
        }
    }

    private class GetLocationsTask extends AsyncTask<Void, Void, JSONObject> {
        private String token;

        public GetLocationsTask(String token) {
            this.token = token;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            Log.d("MainActivity.GetLocationsTask.doInBackground", "Inside GetLocationsTask.doInBackground, got called!!!");
            JSONObject locations = GetLocationsCommand.execute(getInsecureClient(), getToken());
            return locations;
        }

        @Override
        protected void onPostExecute(JSONObject locations) {
            if (locations != null) {
                TextView locationsTextView = (TextView) findViewById(R.id.locations_response);
                locationsTextView.setText(locations.toString());
            }
        }

        public String getToken() {
            return token;
        }
    }

    private OkHttpClient getInsecureClient() {
        if (insecureClient != null) {
            return insecureClient;
        }

        X509TrustManager[] insecureTrustManagers = new X509TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };
        SSLSocketFactory insecureSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, insecureTrustManagers, null);
            insecureSocketFactory = sslContext.getSocketFactory();
        } catch (KeyManagementException e) {
            Log.wtf("help key management", e);
        } catch (Exception e) {
            Log.wtf("HELP SOME OTHER EXCEPTION!!!", e);
        }

        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();
        clientBuilder.sslSocketFactory(insecureSocketFactory, insecureTrustManagers[0]);
        insecureClient = clientBuilder.build();
        return insecureClient;
    }
}
