/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.orange.datavenue.adapter.DatasourceAdapter;
import com.orange.datavenue.client.Config;
import com.orange.datavenue.client.model.Callback;
import com.orange.datavenue.client.model.Datasource;
import com.orange.datavenue.client.model.Page;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.CreateDatasourceOperation;
import com.orange.datavenue.operation.DeleteDatasourceOperation;
import com.orange.datavenue.operation.GetDatasourcesOperation;
import com.orange.datavenue.utils.Errors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class DatasourceListFragment extends ListFragment {

    private static final String TAG_NAME = DatasourceListFragment.class.getSimpleName();

    private DatasourceAdapter mDatasourceAdapter;
    private List<Datasource> mDatasources = new ArrayList<Datasource>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_NAME, "onCreate()");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG_NAME, "onCreateView()");
        View view = inflater.inflate(R.layout.datasource_fragment_layout, container, false);

        ListView listView = (ListView)view.findViewById(android.R.id.list);

        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = (ScrollChildSwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                getActivity().getResources().getColor(R.color.orange),
                getActivity().getResources().getColor(R.color.hint_color),
                getActivity().getResources().getColor(R.color.hint_color)
        );

        swipeRefreshLayout.setScrollUpChild(listView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDatasources();
            }
        });
        return view;
    }

    private void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG_NAME, "onViewCreated()");

        if (mDatasourceAdapter == null) {
            mDatasourceAdapter = new DatasourceAdapter(getActivity(), R.layout.datasource_item, R.id.name, mDatasources);
            setListAdapter(mDatasourceAdapter);
            getDatasources();
        }

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_NAME, "onClick()");
                Model.instance.currentDatasource = (Datasource) parent.getItemAtPosition(position);

                ((DatasourceActivity) getActivity()).changeLayout(DatasourceActivity.MODE_DETAIL);
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
                    deleteDatasource();
                    mode.finish();
                    return true;
                case R.id.action_share_datasource:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    String message = String.format("%1$s/datasources/%2$s",
                            Config.DEFAULT_BASE_URL,
                            Model.instance.currentDatasource.getId());
                    sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
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
                createDatasource();
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
        setTitle(Model.instance.datasources.size(), Model.instance.datasources.size());
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

    /**
     *
     */
    private void getDatasources() {
        setLoadingIndicator(true);
        GetDatasourcesOperation getDatasourceOperation = new GetDatasourcesOperation(
                Model.instance.oapiKey,
                Model.instance.key,
                new OperationCallback() {
                    @Override
                    public void process(Object object, Exception exception) {
                        setLoadingIndicator(false);
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

    /**
     *
     */
    private void updateAdapter() {
        Log.d(TAG_NAME, "updateAdapter()");

        if (mDatasourceAdapter != null) {
            mDatasourceAdapter.changeDataSet(Model.instance.datasources);
        }
    }

    /**
     *
     */
    private void deleteDatasource() {
        final android.app.Dialog dialog = new android.app.Dialog(getActivity());

        dialog.setContentView(R.layout.delete_dialog);
        dialog.setTitle(R.string.delete);

        TextView info = (TextView) dialog.findViewById(R.id.info_label);
        info.setText(String.format(getString(R.string.delete_datasource), Model.instance.currentDatasource.getId()));

        Button deleteButton = (Button) dialog.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG_NAME, "datasource : " + Model.instance.currentDatasource.getId());

                DeleteDatasourceOperation deleteDatasourceOperation = new DeleteDatasourceOperation(
                        Model.instance.oapiKey,
                        Model.instance.key,
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

    private void createDatasource() {
        final android.app.Dialog dialog = new android.app.Dialog(getActivity());

        dialog.setContentView(R.layout.create_datasource_dialog);

        dialog.setTitle(R.string.add_datasource);

        final LinearLayout callbackLayout = (LinearLayout) dialog.findViewById(R.id.callback_layout);
        final EditText name = (EditText) dialog.findViewById(R.id.name);
        final EditText description = (EditText) dialog.findViewById(R.id.description);
        final EditText serial = (EditText) dialog.findViewById(R.id.serial);
        final EditText callback = (EditText) dialog.findViewById(R.id.callback);
        final CheckBox status = (CheckBox) dialog.findViewById(R.id.status);

        status.setChecked(true); // by default status is activated

        Button actionButton = (Button) dialog.findViewById(R.id.add_button);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_NAME, "name : " + name.getText().toString());
                Log.d(TAG_NAME, "description : " + description.getText().toString());
                Log.d(TAG_NAME, "serial : " + serial.getText().toString());
                Log.d(TAG_NAME, "status : " + status.isChecked());

                Datasource newDatasource = new Datasource();

                newDatasource.setName(name.getText().toString());
                newDatasource.setDescription(description.getText().toString());
                newDatasource.setSerial(serial.getText().toString());

                String callbackUrl = callback.getText().toString();

                if ("".equals(callbackUrl)) {
                    newDatasource.setCallback(null);
                } else {
                    try {
                        URL url = new URL(callbackUrl);
                        Callback newCallback = new Callback();
                        newCallback.setUrl(url.toString());
                        newCallback.setStatus("activated");
                        newCallback.setName("Callback");
                        newCallback.setDescription("application callback");
                        newDatasource.setCallback(newCallback);
                    } catch (MalformedURLException e) {
                        Log.e(TAG_NAME, e.toString());
                        newDatasource.setCallback(null);
                        callback.setText("");
                    }
                }

                if (status.isChecked()) {
                    newDatasource.setStatus("activated");
                } else {
                    newDatasource.setStatus("deactivated");
                }

                CreateDatasourceOperation createDatasourceOperation = new CreateDatasourceOperation(
                        Model.instance.oapiKey,
                        Model.instance.key,
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
    }
}