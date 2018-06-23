package com.example.apple.navigationdemo;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.apple.navigationdemo.Database.PieChartData;
import com.example.apple.navigationdemo.Database.PieChartDatabase;
import com.example.apple.navigationdemo.Utils.Constants;

import java.util.ArrayList;

import androidx.navigation.Navigation;


/**
 * A simple {@link Fragment} subclass.
 */
public class PieFragment extends Fragment implements View.OnClickListener{


    Button attachMomentButton;
    TextView dayOfMonthTextView;
    TextView formattedDateTextView;
    ImageView shareImageView;
    Bundle bundle;
    TextView moment1;
    TextView moment2;
    TextView moment3;
    TextView moment4;
    TextView moment5;
    TextView moment6;
    private String formattedDate;
    private String day;
    private int counter = 0;
    protected ArrayList<String> mAttachUrl = new ArrayList<>();
    protected ArrayList<String> mMomentDescription = new ArrayList<>();
    private String date;
    private int selectDateRequestCode = 191;
    private PieChartData[] pieChartData;

    public PieFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        fetchDataFromDB();
    }

    //Initializing Views here
    private void initViews() {
        attachMomentButton = getActivity().findViewById(R.id.attach_moment_button);
        attachMomentButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.editMomentFragment,bundle));
        dayOfMonthTextView = getActivity().findViewById(R.id.day_textview);
        formattedDateTextView = getActivity().findViewById(R.id.date_textview);
        shareImageView = getActivity().findViewById(R.id.share_imageview);
        moment1 = getActivity().findViewById(R.id.moment1);
        moment2 = getActivity().findViewById(R.id.moment2);
        moment3 = getActivity().findViewById(R.id.moment3);
        moment4 = getActivity().findViewById(R.id.moment4);
        moment5 = getActivity().findViewById(R.id.moment5);
        moment6 = getActivity().findViewById(R.id.moment6);
        day = getArguments().getString(getString(R.string.day));
        date = getArguments().getString(getString(R.string.date));
        dayOfMonthTextView.setText(day);
        formattedDate = getArguments().getString(getString(R.string.formatted_date));
        formattedDateTextView.setText(formattedDate);
    }

    private void fetchDataFromDB() {
        AsyncTask asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPostExecute(Void aVoid) {
                if (pieChartData.length == 0) {
                    momentAttached();
                }
                for (int i = 0; i < pieChartData.length; i++) {
                    counter = pieChartData[i].getCounter();
                    if (counter > 0) {
                        mMomentDescription.add(pieChartData[i].getMomentDesc());
                        mAttachUrl.add(pieChartData[i].getAttachedUrl());
                        setValues(mMomentDescription);
                    }
                    if (counter >= 6) {
                        attachMomentButton.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                pieChartData = PieChartDatabase.getInstance(getActivity())
                        .getPieChartDao()
                        .getPieChartData(date);
                return null;
            }
        }.execute();
    }


/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == selectDateRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                final String moment = data.getStringExtra(getString(R.string.moment));
                final String url = data.getStringExtra(getString(R.string.url));
                counter++;
                mMomentDescription.add(moment);
                mAttachUrl.add(url);
                setValues(mMomentDescription);
                AsyncTask asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        PieChartData pieChartData = new PieChartData(date, counter, moment, url);
                        PieChartDatabase.getInstance(getActivity())
                                .getPieChartDao()
                                .insert(pieChartData);
                        return null;
                    }
                }.execute();
            } else {
                //finish();
            }
        }
    }*/


    @Override
    public void onResume() {
        super.onResume();
        if (counter >= 6) {
            attachMomentButton.setVisibility(View.GONE);
        }
    }

    public void setValues(ArrayList<String> val) {
        if (val.size() >= 1) {
            moment1.setText(val.get(0));
            getActivity().findViewById(R.id.relative_moment1).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.showMomentFragment,bundle));
        }
        if (val.size() >= 2) {
            moment2.setText(val.get(1));
            getActivity().findViewById(R.id.relative_moment2).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.showMomentFragment,bundle));
        }
        if (val.size() >= 3) {
            moment3.setText(val.get(2));
            getActivity().findViewById(R.id.relative_moment3).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.showMomentFragment,bundle));
        }
        if (val.size() >= 4) {
            moment4.setText(val.get(3));
            getActivity().findViewById(R.id.relative_moment4).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.showMomentFragment,bundle));
        }
        if (val.size() >= 5) {
            moment5.setText(val.get(4));
            getActivity().findViewById(R.id.relative_moment5).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.showMomentFragment,bundle));
        }
        if (val.size() >= 6) {
            moment6.setText(val.get(5));
            getActivity().findViewById(R.id.relative_moment6).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.showMomentFragment,bundle));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_moment1:
                showMomentActivity(0);
                break;
            case R.id.relative_moment2:
                showMomentActivity(1);
                break;
            case R.id.relative_moment3:
                showMomentActivity(2);
                break;
            case R.id.relative_moment4:
                showMomentActivity(3);
                break;
            case R.id.relative_moment5:
                showMomentActivity(4);
                break;
            case R.id.relative_moment6:
                showMomentActivity(5);
                break;
            case R.id.attach_moment_button:
                momentAttached();
                break;
        }
    }

    private void showMomentActivity(int i) {
        bundle.putString(Constants.MOMENT_DESCRIPTION, mMomentDescription.get(i) + "");
        bundle.putString(Constants.MOMENT_ATTACH_FILE, mAttachUrl.get(i) + "");
        getActivity().findViewById(R.id.relative_moment1).callOnClick();
    }

    public void momentAttached() {
        bundle = new Bundle();
        bundle.putString(getString(R.string.day), day);
        bundle.putString(getString(R.string.formatted_date), formattedDate);
        attachMomentButton.callOnClick();
    }
}
