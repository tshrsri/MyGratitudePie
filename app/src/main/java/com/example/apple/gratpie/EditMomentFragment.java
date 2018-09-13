package com.example.apple.gratpie;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.apple.gratpie.Database.PieChartData;
import com.example.apple.gratpie.Database.PieChartDatabase;
import com.example.apple.gratpie.Utils.Constants;

import java.util.Calendar;
import java.util.Objects;

import androidx.navigation.Navigation;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditMomentFragment extends Fragment implements View.OnClickListener {

    EditText momentTextView;
    ImageView fileAddedPreviewImageview;
    Button addFileButton;
    Button attachMomentButton;
    TextView dayOfMonthTextView;
    TextView formattedDateTextView;
    private int PICK_IMAGE_REQUEST = 100;
    String url = "empty";
    private int totalRange = 100;
    private String date;
    private int counter = 0;
    private PieChartData[] pieChartData;
    private String day;
    private String formattedDate;
    private String attachFile = "";
    private String attachDesc = "";
    private boolean isFromShow = false;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private long getTimeInMili;

    public EditMomentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_moment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!hasPermissions(getActivity(), PERMISSIONS)){
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initViews();
        fetchDataFromDB();
    }

    private void initViews() {
        getActivity().findViewById(R.id.sharing_imageview).setVisibility(View.GONE);
        momentTextView = getActivity().findViewById(R.id.edit_text_moment);
        momentTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        fileAddedPreviewImageview = getActivity().findViewById(R.id.imageview_file_added_preview);
        addFileButton = getActivity().findViewById(R.id.button_add_file);
        attachMomentButton = getActivity().findViewById(R.id.button_add_moment);
        addFileButton.setOnClickListener(this);
        attachMomentButton.setOnClickListener(this);
        dayOfMonthTextView = getActivity().findViewById(R.id.day_edit_frag_textview);
        formattedDateTextView = getActivity().findViewById(R.id.date_edit_frag_textview);
        day = getArguments().getString(getString(R.string.day));
        formattedDate = getArguments().getString(getString(R.string.formatted_date));
        date = getArguments().getString(getString(R.string.date));
        getTimeInMili = getArguments().getLong(getString(R.string.getTimeInMili));
        isFromShow = getArguments().getBoolean(getString(R.string.isComingFromShowFragment));
        if (attachFile != null && attachFile.isEmpty()) {
            attachFile = getArguments().getString(Constants.MOMENT_ATTACH_FILE);
        }
        if (attachDesc != null && attachDesc.isEmpty()) {
            attachDesc = getArguments().getString(Constants.MOMENT_DESCRIPTION);
        }
        if (attachDesc != null && !attachDesc.isEmpty()) {
            momentTextView.setText(attachDesc);

            Glide.with(getActivity())
                    .load(attachFile)
                    .into(fileAddedPreviewImageview);
            fileAddedPreviewImageview.setVisibility(View.VISIBLE);
        }

        dayOfMonthTextView.setText(day);
        formattedDateTextView.setText(formattedDate);
    }

    public void fileAttachedClicked() {
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType(Constants.INPUT_TYPE);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_from)), PICK_IMAGE_REQUEST);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(addFileButton, R.string.need_pemission_to_show_pic, Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE_CODE);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.READ_EXTERNAL_STORAGE_CODE) {
            String permission = permissions[0];
            int grantResult = grantResults[0];

            if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType(Constants.INPUT_TYPE);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_from)), PICK_IMAGE_REQUEST);
                }
            }
        }
    }

    private void addMomentToDatabase() {
        final String momentAdded = momentTextView.getText().toString().trim();
        if (momentAdded.equals("")) {
            Snackbar.make(addFileButton, R.string.please_add_your_moment_first, Snackbar.LENGTH_SHORT).show();
        } else {
            AsyncTask asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    counter++;
                    PieChartData pieChartData = new PieChartData(date, counter, momentAdded, attachFile);
                    PieChartDatabase.getInstance(getActivity())
                            .getPieChartDao()
                            .insert(pieChartData);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    ContentResolver cr = getContext().getContentResolver();
                    ContentValues cv = new ContentValues();
                    cv.put(CalendarContract.Events.TITLE, momentAdded);
                    cv.put(CalendarContract.Events.DTSTART, getTimeInMili);
                    cv.put(CalendarContract.Events.DTEND, getTimeInMili + 60 * 60 * 1000);
                    cv.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());
                    cv.put(CalendarContract.Events.CALENDAR_ID, 1);
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);

                    Snackbar.make(addFileButton, "Successfully Added", Snackbar.LENGTH_SHORT).show();
                    Navigation.findNavController(addFileButton).popBackStack();
                }
            }.execute();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateMoment() {
        final String momentAdded = momentTextView.getText().toString().trim();
        if (momentAdded.equals("")) {
            Snackbar.make(addFileButton, R.string.please_add_your_moment_first, Snackbar.LENGTH_SHORT).show();
        } else {
            AsyncTask asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    PieChartData pieChartData = new PieChartData(date, counter + 1, momentAdded, attachFile);
                    PieChartDatabase.getInstance(getActivity())
                            .getPieChartDao()
                            .updatePie(pieChartData);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Snackbar.make(addFileButton, "Successfully Updated", Snackbar.LENGTH_SHORT).show();
                    Navigation.findNavController(addFileButton).popBackStack();
                }
            }.execute();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_add_file:
                fileAttachedClicked();
                break;
            case R.id.button_add_moment:
                if (isFromShow) {
                    updateMoment();
                } else {
                    addMomentToDatabase();
                }
                break;
        }
    }

    private void fetchDataFromDB() {
        AsyncTask asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPostExecute(Void aVoid) {
                if (isFromShow) {
                    counter = getArguments().getInt(getString(R.string.counter));
                } else {
                    for (int i = 0; i < pieChartData.length; i++) {
                        counter = pieChartData[i].getCounter();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {

                Uri uri = data.getData();
                url = uri.toString();
                attachFile = uri.toString();
                fileAddedPreviewImageview.setVisibility(View.VISIBLE);
                if (uri.toString().contains("image")) {
                    //handle image
                    fileAddedPreviewImageview.setImageURI(uri);
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(projection[0]);
                        //url = cursor.getString(columnIndex); // returns null
                        cursor.close();
                    }
                } else if (uri.toString().contains("video")) {
                    //handle video
                    Glide.with(getActivity())
                            .load(attachFile) // or URI/path
                            .into(fileAddedPreviewImageview);
                }
            }
        }
    }
}