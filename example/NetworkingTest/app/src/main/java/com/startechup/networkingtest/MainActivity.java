package com.startechup.networkingtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.startechup.tools.http.NetworkingValley;
import com.startechup.tools.http.OnAPIListener;

public class MainActivity extends AppCompatActivity implements OnAPIListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new NetworkingValley.Builder(this).build();

        String url = "https://api.ipify.org/?format=json";
        StringRequest stringRequest = NetworkingValley.constructGetRequest(url, this);
        NetworkingValley.addRequestQueue(stringRequest);
    }

    @Override
    public void onSuccess(String response) {
        Log.d("Response", "onSuccess = " + response);
    }

    @Override
    public void onFail(String response) {
        Log.d("Response", "onFail = " + response);
    }
}
