package com.droidoxy.easymoneyrewards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.fragments.InstructionsFragment;
import com.droidoxy.easymoneyrewards.fragments.RedeemFragment;
import com.droidoxy.easymoneyrewards.fragments.ReferFragment;
import com.droidoxy.easymoneyrewards.fragments.TransactionsFragment;
import com.droidoxy.easymoneyrewards.fragments.VideosFragment;
import com.droidoxy.easymoneyrewards.utils.AppUtils;
import com.droidoxy.easymoneyrewards.utils.CustomRequest;
import com.droidoxy.easymoneyrewards.utils.Dialogs;
import com.thefinestartist.ytpa.YouTubePlayerActivity;
import com.thefinestartist.ytpa.enums.Orientation;
import com.thefinestartist.ytpa.utils.YouTubeUrlParser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by DroidOXY
 */

public class FragmentsActivity extends ActivityBase {

    FragmentsActivity ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);
        ctx = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        handleFragments(getIntent());

    }


    private void handleFragments(Intent intent) {

        String Type = intent.getStringExtra("show");

        if (Type != null) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            switch (Type){

                case "transactions" :

                    getSupportActionBar().setTitle(R.string.transactions);
                    transaction.add(R.id.frame_layout, new TransactionsFragment(), "transactions");

                    break;

                case "redeem" :

                    getSupportActionBar().setTitle(R.string.redeem);
                    transaction.add(R.id.frame_layout, new RedeemFragment(), "redeem");

                    break;

                case "refer" :

                    getSupportActionBar().setTitle(R.string.refer);
                    transaction.add(R.id.frame_layout, new ReferFragment(), "refer");

                    break;

                case "about" :

                    startActivity(new Intent(ctx, AboutActivity.class));
                    finish();

                    break;

                case "instructions" :

                    getSupportActionBar().setTitle(R.string.instructions);
                    transaction.add(R.id.frame_layout, new InstructionsFragment(), "instructions");

                    break;

                case "home" :

                    startActivity(new Intent(ctx, MainActivity.class));
                    finish();

                    break;

                case "webvids" :

                    getSupportActionBar().setTitle(R.string.all_videos);
                    transaction.add(R.id.frame_layout, new VideosFragment(), "webvids");

                    break;

                default:

                    ActivityCompat.finishAffinity(ctx);
                    startActivity(new Intent(ctx, AppActivity.class));

                    break;
            }

            transaction.commit();

        }else{
            finish();
        }

    }

    public void closeActivity(){
        finish();
    }

    @Override
    public void onBackPressed(){
        finish();
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

    public void Redeem(String title, String subtitle, String message, String amount, String points, final String payoutId, String status, String image){

        if(Integer.parseInt(App.getInstance().getBalance()) >= Integer.parseInt(points)){

            final EditText editText = new EditText(ctx);
            editText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(99)});
            editText.setMinLines(2);

            Dialogs.editTextDialog(ctx, editText, message, false, true, ctx.getResources().getString(R.string.cancel), ctx.getResources().getString(R.string.proceed), new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {

                    String payoutTo = editText.getText().toString();

                    if(!payoutTo.isEmpty()){

                        if(payoutTo.length() < 4){

                            Dialogs.errorDialog(ctx,getResources().getString(R.string.error),getResources().getString(R.string.enter_something),true,false,"",getResources().getString(R.string.ok),null);

                        }else{

                            sweetAlertDialog.dismiss();
                            showpDialog();
                            processRedeem(payoutId, payoutTo);
                        }

                    }else{

                        Dialogs.errorDialog(ctx,getResources().getString(R.string.error),getResources().getString(R.string.enter_something),true,false,"",getResources().getString(R.string.ok),null);
                    }
                }
            });

        }else{

            Dialogs.warningDialog(ctx,  ctx.getResources().getString(R.string.oops),ctx.getResources().getString(R.string.no_enough)+" "+ctx.getResources().getString(R.string.app_currency)+" "+ctx.getResources().getString(R.string.to)+" "+ctx.getResources().getString(R.string.redeem), false, false, "", ctx.getResources().getString(R.string.ok), null);

        }

    }

    void processRedeem(final String payoutId, final String payoutTo){

        CustomRequest redeemRequest = new CustomRequest(Request.Method.POST, ACCOUNT_REDEEM,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        hidepDialog();

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == ERROR_SUCCESS){

                                // Success
                                Dialogs.succesDialog(ctx,getResources().getString(R.string.redeem_success_title),getResources().getString(R.string.redeem_succes_message),false,false,"",getResources().getString(R.string.ok),null);

                                App.getInstance().updateBalance();

                            }else if(Response.getInt("error_code") == 420){

                                // No Enough Balance
                                Dialogs.warningDialog(ctx,  ctx.getResources().getString(R.string.oops),ctx.getResources().getString(R.string.no_enough)+" "+ctx.getResources().getString(R.string.app_currency)+" "+ctx.getResources().getString(R.string.to)+" "+ctx.getResources().getString(R.string.redeem), false, false, "", ctx.getResources().getString(R.string.ok), null);

                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(ctx,Response.getInt("error_code"));

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(ctx,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                Dialogs.serverError(ctx, getResources().getString(R.string.ok), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        finish();
                                    }
                                });
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

                hidepDialog();

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

            }}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("data", App.getInstance().getDataCustom(payoutId, payoutTo));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(redeemRequest);

    }

    public void playVideo(String videoId, String videoPoints, String videoURL, String openLink){

        Intent playVideo = new Intent(ctx, YouTubePlayerActivity.class);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, YouTubeUrlParser.getVideoId(videoURL));
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_REWARDS, videoPoints);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_ID, videoId);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_LINK, openLink);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_ORIENTATION, Orientation.ONLY_LANDSCAPE);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_SHOW_AUDIO_UI, false);
        playVideo.putExtra(YouTubePlayerActivity.EXTRA_HANDLE_ERROR, false);
        startActivityForResult(playVideo,1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {

                String videoId = data.getStringExtra("id");
                String Points = data.getStringExtra("points");
                String openLink = data.getStringExtra("openLink");

                if(!videoId.isEmpty() && !Points.isEmpty()){
                    awardVideo(Points,videoId,openLink);
                }

            }
        }
    }

    void awardVideo(final String Points,final String videoId,final String openLink){
        if(!openLink.equals("none")){ AppUtils.parse(ctx,openLink); }

        CustomRequest videoRewardRequest = new CustomRequest(Request.Method.POST, APP_VIDEOSTATUS,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == ERROR_SUCCESS){

                                // Video saved Success
                                App.getInstance().store("APPVIDEO_"+videoId,true);
                                AppUtils.toastShort(ctx,Points+ " " + getResources().getString(R.string.app_currency) + " " + getResources().getString(R.string.successfull_received));

                            }else if(Response.getInt("error_code") == 420) {

                                // 420 - Video watched Already
                                AppUtils.toastShort(ctx,getResources().getString(R.string.already_watched));
                                App.getInstance().store("APPVIDEO_"+videoId,true);

                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(ctx,Response.getInt("error_code"));

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(ctx,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(ctx,getResources().getString(R.string.msg_server_problem));
                            }

                        }catch (Exception e){

                            if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(ctx,"Got Error",e.toString() + ", please contact developer immediately",false,false,"","ok",null);

                            }else{

                                // Server error
                                AppUtils.toastShort(ctx,getResources().getString(R.string.msg_server_problem));
                            }

                        }

                    }},new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if(DEBUG_MODE){

                    // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                    Dialogs.errorDialog(ctx,"Got Error",error.toString(),true,false,"","ok",null);

                }else{

                    // Server error
                    AppUtils.toastShort(ctx,getResources().getString(R.string.msg_server_problem));
                }

            }}){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("data", App.getInstance().getDataCustom(videoId,Points));
                return params;
            }
        };

        App.getInstance().addToRequestQueue(videoRewardRequest);

    }

}
