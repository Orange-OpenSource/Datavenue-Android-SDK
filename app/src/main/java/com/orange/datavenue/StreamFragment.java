/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orange.datavenue.client.Config;
import com.orange.datavenue.client.model.Callback;
import com.orange.datavenue.client.model.Page;
import com.orange.datavenue.client.model.Unit;
import com.orange.datavenue.operation.CreateStreamOperation;
import com.orange.datavenue.operation.UpdateStreamOperation;
import com.orange.datavenue.utils.Errors;
import com.orange.datavenue.adapter.StreamAdapter;
import com.orange.datavenue.client.model.Stream;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.DeleteStreamOperation;
import com.orange.datavenue.operation.GetStreamOperation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class StreamFragment extends ListFragment {

    private static final String TAG_NAME = StreamFragment.class.getSimpleName();

    private SharedPreferences mPreferences;
    private StreamAdapter mStreamAdapter;
    private List<Stream> mStreams = new ArrayList<Stream>();

    //private android.app.Dialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stream_fragment_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStreamAdapter = new StreamAdapter(getActivity(), R.layout.stream_item, R.id.name, mStreams);
        setListAdapter(mStreamAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_NAME, "onItemClick()");
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                Model.instance.currentStream = (Stream) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), ValueActivity.class);
                startActivity(intent);
            }

        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_NAME, "onItemLongClick()");
                Model.instance.currentStream = (Stream) parent.getItemAtPosition(position);
                mSelected = position;
                getListView().setItemChecked(mSelected, true);
                mActionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(mActionModeCallback);
                return true;
            }
        });

        getStreams();
    }

    private int mSelected;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_mode_stream, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_stream:
                    deleteStream();
                    mode.finish();
                    return true;
                case R.id.action_share_stream:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    String message = String.format("%1$s%2$s%3$s%4$s%5$s",
                            Config.DEFAULT_BASE_URL,
                            "/datasources/",
                            Model.instance.currentDatasource.getId(),
                            "/streams/", Model.instance.currentStream.getId());
                    sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    mode.finish();
                    return true;
                case R.id.action_update_stream:
                    createOrUpdateStream(false);
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

    @Override
    public void onDestroyView() {
        unregisterForContextMenu(getListView());
        super.onDestroyView();
    }

    private void setTitle(int num, int total) {
        if (getActivity() != null) {
            getActivity().setTitle(String.format(getString(R.string.stream_activity_count), num, total));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stream, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_stream:
                createOrUpdateStream(true);
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

    private void getStreams() {
        GetStreamOperation getStreamOperation =
                new GetStreamOperation(
                        Model.instance.account.getOpeClientId(),
                        Model.instance.account.getMasterKey().getValue(),
                        Model.instance.currentDatasource,
                        new OperationCallback() {

                            @Override
                            public void process(Object object, Exception exception) {
                                if (exception == null) {
                                    Page<List<Stream>> page = (Page<List<Stream>>)object;
                                    Model.instance.streams = page.object;
                                    Log.d(TAG_NAME, "streams received = " + Model.instance.streams.size());
                                    setTitle(page.object.size(), page.totalCount);
                                    updateAdapter();
                                } else {
                                    Errors.displayError(getActivity(), exception);
                                }
                            }
                        });
        getStreamOperation.execute();
    }

    public void updateAdapter() {
        Log.d(TAG_NAME, "updateAdapter()");

        if (mStreamAdapter != null) {
            mStreamAdapter.changeDataSet(Model.instance.streams);
            mStreamAdapter.notifyDataSetChanged();
        }
    }

    /**
     *
     */
    private void deleteStream() {
        final android.app.Dialog dialog = new android.app.Dialog(getActivity());

        dialog.setContentView(R.layout.delete_dialog);
        dialog.setTitle(R.string.delete);

        TextView info = (TextView) dialog.findViewById(R.id.info_label);
        info.setText(String.format(getString(R.string.delete_stream), Model.instance.currentStream.getId()));

        Button deleteButton = (Button) dialog.findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG_NAME, "datasource : " + Model.instance.currentDatasource.getId());
                Log.d(TAG_NAME, "stream : " + Model.instance.currentStream.getId());

                DeleteStreamOperation deleteStreamOperation = new DeleteStreamOperation(
                        Model.instance.account.getOpeClientId(),
                        Model.instance.account.getMasterKey().getValue(),
                        Model.instance.currentDatasource,
                        Model.instance.currentStream,
                        new OperationCallback() {
                            @Override
                            public void process(Object object, Exception exception) {
                                if (exception == null) {
                                    getStreams(); // reload
                                } else {
                                    Errors.displayError(getActivity(), exception);
                                }
                            }
                        });

                deleteStreamOperation.execute("");

                dialog.dismiss();
            }

        });

        Button cancelDeleteButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelDeleteButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     *
     * @param isCreate
     */
    private void createOrUpdateStream(final boolean isCreate) {
        final android.app.Dialog dialog = new android.app.Dialog(getActivity());

        dialog.setContentView(R.layout.create_stream_dialog);

        if (isCreate) {
            dialog.setTitle(R.string.add_stream);
        } else {
            dialog.setTitle(R.string.update_stream);
        }

        final LinearLayout callbackLayout = (LinearLayout) dialog.findViewById(R.id.callback_layout);
        final EditText name = (EditText) dialog.findViewById(R.id.name);
        final EditText description = (EditText) dialog.findViewById(R.id.description);
        final EditText unit = (EditText) dialog.findViewById(R.id.unit);
        final EditText symbol = (EditText) dialog.findViewById(R.id.symbol);
        final EditText callback = (EditText) dialog.findViewById(R.id.callback);
        final EditText latitude = (EditText) dialog.findViewById(R.id.latitude);
        final EditText longitude = (EditText) dialog.findViewById(R.id.longitude);

        Button actionButton = (Button) dialog.findViewById(R.id.add_button);

        if (!isCreate) {
            callbackLayout.setVisibility(View.GONE);
            actionButton.setText(getString(R.string.update));

            /**
             * Set fields with current data
             */
            name.setText(Model.instance.currentStream.getName());
            description.setText(Model.instance.currentStream.getDescription());

            if (Model.instance.currentStream.getLocation() != null) {
                latitude.setText(Double.toString(Model.instance.currentStream.getLocation()[0]));
                longitude.setText(Double.toString(Model.instance.currentStream.getLocation()[1]));
            }

            if (Model.instance.currentStream.getUnit() != null) {
                if (Model.instance.currentStream.getUnit().getName() != null) {
                    unit.setText(Model.instance.currentStream.getUnit().getName());
                }
                if (Model.instance.currentStream.getUnit().getSymbol() != null) {
                    symbol.setText(Model.instance.currentStream.getUnit().getSymbol());
                }
            }
        }

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_NAME, "name : " + name.getText().toString());
                Log.d(TAG_NAME, "description : " + description.getText().toString());
                Log.d(TAG_NAME, "unit : " + unit.getText().toString());
                Log.d(TAG_NAME, "symbol : " + symbol.getText().toString());

                Stream newStream = new Stream();

                if (isCreate) {
                    newStream.setName(name.getText().toString());
                    newStream.setDescription(description.getText().toString());

                    /**
                     * Allocate new Location
                     */
                    Double[] location = null;

                    String strLatitude = latitude.getText().toString();
                    String strLongitude = longitude.getText().toString();

                    try {
                        if ((!"".equals(strLatitude)) && (!"".equals(strLongitude))) {
                            location = new Double[2];
                            location[0] = Double.parseDouble(strLatitude);
                            location[1] = Double.parseDouble(strLongitude);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG_NAME, e.toString());
                        location = null;
                    }

                    if (location != null) {
                        newStream.setLocation(location);
                    }
                    /**************************************************************************/

                    /**
                     * Allocate new Unit & Symbol
                     */
                    Unit newUnit = null;
                    String strUnit = unit.getText().toString();
                    String strSymbol = symbol.getText().toString();

                    if (!"".equals(strUnit)) {
                        if (newUnit == null) {
                            newUnit = new Unit();
                        }
                        newUnit.setName(strUnit);
                    }

                    if (!"".equals(strSymbol)) {
                        if (newUnit == null) {
                            newUnit = new Unit();
                        }
                        newUnit.setSymbol(strSymbol);
                    }

                    if (newUnit != null) {
                        newStream.setUnit(newUnit);
                    }

                    /**
                     * Allocate new Callback
                     */
                    Callback newCallback = null;
                    String callbackUrl = callback.getText().toString();

                    if (!"".equals(callbackUrl)) {
                        try {
                            URL url = new URL(callbackUrl);
                            newCallback = new Callback();
                            newCallback.setUrl(url.toString());
                            newCallback.setName("Callback");
                            newCallback.setDescription("application callback");
                            newCallback.setStatus("activated");
                        } catch (MalformedURLException e) {
                            Log.e(TAG_NAME, e.toString());
                            callback.setText("");
                        }
                    }

                    if (newCallback != null) {
                        newStream.setCallback(newCallback);
                    }

                    /**************************************************************************/

                    CreateStreamOperation createStreamOperation = new CreateStreamOperation(
                            Model.instance.account.getOpeClientId(),
                            Model.instance.account.getMasterKey().getValue(),
                            Model.instance.currentDatasource,
                            newStream,
                            new OperationCallback() {
                                @Override
                                public void process(Object object, Exception exception) {
                                    if (exception == null) {
                                        getStreams();
                                    } else {
                                        Errors.displayError(getActivity(), exception);
                                    }
                                }
                            });

                    createStreamOperation.execute("");

                    dialog.dismiss();
                } else {
                    /**
                     * Fields with no value will be deleted !
                     */
                    newStream.setId(Model.instance.currentStream.getId());             // set the previous ID
                    newStream.setMetadata(Model.instance.currentStream.getMetadata()); // set previous Metadata
                    newStream.setUnit(Model.instance.currentStream.getUnit());         // set previous Unit

                    newStream.setName(name.getText().toString());

                    if ("".equals(description.getText().toString())) {
                        newStream.setDescription(null);
                    } else {
                        newStream.setDescription(description.getText().toString());
                    }

                    /**
                     * Allocate new Location
                     */
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

                    newStream.setLocation(location);

                    /**
                     * Allocate new Unit
                     */
                    Unit newUnit = null;

                    String strUnit = unit.getText().toString();
                    String strSymbol = symbol.getText().toString();

                    if (!"".equals(strUnit)) {
                        if (newUnit == null) {
                            newUnit = new Unit();
                        }
                        newUnit.setName(strUnit);
                    }

                    if (!"".equals(strSymbol)) {
                        if (newUnit == null) {
                            newUnit = new Unit();
                        }
                        newUnit.setSymbol(strSymbol);
                    }

                    if (newUnit != null) {
                        newStream.setUnit(newUnit);
                    }

                    UpdateStreamOperation updateStreamOperation = new UpdateStreamOperation(
                            Model.instance.account.getOpeClientId(),
                            Model.instance.account.getMasterKey().getValue(),
                            Model.instance.currentDatasource,
                            newStream,
                            new OperationCallback() {
                                @Override
                                public void process(Object object, Exception exception) {
                                    if (exception == null) {
                                        getStreams();
                                        Model.instance.currentStream = (Stream)object; // update current Stream
                                    } else {
                                        Errors.displayError(getActivity(), exception);
                                    }
                                }
                            });

                    updateStreamOperation.execute("");

                    dialog.dismiss();
                }
            }
        });

        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }
}