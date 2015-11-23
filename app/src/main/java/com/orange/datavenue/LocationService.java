/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orange.datavenue.client.model.Account;
import com.orange.datavenue.client.model.Datasource;
import com.orange.datavenue.client.model.Stream;
import com.orange.datavenue.client.model.Value;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.CreateValueOperation;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Stéphane SANDON
 */
public class LocationService extends Service {

    private static final String TAG_NAME = LocationService.class.getSimpleName();

    private LocationManager mLocationManager;
    private LocationListener mGpsListener;
    private LocationListener mNetworkListener;

    String     mOpeClient         = null;
    String     mKey               = null;
    private Datasource mDatasource;
    private Stream mStream;
    private Value mValue;
    private Notifier mNotifier;

    private static final SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {

        LocationService getService() {
            return LocationService.this;
        }

    }

    public Value getValue() {
        return mValue;
    }

    public void register(Notifier notifier) {
        mNotifier = notifier;
    }

    public void start() {
        Log.d(TAG_NAME, "start()");
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mGpsListener = new MyLocationListener();
        mNetworkListener = new MyLocationListener();

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mGpsListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, mNetworkListener);
    }

    public void stop() {
        Log.d(TAG_NAME, "stop()");

        if (mNetworkListener != null) {
            mLocationManager.removeUpdates(mNetworkListener);
        }

        if (mGpsListener != null) {
            mLocationManager.removeUpdates(mGpsListener);
        }

        mNotifier = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG_NAME, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG_NAME, "onUnBind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG_NAME, "onCreate()");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG_NAME, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_NAME, "onStartCommand()");
        return START_STICKY;
    }

    public void setServiceParameters(String opeClient, String key, Datasource datasource, Stream stream) {
        mOpeClient  = opeClient;
        mKey        = key;
        mDatasource = datasource;
        mStream     = stream;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Value newValue = new Value();

            Date now = new Date();
            newValue.setAt(ISO8601DATEFORMAT.format(now));
            final double lat = location.getLatitude();
            final double lng = location.getLongitude();

            newValue.setLocation(new Double[] { lat, lng});

            String speed = "";

            if (location.hasSpeed()) {
                speed = " \"spd\" : " + location.getSpeed();
            }

            String bearing = "";

            if (location.hasBearing()) {
                bearing = " \"brg\" : " + location.getBearing();
            }

            String altitude = "";

            if (location.hasAltitude()) {
                altitude = " \"alt\" : " + location.getAltitude();
            }

            String jsonValue = "{ \"lat\" : " + lat + " , \"lng\" : " +  lng +
                    ( (location.hasAltitude())?" , " + altitude:"" ) +
                    ( (location.hasSpeed())?" , " + speed:"" ) +
                    ( (location.hasBearing())?" , " + bearing:"" ) +
                    " }";
            String jsonMetada = "{ \"provider\" : \"" + location.getProvider() + "\" }";

            Log.d(TAG_NAME, jsonValue);

            try {
                JSONObject valueJson = (JSONObject) new JSONParser().parse(jsonValue);
                newValue.setValue(valueJson);
            } catch (ParseException e) {
                Log.e(TAG_NAME, e.toString());
            } catch (ClassCastException ce) {
                Log.e(TAG_NAME, ce.toString());
            }

            Log.d(TAG_NAME, jsonMetada);

            try {
                JSONObject metadataJson = (JSONObject) new JSONParser().parse(jsonMetada);
                newValue.setMetadata(metadataJson);
            } catch (ParseException e) {
                Log.e(TAG_NAME, e.toString());
            } catch (ClassCastException ce) {
                Log.e(TAG_NAME, ce.toString());
            }

            mValue = newValue;

            CreateValueOperation createValueOperation =
                    new CreateValueOperation(
                            mOpeClient,
                            mKey,
                            mDatasource,
                            mStream, newValue,
                            new OperationCallback() {

                            @Override
                            public void process(Object object, Exception exception) {
                              if (exception == null) {
                                    Log.d(TAG_NAME, "location sent !)");
                                    if (mNotifier != null) {
                                        mNotifier.process();
                                    }
                              }

                            }
                        });

            createValueOperation.execute("");
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

}
