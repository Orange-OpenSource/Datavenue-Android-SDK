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
import com.orange.datavenue.client.model.Account;
import com.orange.datavenue.client.model.Datasource;

/**
 * @author Stéphane SANDON
 */
public class CreateDatasourceOperation extends AsyncTask<String, Void, String> {

    private static final String TAG_NAME = CreateDatasourceOperation.class.getSimpleName();

    String     mOpeClient         = null;
    String     mKey               = null;
    Datasource mDatasource        = null;
    Datasource mCreatedDatasource = null;

    OperationCallback mCallback  = null;
    Exception         mException = null;

    /**
     *
     * @param opeClient
     * @param key
     * @param datasource
     * @param callback
     */
    public CreateDatasourceOperation(String opeClient, String key, Datasource datasource, OperationCallback callback) {
        mOpeClient  = opeClient;
        mKey        = key;
        mCallback   = callback;
        mDatasource = datasource;
    }

    @Override
    protected String doInBackground(String... strings) {
        Config config = new Config(mOpeClient, mKey);
        DatasourcesApi datasourceApi = new DatasourcesApi(config);

        try {
            mCreatedDatasource = datasourceApi.createDatasource(mDatasource);
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
            mCallback.process(mCreatedDatasource, mException);
        }
    }
}