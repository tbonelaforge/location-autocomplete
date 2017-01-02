package com.tford.testlocations;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by tford on 12/30/16.
 */

public class GetTokenCommand {
    private static final String url = "https://localapi.fitbit.com/oauth2/token";
    private static final String auth = "Basic MjI4VFNMOjYxNzlrb3B5b3hkcTFzYngyeGpxa3Uycnc2bnhtajg1";

    public static String execute(OkHttpClient client) {
        Log.d("GetTokenCommand.execute", "Inside GetTokenCommand.execute, got called!!!");
        Request request = createRequest();
        Response response = null;
        String token = null;
        try {
            Call newCall = client.newCall(request);
            response = newCall.execute();
            token = extractToken(response);
        } catch(IOException e) {
            Log.wtf("GetTokenCommand.execute", e);
        } catch(Exception e) {
            Log.wtf("GetTokenCommand.execute", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return token;
    }

    private static Request createRequest() {
        Request.Builder requestBuilder = new Request.Builder();
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        RequestBody formBody = formBodyBuilder
                .add("grant_type", "password")
                .add("username", "ttf0809@gmail.com")
                .add("password", "Fu8a$hank")
                .build();
        requestBuilder.url(url)
                .header("Authorization", auth)
                /*
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        "{grant_type:\"password\", email:\"ttf0809@gmail.com\", password:\"Fu8a$hank\"}"
                ));
                */
                //.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "grant_type=password&email=ttf08"));
                .post(formBody);
        Request request = requestBuilder.build();
        return request;
    }

    private static String extractToken(Response response) {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new RuntimeException(String.format("Could not get body of response"));
        }
        Log.d("GetTokenCommand.extractToken", "The response body looks like" + responseBody.toString());
        //return "poopytoken";
        String extracted;
        JSONObject parsed;
        String token;
        try {
            extracted = responseBody.string();
            parsed = new JSONObject(extracted);
            token = parsed.getString("access_token");
        } catch (IOException e) {
            Log.wtf("IOException while trying to stringify the body...", e);
            return "poopytokenresponse";
        } catch (JSONException e) {
            Log.wtf("JSONException while trying to parse body...", e);
            return "poopytokenjson";
        }
        return token;
    }
}
