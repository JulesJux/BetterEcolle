package fr.jux.bettercolloscope;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    Context context;
    ProgressBar progressBar;
    Button login_button;
    EditText username_text;
    EditText password_text;
    private String username;
    private String password;
    private String reponse;
    private static final String url = "https://e-colle.supwallon.fr/app_mobile/connect";
    private static final String TAG1 = "connexion";
    private static final String TAG2 = "get_headers";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        login_button = findViewById(R.id.connection);
        username_text = findViewById(R.id.username);
        password_text = findViewById(R.id.password);
        progressBar = findViewById(R.id.loading_indicator_login);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = username_text.getText().toString().trim();
                password = password_text.getText().toString();
                try {
                    connexion_e_colle(username, password);
                } catch (AuthFailureError e) {
                    throw new RuntimeException(e);
                }
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    // /agendaprograms ; /grades ; /results ; /colles ; /check ;
    public void connexion_e_colle(String username, String password) throws AuthFailureError {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(1, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.INVISIBLE);
                reponse = response;
                Intent resultIntent = new Intent();
                resultIntent.putExtra("reponse", reponse);
                // Toast.makeText(c, "Response :" + reponse, Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG1, "Error :" + error.toString());
                Toast.makeText(context, "Erreur lors de la connexion à e-colle", Toast.LENGTH_LONG).show();
            }
        }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("username", username);
                headers.put("password", password);
                return headers;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                assert response.headers != null;
                Log.i("response", response.headers.toString());
                Map<String, String> responseHeaders = response.headers;
                String rawCookies = responseHeaders.get("Set-Cookie");
                assert rawCookies != null;
                Log.i("cookies", rawCookies);
                return super.parseNetworkResponse(response);
            }

        };
        stringRequest.setTag(TAG1);
        requestQueue.add(stringRequest);
    }

}

