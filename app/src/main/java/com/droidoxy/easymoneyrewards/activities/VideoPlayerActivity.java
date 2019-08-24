package com.droidoxy.easymoneyrewards.activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class VideoPlayerActivity extends ActivityBase implements RewardedVideoAdListener {
    public static final String EXTRA_VIDEO_URL = "video_url";
    public static final String EXTRA_VIDEO_THUMB = "video_thumb";
    public static final String EXTRA_VIDEO_ID = "video_id";
    public static final String EXTRA_REWARDS = "video_amount";
    public static final String EXTRA_VIDEO_TITLE = "video_title";

    String url = "http://172.17.100.2:8000/uploads/videos/videos/2019_08_20_21_14_37_test2.mp4";
    String thumb_url = "http://172.17.100.2:8000/uploads/videos/thumbs/Untitled.png";

    private RewardedVideoAd mAd;
    private InterstitialAd interstitial;
    private boolean isWatched = false;

    private Button btnClaim;
    private Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_VIDEO_TITLE));


        url = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        thumb_url = getIntent().getStringExtra(EXTRA_VIDEO_THUMB);
        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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
        final VideoView myVideoView = findViewById(R.id.video_view);
        myVideoView.setVideoURI(Uri.parse(url));
        myVideoView.setMediaController(new MediaController(this));
        myVideoView.requestFocus();

        ImageView videoImage = findViewById(R.id.video_image);
        Glide.with(this).load(thumb_url).into(videoImage);

        final View btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myVideoView.start();
                findViewById(R.id.video_image).setVisibility(View.GONE);
                findViewById(R.id.btn_play).setVisibility(View.GONE);
                findViewById(R.id.video_view).setVisibility(View.VISIBLE);
            }
        });

        btnClaim = findViewById(R.id.btn_claim);
        btnDownload = findViewById(R.id.btn_download);
        btnClaim.setEnabled(false);
        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer vmp) {
                myVideoView.start();
                isWatched = true;
                btnClaim.setEnabled(true);
            }
        });

        btnClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeWatch();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginDownload();
            }
        });
    }

    private long downloadID;
    private void beginDownload(){
        String strPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String fileName = url.substring(url.lastIndexOf('/'));

        File file=new File( strPath + fileName );
        /*
        Create a DownloadManager.Request with all the information necessary to start the download
         */
        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url))
                .setTitle(getIntent().getStringExtra(EXTRA_VIDEO_TITLE))// Title of the Download Notification
                .setDescription("Downloading Video File")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
    }
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(VideoPlayerActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };
    /*
    public void downloadFile() {
        btnDownload.setEnabled(false);
        String DownloadUrl = url;
        DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
        request1.setDescription(getIntent().getStringExtra(EXTRA_VIDEO_TITLE));   //appears the same in Notification bar while downloading
        request1.setTitle("Downloading Video File");
        request1.setVisibleInDownloadsUi(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request1.allowScanningByMediaScanner();
            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }
        request1.setDestinationInExternalFilesDir(getApplicationContext(), "/File", "Question1.mp3");

        DownloadManager manager1 = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(manager1).enqueue(request1);
        }
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            btnDownload.setEnabled(true);
            Toast.makeText(VideoPlayerActivity.this, "Downloaded successfully!", Toast.LENGTH_SHORT).show();
        }
    }
*/
    void init_admob(){

        MobileAds.initialize(VideoPlayerActivity.this, getString(R.string.admob_appId));

        // ADMOB Banner
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        // ADMOB VIDEO
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        // ADMOB Interstitial
        interstitial = new InterstitialAd(VideoPlayerActivity.this);
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

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    private void loadRewardedVideoAd() {
        if (!mAd.isLoaded()) {
            mAd.loadAd(getResources().getString(R.string.admob_videos), new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.video_image).setVisibility(View.VISIBLE);
        findViewById(R.id.video_view).setVisibility(View.GONE);
        findViewById(R.id.btn_play).setVisibility(View.VISIBLE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == RECOVERY_DIALOG_REQUEST) {
//            // Retry initialization if user performed a recovery action
//            playerView.initialize(googleApiKey, this);
//        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {
        AppUtils.toastShort(VideoPlayerActivity.this, getResources().getString(R.string.watch_till_end));
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
                                AppUtils.toastShort(VideoPlayerActivity.this,Points + " " + getResources().getString(R.string.app_currency) + " " + getResources().getString(R.string.successfull_received));
                                updateBalanceInBg();

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(VideoPlayerActivity.this,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(VideoPlayerActivity.this,getResources().getString(R.string.msg_server_problem));
                            }

                        }catch (Exception e){

                            if(DEBUG_MODE){
                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(VideoPlayerActivity.this,"Got Error",e.toString() + ", please contact developer immediately",false,false,"","ok",null);

                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(DEBUG_MODE){
                    // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                    Dialogs.errorDialog(VideoPlayerActivity.this,"Got Error",error.toString(),true,false,"","ok",null);
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

                                Dialogs.validationError(VideoPlayerActivity.this,response.getInt("error_code"));

                            }else if(response.getInt("error_code") == 799) {

                                Dialogs.warningDialog(VideoPlayerActivity.this, getResources().getString(R.string.update_app), getResources().getString(R.string.update_app_description), false, false, "", getResources().getString(R.string.update), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        AppUtils.gotoMarket(VideoPlayerActivity.this);
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
        intent.putExtra("id", getIntent().getStringExtra(EXTRA_VIDEO_ID));
        setResult(RESULT_OK, intent);
        finish();
    }
    /*
     @Override
    public void onVideoEnded() {

        //Amount = getIntent().getStringExtra(EXTRA_REWARDS);

        Intent intent = new Intent();
        intent.putExtra("points", Amount);
        intent.putExtra("vid", videoId);
        intent.putExtra("id", Id);
        intent.putExtra("openLink", OpenLink);
        setResult(RESULT_OK, intent);
        finish();

    }

     */
}
