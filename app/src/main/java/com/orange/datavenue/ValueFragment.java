/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.orange.datavenue.client.model.Page;
import com.orange.datavenue.utils.Errors;
import com.orange.datavenue.adapter.ValueAdapter;
import com.orange.datavenue.client.model.Value;
import com.orange.datavenue.model.Model;

import com.orange.datavenue.operation.CreateValueOperation;
import com.orange.datavenue.operation.DeleteValueOperation;
import com.orange.datavenue.operation.GetValueOperation;

import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class ValueFragment extends ListFragment implements Notifier {

    private static final String TAG_NAME = ValueFragment.class.getSimpleName();

    private ValueAdapter mValueAdapter;
    private List<Value> mValues = new ArrayList<Value>();
    private int mPageNumber = 1;
    private int mPosition = -1;
    private int mTotalElement = -1;
    private boolean mIsLoading = false;

    private android.app.Dialog mDialog;

    private LocationService mLocationService;
    private boolean mIsBound =false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationService = ((LocationService.LocalBinder)service).getService();
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationService = null;
            mIsBound = true;
        }

    };

    private LinearLayout llInfo;
    private LinearLayout llValue;
    private TextView tvLatitude;
    private TextView tvLongitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (!mIsBound) {
            Intent service = new Intent(getActivity(), LocationService.class);
            getActivity().startService(service);
            getActivity().bindService(new Intent(getActivity(), LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.value_fragment_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mValueAdapter = new ValueAdapter(getActivity(), R.layout.value_item, R.id.name, mValues);

        setListAdapter(mValueAdapter);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int position = firstVisibleItem + visibleItemCount;
                int limit = totalItemCount;
                mPosition = firstVisibleItem;
                //Log.d(TAG_NAME, "position : " + mPosition);

                if (position >= limit && totalItemCount > 0 && !mIsLoading) {
                    if (totalItemCount < mTotalElement) {
                        Log.d(TAG_NAME, "load next page");
                        mPageNumber++;
                        mIsLoading = true;
                        getValues(mPageNumber, false);
                    }
                }
            }

        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Model.instance.currentValue = (Value) parent.getItemAtPosition(position);
                mSelected = position;
                getListView().setItemChecked(mSelected, true);
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                return true;
            }

        });

        getValues(mPageNumber, true);
    }

    private int mSelected;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_mode_value, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_value:
                    mDialog = new android.app.Dialog(getActivity());

                    mDialog.setContentView(R.layout.delete_dialog);
                    mDialog.setTitle(R.string.delete);

                    TextView info = (TextView) mDialog.findViewById(R.id.info_label);
                    info.setText(String.format(getString(R.string.delete_value), Model.instance.currentValue.getId()));

                    Button deleteButton = (Button) mDialog.findViewById(R.id.delete_button);

                    deleteButton.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            Log.d(TAG_NAME, "datasource : " + Model.instance.currentDatasource.getId());
                            Log.d(TAG_NAME, "stream : " + Model.instance.currentStream.getId());
                            Log.d(TAG_NAME, "value : " + Model.instance.currentValue.getId());

                            DeleteValueOperation deleteValueOperation = new DeleteValueOperation(
                                    Model.instance.account,
                                    Model.instance.currentDatasource,
                                    Model.instance.currentStream,
                                    Model.instance.currentValue,
                                    new OperationCallback() {
                                        @Override
                                        public void process(Object object, Exception exception) {
                                            if (exception == null) {
                                                mPageNumber = 1;
                                                getValues(mPageNumber, true);
                                            } else {
                                                Errors.displayError(getActivity(), exception);
                                            }
                                        }
                                    });

                            deleteValueOperation.execute("");

                            mDialog.dismiss();
                        }

                    });

                    Button cancelDeleteButton = (Button) mDialog.findViewById(R.id.cancel_button);
                    cancelDeleteButton.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            mDialog.dismiss();
                        }
                    });

                    mDialog.setCancelable(false);
                    mDialog.show();
                    mode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(TAG_NAME, "onDestroyActionMode()");
            mActionMode = null;
        }
    };


    private void setTitle(int num, int total) {
        if (getActivity() != null) {
            getActivity().setTitle(String.format(getString(R.string.value_activity_count), num, total));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_value, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_add_value:
                /**
                 * Add value
                 */
                mDialog = new android.app.Dialog(getActivity());
                mDialog.setContentView(R.layout.create_value_dialog);
                mDialog.setTitle(R.string.add_value);

                final EditText at = (EditText) mDialog.findViewById(R.id.at);
                final EditText metadata = (EditText) mDialog.findViewById(R.id.metadata);
                final EditText latitude = (EditText) mDialog.findViewById(R.id.latitude);
                final EditText longitude = (EditText) mDialog.findViewById(R.id.longitude);
                final EditText value = (EditText) mDialog.findViewById(R.id.value);

                SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                Date now = new Date();
                at.setText(ISO8601DATEFORMAT.format(now));

                Button addButton = (Button) mDialog.findViewById(R.id.add_button);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG_NAME, "at : " + at.getText().toString());
                        Log.d(TAG_NAME, "metadata : " + metadata.getText().toString());
                        Log.d(TAG_NAME, "value : " + value.getText().toString());

                        Value newValue = new Value();
                        newValue.setAt(at.getText().toString());

                        Double[] location = null;

                        String strLatitude = latitude.getText().toString();
                        String strLongitude = longitude.getText().toString();

                        try {
                            if ((!"".equals(strLatitude))&&(!"".equals(strLongitude))) {
                                location = new Double[2];
                                location[0] = Double.parseDouble(strLatitude);
                                location[1] = Double.parseDouble(strLongitude);
                            }
                        } catch(NumberFormatException e) {
                            Log.e(TAG_NAME, e.toString());
                            location = null;
                        }

                        newValue.setLocation(location);

                        if (!"".equals(value.getText().toString())) {

                            try {
                                JSONObject valueJson = (JSONObject) new JSONParser().parse(value.getText().toString());
                                newValue.setValue(valueJson);
                            } catch (ParseException e) {
                                Log.e(TAG_NAME, e.toString());
                                newValue.setValue(value.getText().toString());
                            } catch (ClassCastException ce) {
                                Log.e(TAG_NAME, ce.toString());
                                newValue.setValue(value.getText().toString());
                            }

                        } else {
                            newValue.setValue(null);
                        }

                        if (!"".equals(metadata.getText().toString())) {

                            try {
                                JSONObject metadataJson = (JSONObject) new JSONParser().parse(metadata.getText().toString());
                                newValue.setMetadata(metadataJson);
                            } catch (ParseException e) {
                                Log.e(TAG_NAME, e.toString());
                                newValue.setMetadata(metadata.getText().toString());
                            } catch (ClassCastException ce) {
                                Log.e(TAG_NAME, ce.toString());
                                newValue.setMetadata(metadata.getText().toString());
                            }

                        } else {
                            newValue.setMetadata(null);
                        }

                        CreateValueOperation createValueOperation = new CreateValueOperation(
                                Model.instance.account,
                                Model.instance.currentDatasource,
                                Model.instance.currentStream,
                                newValue,
                                new OperationCallback() {
                                    @Override
                                    public void process(Object object, Exception exception) {
                                        if (exception == null) {
                                            mPageNumber = 1;
                                            getValues(mPageNumber, true);
                                        } else {
                                            Errors.displayError(getActivity(), exception);
                                        }
                                    }
                                });

                        createValueOperation.execute("");

                        mDialog.dismiss();
                    }
                });

                Button cancelButton = (Button) mDialog.findViewById(R.id.cancel_button);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });

                mDialog.setCancelable(false);
                mDialog.show();
                return true;
            case R.id.action_add_sensor_sensor:
                /**
                 * Geolocation
                 */
                mDialog = new android.app.Dialog(getActivity());

                mDialog.setContentView(R.layout.add_sensor_value_dialog);
                mDialog.setTitle(R.string.geolocation);

                llInfo = (LinearLayout)mDialog.findViewById(R.id.info);
                llValue = (LinearLayout)mDialog.findViewById(R.id.value);
                llValue.setVisibility(View.GONE);
                tvLatitude = (TextView)mDialog.findViewById(R.id.latitude);
                tvLongitude = (TextView)mDialog.findViewById(R.id.longitude);

                if (mLocationService != null) {
                    mLocationService.setServiceParameters(Model.instance.account, Model.instance.currentDatasource, Model.instance.currentStream);
                    mLocationService.register(this);
                    mLocationService.start();
                }

                Button cButton = (Button) mDialog.findViewById(R.id.cancel_button);
                cButton.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {

                                                   if (mLocationService != null) {
                                                       mLocationService.stop();
                                                   }

                                                   tvLatitude = null;
                                                   tvLongitude = null;

                                                   mDialog.dismiss();
                                               }
                                           });
                mDialog.setCancelable(false);
                mDialog.show();
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
            Log.d(TAG_NAME, "onResume()");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG_NAME, "onStart()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG_NAME, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG_NAME, "onDestroy()");

        if (mDialog != null) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }

        if (mIsBound) {
            if (mLocationService != null) {
                mLocationService.stop();
            }
        }
        getActivity().unbindService(mConnection);
    }

    private void getValues(int page, final boolean clearList) {
        String[] params = { String.format("%1$d", page) };
        GetValueOperation getValuesOperation =
                new GetValueOperation(Model.instance.account, Model.instance.currentDatasource, Model.instance.currentStream,
                        new OperationCallback() {
                            @Override
                            public void process(Object object, Exception exception) {
                                if (exception == null) {
                                    Page<List<Value>> page = (Page<List<Value>>)object;
                                    List<Value> values = page.object;

                                    if (clearList) {
                                        Model.instance.values.clear();
                                    }
                                    Model.instance.values.addAll(values);

                                    mTotalElement = page.totalCount;
                                    updateValues();
                                    setTitle(Model.instance.values.size(), page.totalCount);
                                    mIsLoading = false;
                                } else {
                                    Errors.displayError(getActivity(), exception);
                                }
                            }
                        });
        getValuesOperation.execute(params);
    }

    public void updateValues() {
        Log.d(TAG_NAME, "updateValues()");

        if (mValueAdapter == null) {
            Log.d(TAG_NAME, "mValueAdapter is null");
        } else {
            int lastPosition = mPosition;
            mValueAdapter.changeDataSet(Model.instance.values); // reset the position
            if (lastPosition <= getListView().getAdapter().getCount()) {
                getListView().setSelection(lastPosition);
            }
        }
    }

    @Override
    public void process() {
        Log.d(TAG_NAME, "process()");

        if (mLocationService != null) {
            final Value value = mLocationService.getValue();

            if (value != null) {
                if (value.getLocation() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (llInfo != null && llValue != null) {
                                llInfo.setVisibility(View.GONE);
                                llValue.setVisibility(View.VISIBLE);
                            }

                            if (tvLatitude != null && tvLongitude != null) {
                                tvLatitude.setText("" + value.getLocation()[0]);
                                tvLongitude.setText("" + value.getLocation()[1]);
                            }
                        }
                    });
                }
            }

        }

        mPageNumber = 1;
        getValues(mPageNumber, true);
    }
}