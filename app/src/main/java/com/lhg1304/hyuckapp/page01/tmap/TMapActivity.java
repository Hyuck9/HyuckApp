package com.lhg1304.hyuckapp.page01.tmap;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lhg1304.hyuckapp.R;
import com.skp.Tmap.TMapView;

public class TMapActivity extends AppCompatActivity {

    public static final String TMAP_API_KEY = "fc6ca204-a7b4-3076-ac66-644bcba234ec";
    private TMapView mTmapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_01_tmap);

        initializeTMap();
        requestMyLocation();
    }

    /**
     * T-Map 초기화
     * */
    private void initializeTMap() {
        mTmapView = (TMapView) findViewById(R.id.tmapview);
        mTmapView.setSKPMapApiKey(TMAP_API_KEY);
        mTmapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mTmapView.setIconVisibility(true);
        mTmapView.setZoomLevel(15);
        mTmapView.setMapType(TMapView.MAPTYPE_STANDARD);
        mTmapView.setCompassMode(true);
        mTmapView.setTrackingMode(true);
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
        mTmapView.setCenterPoint(location.getLongitude(), location.getLatitude(), true);
    }
}
