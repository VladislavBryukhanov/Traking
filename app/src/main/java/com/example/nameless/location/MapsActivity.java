package com.example.nameless.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location location;
    private LocationRequest locReq;
    private GoogleApiClient googleApiClient;
    private com.google.android.gms.location.LocationListener locListener;

    private LatLng loc;
    private Marker myMarker;
//    private Polyline myPath;
    private PolylineOptions recPath;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private int index = 0;

    private Button btnStop, btnGetLoc;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnStop = findViewById(R.id.btnStop);
        btnGetLoc = findViewById(R.id.btnGetLoc);

        Intent intent = getIntent();
        String action = intent.getStringExtra("Action");
        key = intent.getStringExtra("Key");

        loc = new LatLng(0,0);
        recPath = new PolylineOptions().width(4).color(new Color().rgb(73, 104, 170));

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        if(action.equals("traking")) {
            this.traking();
        } else if(action.equals("host")) {
            this.hosting();
        }

        btnGetLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 20.0f));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        myMarker = mMap.addMarker(new MarkerOptions().position(loc));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    //При перевороте экрана зрителем идет наслойка путей, т к они одновременно "восстанавливаются" из сохраненных данных и рисуются новые в реал тайме
    // у хоста или идет наслоение сервисом - если не выключить сервис, то создастся новый в onConncet, тоесть второй, а если выключить, то рвется связь с юзером по логике
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        powerOffLocationService();
//        outState.putParcelable("location", location);
//        outState.putParcelable("locReq", locReq);
//        outState.putParcelable("loc", loc);
//        outState.putParcelable("recPath", recPath);
//        outState.putInt("index", index);
//
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        location = savedInstanceState.getParcelable("location");
//        locReq = savedInstanceState.getParcelable("locReq");
//        loc = savedInstanceState.getParcelable("loc");
//        recPath = savedInstanceState.getParcelable("recPath");
//        index = savedInstanceState.getInt("index");
//        super.onRestoreInstanceState(savedInstanceState);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        powerOffLocationService();
        finish();
    }

    private void hosting() {
        myRef = myRef.child("LocationRecords").child(key);
        locListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    loc = new LatLng(location.getLatitude(), location.getLongitude());
                    myMarker.setPosition(loc);

                    recPath.add(loc);
                    mMap.addPolyline(recPath);
//                    myRef.child(String.valueOf(index)).setValue(loc);
//                    index++;
                    myRef.push().setValue(loc);

                }
            }
        };

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        if (location != null) {
                            loc = new LatLng(location.getLatitude(), location.getLongitude());
                            myMarker.setPosition(loc);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 20.0f));

                            recPath.add(loc);
//                            myPath = mMap.addPolyline(recPath);
                            mMap.addPolyline(recPath);

//                            myRef.child(String.valueOf(index)).setValue(loc);
//                            index++;
                            myRef.push().setValue(loc);

                        }
                        locReq = new LocationRequest();
                        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locReq.setInterval(0);
                        locReq.setFastestInterval(0);

                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locReq, locListener);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
        googleApiClient.connect();

        btnStop.setVisibility(View.VISIBLE);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerOffLocationService();
                finish();
            }
        });
    }

    private void traking() {
        myRef = myRef.child("LocationRecords").child(key);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    CustomLatLng latLng = data.getValue(CustomLatLng.class);
                    loc = new LatLng(latLng.getLatitude(), latLng.getLongitude());
                    recPath.add(loc);
                }
                myMarker.setPosition(loc);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 20.0f));
                mMap.addPolyline(recPath);

                myRef.orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        CustomLatLng latLng = dataSnapshot.getValue(CustomLatLng.class);
                        loc = new LatLng(latLng.getLatitude(), latLng.getLongitude());
                        recPath.add(loc);
                        myMarker.setPosition(loc);
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 20.0f));
                        mMap.addPolyline(recPath);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0) {
                    buildAlert();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        }
    public void buildAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("The goal ended the way");
        builder.setMessage("You can leave this page if you want");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }

    public void powerOffLocationService() {
        if(googleApiClient != null) {  //Если хостинг то он не равен null, если traking то равен и это вызвало бы эксепшн  при попытке остановить локСервис
            myRef.removeValue();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locListener);
            googleApiClient.disconnect();
            locListener = null;
            googleApiClient = null;
        }
    }
}
