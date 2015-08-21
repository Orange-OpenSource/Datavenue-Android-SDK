/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue;

import android.content.Intent;
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
import android.widget.TextView;

import com.orange.datavenue.adapter.DatasourceAdapter;
import com.orange.datavenue.client.model.Datasource;
import com.orange.datavenue.client.model.Page;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.CreateDatasourceOperation;
import com.orange.datavenue.operation.DeleteDatasourceOperation;
import com.orange.datavenue.operation.GetDatasourceOperation;
import com.orange.datavenue.operation.UpdateDatasourceOperation;
import com.orange.datavenue.utils.Errors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class DatasourceFragment extends ListFragment {

    private static final String TAG_NAME = DatasourceFragment.class.getSimpleName();

    private DatasourceAdapter mDatasourceAdapter;
    private List<Datasource> mDatasources = new ArrayList<Datasource>();
    private android.app.Dialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.datasource_fragment_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatasourceAdapter = new DatasourceAdapter(getActivity(), R.layout.datasource_item, R.id.name, mDatasources);
        setListAdapter(mDatasourceAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_NAME, "onClick()");
                Model.instance.currentDatasource = (Datasource) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), StreamActivity.class);
                startActivity(intent);
            }
        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_NAME, "onItemLongClick()");
                Model.instance.currentDatasource = (Datasource) parent.getItemAtPosition(position);
                mSelected = position;
                getListView().setItemChecked(mSelected, true);
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                return true;
            }
        });

        getDatasources();
    }

    private int mSelected;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_mode_datasource, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_datasource:
                    mDialog = new android.app.Dialog(getActivity());

                    mDialog.setContentView(R.layout.delete_dialog);
                    mDialog.setTitle(R.string.delete);

                    TextView info = (TextView) mDialog.findViewById(R.id.info_label);
                    info.setText(String.format(getString(R.string.delete_datasource), Model.instance.currentDatasource.getId()));

                    Button deleteButton = (Button) mDialog.findViewById(R.id.delete_button);

                    deleteButton.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            Log.d(TAG_NAME, "datasource : " + Model.instance.currentDatasource.getId());

                            DeleteDatasourceOperation deleteDatasourceOperation = new DeleteDatasourceOperation(
                                    Model.instance.account,
                                    Model.instance.currentDatasource,
                                    new OperationCallback() {
                                        @Override
                                        public void process(Object object, Exception exception) {
                                            if (exception == null) {
                                                getDatasources(); // reload
                                            } else {
                                                Errors.displayError(getActivity(), exception);
                                            }
                                        }
                                    });

                            deleteDatasourceOperation.execute("");

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
                case R.id.action_update_datasource:
                    mDialog = new android.app.Dialog(getActivity());

                    mDialog.setContentView(R.layout.create_datasource_dialog);
                    mDialog.setTitle(R.string.update_datasource);

                    final EditText name = (EditText) mDialog.findViewById(R.id.name);
                    final EditText description = (EditText) mDialog.findViewById(R.id.description);
                    final EditText serial = (EditText) mDialog.findViewById(R.id.serial);

                    name.setText(Model.instance.currentDatasource.getName());
                    description.setText(Model.instance.currentDatasource.getDescription());
                    serial.setText(Model.instance.currentDatasource.getSerial());

                    Button updateButton = (Button) mDialog.findViewById(R.id.add_button);
                    updateButton.setText(getString(R.string.update_datasource));

                    updateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG_NAME, "name : " + name.getText().toString());
                            Log.d(TAG_NAME, "description : " + description.getText().toString());
                            Log.d(TAG_NAME, "serial : " + serial.getText().toString());

                            Datasource newDatasource = new Datasource();

                            /**
                             * Fields with no value will be deleted !
                             */
                            newDatasource.setId(Model.instance.currentDatasource.getId());              // set the previous ID
                            newDatasource.setMetadata(Model.instance.currentDatasource.getMetadata());  // set the previous Metadata
                            newDatasource.setGroup(Model.instance.currentDatasource.getGroup());        // set the previous Group
                            newDatasource.setTemplate(Model.instance.currentDatasource.getTemplate());  // set the previous Template

                            newDatasource.setName(name.getText().toString());

                            if ("".equals(description.getText().toString())) {
                                newDatasource.setDescription(null);
                            } else {
                                newDatasource.setDescription(description.getText().toString());
                            }

                            if ("".equals(serial.getText().toString())) {
                                newDatasource.setSerial(null);
                            } else {
                                newDatasource.setSerial(serial.getText().toString());
                            }

                            UpdateDatasourceOperation updateDatasourceOperation = new UpdateDatasourceOperation(
                                    Model.instance.account,
                                    newDatasource,
                                    new OperationCallback() {
                                        @Override
                                        public void process(Object object, Exception exception) {
                                            if (exception == null) {
                                                getDatasources();
                                                Model.instance.currentDatasource = (Datasource)object; // update current Datasource
                                            } else {
                                                Errors.displayError(getActivity(), exception);
                                            }
                                        }
                                    });

                            updateDatasourceOperation.execute("");

                            mDialog.dismiss();
                        }
                    });

                    Button cancelUpdateButton = (Button) mDialog.findViewById(R.id.cancel_button);
                    cancelUpdateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
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
            getActivity().setTitle(String.format(getString(R.string.datasource_activity_count), num, total));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_datasource, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_datasource:
                final android.app.Dialog dialog = new android.app.Dialog(getActivity());

                dialog.setContentView(R.layout.create_datasource_dialog);
                dialog.setTitle(R.string.add_datasource);

                final EditText name = (EditText) dialog.findViewById(R.id.name);
                final EditText description = (EditText) dialog.findViewById(R.id.description);
                final EditText serial = (EditText) dialog.findViewById(R.id.serial);

                Button addButton = (Button) dialog.findViewById(R.id.add_button);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG_NAME, "name : " + name.getText().toString());
                        Log.d(TAG_NAME, "description : " + description.getText().toString());

                        Datasource newDatasource = new Datasource();
                        newDatasource.setName(name.getText().toString());
                        newDatasource.setDescription(description.getText().toString());
                        newDatasource.setSerial(serial.getText().toString());

                        CreateDatasourceOperation createDatasourceOperation = new CreateDatasourceOperation(
                                Model.instance.account,
                                newDatasource,
                                new OperationCallback() {
                                    @Override
                                    public void process(Object object, Exception exception) {
                                        if (exception == null) {
                                            getDatasources();
                                        } else {
                                            Errors.displayError(getActivity(), exception);
                                        }
                                    }
                                });

                        createDatasourceOperation.execute("");

                        dialog.dismiss();
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

    private void getDatasources() {
        GetDatasourceOperation getDatasourceOperation = new GetDatasourceOperation(Model.instance.account,
                        new OperationCallback() {
                            @Override
                            public void process(Object object, Exception exception) {
                                if (exception == null) {
                                    Page<List<Datasource>> datasources = (Page<List<Datasource>>) object;
                                    Model.instance.datasources = datasources.object;
                                    Log.d(TAG_NAME, "datasources received = " + Model.instance.datasources.size());
                                    setTitle(datasources.object.size(), datasources.totalCount);
                                    updateAdapter();
                                } else {
                                    Errors.displayError(getActivity(), exception);
                                }
                            }
                        });

        getDatasourceOperation.execute();
    }

    public void updateAdapter() {
        Log.d(TAG_NAME, "updateAdapter()");

        if (mDatasourceAdapter != null) {
            mDatasourceAdapter.changeDataSet(Model.instance.datasources);
        }

    }
}
