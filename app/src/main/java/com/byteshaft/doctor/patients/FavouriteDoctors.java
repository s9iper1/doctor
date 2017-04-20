package com.byteshaft.doctor.patients;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.gettersetter.FavoriteDoctorsList;
import com.byteshaft.doctor.gettersetter.TimeSlots;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.FilterDialog;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.doctor.utils.Helpers.calculationByDistance;

/**
 * Created by s9iper1 on 3/1/17.
 */

public class FavouriteDoctors extends Fragment implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<FavoriteDoctorsList> favoriteDoctorsList;
    private LinearLayout searchContainer;
    private CustomAdapter customAdapter;
    private Toolbar toolbar;
    private HttpRequest request;
    private HashMap<Integer, ArrayList<TimeSlots>> slotsList;

    @SuppressLint("UseSparseArrays")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.favourite_doctors, container, false);
        mListView = (ListView) mBaseView.findViewById(R.id.favt_doctors_list);
        slotsList = new HashMap<>();
        searchContainer = new LinearLayout(getActivity());
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
        searchContainer.setLayoutParams(containerParams);
        // Setup search view
        EditText toolbarSearchView = new EditText(getActivity());
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
        (searchContainer).addView(toolbarSearchView);

        // Setup the clear button
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearParams.gravity = Gravity.CENTER;
        // Add search view to toolbar and hide it
        toolbar.addView(searchContainer);
        geFavoriteDoctorsList(Helpers.getDate());
        favoriteDoctorsList = new ArrayList<>();
        setHasOptionsMenu(true);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {


            }
        });
        return mBaseView;
    }

    @Override
    public void onPause() {
        super.onPause();
        toolbar.removeView(searchContainer);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doctors_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_filter:
                FilterDialog filterDialog = new FilterDialog(getActivity());
                filterDialog.show();
                return true;
            case R.id.action_location:
                return true;
            default:
                return false;
        }
    }

    public void geFavoriteDoctorsList(String date) {
        Helpers.showProgressDialog(getActivity(), getResources().getString(R.string.getting_favourite_doctors));
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%spatient/doctors/?date=%s", AppGlobals.BASE_URL, date));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        System.out.println(request.getResponseText());
                        System.out.println(request.getResponseURL());
                        try {
                            JSONArray jsonArray = new JSONArray(request.getResponseText());
                            for (int i= 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                com.byteshaft.doctor.gettersetter.FavoriteDoctorsList myFavoriteDoctorsList
                                        = new com.byteshaft.doctor.gettersetter.FavoriteDoctorsList();
                                StringBuilder stringBuilder = new StringBuilder();
                                if (jsonObject.getString("gender").equals("M")) {
                                    stringBuilder.append("Dr.");
                                } else {
                                    stringBuilder.append("Dra.");
                                }
                                stringBuilder.append(jsonObject.getString("first_name"));
                                stringBuilder.append(" ");
                                stringBuilder.append(jsonObject.getString("last_name"));
                                myFavoriteDoctorsList.setDoctorsName(stringBuilder.toString());
                                myFavoriteDoctorsList.setDoctorsLocation(jsonObject.getString("location"));
                                myFavoriteDoctorsList.setId(jsonObject.getInt("id"));
                                JSONObject specialityJsonObject = jsonObject.getJSONObject("speciality");
                                myFavoriteDoctorsList.setSpeciality(specialityJsonObject.getString("name"));
                                myFavoriteDoctorsList.setDoctorImage(jsonObject.getString("photo").replace("http://localhost", AppGlobals.SERVER_IP));
                                myFavoriteDoctorsList.setStars(jsonObject.getInt("review_stars"));
                                JSONArray dateJSONArray = jsonObject.getJSONArray("schedule");
                                for (int j= 0; j < dateJSONArray.length(); j++) {
                                    JSONObject dateJObject = dateJSONArray.getJSONObject(j);
                                    myFavoriteDoctorsList.setSchduleDate(dateJObject.getString("date"));
                                    myFavoriteDoctorsList.setTimeId(dateJObject.getInt("id"));
                                    JSONArray timeJSONArray = dateJObject.getJSONArray("time_slots");
                                    ArrayList<TimeSlots> arrayList = new ArrayList<>();
                                    Log.e("TAG", timeJSONArray.toString());
                                    for (int k= 0; k < timeJSONArray.length(); k++) {
                                        Log.e("TAG", "slots ");
                                        JSONObject timeJobject = timeJSONArray.getJSONObject(k);
                                        TimeSlots timeSlots = new TimeSlots();
                                        timeSlots.setEndTime(timeJobject.getString("end_time"));
                                        timeSlots.setStartTime(timeJobject.getString("start_time"));
                                        timeSlots.setTaken(timeJobject.getBoolean("taken"));
                                        timeSlots.setSlotId(timeJobject.getInt("id"));
                                        arrayList.add(timeSlots);
                                    }
                                    Log.i("TAG", "arraylist" + arrayList);
                                    favoriteDoctorsList.add(myFavoriteDoctorsList);
                                    slotsList.put(jsonObject.getInt("id"), arrayList);
                                    Log.e("TAG", slotsList.toString());
                                }

                            }
                            customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                                    R.layout.favt_doc_delegate, favoriteDoctorsList);
                            mListView.setAdapter(customAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
        }
    }


    class CustomAdapter extends ArrayAdapter<FavoriteDoctorsList> {

        private ArrayList<FavoriteDoctorsList> favoriteDoctorsList;
        private ViewHolder viewHolder;

        public CustomAdapter(Context context, int resource, ArrayList<FavoriteDoctorsList> favoriteDoctorsList) {
            super(context, resource);
            this.favoriteDoctorsList = favoriteDoctorsList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.favt_doc_delegate, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView) convertView.findViewById(R.id.user_image);
                viewHolder.name = (TextView) convertView.findViewById(R.id.dr_name);
                viewHolder.specialist = (TextView) convertView.findViewById(R.id.specialist);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.review = (RatingBar) convertView.findViewById(R.id.ratingBar);
                viewHolder.timingList = (RecyclerView) convertView.findViewById(R.id.timing_list);

                viewHolder.name.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.specialist.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.distance.setTypeface(AppGlobals.typefaceNormal);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                viewHolder.timingList.setLayoutManager(layoutManager);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            FavoriteDoctorsList favorite = favoriteDoctorsList.get(position);
            viewHolder.name.setText(favorite.getDoctorsName());
            String[] startLocation = favorite.getDoctorsLocation().split(",");
            String[] endLocation = AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION).split(",");
            viewHolder.distance.setText(" " + String.valueOf(calculationByDistance(new LatLng(Double.parseDouble(startLocation[0]),
                    Double.parseDouble(startLocation[1])), new LatLng(Double.parseDouble(endLocation[0]),
                    Double.parseDouble(endLocation[1])))) + " " + "km");
            viewHolder.specialist.setText(favorite.getSpeciality());
            viewHolder.review.setRating(favorite.getStars());
            Helpers.getBitMap(favorite.getDoctorImage(), viewHolder.circleImageView);
            System.out.println(favorite.getDoctorImage() + "image url");
            TimeSlots timeSlots = slotsList.get(favorite.getId()).get(0);
            Log.i("TAG", timeSlots.getStartTime());
            TimingAdapter timingAdapter = new TimingAdapter(slotsList.get(favorite.getId()));
            viewHolder.timingList.canScrollVertically(LinearLayoutManager.VERTICAL);
            viewHolder.timingList.setHasFixedSize(true);
            viewHolder.timingList.setAdapter(timingAdapter);
            return convertView;
        }

        @Override
        public int getCount() {
            return favoriteDoctorsList.size();
        }
    }

    private class ViewHolder {
        CircleImageView circleImageView;
        TextView name;
        TextView specialist;
        TextView distance;
        RatingBar review;
        RecyclerView timingList;

    }

    class TimingAdapter extends RecyclerView.Adapter<TimingAdapter.Holder> {

        private ArrayList<TimeSlots> timingList;
        private Holder holder;

        public TimingAdapter(ArrayList<TimeSlots> timingList) {
            super();
            this.timingList = timingList;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delegate_timing_list,
                    parent, false);
            holder = new Holder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            holder.setIsRecyclable(false);
            final TimeSlots timeSlots = timingList.get(position);
            holder.timeButton.setText(timeSlots.getStartTime());
            if (!timeSlots.isTaken()) {
                holder.timeButton.setBackground(getResources().getDrawable(R.drawable.normal_time_slot));
            } else {
                holder.timeButton.setBackground(getResources().getDrawable(R.drawable.pressed_time_slot));
            }
            holder.timeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!timeSlots.isTaken()) {
                        startActivity(new Intent(getActivity(), CreateAppointmentActivity.class));
                    } else {
                        Helpers.showSnackBar(getView(), R.string.time_slot_booked);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return timingList.size();
        }

        class Holder extends RecyclerView.ViewHolder{
            Button timeButton;

            public Holder(View itemView) {
                super(itemView);
                timeButton = (Button) itemView.findViewById(R.id.time);
            }
        }
    }



}
