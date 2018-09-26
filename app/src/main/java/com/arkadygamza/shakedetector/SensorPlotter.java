package com.arkadygamza.shakedetector;

import android.graphics.Color;
import android.hardware.SensorEvent;
import android.support.annotation.NonNull;
import android.widget.SeekBar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Map;

import rx.Observable;
import rx.Subscription;

/**
 * Draws graph of sensor events
 */
public class SensorPlotter {
    public static final int MAX_DATA_POINTS = 50;
    private int VIEWPORT_SECONDS;
    public static final int FPS = 10;

    @NonNull
    private final String mName;

    private final long mStart = System.currentTimeMillis();

    protected final LineGraphSeries<DataPoint> mSeriesXs;
    protected final LineGraphSeries<DataPoint> mSeriesXf;
    protected final LineGraphSeries<DataPoint> mSeriesYs;
    protected final LineGraphSeries<DataPoint> mSeriesYf;
    protected final LineGraphSeries<DataPoint> mSeriesZs;
    protected final LineGraphSeries<DataPoint> mSeriesZf;
    private final Observable<SensorEvent> mSensorEventObservable;
    private long mLastUpdated = mStart;
    private Subscription mSubscription;
    private String state;
    private Map<String,Double> incValue;

    public SensorPlotter(@NonNull String name, @NonNull  GraphView graphView,
                         @NonNull Observable<SensorEvent> sensorEventObservable,String state,Map<String,Double> incValue) {
        this.incValue = incValue;
        this.state = state;
        mName = name;
        mSensorEventObservable = sensorEventObservable;

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(VIEWPORT_SECONDS * 1000); // number of ms in viewport

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-20);
//        graphView.getViewport().setMaxY(20);

        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);

        mSeriesXs = new LineGraphSeries<>();
        mSeriesXf = new LineGraphSeries<>();
        mSeriesYs = new LineGraphSeries<>();
        mSeriesYf = new LineGraphSeries<>();
        mSeriesZs = new LineGraphSeries<>();
        mSeriesZf = new LineGraphSeries<>();

        mSeriesXs.setColor(Color.RED);
        mSeriesXf.setColor(Color.YELLOW);
        mSeriesYs.setColor(Color.GREEN);
        mSeriesYf.setColor(Color.GRAY);
        mSeriesZs.setColor(Color.BLUE);
        mSeriesZf.setColor(Color.CYAN);

        graphView.addSeries(mSeriesXs);
        graphView.addSeries(mSeriesXf);
        graphView.addSeries(mSeriesYs);
        graphView.addSeries(mSeriesYf);
        graphView.addSeries(mSeriesZs);
        graphView.addSeries(mSeriesZf);
    }
    public SensorPlotter(@NonNull String name, @NonNull  GraphView graphView,
                         @NonNull Observable<SensorEvent> sensorEventObservable,String state,Map<String,Double> incValue, int v) {
        this.incValue = incValue;
        this.state = state;
        this.VIEWPORT_SECONDS=v; System.out.println(VIEWPORT_SECONDS + "!!!!!!!!");
        mName = name;
        mSensorEventObservable = sensorEventObservable;

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(VIEWPORT_SECONDS * 1000); // number of ms in viewport

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-20);
        graphView.getViewport().setMaxY(20);

        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graphView.getGridLabelRenderer().setVerticalLabelsVisible(false);

        mSeriesXs = new LineGraphSeries<>();
        mSeriesXf = new LineGraphSeries<>();
        mSeriesYs = new LineGraphSeries<>();
        mSeriesYf = new LineGraphSeries<>();
        mSeriesZs = new LineGraphSeries<>();
        mSeriesZf = new LineGraphSeries<>();

        mSeriesXs.setColor(Color.RED);
        mSeriesXf.setColor(Color.YELLOW);
        mSeriesYs.setColor(Color.GREEN);
        mSeriesYf.setColor(Color.GRAY);
        mSeriesZs.setColor(Color.BLUE);
        mSeriesZf.setColor(Color.CYAN);

        graphView.addSeries(mSeriesXs);
        graphView.addSeries(mSeriesXf);
        graphView.addSeries(mSeriesYs);
        graphView.addSeries(mSeriesYf);
        graphView.addSeries(mSeriesZs);
        graphView.addSeries(mSeriesZf);
    }


    public void onResume(){
        mSubscription = mSensorEventObservable.subscribe(this::onSensorChanged);
    }

    public void onPause(){
        mSubscription.unsubscribe();
    }

    private void onSensorChanged(SensorEvent event) {
        if (!canUpdateUi()) {
            return;
        }
        switch (state) {
            case "X":
                appendData(mSeriesXs, event.values[0]);
                appendData(mSeriesXf, event.values[0] + incValue.get("X"));
                break;
            case "Y":
                appendData(mSeriesYs, event.values[1]);
                appendData(mSeriesYf, event.values[1] + incValue.get("Y"));
                break;
            case "Z":
                appendData(mSeriesZs, event.values[2]);
                appendData(mSeriesZf, event.values[2] + incValue.get("Z"));
                break;
            case "DEFAULT":
                appendData(mSeriesXs, event.values[0]);
                appendData(mSeriesXf, event.values[0] + incValue.get("X"));
                appendData(mSeriesYs, event.values[1]);
                appendData(mSeriesYf, event.values[1] + incValue.get("Y"));
                appendData(mSeriesZs, event.values[2]);
                appendData(mSeriesZf, event.values[2] + incValue.get("Z"));
                break;
        }
    }

    private boolean canUpdateUi() {
        long now = System.currentTimeMillis();
        if (now - mLastUpdated < 1000 / FPS) {
            return false;
        }
        mLastUpdated = now;
        return true;
    }

    private void appendData(LineGraphSeries<DataPoint> series, double value) {
      series.appendData(new DataPoint(getX(), value), true, MAX_DATA_POINTS);
    }

    public void setState(String s) {
        this.state = s;
    }

    public void setIncValue(Map<String,Double> v) {
        this.incValue = v;
    }
    private long getX() {
        return System.currentTimeMillis() - mStart;
    }

    public void changeViewPort(int v) {
        this.VIEWPORT_SECONDS = v;
        System.out.println(VIEWPORT_SECONDS);
    }

    public void getViewPort(int v) {

    }
}
