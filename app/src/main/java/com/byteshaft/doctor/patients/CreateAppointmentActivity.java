package com.byteshaft.doctor.patients;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.messages.ConversationActivity;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateAppointmentActivity extends AppCompatActivity implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private Button mSaveButton;
    private Spinner serviceListSpinner;
    private ImageButton callButton;
    private ImageButton chatButton;
    private EditText mAppointmentEditText;
    private String mPhoneNumber;
    private CircleImageView mDoctorImage;
    private TextView mNameTextView;
    private TextView mSpecialityTextView;
    private TextView mDoctorStartTime;
    private RatingBar mDoctorRating;
    private int appointmentId;
    private ImageView status;

    private int id;

    private HttpRequest request;
    private boolean availableForChat;
    private boolean blocked;
    private ImageButton favouriteButton;
    private TextView dateText;
    private TextView timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_create_appoint);
        dateText = (TextView) findViewById(R.id.date_text);
        timeText = (TextView) findViewById(R.id.time_text);
        callButton = (ImageButton) findViewById(R.id.btn_call);
        chatButton = (ImageButton) findViewById(R.id.btn_chat);
        mDoctorImage = (CircleImageView) findViewById(R.id.doctor_image);
        mNameTextView = (TextView) findViewById(R.id.doctor_name);
        mSpecialityTextView = (TextView) findViewById(R.id.doctor_speciality);
        mDoctorStartTime = (TextView) findViewById(R.id.starts_time);
        mDoctorRating = (RatingBar) findViewById(R.id.user_ratings);
        status = (ImageView) findViewById(R.id.status);
        mAppointmentEditText = (EditText) findViewById(R.id.appointment_reason_editText);
        mSaveButton = (Button) findViewById(R.id.button_save);
        favouriteButton = (ImageButton) findViewById(R.id.btn_fav);
        mNameTextView.setTypeface(AppGlobals.typefaceNormal);
        mSpecialityTextView.setTypeface(AppGlobals.typefaceNormal);
        mDoctorStartTime.setTypeface(AppGlobals.typefaceNormal);

        callButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        favouriteButton.setOnClickListener(this);

        dateText.setText(Helpers.getDate());
        timeText.setText(Helpers.getTime());

        serviceListSpinner = (Spinner) findViewById(R.id.service_spinner);
        List<String> serviceList = new ArrayList<>();
        serviceList.add("Service 1");
        serviceList.add("Service 2");
        serviceList.add("Service 3");
        serviceList.add("Service 4");
        serviceList.add("Service 5");
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, serviceList);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceListSpinner.setAdapter(serviceAdapter);

        final String startTime = getIntent().getStringExtra("start_time");
        final String name = getIntent().getStringExtra("name");
        final String specialist = getIntent().getStringExtra("specialist");
        final int stars = getIntent().getIntExtra("stars", 0);
        mPhoneNumber = getIntent().getStringExtra("number");
        final String photo = getIntent().getStringExtra("photo");
        appointmentId = getIntent().getIntExtra("appointment_id", -1);
        availableForChat = getIntent().getBooleanExtra("available_to_chat", false);
        if (!availableForChat) {
            status.setImageResource(R.mipmap.ic_offline_indicator);
        } else {
            status.setImageResource(R.mipmap.ic_online_indicator);
        }
        blocked = getIntent().getBooleanExtra("block", false);
        mDoctorStartTime.setText(startTime);
        mNameTextView.setText(name);
        mSpecialityTextView.setText(specialist);
        mDoctorRating.setRating(stars);
        if (blocked) {
            chatButton.setEnabled(false);
        }
        if ( AppGlobals.isDoctorFavourite) {
            favouriteButton.setBackground(getResources().getDrawable(R.mipmap.ic_heart_fill));
        }
        Helpers.getBitMap(photo, mDoctorImage);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_call:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            AppGlobals.CALL_PERMISSION);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
                    startActivity(intent);
                }
                break;
            case R.id.btn_chat:
                startActivity(new Intent(getApplicationContext(),
                        ConversationActivity.class));
                break;
            case R.id.btn_fav:
                if (!AppGlobals.isDoctorFavourite) {
                    Helpers.favouriteDoctorTask(id, new HttpRequest.OnReadyStateChangeListener() {
                        @Override
                        public void onReadyStateChange(HttpRequest request, int readyState) {
                            switch (readyState) {
                                case HttpRequest.STATE_DONE:
                                    switch (request.getStatus()) {
                                        case HttpURLConnection.HTTP_CREATED:
                                            Log.i("TAG", "favourite " + request.getResponseText());
                                            JSONObject jsonObject = null;
                                            try {
                                                jsonObject = new JSONObject(request.getResponseText());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            AppGlobals.favouriteHashMap.put(id, jsonObject);
                                            Log.i("TAG", "adding to hashmap " + id + jsonObject);
                                            Log.i("Tag", String.valueOf(AppGlobals.isDoctorFavourite) + "boolean");
                                            AppGlobals.isDoctorFavourite = true;
                                            favouriteButton.setBackgroundResource(R.mipmap.ic_heart_fill);
                                    }
                            }
                        }
                    }, new HttpRequest.OnErrorListener() {
                        @Override
                        public void onError(HttpRequest request, int readyState, short error, Exception exception) {

                        }
                    });
                } else {
                    Log.i("Tag", String.valueOf(AppGlobals.isDoctorFavourite) + "boolean");
                    if (AppGlobals.favouriteHashMap.containsKey(id)) {
                        Log.i("Tag", String.valueOf(AppGlobals.isDoctorFavourite) + id +"doctor id");
                        try {
                            Helpers.unFavouriteDoctorTask(AppGlobals.favouriteHashMap.get(id).getInt("id"), new HttpRequest.OnReadyStateChangeListener() {
                                @Override
                                public void onReadyStateChange(HttpRequest request, int readyState) {
                                    switch (readyState) {
                                        case HttpRequest.STATE_DONE:
                                            switch (request.getStatus()) {
                                                case HttpURLConnection.HTTP_NO_CONTENT:
                                                    AppGlobals.isDoctorFavourite = false;
                                                    favouriteButton.setBackgroundResource(R.mipmap.ic_empty_heart);

                                            }
                                    }

                                }
                            }, new HttpRequest.OnErrorListener() {
                                @Override
                                public void onError(HttpRequest request, int readyState, short error, Exception exception) {

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case R.id.button_save:
                String appointmentReasonString = mAppointmentEditText.getText().toString();
                System.out.println(appointmentReasonString  + "working");
                patientsAppointment(appointmentReasonString, new int[4]);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppGlobals.CALL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(intent);
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_denied);
                }
                break;
        }
    }

    private void patientsAppointment(String appointmentReason, int[] services_id) {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("POST", String.format("%sappointment/%s/request",
                AppGlobals.BASE_URL, appointmentId));
        Log.i("TAG", "id " + appointmentId);
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send(getAppointmentData(appointmentReason, services_id));
    }


    private String getAppointmentData(String appointmentReason, int[] services_id) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reason", appointmentReason);
            jsonObject.put("services_id", services_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();

    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", "response " + request.getResponseText());
                        break;
                    case HttpRequest.STATE_DONE:
                            case HttpURLConnection.HTTP_CREATED:
                                Log.i("TAG", "HTTP_CREATED " + request.getResponseText());
                                break;
                }

        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {

    }
}
