package com.droidoxy.easymoneyrewards.fragments;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droidoxy.easymoneyrewards.R;
import com.droidoxy.easymoneyrewards.adapters.OfferWallsAdapter;
import com.droidoxy.easymoneyrewards.app.App;
import com.droidoxy.easymoneyrewards.model.OfferWalls;
import com.droidoxy.easymoneyrewards.utils.CustomRequest;
import com.droidoxy.easymoneyrewards.utils.Dialogs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.droidoxy.easymoneyrewards.constants.Constants.APP_OFFERWALLS;
import static com.droidoxy.easymoneyrewards.constants.Constants.DEBUG_MODE;

/**
 * Created by DroidOXY
 */
 
public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    View view;

    ProgressBar progressBarOfferwalls;

    RecyclerView offerwalls_list;
    private OfferWallsAdapter offerWallsAdapter;
    ArrayList<OfferWalls> offerWalls;

    TextView emptyText;
    ImageView emptyImage;
    Button retryButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        emptyText = view.findViewById(R.id.emptyText);
        emptyImage = view.findViewById(R.id.emptyImage);
        retryButton = view.findViewById(R.id.retryButton);

        progressBarOfferwalls = view.findViewById(R.id.progressBarOfferwalls);

        /* Offers Walls Listview code is here*/
        offerwalls_list = view.findViewById(R.id.offerwalls_list);
        offerWalls = new ArrayList<>();
        offerWallsAdapter = new OfferWallsAdapter(getActivity(),offerWalls);

        RecyclerView.LayoutManager offerWallsLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        offerwalls_list.setLayoutManager(offerWallsLayoutManager);
        offerwalls_list.setItemAnimator(new DefaultItemAnimator());
        offerwalls_list.setAdapter(offerWallsAdapter);

        load_offerwalls();

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
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    void load_offerwalls(){

        CustomRequest offerwallsRequest = new CustomRequest(Request.Method.POST, APP_OFFERWALLS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                            if (!Response.getBoolean("error")) {

                                JSONArray offerwalls = Response.getJSONArray("offerwalls");

                                if(offerwalls.length() < 1){
                                    showLoadError();
                                }

                                for (int i = 0; i < offerwalls.length(); i++) {

                                    JSONObject obj = offerwalls.getJSONObject(i);

                                    OfferWalls singleOfferWall = new OfferWalls();

                                    singleOfferWall.setOfferid(obj.getString("offer_id"));
                                    singleOfferWall.setTitle(obj.getString("offer_title"));
                                    singleOfferWall.setSubtitle(obj.getString("offer_subtitle"));
                                    singleOfferWall.setImage(obj.getString("offer_thumbnail"));
                                    singleOfferWall.setAmount(obj.getString("offer_points"));
                                    singleOfferWall.setType(obj.getString("offer_type"));
                                    singleOfferWall.setStatus(obj.getString("offer_status"));
                                    singleOfferWall.setPartner("offerwalls");

                                    if(obj.get("offer_status").equals("Active")){
                                        offerWalls.add(singleOfferWall);
                                    }

                                    progressBarOfferwalls.setVisibility(View.GONE);

                                }
                                offerWallsAdapter.notifyDataSetChanged();

                            }else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                Dialogs.validationError(getContext(),Response.getInt("error_code"));

                            }else if(DEBUG_MODE){

                                Dialogs.errorDialog(getContext(),"Got Error",Response.getInt("error_code") + ", please contact developer immediately",true,false,"","ok",null);

                            }else{
                                showLoadError();
                                Dialogs.serverError(getContext(),getResources().getString(R.string.ok),null);
                            }

                        } catch (JSONException e) {

                            showLoadError();
                            if(!DEBUG_MODE){
                                Dialogs.serverError(getContext(),getResources().getString(R.string.ok),null);
                            }else{
                                Dialogs.errorDialog(getContext(),"Got Error",e.toString() + ", please contact developer immediately",true,false,"","ok",null);
                            }
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                showLoadError();
                if(DEBUG_MODE){
                    Dialogs.warningDialog(getContext(),"Got Error",error.toString(),true,false,"","ok",null);
                }else{
                    Dialogs.serverError(getContext(),getResources().getString(R.string.ok),null);
                }

            }
        });

        App.getInstance().addToRequestQueue(offerwallsRequest);

    }

    void showLoadError(){

        progressBarOfferwalls.setVisibility(View.GONE);
        emptyImage.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                retryLoading();

            }
        });

    }

    void retryLoading(){

        progressBarOfferwalls.setVisibility(View.VISIBLE);
        emptyImage.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);

        load_offerwalls();

    }
}
