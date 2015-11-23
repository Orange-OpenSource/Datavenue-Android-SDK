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
import com.orange.datavenue.client.model.AccountsUpdate;

/**
 * @author Stéphane SANDON
 */
public class UpdateAccountOperation extends AsyncTask<String, Void, String> {

    private static final String TAG_NAME = UpdateAccountOperation.class.getSimpleName();

    String  mOpeClient = null;
    String  mKey       = null;
    String  mAccountId = null;
    Account mAccount   = null;
    AccountsUpdate mAccountUpdate = null;

    OperationCallback mCallback  = null;
    Exception         mException = null;

    /**
     *
     * @param opeClient
     * @param key
     * @param accountId
     * @param accountUpdate
     * @param op
     */
    public UpdateAccountOperation(String opeClient, String key, String accountId, AccountsUpdate accountUpdate, OperationCallback op) {
        mOpeClient     = opeClient;
        mKey           = key;
        mAccountId     = accountId;
        mAccountUpdate = accountUpdate;
        mCallback      = op;
    }

    @Override
    protected String doInBackground(String... strings) {
        Config config = new Config(mOpeClient, mKey);
        AccountsApi accountsApi = new AccountsApi(config);

        try {
            accountsApi.updateAccount(mAccountId, mAccountUpdate);
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
