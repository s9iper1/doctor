package com.byteshaft.doctor.doctors;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.gettersetter.DoctorDetails;
import com.byteshaft.doctor.messages.ConversationActivity;
import com.byteshaft.doctor.patients.DoctorsLocator;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.FilterDialog;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

/**
 * Created by s9iper1 on 2/22/17.
 */

public class DoctorsList extends Fragment implements HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<String> addedDates;
    private LinearLayout searchContainer;
    private CustomAdapter customAdapter;
    private HashMap<String, Integer> showingPosition;
    private EditText toolbarSearchView;
    private HttpRequest request;
    private ArrayList<DoctorDetails> doctors;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        doctors = new ArrayList<>();
        getDoctorList();
        mBaseView = inflater.inflate(R.layout.search_doctor, container, false);
        mListView = (ListView) mBaseView.findViewById(R.id.doctors_list);
        searchContainer = new LinearLayout(getActivity());
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
        searchContainer.setLayoutParams(containerParams);

        // Setup search view
        toolbarSearchView = new EditText(getActivity());
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
        toolbarSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    customAdapter.notifyDataSetChanged();
                } else {
                    customAdapter.notifyDataSetChanged();
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
        addedDates = new ArrayList<>();
        showingPosition = new HashMap<>();
        customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                R.layout.doctors_search_delagete, doctors);
        mListView.setAdapter(customAdapter);
        setHasOptionsMenu(true);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {


            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(getActivity(), DoctorDetailsActivity.class));
            }
        });
        return mBaseView;
    }

    private void getDoctorList() {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%spublic/filter-doctors?start_date=%s&end_date=%s",
                AppGlobals.BASE_URL, Helpers.getDate(), Helpers.getDateNextSevenDays()));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }


    @Override
    public void onPause() {
        super.onPause();
        searchContainer.setVisibility(GONE);

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
                startActivity(new Intent(AppGlobals.getContext(), DoctorsLocator.class));
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                   case HttpURLConnection.HTTP_OK:
                       Log.i("TAG", "data " + request.getResponseText());
                       try {
                           JSONObject jsonObject = new JSONObject(request.getResponseText());
                           JSONArray jsonArray = jsonObject.getJSONArray("results");
                           for (int i = 0;i < jsonArray.length(); i++) {
                               JSONObject mainJsonObject = jsonArray.getJSONObject(i);
                               String date = mainJsonObject.getString("date");
                               JSONArray doctorList = jsonArray.getJSONArray(i);
                               for (int j = 0; j< doctorList.length(); j++) {
                                   JSONObject doctorDetail = doctorList.getJSONObject(j);
                                   DoctorDetails doctorDetails = new DoctorDetails();
                                   doctorDetails.setDate(date);
                                   JSONObject speciality = doctorDetail.getJSONObject("speciality");
                                   doctorDetails.setSpeciality(speciality.getString("name"));
                                   doctorDetails.setFirstName(doctorDetail.getString("first_name"));
                                   doctorDetails.setLastName(doctorDetail.getString("last_name"));
                                   doctorDetails.setPhotoUrl(doctorDetail.getString("photo"));
                                   doctorDetails.setGender(doctorDetail.getString("gender"));
                                   doctorDetails.setLocation(doctorDetail.getString("location"));
//                                   doctorDetails.setStartTime(doctorDetail.getString("start_time"));
                                   doctorDetails.setPrimaryPhoneNumber(doctorDetail.getString("phone_number_primary"));
                                   if (doctorDetail.has("phone_number_secondary") && !doctorDetail.isNull("phone_number_secondary")) {
                                       doctorDetails.setPhoneNumberSecondary(doctorDetail.getString("phone_number_secondary"));
                                   }
                                   doctorDetails.setReviewStars(doctorDetail.getInt("review_stars"));
                                   doctorDetails.setUserId(doctorDetail.getInt("user"));
                                   doctors.add(doctorDetails);
                               }
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

    private class CustomAdapter extends ArrayAdapter<ArrayList<DoctorDetails>> {

        private ArrayList<DoctorDetails> doctorsList;
        private ViewHolder viewHolder;

        public CustomAdapter(Context context, int resource, ArrayList<DoctorDetails>
                doctorsList) {
            super(context, resource);
            this.doctorsList = doctorsList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.doctors_search_delagete, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView) convertView.findViewById(R.id.profile_image_view_search);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.specialist = (TextView) convertView.findViewById(R.id.specialist);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.review = (RatingBar) convertView.findViewById(R.id.rating_bar);
                viewHolder.chat = (ImageButton) convertView.findViewById(R.id.chat);
                viewHolder.call = (ImageButton) convertView.findViewById(R.id.call);
                viewHolder.availableTime = (TextView) convertView.findViewById(R.id.available_time);
                viewHolder.openDetailButton = (ImageButton) convertView.findViewById(R.id.open_details);
                viewHolder.dateLayout = (LinearLayout) convertView.findViewById(R.id.date_layout);
                viewHolder.date = (TextView) convertView.findViewById(R.id.date);
                viewHolder.status = (ImageView) convertView.findViewById(R.id.status);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            DoctorDetails singleDoctor = doctorsList.get(position);

            if (addedDates.contains(singleDoctor.getDate()) &&
                    showingPosition.get(singleDoctor.getDate()) != position) {
                viewHolder.dateLayout.setVisibility(GONE);
            } else {
                viewHolder.dateLayout.setVisibility(View.VISIBLE);
                viewHolder.date.setText(singleDoctor.getDate().replaceAll("-", " "));
                addedDates.add(singleDoctor.getDate());
                showingPosition.put(singleDoctor.getDate(), position);
            }
            if (!singleDoctor.isAvailableToChat()) {
                viewHolder.status.setImageResource(R.mipmap.ic_offline_indicator);
            } else {
                viewHolder.status.setImageResource(R.mipmap.ic_online_indicator);
            }
            StringBuilder stringBuilder = new StringBuilder();
            if (singleDoctor.getGender().equals("M")) {
                stringBuilder.append("Dr.");
            } else {
                stringBuilder.append("Dra.");
            }
            stringBuilder.append(singleDoctor.getFirstName());
            stringBuilder.append(" ");
            stringBuilder.append(singleDoctor.getLastName());
            viewHolder.name.setText(stringBuilder.toString());
            viewHolder.specialist.setText(singleDoctor.getSpeciality());
            viewHolder.distance.setText(" " + singleDoctor.get(position)[2] + " km");
            viewHolder.review.setRating(Float.parseFloat(singleDoctor.get(position)[3]));
            viewHolder.availableTime.setText(String.valueOf(singleDoctor.get(position)[4] + " am"));
            viewHolder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                                AppGlobals.CALL_PERMISSION);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "03120676767"));
                        startActivity(intent);
                    }
                }
            });
            viewHolder.chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity().getApplicationContext(),
                            ConversationActivity.class));
                }
            });
            return convertView;
        }


        @Override
        public int getCount() {
            return doctorsList.size();
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
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                    Helpers.showSnackBar(getView(), R.string.permission_denied);
                }
                break;
        }
    }

    class ViewHolder {
        CircleImageView circleImageView;
        TextView name;
        TextView specialist;
        TextView distance;
        RatingBar review;
        ImageButton chat;
        ImageButton call;
        TextView availableTime;
        ImageButton openDetailButton;
        LinearLayout dateLayout;
        TextView date;
        ImageView status;
    }
}
