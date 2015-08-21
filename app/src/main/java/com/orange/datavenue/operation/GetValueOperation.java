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
import com.orange.datavenue.client.model.Page;
import com.orange.datavenue.client.model.Stream;
import com.orange.datavenue.client.model.Value;

import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class GetValueOperation extends AsyncTask<String, Void, String> {

    private static final String TAG_NAME = GetValueOperation.class.getSimpleName();

    Account mAccount = null;
    Datasource mDatasource = null;
    Stream mStream = null;
    Exception mException = null;

    Page<List<Value>> mValues;
    OperationCallback mCallback;

    public GetValueOperation(Account account, Datasource datasource, Stream stream, OperationCallback op) {
        mAccount = account;
        mCallback = op;
        mDatasource = datasource;
        mStream = stream;
    }

    @Override
    protected String doInBackground(String... strings) {
        String page = strings[0];

        Config datasourceConfig = new Config(mAccount.getOpeClientId(), mAccount.getMasterKey().getValue());
        DatasourcesApi datasourceApi = new DatasourcesApi(datasourceConfig);

        try {
            mValues = datasourceApi.listValues(mDatasource.getId(), mStream.getId(), page, "10");
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
                mCallback.process(mValues, mException);
        }
    }
}