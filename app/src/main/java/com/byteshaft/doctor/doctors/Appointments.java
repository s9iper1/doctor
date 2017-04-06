package com.byteshaft.doctor.doctors;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.byteshaft.doctor.R;
import com.byteshaft.doctor.gettersetter.Agenda;
import com.byteshaft.doctor.patients.DoctorsAppointment;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static com.byteshaft.doctor.R.id.state;

;

public class Appointments extends Fragment implements HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private View mBaseView;
    private SwipeMenuListView mListView;
    private HttpRequest request;
    private ArrayList<Agenda> agendaArrayList;
    private Adapter arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.appointments, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getResources().getString(R.string.appointments));
        mListView = (SwipeMenuListView) mBaseView.findViewById(R.id.listView);
        HashSet<Date> events = new HashSet<>();
        events.add(new Date());
        com.byteshaft.doctor.uihelpers.CalendarView calendarView = ((com.byteshaft.doctor.uihelpers.CalendarView)
                mBaseView.findViewById(R.id.calendar_view));
        calendarView.updateCalendar(events);
        TextView dateTextview = (TextView) calendarView.findViewById(R.id.calendar_date_display);
        Log.i("TAG", dateTextview.getText().toString());
        dateTextview.setTextColor(getResources().getColor(R.color.header_background));
        agendaArrayList = new ArrayList<>();
        getAgendaList();

        // assign event handler
        calendarView.setEventHandler(new com.byteshaft.doctor.uihelpers.CalendarView.EventHandler() {
            @Override
            public void onDayPress(Date date) {
                Log.i("TAG", "click");
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(getActivity(), df.format(date), Toast.LENGTH_SHORT).show();
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem close = new SwipeMenuItem(
                        getContext());
                // set item background
                close.setBackground(new ColorDrawable(getResources().getColor(
                        R.color.reject_background)));
                // set item width
                close.setWidth(dpToPx(50));
                // set item title
                close.setIcon(R.mipmap.cross);
                // add to menu
                menu.addMenuItem(close);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(getResources().getColor(
                        R.color.tick_background)));
                // set item width
                deleteItem.setWidth(dpToPx(60));
                // set a icon
                deleteItem.setIcon(R.mipmap.tick);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

// set creator
        mListView.setMenuCreator(creator);
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        ArrayList<String[]> arrayList = new ArrayList<>();
        arrayList.add(new String[]{"Bilal", "24", "ENT checkup"});
        arrayList.add(new String[]{"omer", "25", "ENT checkup"});
        arrayList.add(new String[]{"shahid", "26", "ENT checkup"});
        arrayList.add(new String[]{"hussi", "24", "ENT checkup"});
//        mListView.setAdapter(new Adapter(getContext(), arrayList));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(getActivity().getApplicationContext(), DoctorsAppointment.class));
            }
        });
        return mBaseView;
    }

    private void getAgendaList() {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctor/agenda", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    // close
                    case 0:
                        Log.i("TAG", "Rejected");
                        return true;
                    // tick
                    case 1:
                        Log.i("TAG", "Accepted");
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("agenda List ", request.getResponseText());
                        try {
                            JSONObject jsonObject = new JSONObject(request.getResponseText());
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject agendaObject = jsonArray.getJSONObject(i);
                                Agenda agenda = new Agenda();
                                agenda.setCreatedAt(agendaObject.getString("created_at"));
                                agenda.setDate(agendaObject.getString("date"));
                                agenda.setAgendaState(agendaObject.getString("state"));
                                agenda.setReaseon(agendaObject.getString("reason"));
                                agenda.setDoctorId(agendaObject.getInt("doctor"));
                                agenda.setAgendaId(agendaObject.getInt("id"));
                                agenda.setStartTIme(agendaObject.getString("start_time"));
                                agendaArrayList.add(agenda);
                                arrayAdapter = new Adapter(getActivity(), agendaArrayList);
                                mListView.setAdapter(arrayAdapter);
                                Log.i("TagONE ", agendaObject.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
        }
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {

    }

    private class Adapter extends ArrayAdapter {

        private ViewHolder viewHolder;
        private ArrayList<Agenda> data;

        public Adapter(Context context, ArrayList<Agenda> data) {
            super(context, R.layout.delegate_appointments);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_appointments, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.nameAge = (TextView) convertView.findViewById(R.id.name_age);
                viewHolder.appointmentTime = (TextView) convertView.findViewById(R.id.appointment_time);
                viewHolder.appointmentState = (View) convertView.findViewById(state);
                viewHolder.service = (TextView) convertView.findViewById(R.id.service);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Agenda agenda = agendaArrayList.get(position);
            viewHolder.appointmentTime.setText(agenda.getStartTIme());

            return convertView;
        }
    }

    class ViewHolder {
        View state;
        TextView appointmentTime;
        View appointmentState;
        TextView nameAge;
        TextView service;
        ImageView chatStatus;
    }
}
