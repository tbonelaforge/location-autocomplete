package com.tford.testlocations;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by tford on 1/1/17.
 */

public class GetLocationsCommand {
    private static final String url = "https://localapi.fitbit.com/1/poopy-profile-locations-with-state.json";

    public static JSONObject execute(OkHttpClient client, String token, String query) {
        Log.d("GetLocationsCommand.execute", "Inside GetLocationsCommand.execute, got called!!!");
        Request request = createRequest(token, query);
        Response response = null;
        JSONObject locations = null;
        try {
            Call newCall = client.newCall(request);
            response = newCall.execute();
            locations = extractLocations(response);
        } catch(IOException e) {
            Log.wtf("GetLocationsCommand.execute", e);
        } catch(Exception e) {
            Log.wtf("GetLocationsCommand.execute", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return locations;
    }

    private static Request createRequest(String token, String query) {
        Request.Builder requestBuilder = new Request.Builder();
        String urlWithQuery = url + String.format("?filter[cityQuery]=%s", query);
        String authHeader = "Bearer " + token;
        requestBuilder.url(urlWithQuery)
                .header("Authorization", authHeader)
                .get();
        Request request = requestBuilder.build();
        return request;
    }

    private static JSONObject extractLocations(Response response) {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new RuntimeException(String.format("Could not get body of response"));
        }
        Log.d("GetLocationsCommand.extractLocations", "The response body looks like" + responseBody.toString());
        String extracted;
        JSONObject parsed;
        try {
            extracted = responseBody.string();
            parsed = new JSONObject(extracted);
        } catch (IOException e) {
            Log.wtf("IOException while trying to stringify the body...", e);
            return new JSONObject();
        } catch (JSONException e) {
            Log.wtf("JSONException while trying to parse body...", e);
            return new JSONObject();

        }
        return parsed;
    }
}
