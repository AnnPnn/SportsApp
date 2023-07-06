package com.anpn.sportsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anpn.sportsapp.todos.TodoActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

/**
 * Экран таймера
 */
public class StopwatchActivity extends AppCompatActivity {


    private Chronometer mChronometer;
    private Button buttonStart;
    private Button buttonStop;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(getSupportActionBar()).hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);


        BottomNavigationView btNavView = findViewById(R.id.btNavView);

        btNavView.setSelectedItemId(R.id.stopwatch);


        btNavView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.workoutPlan:
                    startActivity(new Intent(getApplicationContext(), TodoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.stopwatch:
                    return true;
            }
            return false;
        });


        mChronometer = findViewById(R.id.chronometer);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        Button buttonReset = findViewById(R.id.buttonReset);

        buttonStart.setOnClickListener(v -> {
                    mChronometer.start();
                    buttonStart.setVisibility(View.GONE);
                    buttonStop.setVisibility(View.VISIBLE);
                }


        );

        buttonStop.setOnClickListener(
                v -> {
                    mChronometer.stop();
                    buttonStart.setVisibility(View.VISIBLE);
                    buttonStop.setVisibility(View.GONE);
                }


        );

        buttonReset.setOnClickListener(v -> mChronometer.setBase(SystemClock.elapsedRealtime()));

    }

}