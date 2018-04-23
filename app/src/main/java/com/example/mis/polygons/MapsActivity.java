package com.example.mis.polygons;

import android.Manifest;
import android.content.Context;
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
    private static final int finePermissionLocation = 101;
    private EditText markerText;
    static MarkerOptions sampleMarker;
    static LatLng poly1 = new LatLng(50.981019, 11.332072);
    static LatLng poly2 = new LatLng(50.980920, 11.332685);
    static LatLng poly3 = new LatLng(50.980329, 11.332425);
    static LatLng poly4 = new LatLng(50.980418, 11.331831);

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

        sampleMarker = new MarkerOptions()
                .position(new LatLng(50.974505, 11.329045))
                .title("Bauhaus-Universitaet Weimar")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pinmarker));


        Button mapViewBtn = findViewById(R.id.map_button);
        mapViewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mapReady)
                    mMap.setMapType((GoogleMap.MAP_TYPE_NORMAL));
            }
        });

        Button satViewBtn = findViewById(R.id.sat_button);
        satViewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mapReady)
                    mMap.setMapType((GoogleMap.MAP_TYPE_SATELLITE));
            }
        });

        Button homeViewBtn = findViewById(R.id.home_button);
        homeViewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mapReady)
                    goTo(homeView);
            }
        });

        mapFragment.getMapAsync(this);

        markerText = findViewById(R.id.markerInputText);

    }

    private void goTo(CameraPosition location) {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(location), 2000, null);
    }

    /** https://developers.google.com/android/reference/com/google/android/gms/maps/model/CameraPosition
     * https://developers.google.com/maps/documentation/android-api/views?hl=es-419
     * https://developer.android.com/reference/android/widget/Button.html **/

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
        mMap.addMarker(sampleMarker);

        LatLng weimar = new LatLng(50.980089, 11.326042);
        CameraPosition cameraView = new CameraPosition.Builder().target(weimar).zoom(17).tilt(65).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraView));

        /**mMap.addPolyline(new PolylineOptions().geodesic(true)
            .add(poly1).add(poly2).add(poly3).add(poly4).add(poly1));**/

        mMap.addPolygon(new PolygonOptions()
                .add(poly1, poly2, poly3, poly4, poly1)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(markerText.getText().toString())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pinmarker)));
                markerText.setText("");
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