package com.droidoxy.easymoneyrewards.activities;

// import statements
import java.util.Map;
import java.util.Timer;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.os.Bundle;
import java.util.HashMap;
import android.os.Handler;
import java.util.TimerTask;

import android.view.MenuItem;
import android.content.Intent;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

// External import statements
import android.app.AlertDialog;

import cn.pedant.SweetAlert.SweetAlertDialog;
import dmax.dialog.SpotsDialog;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

// Google import statements
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.views.CountDownTimerView;
import com.droidoxy.easymoneyrewards.utils.Dialogs;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;

// import statements
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.utils.AppUtils;
import com.droidoxy.easymoneyrewards.utils.CustomRequest;
import com.droidoxy.easymoneyrewards.utils.UtilsMiscellaneous;
import com.droidoxy.easymoneyrewards.utils.SlidingTabLayout;
import com.droidoxy.easymoneyrewards.adapters.ViewPagerAdapter;
import com.droidoxy.easymoneyrewards.views.ScrimInsetsFrameLayout;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.startapp.android.publish.adsCommon.Ad;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;
import com.startapp.android.publish.adsCommon.VideoListener;
import com.startapp.android.publish.adsCommon.adListeners.AdEventListener;

import org.json.JSONObject;


/**
 * Created by DroidOXY
 */

public class MainActivity extends ActivityBase implements RewardedVideoAdListener {

    // View Variables
    private  Menu menu;
    ViewPagerAdapter adapter;
    MainActivity context;
    private RewardedVideoAd mAd;
    ProgressDialog progressDialog ;
    private InterstitialAd interstitial;
    public boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        init_v3();

    }

    void init_admob(){

        MobileAds.initialize(context, getString(R.string.admob_appId));

        // ADMOB Banner
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        // ADMOB VIDEO
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        // ADMOB Interstitial
        interstitial = new InterstitialAd(context);
        interstitial.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitial.loadAd(adRequest);

        final Timer AdTimer = new Timer();
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                AdTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayInterstitial();
                            }
                        });
                    }
                }, Integer.parseInt(getString(R.string.admob_interstitial_delay)));
            }
        });
    }


    private void loadRewardedVideoAd() {
        if (!mAd.isLoaded()) {
            mAd.loadAd(getResources().getString(R.string.admob_videos), new AdRequest.Builder().build());
        }
    }@Override
    public void onRewarded(RewardItem reward) {
        award(Integer.parseInt(App.getInstance().get("AdmobVideoCredit_Amount","1")),App.getInstance().get("AdmobVideoCredit_Title",""));
    }

    @Override
    public void onRewardedVideoCompleted() {
        // Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        // Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        //Toast.makeText(this, "Watch More Videos To Get More Points", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        // Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
    }

    @Override
    public void onRewardedVideoAdOpened() {
        //   Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        AppUtils.toastShort(context,getResources().getString(R.string.watch_till_end));
    }

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    void init_v3(){

        initViews();
        initNavDrawer();

        init_startapp();

        // initializing Admob
        init_admob();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDisplay();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    // Main fuctions
    void openInstructions(){
        Intent transactions = new Intent(context, FragmentsActivity.class);
        transactions.putExtra("show","instructions");
        startActivity(transactions);
    }

    void openRefer(){
        Intent transactions = new Intent(context, FragmentsActivity.class);
        transactions.putExtra("show","refer");
        startActivityForResult(transactions,1);
    }

    void openAbout(){
        startActivity(new Intent(context, AboutActivity.class));
    }

    void openTransactions(){

        Intent transactions = new Intent(context, FragmentsActivity.class);
        transactions.putExtra("show","transactions");
        startActivity(transactions);
    }

    void openRedeem(){
        Intent redeem = new Intent(context, FragmentsActivity.class);
        redeem.putExtra("show","redeem");
        startActivityForResult(redeem,1);
    }

    public void dailyCheckin(String Title, String Message){

        if(App.getInstance().get("NEWINSTALL",true)){

            hidepDialog();
            Dialogs.normalDialog(context, Title, Message, false, true, getResources().getString(R.string.cancel), getResources().getString(R.string.proceed), new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    App.getInstance().store("NEWINSTALL",false);
                    dailyChekinReward();
                }
            });

        }else{
            hidepDialog();
            dailyChekinReward();
        }

    }

    // AdNetworks
    void openAdmobVideo(){
        if (mAd.isLoaded()) {
            mAd.show();
        }else{
            AppUtils.toastShort(context,getString(R.string.no_videos_available));
            loadRewardedVideoAd();
        }
    }

    void openRewardedVideos(){

        Intent webvids = new Intent(context, FragmentsActivity.class);
        webvids.putExtra("show","webvids");
        startActivityForResult(webvids,1);

    }

    public void openStartAppVideo(){

        if(App.getInstance().get("StartAppActive",true)){

            showLoadingVideo();

            final StartAppAd rewardedVideo = new StartAppAd(this);
            rewardedVideo.setVideoListener(new VideoListener() {

                @Override
                public void onVideoCompleted() {

                    award(Integer.parseInt(App.getInstance().get("StartAppVideoCredit_Amount","1")),App.getInstance().get("StartAppVideoCredit_Title",""));

                }
            });

            rewardedVideo.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, new AdEventListener() {

                @Override
                public void onReceiveAd(Ad arg0) {

                    progressDialog.dismiss();

                    rewardedVideo.showAd();
                }

                @Override
                public void onFailedToReceiveAd(Ad arg0) {

                    progressDialog.dismiss();
                    AppUtils.toastShort(context,getString(R.string.no_videos_available));

                }
            });

        }else{ adnetworkdisabled("StartAppVideoAds"); }

    }

    void parseURL(String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        context.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.points:

                openRedeem();

                return true;

            case R.id.sync:
                updateBalance();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setOptionTitle(String title){
        MenuItem item = menu.findItem(R.id.points);
        item.setTitle(title);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.points).setTitle(getString(R.string.app_currency).toUpperCase()+" : " + App.getInstance().getBalance());
        return super.onPrepareOptionsMenu(menu);
    }

    void updateBalance() {
        final AlertDialog updating = new SpotsDialog(context, R.style.Custom);
        updating.show();
        updateBalanceInBg();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updating.dismiss();
            }
        }, 1000);
    }

    void updateBalanceInBg() {

        CustomRequest balanceRequest = new CustomRequest(Request.Method.POST, ACCOUNT_BALANCE,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            if(!response.getBoolean("error")){

                                setOptionTitle(getString(R.string.app_currency).toUpperCase()+" : " +response.getString("user_balance"));
                                App.getInstance().store("balance",response.getString("user_balance"));

                            }else if(response.getInt("error_code") == 699 || response.getInt("error_code") == 999){

                                Dialogs.validationError(context,response.getInt("error_code"));

                            }else if(response.getInt("error_code") == 799) {

                                Dialogs.warningDialog(context, getResources().getString(R.string.update_app), getResources().getString(R.string.update_app_description), false, false, "", getResources().getString(R.string.update), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        AppUtils.gotoMarket(context);
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

    // Linked Functions
    void dailyChekinReward(){

        showpDialog();

        CustomRequest dailyCheckinRequest = new CustomRequest(Request.Method.POST, ACCOUNT_CHECKIN,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        hidepDialog();

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == ERROR_SUCCESS){

                                // Reward received Succesfully
                                Dialogs.succesDialog(context,getResources().getString(R.string.congratulations),App.getInstance().get("DAILY_REWARD","") + " " + getResources().getString(R.string.app_currency) + " " + getResources().getString(R.string.successfull_received),false,false,"",getResources().getString(R.string.ok),null);
                                updateBalanceInBg();

                            }else if(Response.getInt("error_code") == 410){

                                // Reward Taken Today - Try Again Tomorrow
                                showTimerDialog(Response.getInt("error_description"));

                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(context,Response.getInt("error_code"));

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(context,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                Dialogs.serverError(context, getResources().getString(R.string.ok), null);

                            }

                        }catch (Exception e){

                            if(!DEBUG_MODE){
                                Dialogs.serverError(context, getResources().getString(R.string.ok), null);
                            }else{
                                Dialogs.errorDialog(context,"Got Error",e.toString() + ", please contact developer immediately",false,false,"",getResources().getString(R.string.ok),null);
                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                hidepDialog();

                if(!DEBUG_MODE){
                    Dialogs.serverError(context, getResources().getString(R.string.ok), null);
                }else{
                    Dialogs.errorDialog(context,"Got Error",error.toString(),true,false,"",getResources().getString(R.string.ok),null);
                }

            }}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("data", App.getInstance().getData());
                return params;
            }
        };

        App.getInstance().addToRequestQueue(dailyCheckinRequest);

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
                                AppUtils.toastShort(context,Points + " " + getResources().getString(R.string.app_currency) + " " + getResources().getString(R.string.successfull_received));
                                updateBalanceInBg();

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(context,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(context,getResources().getString(R.string.msg_server_problem));
                            }

                        }catch (Exception e){

                            if(DEBUG_MODE){
                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(context,"Got Error",e.toString() + ", please contact developer immediately",false,false,"","ok",null);

                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(DEBUG_MODE){
                    // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                    Dialogs.errorDialog(context,"Got Error",error.toString(),true,false,"","ok",null);
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


    public void openOfferWall(String Title, String SubTitle, String Type){

        switch (Type) {

            case "checkin":

                showpDialog();
                dailyCheckin(Title, SubTitle);

                break;

            case "redeem":

                openRedeem();

                break;

            case "refer":

                openRefer();

                break;

            case "about":

                openAbout();

                break;

            case "instructions":

                openInstructions();

                break;

            case "transactions":

                openTransactions();

                break;

            case "share":

                AppUtils.shareApplication(context);

                break;

            case "rate":

                AppUtils.gotoMarket(context);

                break;

            case "webvids":

                openRewardedVideos();

                break;

            case "startapp":

                openStartAppVideo();

                break;

            case "admobvideo":

                openAdmobVideo();

                break;

            default:

                parseURL(Type);

                break;
        }
    }

    void showTimerDialog(int TimeLeft){

        CountDownTimerView timerView = new CountDownTimerView(context);
        timerView.setTextSize(getResources().getInteger(R.integer.daily_checkin_timer_size));
        timerView.setPadding(0,0,0,25);
        timerView.setGravity(Gravity.CENTER);
        timerView.setTime(TimeLeft * 1000);
        timerView.startCountDown();
        Dialogs.customDialog(context, timerView,getResources().getString(R.string.daily_reward_taken),false,false,"",getResources().getString(R.string.ok),null);

    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) { finish(); return; }
        context.doubleBackToExitPressedOnce = true;

        AppUtils.toastShort(context,getString(R.string.click_back_again));

        new Handler().postDelayed(new Runnable() { @Override public void run() { doubleBackToExitPressedOnce = false; }}, 1500);

        //  super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        updateBalanceInBg();

    }

    // AdNetworks Linked Functions
    void init_startapp(){
        if(App.getInstance().get("StartAppActive",true)){
            StartAppSDK.init(this, App.getInstance().get("StartApp_AppID",""), false);
            StartAppAd.disableSplash();
        }
    }

    void showLoadingVideo(){
        progressDialog = ProgressDialog.show(context,getResources().getString(R.string.loading_video),getResources().getString(R.string.please_wait),false,false);
    }

    void adnetworkdisabled(String AdNetworkName){
        Dialogs.normalDialog(context,getResources().getString(R.string.adnetwork_disabled),getResources().getString(R.string.adnetwork_disabled_mesage),true,false,"",getResources().getString(R.string.ok),null);
    }

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    ScrimInsetsFrameLayout mScrimInsetsFrameLayout;

    void initViews(){

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        int Numboftabs = 1;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            toolbar.setElevation(4);
        }

        ViewPager pager = findViewById(R.id.pager);
        SlidingTabLayout tabs = findViewById(R.id.tabs);
        CharSequence Titles[] = {getResources().getString(R.string.home)};
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        if(App.getInstance().get("APP_TABS_ENABLE",false)){

            Numboftabs = 2;
            CharSequence Titles2[] = {getResources().getString(R.string.home), getResources().getString(R.string.transactions)};
            adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles2,Numboftabs);
            tabs.setVisibility(View.VISIBLE);

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                tabs.setElevation(4);
            }

        }

        pager.setAdapter(adapter);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);

        // Navigation Drawer
        mDrawerLayout = findViewById(R.id.main_activity_DrawerLayout);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        mScrimInsetsFrameLayout = findViewById(R.id.navigation_drawer_Layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(context,mDrawerLayout,toolbar,R.string.navigation_drawer_opened,R.string.navigation_drawer_closed)
        {   @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                // Disables the burger/arrow animation by default
                super.onDrawerSlide(drawerView, 0);
            }
        };

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }

        mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);

        if(App.getInstance().get("APP_NAVBAR_ENABLE",true)){

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.menu_icon, context.getTheme());
            mActionBarDrawerToggle.setHomeAsUpIndicator(drawable);
            mActionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mDrawerLayout.isDrawerVisible(GravityCompat.START)){
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }else{
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });

        }else{
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        mActionBarDrawerToggle.syncState();

        // Navigation Drawer layout width
        int possibleMinDrawerWidth = AppUtils.getScreenWidth(context) - UtilsMiscellaneous.getThemeAttributeDimensionSize(context, android.R.attr.actionBarSize);
        int maxDrawerWidth = getResources().getDimensionPixelSize(R.dimen.space280);
        mScrimInsetsFrameLayout.getLayoutParams().width = Math.min(possibleMinDrawerWidth, maxDrawerWidth);

    }

    void refreshDisplay(){

        TextView fullname = findViewById(R.id.nav_bar_display_name);
        TextView email = findViewById(R.id.nav_bar_display_email);

        fullname.setText(App.getInstance().getFullname());
        email.setText(App.getInstance().getEmail());

    }

    void initNavDrawer() {

        invalidateOptionsMenu();

        // Instructions
        FrameLayout instructions = findViewById(R.id.instructions);
        if(!App.getInstance().get("INSTRUCTIONS_ACTIVE",true)){ instructions.setVisibility(View.GONE); }
        instructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openInstructions();
                mDrawerLayout.closeDrawers();
            }
        });

        // Refer & Earn
        FrameLayout refer = findViewById(R.id.refer);
        if(!App.getInstance().get("REFER_ACTIVE",true)){ refer.setVisibility(View.GONE); }
        refer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openRefer();
                mDrawerLayout.closeDrawers();
            }
        });

        // Redeem
        FrameLayout redeem = findViewById(R.id.redeem);
        redeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openRedeem();
                mDrawerLayout.closeDrawers();
            }
        });

        // About
        FrameLayout about = findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openAbout();
                mDrawerLayout.closeDrawers();
            }
        });

        // Transactions
        FrameLayout reward_his = findViewById(R.id.nav_transactions);
        reward_his.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openTransactions();
                mDrawerLayout.closeDrawers();

            }
        });

        // Share
        FrameLayout share = findViewById(R.id.nav_share);
        if(!App.getInstance().get("SHARE_APP_ACTIVE",true)){ share.setVisibility(View.GONE); }
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.shareApplication(context);
                mDrawerLayout.closeDrawers();
            }
        });

        // Rate App
        FrameLayout rate = findViewById(R.id.rate_this_app);
        if(!App.getInstance().get("RATE_APP_ACTIVE",true)){ rate.setVisibility(View.GONE); }
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.gotoMarket(context);
                mDrawerLayout.closeDrawers();
            }
        });

        // Privacy Policy
        FrameLayout policy = findViewById(R.id.policy);
        if(!App.getInstance().get("POLICY_ACTIVE",true)){ policy.setVisibility(View.GONE); }
        policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parseURL(App.getInstance().get("APP_POLICY_URL",""));
                mDrawerLayout.closeDrawers();
            }
        });

        // Contact Us
        FrameLayout contact = findViewById(R.id.contact);
        if(!App.getInstance().get("CONTACT_US_ACTIVE",true)){ contact.setVisibility(View.GONE); }
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parseURL(App.getInstance().get("APP_CONTACT_US_URL",""));
                mDrawerLayout.closeDrawers();
            }
        });

    }
}