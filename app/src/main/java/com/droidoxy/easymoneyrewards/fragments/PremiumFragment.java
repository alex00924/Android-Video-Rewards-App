package com.droidoxy.easymoneyrewards.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.adapters.SliderAdapter;
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.model.News;
import com.droidoxy.easymoneyrewards.utils.CustomRequest;
import com.droidoxy.easymoneyrewards.utils.Dialogs;
import com.droidoxy.easymoneyrewards.views.TextView_Lato;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import co.paystack.android.PaystackSdk;

import static com.droidoxy.easymoneyrewards.constants.Constants.APP_NEWS;
import static com.droidoxy.easymoneyrewards.constants.Constants.DEBUG_MODE;
import static com.droidoxy.easymoneyrewards.constants.Constants.PAYMENT_VERIFY;
import static com.droidoxy.easymoneyrewards.constants.Constants.PAYPAL_PAY;
import static com.droidoxy.easymoneyrewards.constants.Constants.PAYSTACK_PAY;

/**
 * Created by DroidOXY
 */
 
public class PremiumFragment extends Fragment {

    Context ctx;

    SliderView sliderView;
    View view;
    TextView txtPremium;
    Button btnPaystack;
    Button btnPaypal;

    public PremiumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();
        PaystackSdk.initialize(ctx);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_premium, container, false);
        initView();
        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        checkPremium();

    }


    private void initView() {
        txtPremium = view.findViewById(R.id.txt_premium);
        sliderView = view.findViewById(R.id.imageSlider);
        btnPaystack = view.findViewById(R.id.btnPaystack);
        btnPaypal = view.findViewById(R.id.btnPaypal);
        btnPaystack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(PAYSTACK_PAY + App.getInstance().getId());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        btnPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(PAYPAL_PAY + App.getInstance().getId());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        final SliderAdapter adapter = new SliderAdapter(getContext());

        sliderView.setSliderAdapter(adapter);

        sliderView.setIndicatorAnimation(IndicatorAnimations.SLIDE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINROTATIONTRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.startAutoCycle();

        sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
            @Override
            public void onIndicatorClicked(int position) {
                sliderView.setCurrentPagePosition(position);
            }
        });

    }

    private void updateView() {

        String strPremium = App.getInstance().get("user_premium", "0");
        boolean bPremium = strPremium.equals("1") ? true : false;
        if (bPremium) {
            txtPremium.setText("You are\nPremium Member");
            view.findViewById(R.id.layout_btn).setVisibility(View.GONE);
        } else {
            txtPremium.setText("Upgrade\nto Premium");
            view.findViewById(R.id.layout_btn).setVisibility(View.VISIBLE);
        }

    }

    private void checkPremium() {

        String strPremium = App.getInstance().get("user_premium", "0");
        boolean bPremium = strPremium.equals("1") ? true : false;

        if (bPremium) {
            updateView();
            return;
        }

        CustomRequest transactionsRequest = new CustomRequest(Request.Method.POST, PAYMENT_VERIFY,null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("PAYSTACK", response.toString());
                    try{
                        JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));
                        if (!Response.getBoolean("error")) {
                            boolean bPremium = Response.getBoolean("premium");
                            if (bPremium) {
                                App.getInstance().store("user_premium","1");
                            }
                        }
                        updateView();
                    }catch (Exception e){
                        updateView();
                    }
                }},
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("PAYSTACK", error.toString());
                    updateView();
                }
            })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                String strId = Long.toString(App.getInstance().getId());
                params.put("accountID", strId);
                return params;
            }
        };

        App.getInstance().addToRequestQueue(transactionsRequest);
    }
}