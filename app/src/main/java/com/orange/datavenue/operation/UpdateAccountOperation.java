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

    Config mConfigAccount;

    String mIssPrimaryMasterKey = "";
    String mOapiKey = "";
    String mAccountId = "";
    Account mAccount;
    Exception mException = null;
    AccountsUpdate mAccountUpdate;

    OperationCallback mCallback;

    public UpdateAccountOperation(String primaryKey, String oapiKey, String accountId, AccountsUpdate accountUpdate, OperationCallback op) {
        mIssPrimaryMasterKey = primaryKey;
        mOapiKey = oapiKey;
        mAccountId = accountId;
        mAccountUpdate = accountUpdate;
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
