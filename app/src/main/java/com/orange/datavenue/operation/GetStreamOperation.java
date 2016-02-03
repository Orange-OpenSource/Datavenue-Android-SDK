/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue.operation;

import android.os.AsyncTask;
import android.util.Log;

import com.orange.datavenue.OperationCallback;
import com.orange.datavenue.client.Config;
import com.orange.datavenue.client.api.DatasourcesApi;
import com.orange.datavenue.client.common.HTTPException;
import com.orange.datavenue.client.common.SDKException;
import com.orange.datavenue.client.model.Datasource;

import com.orange.datavenue.client.model.Stream;

/**
 * @author Stéphane SANDON
 */
public class GetStreamOperation extends AsyncTask<String, Void, String> {

    private static final String TAG_NAME = GetStreamOperation.class.getSimpleName();

    String     mOpeClient  = null;
    String     mKey        = null;
    Datasource mDatasource = null;
    Stream     mStream = null;

    OperationCallback mCallback = null;
    Exception         mException = null;

    /**
     *
     * @param opeClient
     * @param key
     * @param datasource
     * @param op
     */
    public GetStreamOperation(String opeClient, String key, Datasource datasource, OperationCallback op) {
        mOpeClient  = opeClient;
        mKey        = key;
        mCallback   = op;
        mDatasource = datasource;
    }

    @Override
    protected String doInBackground(String... strings) {
        Config config = new Config(mOpeClient, mKey);
        DatasourcesApi datasourceApi = new DatasourcesApi(config);

        try {
            mStream = datasourceApi.getStream(mDatasource.getId(), strings[0]);
        } catch(HTTPException e) {
            Log.e(TAG_NAME, e.toString());
            mException = e;
        } catch (SDKException s) {
            Log.e(TAG_NAME, s.toString());
            mException = s;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mCallback != null) {
            mCallback.process(mStream, mException);
        }
    }
}