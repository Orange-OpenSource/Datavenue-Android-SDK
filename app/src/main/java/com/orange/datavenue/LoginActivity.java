/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orange.datavenue.utils.Errors;
import com.orange.datavenue.client.model.Account;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.GetAccountOperation;

/**
 * @author Stéphane SANDON
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG_NAME = LoginActivity.class.getSimpleName();

    private SharedPreferences mPreferences;
    private EditText mOapiKey;
    private EditText mPrimaryMasterKey;
    private EditText mAccountId;
    private TextView mRegister;
    private Button mLoginButton;
    private Button mCancelButton;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        if (mPreferences == null) {
            mPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        }

        Intent intent = getIntent();

        String account = intent.hasExtra("account")?intent.getStringExtra("account"):"";
        String ope = intent.hasExtra("ope")?intent.getStringExtra("ope"):"";
        String key = intent.hasExtra("key")?intent.getStringExtra("key"):"";

        mOapiKey = (EditText)findViewById(R.id.oapi_key_value);
        mPrimaryMasterKey = (EditText)findViewById(R.id.primary_master_key_value);
        mAccountId = (EditText)findViewById(R.id.account_id_value);

        mOapiKey.setText(ope);
        mPrimaryMasterKey.setText(key);
        mAccountId.setText(account);

        mLoginButton = (Button)findViewById(R.id.login_button);
        mCancelButton = (Button)findViewById(R.id.cancel_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oapi = mOapiKey.getText().toString().trim();
                String primaryMaster = mPrimaryMasterKey.getText().toString().trim();
                String account = mAccountId.getText().toString().trim();

                GetAccountOperation getAccountOperation =
                        new GetAccountOperation(
                                oapi,
                                primaryMaster,
                                account,
                                new OperationCallback() {
                                    @Override
                                    public void process(Object object, Exception exception) {
                                        SharedPreferences.Editor editor = mPreferences.edit();

                                        if (exception == null) {
                                            Account account = (Account) object;
                                            Model.instance.account = account;

                                            Model.instance.oapiKey = mOapiKey.getText().toString().trim();
                                            Model.instance.key = mPrimaryMasterKey.getText().toString().trim();
                                            Model.instance.accountId = mAccountId.getText().toString().trim();

                                            editor.putString("OAPI_KEY", Model.instance.oapiKey);
                                            editor.putString("PRIMARY_MASTER_KEY", Model.instance.key);
                                            editor.putString("ACCOUNT_ID", Model.instance.accountId);
                                            editor.putBoolean("LOGIN_VERIFIED", true);
                                            editor.commit();

                                            setResult(0);
                                            finish();
                                        } else {
                                            Errors.displayError(LoginActivity.this, exception);

                                            editor.putBoolean("LOGIN_VERIFIED", false);
                                            editor.commit();
                                            setResult(-1);
                                        }
                                    }
                                });
                getAccountOperation.execute();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(-1);
                finish();
            }
        });

        mRegister = (TextView)findViewById(R.id.register);
        mRegister.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

}