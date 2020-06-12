package com.uzair.googlelogin.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.uzair.googlelogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapDirectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleSignInClient googleSignInClient;
    private GoogleMap mMap;
    private EditText edSource, edDestination;
    private Button drawDirectionBtn;
    private double sourceLat, sourceLng, destinationLat, destinationLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        userInfoMethod();

        initViews();
        drawDirectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                edSource = findViewById(R.id.edTextSource);
                edDestination = findViewById(R.id.edTextDestination);

                String sourceLocation = edSource.getText().toString().trim();
                String destLocation = edDestination.getText().toString().trim();


                List<Address> sourceAddressList = null;
                List<Address> destinationAddressList = null;


                if (sourceLocation != null || !sourceLocation.equals("")
                        && !destLocation.equals("") || destLocation != null) {

                    Geocoder geocoder = new Geocoder(MapDirectionActivity.this);

                    try {

                        sourceAddressList = geocoder.getFromLocationName(sourceLocation, 1);
                        destinationAddressList = geocoder.getFromLocationName(destLocation, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    if (sourceAddressList.size() > 0 && destinationAddressList.size() > 0) {
                        Address sourceAddress = sourceAddressList.get(0);
                        sourceLat = sourceAddress.getLatitude();
                        sourceLng = sourceAddress.getLongitude();
                        Log.d("LocationLatLng", "onClick: Destination" + sourceAddress.getLatitude() + "," + sourceAddress.getLongitude());

                        Address destinationAddress = destinationAddressList.get(0);
                        destinationLat = destinationAddress.getLatitude();
                        destinationLng = destinationAddress.getLongitude();
                        Log.d("LocationLatLng", "onClick: Destination" + destinationAddress.getLatitude() + "," + destinationAddress.getLongitude());


                        getDirection();
                    } else {
                        Toast.makeText(MapDirectionActivity.this, "Please enter complete address of you locations", Toast.LENGTH_SHORT).show();
                    }

                } // outer if closing bracket
                else {
                    Toast.makeText(MapDirectionActivity.this, "Address required", Toast.LENGTH_SHORT).show();
                }
            }
        }); // direction btn bracket


    } // onCreate closing bracket

    private void initViews() {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        edSource = findViewById(R.id.edTextSource);
        edDestination = findViewById(R.id.edTextDestination);

        drawDirectionBtn = findViewById(R.id.drawDirectionBtn);
    }

    private void userInfoMethod() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // TextView textView  = findViewById(R.id.text);

        // to get the user information from google api e.g user name, email etc
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();


//           textView.append(personName+"\n");
//           textView.append(personGivenName+"\n");
//           textView.append(personFamilyName+"\n");
//           textView.append(personEmail+"\n");
//           textView.append(personId+"\n");
//           textView.append(personPhoto+"\n");

        }
        else
        {
            startActivity(new Intent(this , MainActivity.class));
            this.finish();
        }


    }

    // logout button click
    public void logout(View view) {
        // to logout user
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MapDirectionActivity.this, "Successfully logout", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MapDirectionActivity.this, MainActivity.class));
                    MapDirectionActivity.this.finish();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    // to make url
    public String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyC25fz7R_AYrRD5v6spK89aW9yt2Oiafl4");
        return urlString.toString();
    }


    // to get the direction send request to get the polyine points
    private void getDirection() {
        //Getting the URL
        String url = makeURL(sourceLat, sourceLng, destinationLat, destinationLng);

        //Showing a dialog till we get the route
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Route", "Please wait...", false, false);

        //Creating a string request
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        //Calling the method drawPath to draw the path
                        drawPath(response);
                        Log.d("responseOfDirection", "onResponse: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.d("errorInVolleyDirection", "onErrorResponse: " + error.getMessage());
                    }
                });

        //Adding the request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //The parameter is the server response
    public void drawPath(String result) {
        //Getting both the coordinates
        Location startPoint = new Location("locationA");
        startPoint.setLatitude(sourceLat);
        startPoint.setLongitude(sourceLng);
        addMarkerOnSourceLocation(new LatLng(sourceLat, sourceLng), "Source");

        Location endPoint = new Location("locationA");
        endPoint.setLatitude(destinationLat);
        endPoint.setLongitude(destinationLng);
        addMarkerOnSourceLocation(new LatLng(destinationLat, destinationLng), "Destination");

        //Calculating the distance in meters
        double distance = startPoint.distanceTo(endPoint);

        //Displaying the distance
        Toast.makeText(this, String.valueOf(distance + " Meters"), Toast.LENGTH_SHORT).show();


        try {
            //Parsing json
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            Log.d("PointsEncode", "drawPath: " + encodedString);
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(8)
                    .color(Color.BLUE)
                    .geodesic(true)
            );


        } catch (JSONException e) {

        }
    }

    // decode the polyline points
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    // add the marker on location
    private void addMarkerOnSourceLocation(LatLng latLng, String location) {
        //Adding marker to map
        mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .title(location)); //Adding a title

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    /// button click to move to nearby place activity
    public void nearByPlaces(View view) {
        Intent intent = new Intent(this, NearByPalcesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}

