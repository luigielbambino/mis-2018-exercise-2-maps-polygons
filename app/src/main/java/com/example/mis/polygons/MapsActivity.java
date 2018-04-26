package com.example.mis.polygons;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    boolean mapReady = false;
    boolean polygonStarted = false;
    private static final int finePermissionLocation = 101;
    private EditText markerText;
    static MarkerOptions sampleMarker;
    private SharedPreferences preferences;
    static final CameraPosition homeView = CameraPosition.builder()
            .target(new LatLng(29.184083, -101.348890))
            .zoom(3)
            .tilt(0)
            .bearing(0)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markerText = findViewById(R.id.markerInputText);
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

    /** References:
     *  https://developers.google.com/android/reference/com/google/android/gms/maps/model/CameraPosition
     *  https://developers.google.com/maps/documentation/android-api/views?hl=es-419
     *  https://developer.android.com/reference/android/widget/Button.html
     *  https://developer.android.com/training/basics/data-storage/shared-preferences.html?hl=es-419
     *  https://developer.android.com/reference/android/content/SharedPreferences.html?hl=es-419#getString(java.lang.String,%20java.lang.String)**/

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
        //mMap.addMarker(sampleMarker);
        loadPreferences();
        LatLng weimar = new LatLng(50.980089, 11.326042);
        CameraPosition cameraView = new CameraPosition.Builder().target(weimar).zoom(17).tilt(65).build();
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
}