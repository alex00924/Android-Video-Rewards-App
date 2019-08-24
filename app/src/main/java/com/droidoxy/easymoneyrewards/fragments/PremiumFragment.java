package com.droidoxy.easymoneyrewards.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.activities.FragmentsActivity;
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.constants.Constants;
import com.droidoxy.easymoneyrewards.utils.AppUtils;
import com.droidoxy.easymoneyrewards.utils.CustomRequest;
import com.droidoxy.easymoneyrewards.utils.Dialogs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import dmax.dialog.SpotsDialog;

import static com.droidoxy.easymoneyrewards.constants.Constants.APP_UPGRADE_PREMIUM;
import static com.droidoxy.easymoneyrewards.constants.Constants.DEBUG_MODE;

/**
 * Created by DroidOXY
 */
 
public class PremiumFragment extends Fragment {
    private Card card;
    private Charge charge;

    private EditText emailField;
    private EditText cardNumberField;
    private EditText expiryMonthField;
    private EditText expiryYearField;
    private EditText cvvField;

    private String email, cardNumber, cvv;
    private int expiryMonth, expiryYear;
    Context ctx;
    private AlertDialog updatingDlg;

    public PremiumFragment() {
        // Required empty public constructor
    }

    View view;

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

    private void initView() {

        Button upgradeButton = view.findViewById(R.id.btn_upgrade);

        emailField = view.findViewById(R.id.edit_email_address);
        cardNumberField = view.findViewById(R.id.edit_card_number);
        expiryMonthField = view.findViewById(R.id.edit_expiry_month);
        expiryYearField = view.findViewById(R.id.edit_expiry_year);
        cvvField = view.findViewById(R.id.edit_cvv);


        upgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateForm()) {
                    return;
                }
                try {
                    email = emailField.getText().toString().trim();
                    cardNumber = cardNumberField.getText().toString().trim();
                    expiryMonth = Integer.parseInt(expiryMonthField.getText().toString().trim());
                    expiryYear = Integer.parseInt(expiryYearField.getText().toString().trim());
                    cvv = cvvField.getText().toString().trim();

                    card = new Card(cardNumber, expiryMonth, expiryYear, cvv);

                    if (card.isValid()) {
                        performCharge();
                    } else {
                        Toast.makeText(ctx, "Card not Valid", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        updateView();

    }

    private void updateView() {
        String strPremium = App.getInstance().get("user_premium", "0");
        boolean bPremium = strPremium.equals("1") ? true : false;

        if (bPremium) {
            view.findViewById(R.id.layout_create).setVisibility(View.GONE);
            view.findViewById(R.id.layout_done).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.layout_create).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_done).setVisibility(View.GONE);
        }

    }

    private void performCharge() {

        updatingDlg = new SpotsDialog(getActivity(), R.style.Custom_Premium);
        updatingDlg.show();

        //create a Charge object
        charge = new Charge();

        //set the card to charge
        charge.setCard(card);

        //call this method if you set a plan
        //charge.setPlan("PLN_yourplan");

        charge.setEmail(email); //dummy email address

        charge.setAmount(Constants.UPGRADE_AMOUNT); //test amount

        PaystackSdk.chargeCard(getActivity(), charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                // This is called only after transaction is deemed successful.
                // Retrieve the transaction, and send its reference to your server
                // for verification.
                String paymentReference = transaction.getReference();
                Toast.makeText(ctx, "Transaction Successful! payment reference: "
                        + paymentReference, Toast.LENGTH_LONG).show();
                upgradeToPremium(paymentReference);
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                // This is called only before requesting OTP.
                // Save reference so you may send to server. If
                // error occurs with OTP, you should still verify on server.
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                //handle error here
                Toast.makeText(ctx, "Transaction Failed! Please Try Again.", Toast.LENGTH_SHORT).show();
                updatingDlg.dismiss();
            }
        });
    }

    private void upgradeToPremium(final String referenceTransaction) {

        CustomRequest upgradePremiumRequest = new CustomRequest(Request.Method.POST, APP_UPGRADE_PREMIUM,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        updatingDlg.dismiss();

                        Log.e("UPGRADE REsponse--", response.toString());
                        try{

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if(!Response.getBoolean("error") && Response.getInt("error_code") == Constants.ERROR_SUCCESS){

                                // Video saved Success
                                App.getInstance().store("user_premium","1");
                                AppUtils.toastShort(ctx,"Congrats! You updated to premium.");

                            }else if(Response.getInt("error_code") == 420) {

                                // 420 - upgraded already
                                AppUtils.toastShort(ctx,getResources().getString(R.string.already_watched));
                                App.getInstance().store("user_premium","1");

                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(ctx,Response.getInt("error_code"));

                            }else if(DEBUG_MODE){

                                // For Testing ONLY - intended for Developer Use ONLY not visible for Normal App user
                                Dialogs.errorDialog(ctx,Response.getString("error_code"),Response.getString("error_description"),false,false,"",getResources().getString(R.string.ok),null);

                            }else{

                                // Server error
                                AppUtils.toastShort(ctx,getResources().getString(R.string.msg_server_problem));
                            }

                            updateView();
                        }catch (Exception e){
                            updatingDlg.dismiss();

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
                updatingDlg.dismiss();

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
                params.put("data", App.getInstance().getDataCustom(referenceTransaction, "" + Constants.UPGRADE_AMOUNT));
                Log.e("params::", params.toString());
                return params;
            }
        };
        App.getInstance().addToRequestQueue(upgradePremiumRequest);

    }
    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        String cardNumber = cardNumberField.getText().toString();
        if (TextUtils.isEmpty(cardNumber)) {
            cardNumberField.setError("Required.");
            valid = false;
        } else {
            cardNumberField.setError(null);
        }


        String expiryMonth = expiryMonthField.getText().toString();
        if (TextUtils.isEmpty(expiryMonth)) {
            expiryMonthField.setError("Required.");
            valid = false;
        } else {
            expiryMonthField.setError(null);
        }

        String expiryYear = expiryYearField.getText().toString();
        if (TextUtils.isEmpty(expiryYear)) {
            expiryYearField.setError("Required.");
            valid = false;
        } else {
            expiryYearField.setError(null);
        }

        String cvv = cvvField.getText().toString();
        if (TextUtils.isEmpty(cvv)) {
            cvvField.setError("Required.");
            valid = false;
        } else {
            cvvField.setError(null);
        }

        return valid;
    }

    void finish(){

        Activity close = getActivity();
        if(close instanceof FragmentsActivity){
            FragmentsActivity show = (FragmentsActivity) close;
            show.closeActivity();
        }

    }

}