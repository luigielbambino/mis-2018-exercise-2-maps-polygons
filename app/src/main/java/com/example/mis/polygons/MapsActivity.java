package com.example.mis.polygons;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import static com.google.maps.android.SphericalUtil.computeArea;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    boolean mapReady = false;
    boolean polygonStarted = false;
    private static final int finePermissionLocation = 101;
    private EditText markerText;
    private SharedPreferences preferences;
    static ArrayList<LatLng> coordinatesForPolygon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markerText = findViewById(R.id.markerInputText);

        Button clearMapBtn = findViewById(R.id.clear_button);
        clearMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mapReady)
                    mMap.clear();
                    clearPreferences();
            }
        });
    }

    /* References:
     *  https://developers.google.com/android/reference/com/google/android/gms/maps/model/CameraPosition
     *  https://developers.google.com/maps/documentation/android-api/views?hl=es-419
     *  https://developer.android.com/reference/android/widget/Button.html
     *  https://developer.android.com/training/basics/data-storage/shared-preferences.html?hl=es-419
     *  https://developer.android.com/reference/android/content/SharedPreferences.html?hl=es-419#getString(java.lang.String,%20java.lang.String)
     *  */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, finePermissionLocation);
            }
        }
        loadPreferences();
        LatLng weimar = new LatLng(50.980089, 11.326042);
        CameraPosition cameraView = new CameraPosition.Builder().target(weimar).zoom(17).tilt(0).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraView));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String inputText = markerText.getText().toString();
                if(inputText.equals("") || inputText.equals(" ")){inputText = "Just another Marker";}
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(inputText)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pinmarker)));
                markerText.setText("");
                savedPreferences(latLng, inputText);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case finePermissionLocation:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Location permissions are required", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    public void startPolygonButtonOnClick(View v)
    {
        Button buttonStartPolygon = (Button) v;
        buttonStartPolygon.setVisibility(View.GONE);

        Button buttonEndPolygon = findViewById(R.id.end_polygon_button);
        buttonEndPolygon.setVisibility(View.VISIBLE);

        coordinatesForPolygon = new ArrayList<>();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(markerText.getText().toString()));
                markerText.setText("");

                coordinatesForPolygon.add(latLng);
            }
        });
    }

    public void endPolygonButtonOnClick(View view)
    {
        mMap.setOnMapClickListener(null);

        Button buttonEndPolygon = (Button) view;
        buttonEndPolygon.setVisibility(View.GONE);

        Button buttonStartPolygon = findViewById(R.id.start_polygon_button);
        buttonStartPolygon.setVisibility(View.VISIBLE);

        if (coordinatesForPolygon.size() >= 3)
        {
            mMap.addPolygon(new PolygonOptions()
                    .addAll(coordinatesForPolygon)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(25,15,30,90)));

            LatLng centerPoint = getPolygonCenterPoint(coordinatesForPolygon);

            /*
            * Reference: http://googlemaps.github.io/android-maps-utils/javadoc/com/google/maps/android/SphericalUtil.html#computeArea-java.util.List-
            * We tried to use this method to calculate the area but the area was returned in Degrees square and
            * we did not know how to convert that to km square/ meter square so we ended up using SphericalUtil
            * library from android-maps-utils instead fourth is calculate the area
            * */
            double area = computeArea(coordinatesForPolygon);
            String unitOfMeasurements = " m²";

            if (area > 1000000)
            {
                // reference: https://www.metric-conversions.org/area/square-meters-to-square-kilometers.htm
                area = ((double) Math.round(area / 10000)) / 100;
                unitOfMeasurements = " km²";
            }
            else
            {
                area = ((double) Math.round(area * 100) / 100.0);
            }

            mMap.addMarker(new MarkerOptions()
                    .position(centerPoint)
                    .title("Area = " + area + unitOfMeasurements)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            // Reference: https://developers.google.com/maps/documentation/android-api/marker#customize_the_marker_color
        }
    }

    // Reference: https://stackoverflow.com/questions/18440823/how-do-i-calculate-the-center-of-a-polygon-in-google-maps-android-api-v2
    private LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList){
        LatLng centerLatLng;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0 ; i < polygonPointsList.size() ; i++)
        {
            builder.include(polygonPointsList.get(i));
        }
        LatLngBounds bounds = builder.build();
        centerLatLng =  bounds.getCenter();

        return centerLatLng;
    }

    // Reference: http://www.mathopenref.com/coordpolygonarea2.html
    // Reference: https://www.mathopenref.com/coordpolygonarea.html
    private double calculateAreaOfPolygon(ArrayList<LatLng> polygonPointsList){

        double area = 0;
        int lastPoint = polygonPointsList.size() - 1;

        if (polygonPointsList.size() >= 3)
        {
            for(int i = 0; i < polygonPointsList.size(); i++)
            {
                area = area +
                       (polygonPointsList.get(i).latitude * polygonPointsList.get(lastPoint).longitude) -
                       (polygonPointsList.get(i).longitude * polygonPointsList.get(lastPoint).latitude);

                lastPoint = i;
            }

            area = area / 2;

            if (area < 0)
            {
                area = area * -1;
            }
        }

        return area;
    }

    private void savedPreferences(LatLng latLang, String markText){
        if(!polygonStarted){
            preferences =  MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
            String savedString = preferences.getString("markers", "" );
            SharedPreferences.Editor editor = preferences.edit();
            String makersInfo = markText + "/" + String.valueOf(latLang.latitude) + "/" + String.valueOf(latLang.longitude) + "&" + savedString;
            editor.putString("markers", makersInfo);
            editor.commit();
            Toast.makeText(getApplicationContext(), "Marker Saved", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPreferences(){
        preferences =  MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
        String defaultMarkers = preferences.getString("markers", "No markers available" );

        if(!defaultMarkers.equals("No markers available")) {
            String textM;
            Double latM;
            Double langM;
            LatLng latLngM;
            String[] markersArray = defaultMarkers.split("&");
            String[] singleMarker;

            for (int i = 0; i < markersArray.length; i++) {
                singleMarker = markersArray[i].split("/");
                textM = singleMarker[0];
                latM = Double.parseDouble(singleMarker[1]);
                langM = Double.parseDouble(singleMarker[2]);
                latLngM = new LatLng(latM, langM);

                mMap.addMarker(new MarkerOptions()
                        .position(latLngM)
                        .title(textM)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pinmarker)));
            }
            Toast.makeText(getApplicationContext(), "Markers loaded", Toast.LENGTH_LONG).show();
        } else {Toast.makeText(getApplicationContext(), defaultMarkers, Toast.LENGTH_LONG).show();}
    }

    private void clearPreferences() {
        preferences =  MapsActivity.this.getPreferences(Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }
}