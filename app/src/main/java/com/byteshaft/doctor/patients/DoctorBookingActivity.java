package com.byteshaft.doctor.patients;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.messages.ConversationActivity;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorBookingActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mDoctorName;
    private TextView mDoctorSpeciality;
    private TextClock mtime;
    private CircleImageView mDoctorImage;
    private RatingBar mDoctorRating;
    private ImageButton mCallButton;
    private ImageButton mChatButton;
    private ImageButton mFavButton;
    private GridView timeTableGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_doctor_booking);
        timeTableGrid = (GridView) findViewById(R.id.time_table);
        HashSet<Date> events = new HashSet<>();
        events.add(new Date());
        com.byteshaft.doctor.uihelpers.CalendarView cv = ((com.byteshaft.doctor.uihelpers.CalendarView)
                findViewById(R.id.calendar_view));
        cv.updateCalendar(events);

        // assign event handler
        cv.setEventHandler(new com.byteshaft.doctor.uihelpers.CalendarView.EventHandler() {
            @Override
            public void onDayPress(Date date) {
                Log.i("TAG", "click");
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(DoctorBookingActivity.this, df.format(date), Toast.LENGTH_SHORT).show();

            }
        });
//        mDoctorName = (TextView) findViewById(R.id.doctor_name_booking);
//        mDoctorSpeciality = (TextView) findViewById(R.id.doctor_sp_booking);
//        mDoctorRating = (RatingBar) findViewById(R.id.doctor_rating_booking);
//        mtime = (TextClock) findViewById(R.id.clock);
//        mDoctorImage = (CircleImageView) findViewById(R.id.doctor_image_booking);
        mCallButton = (ImageButton) findViewById(R.id.call_button);
        mChatButton = (ImageButton) findViewById(R.id.message_button);
//        mFavButton = (ImageButton) findViewById(R.id.button_fav_booking);
        mCallButton.setOnClickListener(this);
        mChatButton.setOnClickListener(this);
//        mFavButton.setOnClickListener(this);
        JSONArray jsonArray = new JSONArray();
        try {

            JSONObject time = new JSONObject();
            time.put("time", "9:00");
            time.put("state", 0);
            jsonArray.put(time);
            JSONObject timeTwo = new JSONObject();
            timeTwo.put("time", "9:30");
            timeTwo.put("state", 1);
            jsonArray.put(timeTwo);
            JSONObject timeThree = new JSONObject();
            timeThree.put("time", "10:00");
            timeThree.put("state", 0);
            jsonArray.put(timeThree);

            JSONObject timeFour = new JSONObject();
            timeFour.put("time", "10:30");
            timeFour.put("state", 1);
            jsonArray.put(timeFour);

            JSONObject timeFive = new JSONObject();
            timeFive.put("time", "11:00");
            timeFive.put("state", 0);
            jsonArray.put(timeFive);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("TAG", "array " + jsonArray);
        timeTableGrid.setAdapter(new TimeTableAdapter(getApplicationContext(), jsonArray));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_locator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                startActivity(new Intent(getApplicationContext(), DoctorsLocator.class));
                return true;
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

    private class TimeTableAdapter extends ArrayAdapter<JSONArray> {

        private JSONArray timeTable;
        private ViewHolder viewHolder;

        public TimeTableAdapter(@NonNull Context context,
                                JSONArray timeTable) {
            super(context, R.layout.delegate_time_table);
            this.timeTable = timeTable;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_time_table, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.time = (AppCompatButton) convertView.findViewById(R.id.time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            try {
            final JSONObject jsonObject = timeTable.getJSONObject(position);
                viewHolder.time.setText(jsonObject.getString("time"));
                viewHolder.time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (jsonObject.getInt("state") == 0) {
                                viewHolder.time.setBackgroundColor(getResources().getColor(R.color.appointment_bg));
                                viewHolder.time.setPressed(true);
                                startActivity(new Intent(getApplicationContext(), CreateAppointmentActivity.class));
                            } else {
                                Helpers.showSnackBar(findViewById(android.R.id.content), R.string.time_slot_booked);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (jsonObject.getInt("state") == 0) {
                    viewHolder.time.setPressed(false);
                } else {
                    viewHolder.time.setPressed(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return timeTable.length();
        }
    }

    private class ViewHolder {
        AppCompatButton time;
    }
}
