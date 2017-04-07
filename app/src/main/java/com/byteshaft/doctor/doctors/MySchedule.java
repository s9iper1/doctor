package com.byteshaft.doctor.doctors;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MySchedule extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener, View.OnClickListener {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<JSONObject> scheduleList;
    private LinearLayout searchContainer;
    private String currentDate;
    private ArrayList<String> initialTimeSLots;
    private HttpRequest request;
    private AppCompatButton save;
    private ScheduleAdapter scheduleAdapter;
    private JSONArray jsonArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.my_schedule, container, false);
        com.byteshaft.doctor.uihelpers.CalendarView cv = ((com.byteshaft.doctor.uihelpers.CalendarView)
                mBaseView.findViewById(R.id.calendar_view));

        // assign event handler
        cv.setEventHandler(new com.byteshaft.doctor.uihelpers.CalendarView.EventHandler() {
            @Override
            public void onDayPress(Date date) {
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(getActivity(), df.format(date), Toast.LENGTH_SHORT).show();
            }
        });
        setHasOptionsMenu(true);
        currentDate = Helpers.getDate();
        initialTimeSLots = new ArrayList<>();
        scheduleList = new ArrayList<>();
        getTimeSlotsForDate(currentDate, TimeUnit.MINUTES.toMillis(45));
        save = (AppCompatButton) mBaseView.findViewById(R.id.save_button);
        save.setOnClickListener(this);
        searchContainer = new LinearLayout(getActivity());
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
        getSchedule(Helpers.getDate());
        searchContainer.setLayoutParams(containerParams);
        // Setup search view
        final EditText toolbarSearchView = new EditText(getActivity());
        toolbarSearchView.setBackgroundColor(getResources().getColor(R.color.search_background));
        // Set width / height / gravity
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = getActivity().obtainStyledAttributes(new TypedValue().data, textSizeAttr);
        int actionBarHeight = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, actionBarHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(5, 5, 5, 5);
        params.weight = 1;
        toolbarSearchView.setLayoutParams(params);
        // Setup display
        toolbarSearchView.setPadding(2, 0, 0, 0);
        toolbarSearchView.setTextColor(Color.WHITE);
        toolbarSearchView.setGravity(Gravity.CENTER_VERTICAL);
        toolbarSearchView.setSingleLine(true);
        toolbarSearchView.setImeActionLabel("Search", EditorInfo.IME_ACTION_UNSPECIFIED);
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(toolbarSearchView, R.drawable.cursor_color);
        } catch (Exception ignored) {

        }
        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        toolbarSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {

                } else {

                }
            }
        });
        toolbarSearchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toolbarSearchView.setFocusable(true);
                toolbarSearchView.setFocusableInTouchMode(true);
                return false;
            }
        });
        toolbarSearchView.setFocusableInTouchMode(false);
        toolbarSearchView.setFocusable(false);
        (searchContainer).addView(toolbarSearchView);

        // Setup the clear button
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearParams.gravity = Gravity.CENTER;
        // Add search view to toolbar and hide it
        toolbar.addView(searchContainer);
        return mBaseView;
    }

    private void getTimeSlotsForDate(String targetDate, long duration) {
        String time1 = "08:00:00";
        String time2 = "22:31:00";

        String format = "dd/MM/yyyy hh:mm";

        Log.i("TAG", "" + targetDate);
        Log.i("TAG", "" + targetDate);

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Date dateObj1 = null;
        Date dateObj2 = null;
        try {
            dateObj1 = sdf.parse(targetDate + " " + time1);
            dateObj2 = sdf.parse(targetDate + " " + time2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("Date Start: " + dateObj1);
        System.out.println("Date End: " + dateObj2);

        long dif = dateObj1.getTime();
        while (dif < dateObj2.getTime()) {
            Date slot = new Date(dif);
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            String date = df.format(slot.getTime());
            initialTimeSLots.add(date);
            dif += duration;
        }

        for (int i = 0; i < initialTimeSLots.size(); i++) {
            StringBuilder time = new StringBuilder();
            if (i + 1 < initialTimeSLots.size()) {
                time.append(initialTimeSLots.get(i));
            }
            time.append(" , ");
            if (i + 1 < initialTimeSLots.size()) {
                time.append(initialTimeSLots.get(i + 1));
            }

            if (!time.toString().trim().isEmpty()) {
                String[] bothTimes = time.toString().split(",");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("start_time", bothTimes[0]);
                    jsonObject.put("end_time", bothTimes[1]);
                    jsonObject.put("state", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!bothTimes[0].trim().isEmpty()) {
                    scheduleList.add(jsonObject);
                }
            }
        }
        Log.i("TAG", String.valueOf(scheduleList));
        mListView = (ListView) mBaseView.findViewById(R.id.schedule_list);
        scheduleAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), scheduleList);
        mListView.setAdapter(scheduleAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                if (jsonArray.length() > 0) {
                    sendSchedule("PUT");
                } else {
                    sendSchedule("POST");
                }
                break;
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<String> {

        private ArrayList<JSONObject> scheduleList;
        private ViewHolder viewHolder;

        public ScheduleAdapter(@NonNull Context context, ArrayList<JSONObject> scheduleList) {
            super(context, R.layout.delegate_doctor_schedule);
            this.scheduleList = scheduleList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.delegate_doctor_schedule, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.startTime = (TextView) convertView.findViewById(R.id.start_time);
                viewHolder.endTime = (TextView) convertView.findViewById(R.id.end_time);
                viewHolder.state = (CheckBox) convertView.findViewById(R.id.check_box_appointment);
                convertView.setTag(viewHolder);
                viewHolder.state.setTag(position);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Log.i("TAG", " boolean " + b);
                    JSONObject jsonObject = scheduleList.get(position);
                    int pos = (int) viewHolder.state.getTag();
                    View checkBoxView = mListView.getChildAt(pos);
                    if (checkBoxView != null) {
                        CheckBox cbx = (CheckBox) checkBoxView.findViewById(R.id.check_box_appointment);
                        Log.i("TAG", " checked" + b);
                        Log.i("TAG", " id" + String.valueOf(cbx.getId() == R.id.check_box_appointment));
                        if (b) {
                            try {
                                jsonObject.put("state", 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            scheduleList.remove(position);
                            scheduleList.add(position, jsonObject);
                        } else {
                            try {
                                jsonObject.put("state", 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            scheduleList.remove(position);
                            scheduleList.add(position, jsonObject);
                        }
                    }

                }
            });
            try {
                viewHolder.startTime.setText(scheduleList.get(position).getString("start_time"));
                viewHolder.endTime.setText(scheduleList.get(position).getString("end_time"));
                if (scheduleList.get(position).getInt("state") == 0) {
                    viewHolder.state.setChecked(false);
                } else {
                    viewHolder.state.setChecked(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return scheduleList.size();
        }
    }

    private class ViewHolder {
        TextView startTime;
        TextView endTime;
        CheckBox state;
    }

    // Internet Connectivity Functions

    private void getSchedule(String date) {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctor/schedule?date=%s", AppGlobals.BASE_URL, date));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    private void sendSchedule(String method) {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open(method, String.format("%sdoctor/schedule", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("date", Helpers.getDate());
            JSONArray jsonArray = new JSONArray();
            for (JSONObject singleJson : scheduleList) {
                if (singleJson.getInt("state") == 1) {
                    JSONObject time = new JSONObject();
                    time.put("start_time", singleJson.get("start_time"));
                    time.put("end_time", singleJson.get("end_time"));
                    jsonArray.put(time);
                }
            }
            if (jsonArray.length() > 0) {
                jsonObject.put("items", jsonArray);
                request.send(jsonObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", request.getResponseText());
                        Log.i("TAG", "" + scheduleList);
                        ArrayList<String> arrayList = new ArrayList<>();
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject timeSlot = jsonArray.getJSONObject(i);
                                String startTime = timeSlot.getString("start_time");
                                arrayList.add(startTime.trim());
                            }
                            for (int j = 0; j < scheduleList.size(); j++) {
                                Log.i("TAG", "schedule " + scheduleList.get(j).getString("start_time"));
                                Log.i("TAG", "schedule " + arrayList);
                                if (arrayList.contains(scheduleList.get(j).getString("start_time").trim())) {
                                    Log.i("TAG", "Condition match");
                                    JSONObject slot = scheduleList.get(j);
                                    slot.put("state", 1);
                                    scheduleList.remove(j);
                                    scheduleList.add(j, slot);
                                    scheduleAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        Log.i("TAG", request.getResponseText());
                        break;
                }
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {

    }
}
