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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.gettersetter.AppointmentDetail;
import com.byteshaft.doctor.messages.ConversationActivity;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorBookingActivity extends AppCompatActivity implements View.OnClickListener, HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener, AdapterView.OnItemClickListener {

    private TextView mDoctorName;
    private TextView mDoctorSpeciality;
    private TextView mtime;
    private CircleImageView mDoctorImage;
    private RatingBar mDoctorRating;
    private ImageButton mCallButton;
    private ImageButton mChatButton;
    private ImageButton mFavButton;
    private GridView timeTableGrid;
    private ImageView status;
    private int id;
    private HttpRequest request;
    private ArrayList<AppointmentDetail> timeSlots;
    private TimeTableAdapter timeTableAdapter;
    private String currentDate;
    private boolean favourite;
    private boolean isBlocked;
    private String startTime;

    private String phonenumber;
    private String drName;
    private String drSpecialist;
    private float drStars;
    private String drPhoto;
    private boolean availableForChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_doctor_booking);
        timeTableGrid = (GridView) findViewById(R.id.time_table);
        timeTableGrid.setOnItemClickListener(this);
        timeSlots = new ArrayList<>();
        HashSet<Date> events = new HashSet<>();
        events.add(new Date());
        currentDate = Helpers.getDate();
        com.byteshaft.doctor.uihelpers.CalendarView cv = ((com.byteshaft.doctor.uihelpers.CalendarView)
                findViewById(R.id.calendar_view));
        cv.updateCalendar(events);

        // assign event handler
        cv.setEventHandler(new com.byteshaft.doctor.uihelpers.CalendarView.EventHandler() {
            @Override
            public void onDayPress(Date date) {
                DateFormat df = SimpleDateFormat.getDateInstance();
                String resultDate = df.format(date);
                SimpleDateFormat formatterFrom = new SimpleDateFormat("MMM d, yyyy");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date formattedDate = null;
                try {
                    formattedDate = formatterFrom.parse(resultDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Toast.makeText(DoctorBookingActivity.this, dateFormat.format(formattedDate), Toast.LENGTH_SHORT).show();
                currentDate = dateFormat.format(formattedDate);
                Log.i("TAG", "current date  " + currentDate);
                getSchedule(currentDate);

            }
        });
        mDoctorName = (TextView) findViewById(R.id.doctor_name);
        mDoctorSpeciality = (TextView) findViewById(R.id.doctor_sp);
        mDoctorRating = (RatingBar) findViewById(R.id.user_ratings);
        mtime = (TextView) findViewById(R.id.clock);
        mDoctorImage = (CircleImageView) findViewById(R.id.profile_image_view_search);
        mCallButton = (ImageButton) findViewById(R.id.call_button);
        mChatButton = (ImageButton) findViewById(R.id.message_button);
        mFavButton = (ImageButton) findViewById(R.id.favt_button);
        status = (ImageView) findViewById(R.id.status);
        mCallButton.setOnClickListener(this);
        mChatButton.setOnClickListener(this);
        mFavButton.setOnClickListener(this);
        startTime = getIntent().getStringExtra("start_time");
        isBlocked = getIntent().getBooleanExtra("block", false);
        favourite = getIntent().getBooleanExtra("favourite", false);
        final String startTime = getIntent().getStringExtra("start_time");
        drName = getIntent().getStringExtra("name");
        drSpecialist = getIntent().getStringExtra("specialist");
        drStars = getIntent().getFloatExtra("stars", 0);
        final boolean favourite = getIntent().getBooleanExtra("favourite", false);
        phonenumber = getIntent().getStringExtra("number");
         drPhoto = getIntent().getStringExtra("photo");
        availableForChat = getIntent().getBooleanExtra("available_to_chat", false);
        id = getIntent().getIntExtra("user", -1);
        if (!availableForChat) {
            status.setImageResource(R.mipmap.ic_offline_indicator);
        } else {
            status.setImageResource(R.mipmap.ic_online_indicator);
        }
        if (isBlocked) {
            mChatButton.setEnabled(false);
        }
        if (favourite) {
            mFavButton.setBackground(getResources().getDrawable(R.mipmap.ic_heart_fill));
        }

        mDoctorName.setText(drName);
        mDoctorSpeciality.setText(drSpecialist);
        mDoctorRating.setRating(drStars);
        mtime.setText(startTime);
        Helpers.getBitMap(drPhoto, mDoctorImage);
        getSchedule(currentDate);
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
            default:
                return false;
        }
    }

    private void getSchedule(String targetDate) {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        String url = String.format("%spublic/doctor/%s/schedule?date=%s",
                AppGlobals.BASE_URL, id, targetDate);
        Log.i("TAG", "url" + url);
        request.open("GET", url);
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
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
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phonenumber));
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

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", "response " + request.getResponseText());
                        timeSlots = new ArrayList<>();
                        timeTableAdapter = new TimeTableAdapter(getApplicationContext(), timeSlots);
                        timeTableGrid.setAdapter(timeTableAdapter);
                        try {
                            JSONArray jsonArray = new JSONArray(request.getResponseText());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                AppointmentDetail appointmentDetail = new AppointmentDetail();
                                appointmentDetail.setDoctorId(jsonObject.getInt("doctor"));
                                appointmentDetail.setAppointmentId(jsonObject.getInt("id"));
                                appointmentDetail.setStartTime(Helpers.getFormattedTime(jsonObject.getString("start_time")));
                                appointmentDetail.setState(jsonObject.getString("state"));
                                timeSlots.add(appointmentDetail);
                                timeTableAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        AppointmentDetail appointmentDetail = timeSlots.get(i);
        TextView textView = (TextView) view;
        Log.i("TAG", timeSlots.get(i).getStartTime());
        if (appointmentDetail.getState().equals("pending")) {
            textView.setBackground(getResources().getDrawable(R.drawable.pressed_time_slot));
            Intent intent = new Intent(this, CreateAppointmentActivity.class);
            intent.putExtra("appointment_id", appointmentDetail.getAppointmentId());
            intent.putExtra("start_time", appointmentDetail.getStartTime());
            intent.putExtra("available_to_chat", availableForChat);
            intent.putExtra("name", drName);
            intent.putExtra("favourite", favourite);
            intent.putExtra("block", isBlocked);
            intent.putExtra("photo", drPhoto);
            intent.putExtra("number", phonenumber);
            intent.putExtra("stars", drStars);
            intent.putExtra("specialist", drSpecialist);
            intent.putExtra("specialist", drSpecialist);
            startActivity(intent);
        } else {
            Helpers.showSnackBar(findViewById(android.R.id.content), R.string.time_slot_booked);
        }
    }

    private class TimeTableAdapter extends ArrayAdapter<ArrayList<AppointmentDetail>> {

        private ArrayList<AppointmentDetail> timeTable;
        private ViewHolder viewHolder;

        public TimeTableAdapter(@NonNull Context context,
                                ArrayList<AppointmentDetail> timeTable) {
            super(context, R.layout.delegate_time_table);
            this.timeTable = timeTable;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_time_table, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final AppointmentDetail appointmentDetail = timeTable.get(position);

            viewHolder.time.setText(appointmentDetail.getStartTime());
            if (appointmentDetail.getState().equals("pending")) {
                viewHolder.time.setBackground(getResources().getDrawable(R.drawable.rounded_button));
            } else {
                viewHolder.time.setBackground(getResources().getDrawable(R.drawable.pressed_time_slot));
            }


            return convertView;
        }

        @Override
        public int getCount() {
            return timeTable.size();
        }
    }

    private class ViewHolder {
        TextView time;
    }
}
