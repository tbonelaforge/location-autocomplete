package com.tford.testlocations;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by tford on 12/30/16.
 */

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static OkHttpClient insecureClient;
    private static String requestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) findViewById(R.id.locations_autocomplete);
        autoComplete.setAdapter(new LocationsAutocompleteAdapter(this, R.layout.location_autocomplete_item));
        autoComplete.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String locationString = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, locationString, Toast.LENGTH_SHORT).show();
    }

    public static ArrayList autocomplete(String input) {
        ArrayList suggestions = new ArrayList<String>();
        JSONObject locationsJSONObject = GetLocationsCommand.execute(getInsecureClient(), requestToken, input);
        try {
            JSONArray locationsData = locationsJSONObject.getJSONArray("data");
            for (int i = 0; i < locationsData.length(); i++) {
                JSONObject datum = locationsData.getJSONObject(i);
                JSONObject locationAttributes = datum.getJSONObject("attributes");
                String city = locationAttributes.getString("city");
                String state = locationAttributes.getString("state");
                String locationSuggestion = String.format("%s, %s", city, state);
                suggestions.add(locationSuggestion);
            }
        } catch(JSONException e) {
            System.out.println("Error processing JSON results:");
            System.out.println(e);
        }
        return suggestions;
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

    private class LocationsAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> suggestionList;

        public LocationsAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return suggestionList.size();
        }

        @Override
        public String getItem(int index) {
            return suggestionList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        suggestionList = autocomplete(constraint.toString());
                        System.out.printf("INSIDE LocationsAutocompleteAdapter, got %d suggestions %n", suggestionList.size());
                        filterResults.values = suggestionList;
                        filterResults.count = suggestionList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
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
            JSONObject locations = GetLocationsCommand.execute(getInsecureClient(), getToken(), "Fruitvale");
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

    private static OkHttpClient getInsecureClient() {
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
