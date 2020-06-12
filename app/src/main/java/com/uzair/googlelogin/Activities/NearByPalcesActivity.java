package com.uzair.googlelogin.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.uzair.googlelogin.Adapters.NearByPlacesAdapter;
import com.uzair.googlelogin.R;
import com.uzair.googlelogin.Utils.NearPlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NearByPalcesActivity extends AppCompatActivity {

    private Button searchBtn;
    private EditText placeName;
    private List<NearPlaces> list;
    private RecyclerView nearPlacesList;
    private NearByPlacesAdapter adapter;
    private ProgressBar progressBar;
    private double currentLat , currentLng;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private NearPlaces nearPlaces;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_near_by_palces);

        initViews();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);
                String name = placeName.getText().toString().trim();

                if(!name.isEmpty() && !name.equals("")) {
                    list.clear();
                    getNearPlaces(name);
                }
                else
                {
                    Toast.makeText(NearByPalcesActivity.this, "Place name require", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void initViews()
    {
        searchBtn = findViewById(R.id.searchButon);
        placeName = findViewById(R.id.placeName);

        nearPlacesList = findViewById(R.id.listOfPlaces);
        nearPlacesList.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new NearByPlacesAdapter(list , this);

        progressBar = findViewById(R.id.progressbar);
        nearPlaces = new NearPlaces();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }


    private void getNearPlaces(final String name)
    {

        Log.d("UserCurrentLocation", "currentLatAndLng"+currentLat+","+currentLng);

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location="+currentLat+","+currentLng+"&radius=1000&types="+name+"&key=AIzaSyC25fz7R_AYrRD5v6spK89aW9yt2Oiafl4";


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    Log.d("JsonResponse", "onResponse: "+response);

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("results");

                    if(array.length() >0)
                    {
                        Gson gson = new Gson();

                        for (int i = 0; i < array.length(); i++) {

                            NearPlaces nearPlaces = gson.fromJson(String.valueOf(array.get(i)), NearPlaces.class);
                            list.add(nearPlaces);
                        }

                        nearPlacesList.setAdapter(adapter);
                        progressBar.setVisibility(View.GONE);
                    }
                    else
                    {
                        Toast.makeText(NearByPalcesActivity.this, "No "+name+" founds near you", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JsonResponse", "onResponse: "+e.getMessage());
                    progressBar.setVisibility(View.GONE);

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(NearByPalcesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("JsonResponse", "onResponse: "+error.getMessage());
                progressBar.setVisibility(View.GONE);


            }
        });


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);







    }



    // get the user current location
    private void getLastCurrentLocation() {

        if (checkPermission()) {
            if (checkGPS()) {
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        Location location = task.getResult();

                        if (location == null) {
                            requestLocation();
                        } else {

                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();

                            nearPlaces.setCurrentLat(currentLat);
                            nearPlaces.setCurrentLng(currentLng);
                            progressBar.setVisibility(View.GONE);
                            Log.d("UserCurrentLocation", "onComplete: " + currentLng + "," + currentLat);
                            //nearPlaces.setCurrentLat(location.getLatitude());
                            //nearPlaces.setCurrentLng(location.getLongitude());
                        }

                    }
                });

            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermission();
        }

    }

    // reqeust location
    private void requestLocation() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setNumUpdates(1);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    // location call back method to get the last location
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Location result = locationResult.getLastLocation();
            currentLng = result.getLongitude();
            currentLat = result.getLatitude();

            nearPlaces.setCurrentLat(currentLat);
            nearPlaces.setCurrentLng(currentLng);

            progressBar.setVisibility(View.GONE);
            Log.d("UserCurrentLocation", "onLocationResult: " + currentLng + "," + currentLat);

        }
    };

    // check permsision
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            return true;
        }
        return false;
    }

    // request permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 9);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 9 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied unable to use the map", Toast.LENGTH_LONG).show();
        }

    }

    // check gps
    private boolean checkGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER);
    }


    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        getLastCurrentLocation();
    }



}
