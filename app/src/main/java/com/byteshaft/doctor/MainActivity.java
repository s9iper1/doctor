package com.byteshaft.doctor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.byteshaft.doctor.accountfragments.AccountManagerActivity;
import com.byteshaft.doctor.accountfragments.UserBasicInfoStepOne;
import com.byteshaft.doctor.doctors.Appointments;
import com.byteshaft.doctor.doctors.Dashboard;
import com.byteshaft.doctor.doctors.DoctorsList;
import com.byteshaft.doctor.doctors.MyPatients;
import com.byteshaft.doctor.doctors.MySchedule;
import com.byteshaft.doctor.doctors.Services;
import com.byteshaft.doctor.introscreen.IntroScreen;
import com.byteshaft.doctor.messages.MainMessages;
import com.byteshaft.doctor.patients.FavouriteDoctors;
import com.byteshaft.doctor.patients.MyAppointments;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.byteshaft.doctor.utils.Helpers.getBitMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    public static MainActivity sInstance;
    private static final int STORAGE_PERMISSION = 0;

    public static MainActivity getInstance() {
        return sInstance;
    }

    private SwitchCompat doctorOnlineSwitch;
    private SwitchCompat patientOnlineSwitch;
    private CircleImageView profilePicture;
    private HttpRequest request;
    private boolean isError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        setContentView(R.layout.activity_main);
        if (AccountManagerActivity.getInstance() != null) {
            AccountManagerActivity.getInstance().finish();
        }
        if (IntroScreen.getInstance() != null) {
            IntroScreen.getInstance().finish();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.i("TAG", "token  "+ AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));

        if (AppGlobals.isDoctor()) {
            View headerView;
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

                }
            });
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setItemIconTintList(null);
            headerView = getLayoutInflater().inflate(R.layout.nav_header_doctor, navigationView, false);
            navigationView.addHeaderView(headerView);
            navigationView.inflateMenu(R.menu.doctor_menus);
            /// Doctor's Navigation items

            TextView docName = (TextView) headerView.findViewById(R.id.doc_nav_name);
            TextView docEmail = (TextView) headerView.findViewById(R.id.doc_nav_email);
            TextView docSpeciality = (TextView) headerView.findViewById(R.id.doc_nav_speciality);
            TextView docExpDate = (TextView) headerView.findViewById(R.id.doc_nav_expiry_date);
            doctorOnlineSwitch = (SwitchCompat) headerView.findViewById(R.id.doc_nav_online_switch);
            profilePicture = (CircleImageView) headerView.findViewById(R.id.nav_imageView);

            //setting typeface
            docName.setTypeface(AppGlobals.typefaceNormal);
            docEmail.setTypeface(AppGlobals.typefaceNormal);
            docSpeciality.setTypeface(AppGlobals.typefaceNormal);
            docExpDate.setTypeface(AppGlobals.typefaceNormal);

            // setting up information
            docName.setText(AppGlobals.getStringFromSharedPreferences(
                    AppGlobals.KEY_FIRST_NAME) + " " +
                    AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
            docEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
            docSpeciality.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DOC_SPECIALITY));
            docExpDate.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_SUBSCRIPTION_TYPE));
            doctorOnlineSwitch.setTypeface(AppGlobals.typefaceNormal);
            if (AppGlobals.isOnline()) {
                doctorOnlineSwitch.setChecked(true);
                doctorOnlineSwitch.setText(R.string.online);
            } else {
                doctorOnlineSwitch.setChecked(false);
                doctorOnlineSwitch.setText(R.string.offline);
            }
            doctorOnlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    switch (compoundButton.getId()) {
                        case R.id.doc_nav_online_switch:
                            if (isError) {
                                isError = false;
                            } else {
                                if (b) {
                                    doctorOnlineSwitch.setText(R.string.online);
                                } else {
                                    doctorOnlineSwitch.setText(R.string.offline);
                                }
                                changeStatus(b);
                                doctorOnlineSwitch.setEnabled(false);
                            }
                            break;
                    }
                }
            });
            loadFragment(new Dashboard());

        } else {
            View headerView;
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

                }
            });
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setItemIconTintList(null);
            headerView = getLayoutInflater().inflate(R.layout.nav_header_patient, navigationView, false);
            navigationView.addHeaderView(headerView);
            navigationView.inflateMenu(R.menu.patient_menu);
            TextView patientName = (TextView) headerView.findViewById(R.id.patient_nav_name);
            TextView patientEmail = (TextView) headerView.findViewById(R.id.patient_nav_email);
            TextView patientAge = (TextView) headerView.findViewById(R.id.patient_nav_age);
            patientOnlineSwitch = (SwitchCompat) headerView.findViewById(R.id.patient_nav_online_switch);
            profilePicture = (CircleImageView) headerView.findViewById(R.id.nav_imageView);
            patientName.setText(AppGlobals.getStringFromSharedPreferences(
                    AppGlobals.KEY_FIRST_NAME) + " " +
                    AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
            String years = Helpers.calculateAge(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH));
            patientAge.setText(years + " years");
            patientEmail.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMAIL));
            // setting typeface
            patientName.setTypeface(AppGlobals.typefaceNormal);
            patientEmail.setTypeface(AppGlobals.typefaceNormal);
            patientAge.setTypeface(AppGlobals.typefaceNormal);
            patientOnlineSwitch.setTypeface(AppGlobals.typefaceNormal);

            patientOnlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    switch (compoundButton.getId()) {
                        case R.id.patient_nav_online_switch:
                            if (isError) {
                                isError = false;
                            } else {
                                if (b) {
                                    patientOnlineSwitch.setText(R.string.online);
                                } else {
                                    patientOnlineSwitch.setText(R.string.offline);
                                }
                                changeStatus(b);
                                patientOnlineSwitch.setEnabled(false);
                            }
                            break;
                    }
                }
            });
            loadFragment(new DoctorsList());
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
            alertDialogBuilder.setTitle(getResources().getString(R.string.storage_permission_dialog_title));
            alertDialogBuilder.setMessage(getResources().getString(R.string.storage_permission_message))
                    .setCancelable(false).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } else {
            if (AppGlobals.isLogin() && AppGlobals.getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL) != null) {
                String url = String.format("%s" + AppGlobals
                        .getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL), AppGlobals.SERVER_IP);
                Log.i("TAG", "url " + url);
                getBitMap(url, profilePicture);
            }
        }
    }

    private void changeStatus(boolean status) {
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("PATCH", String.format("%sprofile", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AppGlobals.KEY_CHAT_STATUS, status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.send(jsonObject.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String url = String.format("%s" + AppGlobals
                                    .getStringFromSharedPreferences(AppGlobals.SERVER_PHOTO_URL),
                            AppGlobals.SERVER_IP);
                    getBitMap(url, profilePicture);
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_denied);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            loadFragment(new Dashboard());
        } else if (id == R.id.nav_search_doctor) {
            loadFragment(new DoctorsList());
        } else if (id == R.id.nav_appointment) {
            loadFragment(new MyAppointments());
        } else if (id == R.id.nav_favt_doc) {
            loadFragment(new FavouriteDoctors());
        } else if (id == R.id.nav_patients) {
            loadFragment(new MyPatients());
        } else if (id == R.id.nav_doc_appointment) {
            loadFragment(new Appointments());
        } else if (id == R.id.nav_messages) {
            loadFragment(new MainMessages());
        } else if (id == R.id.nav_profile) {
            loadFragment(new UserBasicInfoStepOne());
        } else if (id == R.id.nav_schedule) {
            loadFragment(new MySchedule());
        } else if (id == R.id.nav_my_services) {
            loadFragment(new Services());
        } else if (id == R.id.nav_exit) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
            alertDialogBuilder.setTitle("Confirmation");
            alertDialogBuilder.setMessage("Do you really want to exit?").setCancelable(false).setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
            alertDialogBuilder.setTitle("Confirmation");
            alertDialogBuilder.setMessage("Do you really want to logout?")
                    .setCancelable(false).setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AppGlobals.clearSettings();
                            AppGlobals.firstTimeLaunch(true);
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), IntroScreen.class));
                        }
                    });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.container, fragment);
        tx.commit();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        if (!AppGlobals.isDoctor()) {
                            patientOnlineSwitch.setEnabled(true);
                            AppGlobals.saveChatStatus(patientOnlineSwitch.isChecked());

                        } else {
                            doctorOnlineSwitch.setEnabled(true);
                            AppGlobals.saveChatStatus(doctorOnlineSwitch.isChecked());
                        }
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        if (AppGlobals.isDoctor()) {
                            Helpers.alertDialog(this, getResources().getString(R.string.account),
                                    getResources().getString(R.string.account_not_activated),
                                    doctorOnlineSwitch);

                        }
                }
        }
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        isError = true;
        Helpers.showSnackBar(findViewById(android.R.id.content), exception.getLocalizedMessage());
        if (!AppGlobals.isDoctor()) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    patientOnlineSwitch.setEnabled(true);
                }
            }, 500);
            if (patientOnlineSwitch.isChecked()) {
                patientOnlineSwitch.setChecked(false);
                patientOnlineSwitch.setText(R.string.offline);
            } else {
                patientOnlineSwitch.setChecked(true);
                patientOnlineSwitch.setText(R.string.online);
            }
        } else {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doctorOnlineSwitch.setEnabled(true);
                }
            }, 500);
            if (doctorOnlineSwitch.isChecked()) {
                doctorOnlineSwitch.setChecked(false);
                doctorOnlineSwitch.setText(R.string.offline);
            } else {
                doctorOnlineSwitch.setChecked(true);
                doctorOnlineSwitch.setText(R.string.online);
            }

        }

    }
}
