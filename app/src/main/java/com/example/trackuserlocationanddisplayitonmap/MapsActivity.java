package com.example.trackuserlocationanddisplayitonmap;

import static android.widget.Toast.LENGTH_LONG;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.trackuserlocationanddisplayitonmap.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    Location lastKnownLocation;
    String address;

    //Methode just used for some testing, not relevant to the actual code
    public Location getMockLocation(double latitude, double longitude) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                LatLng userLocation = new LatLng( location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10f));

                //The app displays a Toast with the current/changed address info
                try {
                    List<Address> addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);
                    if(addresses != null && addresses.size() >= 1)
                    address = addresses.get(0).getAddressLine(0);
                    Toast.makeText(getApplicationContext(), address,LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        // Add a marker to Users current/last-known position and move the camera
            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

        //If the location of the user changes for at least 10 meters, the location gets updated and onLocationChanged in the locationListener gets called.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,10, locationListener);


    }
}


/*Eine Sache zum merken: Aus irgendeinem Grund wird crasht selbst die default Google Maps Activity wenn man die App nicht mit den Run-Button
startet, sondern mit dem "Apply Changes and restart Activity"-Button. Mir ist noch nicht ganz klar, warum das passiert. Danach lässt sich die
App auch nicht mehr mit dem Run-Button starten. Um das aber zu fixen, kann man einfach random irgendeine Permission im Manifest hinzufügen
oder rausnehmen, dann lässt sich die App wieder mit "Run"-Button starten. Aber den "Apply Changes and restart Activity"-Button kann man immernoch
nicht benutzen

Mittlerweile benutzt man einen GeocodeListener (Seit API 33), wenn man mit Adressen arbeitet. Habe es jetzt aber ohne gemacht, weil man wenig Material im Internet dazu findet. Scheint aber nicht
viel schwieriger zu sein.
 */