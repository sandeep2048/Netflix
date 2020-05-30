package com.santosh.netflix;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<Movie> rowsArrayList = new ArrayList<>();

    boolean isLoading = false;
    LinearLayout parentView;
    SpinKitView spinKitView;
    int count = 1;

    void showLoading() {
        recyclerView.setVisibility(View.GONE);
        spinKitView.setVisibility(View.VISIBLE);
    }

    void hidLoading() {
        recyclerView.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinKitView = findViewById(R.id.loading);
        parentView = findViewById(R.id.parent_view);
        final ConstraintLayout constraintLayout = findViewById(R.id.id);


        final EditText editText = findViewById(R.id.search_text);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rowsArrayList.clear();
                if ((Util.isNetworkConnectionAvailable(MainActivity.this))) {
                    showLoading();
                    populateData(s.toString());
                    initAdapter();
                    initScrollListener();
                } else {
                    Toast.makeText(MainActivity.this, " Internet Not Avaiable",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        };
        editText.addTextChangedListener(textWatcher);
        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(MainActivity.this, R.layout.activity_main_animation);

                ChangeBounds transition = new ChangeBounds();
                transition.setInterpolator(new AnticipateInterpolator(1.0f));
                transition.setDuration(1200);


                transition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {

                        findViewById(R.id.search).setVisibility(View.GONE);
                        findViewById(R.id.info).setVisibility(View.GONE);
                        editText.setVisibility(View.VISIBLE);
                        editText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
                TransitionManager.beginDelayedTransition(constraintLayout, transition);
                constraintSet.applyTo(constraintLayout);


            }
        });


        recyclerView = findViewById(R.id.recyclerView);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); //hide the title bar


    }

    private void populateData(String string) {

        loadData(1, string.toLowerCase());
    }

    void loadData(int page, String searchString) {
        final int oldSize = rowsArrayList.size();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.flixwatch.co/?_page=" + page + "&s=" + searchString;
        Log.d("apicall", "loadData: " + url);

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        hidLoading();
                        Document doc = Jsoup.parse(response);
                        doc.body();
                        Elements links = doc.select("a[href]");

                        for (Element link : links) {


                            System.out.println("link : " + link.attr("href"));

                            if (link.attr("class").contains("_blank pt-cv-href-thumbnail")) {
                                Movie movie = new Movie();
                                movie.setMovieName(link.childNode(0).attr("alt"));
                                movie.setMovieLink(link.attr("href"));
                                movie.setImageUrl(link.childNode(0).attr("data-cvpsrc"));

                                rowsArrayList.add(movie);
                            }
                            System.out.println("text : " + link.text());
                            System.out.println("class : " + link.attr("class"));

                            if (link.attr("class").contains("_blank pt-cv-href")) {
                                System.out.println("class : " + link.attr("class"));
                            }
                        }
                        int newSize = rowsArrayList.size();
                        if (oldSize == newSize) {
                            isLoading = true;
                        } else {
                            recyclerViewAdapter.notifyDataSetChanged();
                            isLoading = false;
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

    private void initAdapter() {

        recyclerViewAdapter = new RecyclerViewAdapter(rowsArrayList, this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size() - 1) {
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });


    }

    private void loadMore() {
        rowsArrayList.add(null);
        recyclerViewAdapter.notifyItemInserted(rowsArrayList.size() - 1);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rowsArrayList.remove(rowsArrayList.size() - 1);
                int scrollPosition = rowsArrayList.size();
                recyclerViewAdapter.notifyItemRemoved(scrollPosition);
                int currentSize = scrollPosition;
                int nextLimit = currentSize + 10;

//                while (currentSize - 1 < nextLimit) {
//                    rowsArrayList.add("Item " + currentSize);
//                    currentSize++;
//                }
                count++;
                loadData(count, "after");
                //     recyclerViewAdapter.notifyDataSetChanged();

            }
        }, 2000);


    }
}

