package com.santosh.netflix;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.SpinKitView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public List<Movie> mItemList;
    Context context;


    public RecyclerViewAdapter(List<Movie> itemList, Context context) {

        mItemList = itemList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ItemViewHolder) {

            populateItemRows((ItemViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }

    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }

    private void populateItemRows(final ItemViewHolder viewHolder, final int position) {

        Movie item = mItemList.get(position);
        ImageView image = viewHolder.movieImage;

        if (item.getImageUrl() != null) {
            Picasso.with(context)
                    .load(item.getImageUrl())
                    .centerCrop()
                    .transform(new CircleTransform(50, 0))
                    .fit()
                    .into(image);
        }
        viewHolder.tvItem.setText(item.movieName);
        viewHolder.showLoading.setVisibility(View.GONE);
        viewHolder.checkCountries.setVisibility(View.VISIBLE);
        viewHolder.checkCountries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Intent intent = new Intent(context, MovieDetailsActivity.class);
                intent.putExtra("urlName", mItemList.get(position).movieLink);
                RequestQueue queue = Volley.newRequestQueue(context);
                String url = mItemList.get(position).movieLink;
                Log.d("apicall", "loadData: " + url);
                viewHolder.showLoading.setVisibility(View.VISIBLE);
                viewHolder.checkCountries.setVisibility(View.GONE);

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
                                                    JSONArray jsonArray = jsonObject.getJSONObject("audience").getJSONArray("geographicArea");

                                                    String countries = "";
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        countries = countries + jsonArray.getJSONObject(i).getString("name") + ", ";
                                                    }
                                                    viewHolder.showLoading.setVisibility(View.GONE);
                                                    viewHolder.checkCountries.setVisibility(View.VISIBLE);
                                                    viewHolder.checkCountries.setText(countries.substring(0, countries.length() - 2) + ".");

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
        });

    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvItem;
        ImageView movieImage;
        CardView parent;
        TextView checkCountries;
        SpinKitView showLoading;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.parent);
            movieImage = itemView.findViewById(R.id.movie_image);
            tvItem = itemView.findViewById(R.id.tvItem);
            checkCountries = itemView.findViewById(R.id.check_country);
            showLoading = itemView.findViewById(R.id.show_loading);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }


}