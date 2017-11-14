package com.lhg1304.hyuckapp.page01.google_map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lhg1304.hyuckapp.R;

public class GoogleMapActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;

    SupportMapFragment mMapFragment;
    GoogleMap mGoogleMap;

    MarkerOptions mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_01_google_map);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_google_map);
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d("GoogleMapActivity", "GoogleMap 객체가 준비됨.");

                mGoogleMap = googleMap;
            }
        });

        MapsInitializer.initialize(this);   // 이전 단말에서 문제가 있는 경우가 있기 때문에 해당 코드 추가

        requestMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    public void requestMyLocation() {
        long minTime = 10000;
        float minDistance = 0;

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        showCurrentLocation(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }
        );
    }

    public void showCurrentLocation(Location location) {
        LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        showMarker(location);
    }

    public void showMarker(Location location) {
        if (mMarker == null) {
            mMarker = new MarkerOptions();
            mMarker.position(new LatLng(location.getLatitude()+0.01, location.getLongitude()-0.01));
            mMarker.title("커피숍");
            mMarker.snippet("약속장소");
            mMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.hg_icon));

            mGoogleMap.addMarker(mMarker);
        } else {
            mMarker.position(new LatLng(location.getLatitude()+0.01, location.getLongitude()-0.01));
        }
    }

}
