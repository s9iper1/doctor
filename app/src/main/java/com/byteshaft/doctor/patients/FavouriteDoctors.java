package com.byteshaft.doctor.patients;

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
import com.byteshaft.doctor.utils.FilterDialog;
import com.byteshaft.doctor.utils.Helpers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by s9iper1 on 3/1/17.
 */

public class FavouriteDoctors extends Fragment {

    private View mBaseView;
    private ListView mListView;
    private HashMap<Integer, String[]> doctorsList;
    private ArrayList<String> addedDates;
    private LinearLayout searchContainer;
    private CustomAdapter customAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.favourite_doctors, container, false);
        mListView = (ListView) mBaseView.findViewById(R.id.favt_doctors_list);
        searchContainer = new LinearLayout(getActivity());
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
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
        doctorsList.put(0, new String[]{"Bilal", "Dermatologist", "2", "2.5", "9:30", "12-February-2017", "0"});
        doctorsList.put(1, new String[]{"Husnain", "Dermatologist", "2", "3.5", "7:30", "12-February-2017", "1"});
        doctorsList.put(2, new String[]{"shahid", "Dermatologist", "3", "4.5", "5:30", "12-February-2017", "1"});
        doctorsList.put(3, new String[]{"Omer", "Dermatologist", "4", "1.5", "6:30", "13-February-2017", "0"});
        doctorsList.put(4, new String[]{"Mohsin", "Dermatologist", "6", "3.2", "7:30", "13-February-2017", "1"});
        doctorsList.put(5, new String[]{"Imran Hakeem", "Dermatologist", "8", "2.3", "5:30", "13-February-2017", "1"});
        customAdapter = new CustomAdapter(getActivity().getApplicationContext(),
                R.layout.favt_doc_delegate, doctorsList);
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
        return mBaseView;
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
                        .inflate(R.layout.favt_doc_delegate, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView) convertView.findViewById(R.id.user_image);
                viewHolder.name = (TextView) convertView.findViewById(R.id.dr_name);
                viewHolder.specialist = (TextView) convertView.findViewById(R.id.specialist);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.review = (RatingBar) convertView.findViewById(R.id.ratingBar);
                viewHolder.timingList = (RecyclerView) convertView.findViewById(R.id.timing_list);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                viewHolder.timingList.setLayoutManager(layoutManager);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.name.setText(doctorsList.get(position)[0]);
            viewHolder.specialist.setText(doctorsList.get(position)[1]);
            viewHolder.distance.setText(" " + doctorsList.get(position)[2] + " km");
            viewHolder.review.setRating(Float.parseFloat(doctorsList.get(position)[3]));
            ArrayList<String> arrayList = new ArrayList();
            arrayList.add("01:00");
            arrayList.add("01:30");
            arrayList.add("02:00");
            arrayList.add("02:30");
            arrayList.add("03:00");
            arrayList.add("03:30");
            arrayList.add("04:00");
            arrayList.add("04:30");
            arrayList.add("05:00");
            arrayList.add("05:30");
            arrayList.add("06:00");
            arrayList.add("06:30");
            arrayList.add("07:00");
            arrayList.add("07:30");
            arrayList.add("08:00");
            arrayList.add("08:30");
            arrayList.add("09:00");
            arrayList.add("09:30");
            arrayList.add("10:00");
            arrayList.add("10:30");
            arrayList.add("11:00");
            arrayList.add("11:30");
            arrayList.add("12:00");
            arrayList.add("12:30");

            TimingAdapter timingAdapter = new TimingAdapter(arrayList);
            viewHolder.timingList.canScrollVertically(LinearLayoutManager.VERTICAL);
            viewHolder.timingList.setHasFixedSize(true);
            viewHolder.timingList.setAdapter(timingAdapter);
            return convertView;
        }

        @Override
        public int getCount() {
            return doctorsList.size();
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

        private ArrayList<String> timingList;
        private Holder holder;

        public TimingAdapter( ArrayList<String> timingList) {
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
            holder.timeButton.setText(timingList.get(position));
            if (position % 2 == 0) {
                holder.timeButton.setPressed(true);
            } else {
                holder.timeButton.setPressed(false);
            }
            holder.timeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position % 2 == 0) {
                        holder.timeButton.setPressed(true);
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
