package com.droidoxy.easymoneyrewards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.utils.AppUtils;
import com.droidoxy.easymoneyrewards.utils.CustomRequest;
import com.droidoxy.easymoneyrewards.utils.Dialogs;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NewsPlayerActivity extends ActivityBase implements RewardedVideoAdListener {
    public static final String EXTRA_NEWS_ID = "news_id";
    public static final String EXTRA_NEWS_CONTENTS = "news_contents";
    public static final String EXTRA_NEWS_IMAGE = "news_image";
    public static final String EXTRA_REWARDS = "news_amount";
    public static final String EXTRA_NEWS_TITLE = "news_title";

    String imageUrl = "";

    private RewardedVideoAd mAd;
    private InterstitialAd interstitial;
    private boolean isWatched = false;

    private Button btnClaim;
    private Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_player);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_NEWS_TITLE));

        imageUrl = getIntent().getStringExtra(EXTRA_NEWS_IMAGE);
        initVIew();
        init_admob();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void initVIew() {
        ImageView newsImage = findViewById(R.id.news_image);
        Glide.with(this).load(imageUrl).into(newsImage);
        TextView newsTitle = findViewById(R.id.news_title);
        newsTitle.setText(getIntent().getStringExtra(EXTRA_NEWS_TITLE));
        TextView newsContent = findViewById(R.id.news_content);
        newsContent.setText(getIntent().getStringExtra(EXTRA_NEWS_CONTENTS));
        btnClaim = findViewById(R.id.btn_claim);
        btnClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                awardNews(getIntent().getStringExtra(EXTRA_REWARDS), getIntent().getStringExtra(EXTRA_NEWS_ID));
                displayInterstitial();
            }
        });
    }

    void init_admob(){

        MobileAds.initialize(NewsPlayerActivity.this, getString(R.string.admob_appId));

        // ADMOB Banner
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        // ADMOB VIDEO
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        // ADMOB Interstitial
        interstitial = new InterstitialAd(NewsPlayerActivity.this);
        interstitial.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                if (interstitial.isLoaded()) {
                    interstitial.show();
                }
            }
        });
    }

    public void displayInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        interstitial.loadAd(adRequest);
    }

    private void loadRewardedVideoAd() {
        if (!mAd.isLoaded()) {
            mAd.loadAd(getResources().getString(R.string.admob_videos), new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {
        AppUtils.toastShort(NewsPlayerActivity.this, getResources().getString(R.string.watch_till_end));
    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        award(Integer.parseInt(App.getInstance().get("AdmobVideoCredit_Amount","1")),App.getInstance().get("AdmobVideoCredit_Title",""));
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    void award(final int Points, final String CreditType){

        CustomRequest rewardRequest = new CustomRequest(Request.Method.POST, ACCOUNT_REWARD,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == ERROR_SUCCESS){

                                // User Rewarded Successfully
                                AppUtils.toastShort(NewsPlayerActivity.this,Points + " " + getResources().getString(R.string.app_currency) + " " + getResources().getString(R.string.successfull_received));
                                updateBalanceInBg();

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(NewsPlayerActivity.this,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(NewsPlayerActivity.this,getResources().getString(R.string.msg_server_problem));
                            }

                        }catch (Exception e){

                            if(DEBUG_MODE){
                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(NewsPlayerActivity.this,"Got Error",e.toString() + ", please contact developer immediately",false,false,"","ok",null);

                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(DEBUG_MODE){
                    // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                    Dialogs.errorDialog(NewsPlayerActivity.this,"Got Error",error.toString(),true,false,"","ok",null);
                }

            }}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("data", App.getInstance().getDataCustom(Integer.toString(Points),CreditType));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(rewardRequest);

    }

    void updateBalanceInBg() {

        CustomRequest balanceRequest = new CustomRequest(Request.Method.POST, ACCOUNT_BALANCE,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            if(!response.getBoolean("error")){

//                                setOptionTitle(getString(R.string.app_currency).toUpperCase()+" : " +response.getString("user_balance"));
                                App.getInstance().store("balance",response.getString("user_balance"));

                            }else if(response.getInt("error_code") == 699 || response.getInt("error_code") == 999){

                                Dialogs.validationError(NewsPlayerActivity.this,response.getInt("error_code"));

                            }else if(response.getInt("error_code") == 799) {

                                Dialogs.warningDialog(NewsPlayerActivity.this, getResources().getString(R.string.update_app), getResources().getString(R.string.update_app_description), false, false, "", getResources().getString(R.string.update), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        AppUtils.gotoMarket(NewsPlayerActivity.this);
                                    }
                                });

                            }

                        }catch (Exception e){
                            // do nothin
                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("data", App.getInstance().getData());
                return params;
            }
        };

        App.getInstance().addToRequestQueue(balanceRequest);
    }

    private void completeWatch() {
        Intent intent = new Intent();
        intent.putExtra("points", getIntent().getStringExtra(EXTRA_REWARDS));
        intent.putExtra("id", getIntent().getStringExtra(EXTRA_NEWS_ID));
        setResult(RESULT_OK, intent);
        finish();
    }

    void awardNews(final String Points,final String newsId){

        CustomRequest videoRewardRequest = new CustomRequest(Request.Method.POST, APP_NEWSSTATUS,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == ERROR_SUCCESS){

                                // Video saved Success
                                App.getInstance().store("APPNEWS_"+newsId,true);
                                AppUtils.toastShort(NewsPlayerActivity.this,Points+ " " + getResources().getString(R.string.app_currency) + " " + getResources().getString(R.string.successfull_received));

                            }else if(Response.getInt("error_code") == 420) {

                                // 420 - Video watched Already
                                AppUtils.toastShort(NewsPlayerActivity.this,getResources().getString(R.string.already_read));
                                App.getInstance().store("APPNEWS_"+newsId,true);

                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(NewsPlayerActivity.this,Response.getInt("error_code"));

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(NewsPlayerActivity.this,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(NewsPlayerActivity.this,getResources().getString(R.string.msg_server_problem));
                            }

                        }catch (Exception e){

                            if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(NewsPlayerActivity.this,"Got Error",e.toString() + ", please contact developer immediately",false,false,"","ok",null);

                            }else{

                                // Server error
                                AppUtils.toastShort(NewsPlayerActivity.this,getResources().getString(R.string.msg_server_problem));
                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(DEBUG_MODE){

                    // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                    Dialogs.errorDialog(NewsPlayerActivity.this,"Got Error",error.toString(),true,false,"","ok",null);

                }else{

                    // Server error
                    AppUtils.toastShort(NewsPlayerActivity.this,getResources().getString(R.string.msg_server_problem));
                }

            }}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("data", App.getInstance().getDataCustom(newsId,Points));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(videoRewardRequest);

    }

}
