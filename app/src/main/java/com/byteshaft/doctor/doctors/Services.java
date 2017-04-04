package com.byteshaft.doctor.doctors;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.doctor.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by s9iper1 on 3/15/17.
 */

public class Services extends Fragment {

    private LinearLayout searchContainer;
    private ListView serviceList;
    private View mBaseView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.services_layout, container, false);
        setHasOptionsMenu(true);
        serviceList = (ListView) mBaseView.findViewById(R.id.service_list);
        searchContainer = new LinearLayout(getActivity());
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
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
        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[]{"service bla bla ", "120.00", "0"});
        data.add(new String[]{"service abc ", "125.00", "1"});
        data.add(new String[]{"service abc ", "125.00", "2"});
        data.add(new String[]{"service abc ", "125.00", "1"});
        data.add(new String[]{"service bcd ", "130.00", "2"});
        data.add(new String[]{"service efg ", "150.00", "0"});
        serviceList.setAdapter(new ServiceAdapter(getActivity().getApplicationContext(), data));
        return mBaseView;
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

    private class ServiceAdapter extends ArrayAdapter<ArrayList<String[]>> {

        private ViewHolder viewHolder;
        private ArrayList<String[]> data;

        public ServiceAdapter(Context context, ArrayList<String[]> data) {
            super(context, R.layout.delegate_service);
            this.data = data;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_service, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.serviceName = (TextView) convertView.findViewById(R.id.service_name);
                viewHolder.servicePrice = (TextView) convertView.findViewById(R.id.service_price);
                viewHolder.serviceStatus = (CheckBox) convertView.findViewById(R.id.service_checkbox);
                viewHolder.removeService = (ImageButton) convertView.findViewById(R.id.remove_service);
                viewHolder.addService = (ImageButton) convertView.findViewById(R.id.add_service);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.serviceName.setText(data.get(position)[0]);
            viewHolder.servicePrice.setText(data.get(position)[1]);
            if (Integer.valueOf(data.get(position)[2]) == 0) {
                viewHolder.serviceStatus.setChecked(false);
            } else if (Integer.valueOf(data.get(position)[2]) == 2) {
                viewHolder.serviceStatus.setVisibility(View.GONE);
                viewHolder.addService.setVisibility(View.VISIBLE);
                viewHolder.removeService.setVisibility(View.GONE);
            } else if (Integer.valueOf(data.get(position)[2]) == 1) {
                viewHolder.serviceStatus.setVisibility(View.VISIBLE);
                viewHolder.serviceStatus.setChecked(true);
                viewHolder.addService.setVisibility(View.GONE);
                viewHolder.removeService.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return data.size();
        }
    }

    class ViewHolder {
        TextView serviceName;
        TextView servicePrice;
        CheckBox serviceStatus;
        ImageButton removeService;
        ImageButton addService;

    }
}
