package com.example.nameless.location;

import android.*;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnHosting, btnTraking;
    private ListView lvTraking;
    private EditText etName;
    private Intent intent;
    private ArrayList<String> items;

    private String key;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.linearLayout);
        tabSpec.setIndicator("Hosting");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.linearLayout2);
        tabSpec.setIndicator("Traking");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        btnHosting = findViewById(R.id.btnHosting);
        btnTraking = findViewById(R.id.btnTraking);
        lvTraking = findViewById(R.id.lvTraking);
        etName = findViewById(R.id.etName);
        btnTraking.setEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        btnHosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, MapsActivity.class);
                String action = "host";
//                key = myRef.child("LocationRecords").push().getKey();
                key = String.valueOf(etName.getText());
                if(items.indexOf(key) == -1) {
                    intent.putExtra("Key", key);
                    intent.putExtra("Action", action);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "This name already exist, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnTraking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, MapsActivity.class);
                String action = "traking";
                intent.putExtra("Key", key);
                intent.putExtra("Action", action);
                startActivity(intent);
            }
        });

        myRef.child("LocationRecords").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items = new ArrayList<>();
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    items.add(data.getKey());
                }

                String[] itemArray =new String[items.size()];
                items.toArray(itemArray);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, itemArray);
                lvTraking.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lvTraking.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                key = lvTraking.getAdapter().getItem(i).toString();
                btnTraking.setEnabled(true);
            }
        });
    }
}
