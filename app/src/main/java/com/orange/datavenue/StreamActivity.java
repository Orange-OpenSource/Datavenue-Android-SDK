/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.orange.datavenue.client.model.Datasource;
import com.orange.datavenue.client.model.Stream;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.GetStreamOperation;
import com.orange.datavenue.operation.GetStreamsOperation;
import com.orange.datavenue.utils.Errors;
import com.orange.datavenue.utils.IntentHelper;

/**
 * @author Stéphane SANDON
 */
public class StreamActivity extends AppCompatActivity {

    private static final String TAG_NAME = StreamActivity.class.getSimpleName();

    public static final int MODE_LIST = 1;
    public static final int MODE_DETAIL = 2;

    private int mMode = MODE_LIST;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_NAME, "onCreate()");

        IntentHelper.Data intentData = IntentHelper.getIntentData(getIntent());
        if (intentData != null) {
            Model.instance.oapiKey = intentData.ope;
            Model.instance.key     = intentData.key;
            Datasource datasource = new Datasource();
            datasource.setId(intentData.datasourceId);
            Model.instance.currentDatasource = datasource;
            Stream stream = new Stream();
            stream.setId(intentData.streamId);
            Model.instance.currentStream = stream;
        }

        setContentView(R.layout.stream_layout);

        if (intentData != null) {
            mMode = MODE_DETAIL;
            GetStreamOperation getStreamOperation = new GetStreamOperation(Model.instance.oapiKey, Model.instance.key, Model.instance.currentDatasource, new OperationCallback() {
                @Override
                public void process(Object object, Exception exception) {
                    if (exception == null) {
                        if (object != null) {
                            Model.instance.currentStream = (Stream) object;
                            setContentView(R.layout.stream_layout);
                            changeLayout(mMode);
                        }
                    } else {
                        Errors.displayError(StreamActivity.this, exception);
                    }
                }
            });
            getStreamOperation.execute(Model.instance.currentStream.getId());
        } else {
            changeLayout(mMode);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     *
     * @param mode
     */
    public void changeLayout(int mode) {
        mMode = mode;

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (mMode == MODE_LIST) {
            StreamListFragment newFragment = new StreamListFragment();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
        } else {
            StreamDetailFragment newFragment = new StreamDetailFragment();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    private void navigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        navigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}