package tomer.spivak.androidstudio2dgame.home;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
public class EmailSender {


    public static void sendEmail(String toEmail, Context context) {
        Log.d("Email", "wtf");
        String url = "https://api.sendgrid.com/v3/mail/send";
        String apiKey = "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"; // Replace with your API key

        // Construct the email data in JSON format
        JSONObject emailData = new JSONObject();
        try {
            emailData.put("personalizations", new JSONArray()
                    .put(new JSONObject().put("to", new JSONArray()
                            .put(new JSONObject().put("email", toEmail))
                    )));
            emailData.put("from", new JSONObject().put("email", "spivak.toti@gmail.com"));
            emailData.put("subject", "Welcome to Our App!");
            emailData.put("content", new JSONArray()
                    .put(new JSONObject().put("type", "text/plain")
                            .put("value", "Hello! Welcome to our app. We're excited to have you!")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create and send the request using Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, emailData,
                response -> Log.d("Email", "Email sent successfully"),
                error -> Log.e("Email", "Failed to send email", error)) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + apiKey);
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Convert response data to String
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    // If the response is empty and the status code is 202, consider it a success.
                    if (jsonString.isEmpty() && response.statusCode == 202) {
                        Log.d("Email", "Empty response received, but status is 202 Accepted.");
                        // Return an empty JSONObject to satisfy the response.
                        return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
                    }
                    // Otherwise, try to parse the response normally.
                    return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        // Add the request to the Volley request queue
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
