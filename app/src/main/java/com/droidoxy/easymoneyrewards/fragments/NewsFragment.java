package com.droidoxy.easymoneyrewards.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.activities.FragmentsActivity;
import com.droidoxy.easymoneyrewards.adapters.NewsAdapter;
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.model.News;
import com.droidoxy.easymoneyrewards.utils.CustomRequest;
import com.droidoxy.easymoneyrewards.utils.Dialogs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.droidoxy.easymoneyrewards.constants.Constants.APP_NEWS;
import static com.droidoxy.easymoneyrewards.constants.Constants.DEBUG_MODE;

/**
 * Created by DroidOXY
 */
 
public class NewsFragment extends Fragment {

    TextView emptyText;
    ImageView emptyImage;
    RecyclerView news;
    NewsAdapter newsAdapter;
    ArrayList<News> allNews;
    ProgressBar progressBar;
    Context ctx;

    public NewsFragment() {
        // Required empty public constructor
    }

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = getActivity();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_news, container, false);

        emptyText = view.findViewById(R.id.empty);
        emptyImage = view.findViewById(R.id.emptyImage);
        progressBar = view.findViewById(R.id.progressBar);

        news = view.findViewById(R.id.news);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ctx);
        news.setLayoutManager(layoutManager);
        news.setItemAnimator(new DefaultItemAnimator());

        allNews = new ArrayList<>();

        newsAdapter = new NewsAdapter(ctx, allNews);
        news.setAdapter(newsAdapter);

        CustomRequest transactionsRequest = new CustomRequest(Request.Method.POST, APP_NEWS,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if (!Response.getBoolean("error")) {

                                JSONArray news = Response.getJSONArray("news");

                                for (int i = 0; i < news.length(); i++) {

                                    JSONObject obj = news.getJSONObject(i);

                                    News singleNewsItem = new News();

                                    singleNewsItem.setNewsId(obj.getString("news_id"));
                                    singleNewsItem.setTitle(obj.getString("news_title"));
                                    singleNewsItem.setContents(obj.getString("news_content"));
                                    singleNewsItem.setAmount(obj.getString("news_amount"));
                                    singleNewsItem.setAmountPremium(obj.getString("news_amount_premium"));
                                    singleNewsItem.setImage(obj.getString("news_image"));
                                    singleNewsItem.setStatus(obj.getString("news_status"));

                                    if(obj.get("news_status").equals("Active") && !App.getInstance().get("APPNEWS_"+ obj.getString("news_id"),false)){
                                        allNews.add(singleNewsItem);
                                        progressBar.setVisibility(View.GONE);
                                    }

                                }

                                newsAdapter.notifyDataSetChanged();

                                checkHaveNews();

                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(ctx,Response.getInt("error_code"));

                            }else{

                                if(!DEBUG_MODE){
                                    Dialogs.serverError(ctx, getResources().getString(R.string.ok), new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            finish();
                                        }
                                    });
                                }

                            }


                        }catch (Exception e){

                            if(!DEBUG_MODE){
                                Dialogs.serverError(ctx, getResources().getString(R.string.ok), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        finish();
                                    }
                                });
                            }else{
                                Dialogs.errorDialog(ctx,"Got Error",e.toString() + ", please contact developer immediately",true,false,"","ok",null);
                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(!DEBUG_MODE){
                    Dialogs.serverError(ctx, getResources().getString(R.string.ok), new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            finish();
                        }
                    });
                }else{
                    Dialogs.errorDialog(ctx,"Got Error",error.toString(),true,false,"","ok",null);
                }

            }});

        App.getInstance().addToRequestQueue(transactionsRequest);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    void checkHaveNews(){

        if(progressBar.getVisibility() == View.VISIBLE){
            emptyText.setVisibility(View.VISIBLE);
            emptyImage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    void finish(){

        Activity close = getActivity();
        if(close instanceof FragmentsActivity){
            FragmentsActivity show = (FragmentsActivity) close;
            show.closeActivity();
        }

    }

}