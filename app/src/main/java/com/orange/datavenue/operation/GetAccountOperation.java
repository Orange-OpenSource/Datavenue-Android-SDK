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
import com.orange.datavenue.client.api.AccountsApi;
import com.orange.datavenue.client.common.HTTPException;
import com.orange.datavenue.client.common.SDKException;
import com.orange.datavenue.client.model.Account;

/**
 * @author Stéphane SANDON
 */
public class GetAccountOperation extends AsyncTask<String, Void, String> {

    private static final String TAG_NAME = GetAccountOperation.class.getSimpleName();

    Config mConfigAccount;

    String mIssPrimaryMasterKey = "";
    String mOapiKey = "";
    String mAccountId = "";

    Account mAccount = null;
    Exception mException = null;

    OperationCallback mCallback;

    public GetAccountOperation(String primaryKey, String oapiKey, String accountId, OperationCallback op) {
        mIssPrimaryMasterKey = primaryKey;
        mOapiKey = oapiKey;
        mAccountId = accountId;
        mCallback = op;
    }

    @Override
    protected void onPreExecute() {
        mConfigAccount = new Config(mOapiKey, mIssPrimaryMasterKey);
    }

    @Override
    protected String doInBackground(String... strings) {
        AccountsApi accountsApi = new AccountsApi(mConfigAccount);

        try {
            mAccount = accountsApi.getAccount(mAccountId);
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
                mCallback.process(mAccount, mException);
        }
    }
}