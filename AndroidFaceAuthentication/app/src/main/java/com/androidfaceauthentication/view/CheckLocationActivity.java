package com.androidfaceauthentication.view;

import static java.lang.Boolean.TRUE;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.network.APIInterface;
import com.androidfaceauthentication.network.RetrofitClient;
import com.androidfaceauthentication.view.pojo.LokasiRuangRequest;
import com.androidfaceauthentication.view.pojo.LokasiRuangResponse;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckLocationActivity extends AppCompatActivity {
    private String TAG = "CheckLocationActivity";

    private final static int REQUEST_CODE = 100;

    FusedLocationProviderClient fusedLocationProviderClient;
    TextView showLocation, withinRadius;
    Double currentLatitude, currentLongtitude;
    Double targetLatitude;
    Double targetLongtitude;
    LocationRequest locationRequest;

    String idjadwal, nim, email;
    APIInterface apiInterface;
    Button btnCobaLagi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_location);
        showLocation = findViewById(R.id.showLocation);
        withinRadius = findViewById(R.id.withinradius);
        btnCobaLagi = findViewById(R.id.btn_check_location);
        Intent receiveintent = getIntent();
        idjadwal = receiveintent.getStringExtra("idjadwal");
        nim = receiveintent.getStringExtra("nim");
        email = receiveintent.getStringExtra("email");
        btnCobaLagi.setVisibility(View.GONE);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        LokasiRuangRequest lokasiRuangRequest = new LokasiRuangRequest(idjadwal);
        Call<LokasiRuangResponse> call = apiInterface.getLokasiRuang(lokasiRuangRequest);
        call.enqueue(new Callback<LokasiRuangResponse>() {
            @Override
            public void onResponse(Call<LokasiRuangResponse> call, Response<LokasiRuangResponse> response) {
                String latitude1 = response.body().getLatitude();
                String longtitude1 = response.body().getLongtitude();
                targetLatitude = Double.valueOf(latitude1);
                targetLongtitude = Double.valueOf(longtitude1);

            }

            @Override
            public void onFailure(Call<LokasiRuangResponse> call, Throwable t) {

            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        getCurrentLocation();
        btnCobaLagi.setOnClickListener( view -> {
            withinRadius.setText("proses pengecekan lokasi");
            getCurrentLocation();
        });

    }

    public boolean isWithinRadius(double userLatitude, double userLongitude, double targetLatitude, double targetLongitude, float radiusInMeters) {
        Location userLocation = new Location("");
        userLocation.setLatitude(userLatitude);
        userLocation.setLongitude(userLongitude);

        Location targetLocation = new Location("");
        targetLocation.setLatitude(targetLatitude);
        targetLocation.setLongitude(targetLongitude);

        float distance = userLocation.distanceTo(targetLocation); // Distance in meters
        Log.d(TAG, "device distance to location: " + distance);

        //Toast.makeText(getApplicationContext(), String.valueOf(distance), Toast.LENGTH_LONG).show();
        return distance <= radiusInMeters;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    getCurrentLocation();

                } else {

                    turnOnGPS();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {
        btnCobaLagi.setVisibility(View.GONE);
        Log.d(TAG, "function get current location dipanggil");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(CheckLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(CheckLocationActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(CheckLocationActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult.getLocations().size() > 0) {

                                        int index = locationResult.getLocations().size() - 1;
                                        currentLatitude = locationResult.getLocations().get(index).getLatitude();
                                        currentLongtitude = locationResult.getLocations().get(index).getLongitude();
                                        Log.d(TAG, "currentLatitude: " + currentLatitude + ", " + "currentLongtitude: " + currentLongtitude);
                                        Log.d(TAG, "targetLatitude: " + targetLatitude + ", " + "targetLongtitude: " + targetLongtitude);
                                        //showLocation.setText("Your Location: " + " " + "Latitude: " + currentLatitude + " " + "Longitude: " + currentLongtitude);

                                        boolean statusRadius = isWithinRadius(currentLatitude, currentLongtitude, targetLatitude, targetLongtitude, 10.0f);
                                        //akurasi.setText("akurasi : " + location.getAccuracy() + "M");
                                        if (statusRadius) {
                                            withinRadius.setText("USER BERADA DI DALAM RADIUS RUANGAN");
                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Do something after 5s = 5000ms
                                                    Intent intent = new Intent(CheckLocationActivity.this, ScannerActivity.class);
                                                    intent.putExtra("idjadwal", idjadwal);
                                                    intent.putExtra("nim", nim);
                                                    intent.putExtra("email", email);
                                                    startActivity(intent);

                                                }
                                            }, 1000);

                                        } else {
                                            withinRadius.setText("USER BERADA DI LUAR RADIUS RUANGAN");
                                            btnCobaLagi.setVisibility(View.VISIBLE);
                                            //Intent intent = new Intent(CheckLocationActivity.this, JadwalActivity.class);

                                        }


                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(CheckLocationActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(CheckLocationActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }


}