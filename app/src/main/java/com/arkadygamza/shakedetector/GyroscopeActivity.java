package com.arkadygamza.shakedetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;

import static com.arkadygamza.shakedetector.MainActivity.restartActivity;

public class GyroscopeActivity extends AppCompatActivity
      implements View.OnClickListener
{

    private final List<SensorPlotter> mPlotters = new ArrayList<>(3);

    private Observable<?> mShakeObservable;
    private Subscription mShakeSubscription;
    public String state = "DEFAULT";
    public Map<String, Double> increaseValue;
    EditText editValue;
    EditText shagValue;
    SensorPlotter sensorPlotter;
    private int VIEWPORT_SECONDS; Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);
        if (savedInstanceState == null || !savedInstanceState.containsKey("VIEWPORT_SECONDS")) {VIEWPORT_SECONDS=10;
        } else {
            VIEWPORT_SECONDS = (int) savedInstanceState.getSerializable("VIEWPORT_SECONDS");
        }

        increaseValue = new HashMap<>();
        increaseValue.put("X", 0.0);
        increaseValue.put("Y", 0.0);
        increaseValue.put("Z", 0.0);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_gyroscope);
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        shagValue= (EditText) findViewById(R.id.value_shag);
        button=(Button) findViewById(R.id.shag);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int i= Integer.parseInt(shagValue.getText().toString());
                sensorPlotter.changeViewPort(i);
                VIEWPORT_SECONDS=i;
                restartActivity(GyroscopeActivity.this);
            }
        });
//
//
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        state = "DEFAULT";
                        changeState(state);
                        break;

                    case 1:
                        state = "X";
                        changeState(state);
                        break;

                    case 2:
                        state = "Y";
                        changeState(state);
                        break;

                    case 3:
                        state = "Z";
                        changeState(state);
                        break;
                    default:
                        state = "DEFAULT";
                        changeState(state);
                        break;
                }
                Toast.makeText(getApplicationContext(), i + " + " + l, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//
//
        editValue = (EditText) findViewById(R.id.value_edit);
        Button btnX = (Button) findViewById(R.id.btn_x);
        Button btnY = (Button) findViewById(R.id.btn_y);
        Button btnZ = (Button) findViewById(R.id.btn_z);
        Button btnAll = (Button) findViewById(R.id.btn_all);
        Button btnCancel = (Button) findViewById(R.id.btn_cancel);

        btnX.setOnClickListener(this);
        btnY.setOnClickListener(this);
        btnZ.setOnClickListener(this);
        btnAll.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        setupPlotters();
        mShakeObservable = ShakeDetector.create(this);

        btnX.setEnabled(false);
        btnY.setEnabled(false);
        btnZ.setEnabled(false);
        btnCancel.setEnabled(false);
        btnAll.setEnabled(false);

        Observable<String> valueObservable = RxEditText.getTextWatcherObservable(editValue);
        Observable.combineLatest(valueObservable, valueObservable, new Func2<String, String, Boolean>() {
            @Override
            public Boolean call(String s, String s2) {
                if (s.isEmpty() || s2.isEmpty())
                    return false;
                else
                    return true;
            }
        }).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                btnX.setEnabled(aBoolean);
                btnY.setEnabled(aBoolean);
                btnZ.setEnabled(aBoolean);
                btnCancel.setEnabled(aBoolean);
                btnAll.setEnabled(aBoolean);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(VIEWPORT_SECONDS>0){
            outState.putSerializable("VIEWPORT_SECONDS", VIEWPORT_SECONDS);
        }
    }

    public void updateIncValue(String line, String value) {
        increaseValue.put(line, Double.valueOf(value));
        changeIncValue(increaseValue);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.line_gyroscope:
                state = "gyroscope";
                return true;

            case R.id.line_accelerometr:
                state = "accelerometr";
                Intent intent = new Intent(GyroscopeActivity.this,MainActivity.class);
                startActivity(intent);
                return true;

            default:
                return true;
        }
    }

    public void changeState(String state) {
        mPlotters.get(0).setState(state);
    }

    public void changeIncValue(Map<String, Double> value) {
        mPlotters.get(0).setIncValue(value);

    }

    private void setupPlotters() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> linearAccSensors = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        mPlotters.add(new SensorPlotter("GYR", (GraphView) findViewById(R.id.graph_gyroscope), SensorEventObservableFactory.createSensorEventObservable(linearAccSensors.get(0), sensorManager), state, increaseValue));
        mPlotters.add(sensorPlotter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Observable.from(mPlotters).subscribe(SensorPlotter::onResume);
        mShakeSubscription = mShakeObservable.subscribe((object) -> Utils.beep());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Observable.from(mPlotters).subscribe(SensorPlotter::onPause);
        mShakeSubscription.unsubscribe();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_x:
                updateIncValue("X", editValue.getText().toString());
                break;
            case R.id.btn_y:
                updateIncValue("Y", editValue.getText().toString());
                break;
            case R.id.btn_z:
                updateIncValue("Z", editValue.getText().toString());
                break;
            case R.id.btn_all:
                updateIncValue("X", editValue.getText().toString());
                updateIncValue("Y", editValue.getText().toString());
                updateIncValue("Z", editValue.getText().toString());
                break;
            case R.id.btn_cancel:
                updateIncValue("X", "0.0");
                updateIncValue("Y", "0.0");
                updateIncValue("Z", "0.0");
                break;
        }
    }
    public static void restartActivity(Activity activity) {

        if (Build.VERSION.SDK_INT >= 11) {
            activity.recreate();
        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }
}
