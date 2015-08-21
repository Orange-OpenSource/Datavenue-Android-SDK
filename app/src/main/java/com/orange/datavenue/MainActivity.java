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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orange.datavenue.utils.Errors;
import com.orange.datavenue.client.model.Account;
import com.orange.datavenue.client.model.AccountsUpdate;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.GetAccountOperation;
import com.orange.datavenue.operation.UpdateAccountOperation;

/**
 * @author Stéphane SANDON
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_NAME = MainActivity.class.getSimpleName();

    private TextView mAccountId;
    private TextView mAccountCreated;
    private TextView mAccountUpdated;
    private EditText mAccountFirstname;
    private EditText mAccountLastname;
    private EditText mAccountEmail;
    private TextView mAccountStatus;
    private TextView mAccountOpeClientId;
    private EditText mAccountOpeUserPassword;
    private Button mUpdateButton;
    private Button mDatasourcesButton;

    private SharedPreferences mPreferences;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.account_layout);

        Model.instance.reset();

        if (mPreferences == null) {
            mPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        }

        mAccountId = (TextView) findViewById(R.id.account_id);
        mAccountCreated = (TextView) findViewById(R.id.account_created);
        mAccountUpdated = (TextView) findViewById(R.id.account_updated);
        mAccountFirstname = (EditText) findViewById(R.id.account_firstname);
        mAccountLastname = (EditText) findViewById(R.id.account_lastname);
        mAccountEmail = (EditText) findViewById(R.id.account_email);
        mAccountStatus = (TextView) findViewById(R.id.account_status);
        mAccountOpeClientId = (TextView) findViewById(R.id.account_ope_clientid);
        mAccountOpeUserPassword = (EditText) findViewById(R.id.account_user_password);
        mUpdateButton = (Button) findViewById(R.id.update);
        mDatasourcesButton = (Button) findViewById(R.id.datasources);

        // if not already logged
        if (! mPreferences.getBoolean("LOGIN_VERIFIED", false)) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, 100);
        } else {
            getAccount();
        }

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Model.instance.account != null) {
                    AccountsUpdate accountsUpdate = new AccountsUpdate();
                    String userPassword = mAccountOpeUserPassword.getText().toString();

                    if (userPassword != null) {
                        if (!"".equals(userPassword)) {
                            accountsUpdate.setUserPassword(userPassword);
                        }
                    }

                    String firstname = mAccountFirstname.getText().toString();

                    if (firstname != null) {
                        if (!"".equals(firstname)) {
                            accountsUpdate.setFirstname(firstname);
                        }
                    }

                    String lastname = mAccountLastname.getText().toString();

                    if (lastname != null) {
                        if (!"".equals(lastname)) {
                            accountsUpdate.setLastname(lastname);
                        }
                    }

                    String email = mAccountEmail.getText().toString();

                    if (email != null) {
                        if (!"".equals(email)) {
                            accountsUpdate.setEmail(email);
                        }
                    }

                    String opeClientId = Model.instance.account.getOpeClientId();

                    if (opeClientId != null) {
                        if (!"".equals(opeClientId)) {
                            accountsUpdate.setOpeClientId(opeClientId);
                        }
                    }

                    Log.d(TAG_NAME, "accountsUpdate : " + accountsUpdate.toString());
                    updateAccount(accountsUpdate);
                }
            }
        });

        mDatasourcesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Model.instance.account != null) {
                    Intent intent = new Intent(getApplicationContext(), DatasourceActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("LOGIN_VERIFIED", false);
            editor.commit();
            Model.instance.reset();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // we came from login screen
        if (requestCode == 100) {
            if (resultCode == 0) {
                getAccount();
            } else {
                finish();
            }
        }
    }

    private void getAccount() {
        String[] params = { "" };
        String oapiKey = mPreferences.getString("OAPI_KEY", "");
        String primaryKey = mPreferences.getString("PRIMARY_MASTER_KEY", "");
        String accountId = mPreferences.getString("ACCOUNT_ID", "");

        GetAccountOperation getAccountOperation =
                new GetAccountOperation(primaryKey, oapiKey, accountId,
                        new OperationCallback() {
                            @Override
                            public void process(Object object, Exception exception) {
                                if (exception == null) {
                                    Account account = (Account) object;
                                    Model.instance.account = account;

                                    mAccountId.setText(account.getId());
                                    mAccountCreated.setText(account.getCreated());
                                    mAccountUpdated.setText(account.getUpdated());
                                    mAccountFirstname.setText(account.getFirstname());
                                    mAccountLastname.setText(account.getLastname());
                                    mAccountEmail.setText(account.getEmail());
                                    mAccountStatus.setText(account.getStatus());
                                    mAccountOpeClientId.setText(account.getOpeClientId());
                                    mAccountOpeUserPassword.setText(account.getUserPassword());
                                } else {
                                    Errors.displayError(MainActivity.this, exception);
                                }
                            }
                        });
        getAccountOperation.execute(params);
    }

    private void updateAccount(AccountsUpdate accountsUpdate) {
        String[] params = { "" };
        String oapiKey = mPreferences.getString("OAPI_KEY", "");
        String primaryKey = mPreferences.getString("PRIMARY_MASTER_KEY", "");
        String accountId = mPreferences.getString("ACCOUNT_ID", "");

        UpdateAccountOperation updateAccountOperation =
                new UpdateAccountOperation(primaryKey, oapiKey, accountId, accountsUpdate,
                        new OperationCallback() {
                            @Override
                            public void process(Object object, Exception exception) {
                                if (exception == null) {
                                    Account account = (Account) object;
                                    Model.instance.account = account;

                                    mAccountId.setText(account.getId());
                                    mAccountCreated.setText(account.getCreated());
                                    mAccountUpdated.setText(account.getUpdated());
                                    mAccountFirstname.setText(account.getFirstname());
                                    mAccountLastname.setText(account.getLastname());
                                    mAccountEmail.setText(account.getEmail());
                                    mAccountStatus.setText(account.getStatus());
                                    mAccountOpeClientId.setText(account.getOpeClientId());
                                    mAccountOpeUserPassword.setText(account.getUserPassword());
                                } else {
                                    Errors.displayError(MainActivity.this, exception);
                                }
                            }
                        });
        updateAccountOperation.execute(params);
    }

}