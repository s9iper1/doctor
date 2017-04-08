package com.byteshaft.doctor.doctors;

import android.Manifest;
import android.content.Context;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.messages.ConversationActivity;
import com.byteshaft.doctor.patients.DoctorBookingActivity;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import java.net.HttpURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorDetailsActivity extends AppCompatActivity implements View.OnClickListener, HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private TextView doctorName;
    private TextView doctorSpeciality;
    private RatingBar ratingBar;

    private ImageButton callButton;
    private ImageButton chatButton;
    private ImageButton heartButton;
    private Button bookingButton;
    private Button showallReviewButton;
    private TextView textClock;
    private ImageView status;
    private ReviewAdapter adapter;
    private String number;
    private CircleImageView circleImageView;
    private boolean isFavourite;
    private HttpRequest request;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_doctor_details);

        final String startTime = getIntent().getStringExtra("start_time");
        final String name = getIntent().getStringExtra("name");
        final String specialist = getIntent().getStringExtra("specialist");
        final int stars = getIntent().getIntExtra("stars", 0);
        final boolean favourite = getIntent().getBooleanExtra("favourite", false);
        number = getIntent().getStringExtra("number");
        final String photo = getIntent().getStringExtra("photo");
        final boolean availableForChat = getIntent().getBooleanExtra("available_to_chat", false);
        id = getIntent().getIntExtra("user", -1);

        doctorName = (TextView) findViewById(R.id.doctor_name);
        doctorName.setText(name);
        doctorSpeciality = (TextView) findViewById(R.id.doctor_sp);
        doctorSpeciality.setText(specialist);
        circleImageView = (CircleImageView) findViewById(R.id.profile_image_view);
        ratingBar = (RatingBar) findViewById(R.id.user_ratings);
        ratingBar.setRating(stars);
        callButton = (ImageButton) findViewById(R.id.call_button);
        chatButton = (ImageButton) findViewById(R.id.message_button);
        heartButton = (ImageButton) findViewById(R.id.heart_button);
        status = (ImageView) findViewById(R.id.status);
        callButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
        heartButton.setOnClickListener(this);
        bookingButton = (Button) findViewById(R.id.button_book);
        showallReviewButton = (Button) findViewById(R.id.review_all_button);
        textClock = (TextView) findViewById(R.id.clock);
        textClock.setText(startTime);
        bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DoctorBookingActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("specialist", specialist);
                intent.putExtra("stars", stars);
                intent.putExtra("favourite", favourite);
                intent.putExtra("number", number);
                intent.putExtra("available_to_chat", availableForChat);
                intent.putExtra("user", id);
                intent.putExtra("photo", photo);
                intent.putExtra("start_time", startTime);
                startActivity(intent);
            }
        });
        if (!availableForChat) {
            status.setImageResource(R.mipmap.ic_offline_indicator);
        } else {
            status.setImageResource(R.mipmap.ic_online_indicator);
        }
        Helpers.getBitMap(photo, circleImageView);
        getReviews();
    }

    private void getReviews() {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%spublic/doctor/%s/review",
                AppGlobals.BASE_URL, id));
        Log.i("TAG", "id "  + id);
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default: return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_button:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            AppGlobals.CALL_PERMISSION);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                    startActivity(intent);
                }
                break;
            case R.id.message_button:
                startActivity(new Intent(getApplicationContext(),
                        ConversationActivity.class));
                break;
            case R.id.heart_button:
                if (isFavourite) {
                    heartButton.setBackgroundResource(R.mipmap.heart);
                    isFavourite = true;
                } else {
                    heartButton.setBackgroundResource(R.mipmap.ic_heart_fill);
                    isFavourite = false;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppGlobals.CALL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_granted);
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_denied);
                }
                break;
        }
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {

    }

    private class ReviewAdapter extends ArrayAdapter {

        private ViewHolder viewHolder;

        public ReviewAdapter(Context context, int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_dashboard, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.userName = (TextView) convertView.findViewById(R.id.by_username);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.userComment = (TextView) convertView.findViewById(R.id.tv_review);
                viewHolder.userRating = (RatingBar) convertView.findViewById(R.id.user_ratings);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            return convertView;
        }
    }

    private class ViewHolder {
        TextView userName;
        TextView time;
        TextView userComment;
        RatingBar userRating;
    }
}
