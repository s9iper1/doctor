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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextClock;
import android.widget.TextView;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.messages.ConversationActivity;
import com.byteshaft.doctor.patients.DoctorBookingActivity;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;

public class DoctorDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView doctorName;
    private TextView doctorSpeciality;
    private RatingBar ratingBar;

    private ImageButton callButton;
    private ImageButton chatButton;
    private Button bookingButton;
    private Button showallReviewButton;

    private TextClock textClock;

    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_doctor_details);
        doctorName = (TextView) findViewById(R.id.doctor_name);
        doctorSpeciality = (TextView) findViewById(R.id.doctor_sp);
        ratingBar = (RatingBar) findViewById(R.id.user_ratings);
        callButton = (ImageButton) findViewById(R.id.call_button);
        chatButton = (ImageButton) findViewById(R.id.message_button);
        callButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
        bookingButton = (Button) findViewById(R.id.button_book);
        showallReviewButton = (Button) findViewById(R.id.review_all_button);
        textClock = (TextClock) findViewById(R.id.clock);
        bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), DoctorBookingActivity.class));
            }
        });
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
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "03120676767"));
                    startActivity(intent);
                }
                break;
            case R.id.message_button:
                startActivity(new Intent(getApplicationContext(),
                        ConversationActivity.class));
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
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "03120676767"));
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
