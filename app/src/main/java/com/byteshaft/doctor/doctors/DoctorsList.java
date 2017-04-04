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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.byteshaft.doctor.messages.ConversationActivity;
import com.byteshaft.doctor.patients.DoctorsLocator;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.FilterDialog;
import com.byteshaft.doctor.utils.Helpers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

/**
 * Created by s9iper1 on 2/22/17.
 */

public class DoctorsList extends Fragment {

    private View mBaseView;
    private ListView mListView;
    private HashMap<Integer, String[]> doctorsList;
    private ArrayList<String> addedDates;
    private LinearLayout searchContainer;
    private CustomAdapter customAdapter;
    private HashMap<String, Integer> showingPosition;
    private EditText toolbarSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        doctorsList = new HashMap<>();
        addedDates = new ArrayList<>();
        showingPosition = new HashMap<>();
        doctorsList.put(0, new String[]{"Bilal", "Dermatologist", "2", "2.5", "9:30", "12-February-2017", "0"});
        doctorsList.put(1, new String[]{"Husnain", "Dermatologist", "2", "3.5", "7:30", "12-February-2017" , "1"});
        doctorsList.put(2, new String[]{"shahid", "Dermatologist", "3", "4.5", "5:30", "12-February-2017", "1"});
        doctorsList.put(3, new String[]{"Omer", "Dermatologist", "4", "1.5", "6:30", "13-February-2017", "0"});
        doctorsList.put(4, new String[]{"Mohsin", "Dermatologist", "6", "3.2", "7:30", "13-February-2017", "1"});
        doctorsList.put(5, new String[]{"Imran Hakeem", "Dermatologist", "8", "2.3", "5:30", "13-February-2017", "1"});
        customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                R.layout.doctors_search_delagete, doctorsList);
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
            default:return false;
        }
    }

    class CustomAdapter extends ArrayAdapter<HashMap<Integer, String[]>> {

        private HashMap<Integer, String[]> doctorsList;
        private ViewHolder viewHolder;

        public CustomAdapter(Context context, int resource, HashMap<Integer,
                String[]> doctorsList) {
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

            if (addedDates.contains(doctorsList.get(position)[5]) &&
                    showingPosition.get(doctorsList.get(position)[5]) != position) {
                viewHolder.dateLayout.setVisibility(GONE);
            } else {
                viewHolder.dateLayout.setVisibility(View.VISIBLE);
                viewHolder.date.setText(doctorsList.get(position)[5].replaceAll("-" , " "));
                addedDates.add(doctorsList.get(position)[5]);
                showingPosition.put(doctorsList.get(position)[5], position);
            }
            if (Integer.valueOf(doctorsList.get(position)[6]) == 0) {
                viewHolder.status.setImageResource(R.mipmap.ic_offline_indicator);
            } else {
                viewHolder.status.setImageResource(R.mipmap.ic_online_indicator);
            }
            viewHolder.name.setText(doctorsList.get(position)[0]);
            viewHolder.specialist.setText(doctorsList.get(position)[1]);
            viewHolder.distance.setText(" "+doctorsList.get(position)[2]+" km");
            viewHolder.review.setRating(Float.parseFloat(doctorsList.get(position)[3]));
            viewHolder.availableTime.setText(String.valueOf(doctorsList.get(position)[4]+ " am"));
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
