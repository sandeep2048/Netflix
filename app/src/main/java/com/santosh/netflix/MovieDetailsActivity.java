package com.santosh.netflix;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MovieDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        String url = getIntent().getStringExtra("urlName");
        RequestQueue queue = Volley.newRequestQueue(this);

        Log.d("apicall", "loadData: " + url);

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Document doc = Jsoup.parse(response);
                        Elements scriptElements = doc.getElementsByTag("script");

                        for (Element element : scriptElements) {
                            for (DataNode node : element.dataNodes()) {
                                if (node.getWholeData().contains("https://schema.org")) {

                                    System.out.println(node.getWholeData());
                                    try {
                                        System.out.println("-------------------");
                                        JSONObject jsonObject = new JSONObject(node.getWholeData());
                                        if (jsonObject.has("@type")) {
                                            jsonObject.toString();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //need to implement error
            }
        });

        queue.add(stringRequest);


    }
}
