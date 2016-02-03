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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orange.datavenue.client.model.Datasource;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.UpdateDatasourceOperation;
import com.orange.datavenue.utils.Errors;

/**
 * @author Stéphane SANDON
 */
public class DatasourceDetailFragment extends Fragment {

    private static final String TAG_NAME = DatasourceDetailFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.datasource_detail_fragment_layout, container, false);

        getActivity().setTitle(R.string.datasource);
        final LinearLayout callbackLayout = (LinearLayout) view.findViewById(R.id.callback_layout);
        final TextView id = (TextView) view.findViewById(R.id.id);
        final EditText name = (EditText) view.findViewById(R.id.name);
        final EditText description = (EditText) view.findViewById(R.id.description);
        final EditText serial = (EditText) view.findViewById(R.id.serial);
        final EditText callback = (EditText) view.findViewById(R.id.callback);
        final CheckBox status = (CheckBox) view.findViewById(R.id.status);
        final TextView created = (TextView) view.findViewById(R.id.created);
        final TextView updated = (TextView) view.findViewById(R.id.updated);

        status.setChecked(true); // by default status is activated

        /**
         * Set fields with current data
         */
        id.setText(Model.instance.currentDatasource.getId());
        name.setText(Model.instance.currentDatasource.getName());
        description.setText(Model.instance.currentDatasource.getDescription());
        serial.setText(Model.instance.currentDatasource.getSerial());

        if (Model.instance.currentDatasource.getCallback() != null) {
            String callbackUrl = Model.instance.currentDatasource.getCallback().getUrl().toString();
            callback.setText(callbackUrl);
        }

        if ("activated".equals(Model.instance.currentDatasource.getStatus())) {
            status.setChecked(true);
        } else if ("deactivated".equals(Model.instance.currentDatasource.getStatus())) {
            status.setChecked(false);
        }

        created.setText(Model.instance.currentDatasource.getCreated());
        updated.setText(Model.instance.currentDatasource.getUpdated());

        Button actionButton = (Button) view.findViewById(R.id.update_button);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_NAME, "name : " + name.getText().toString());
                Log.d(TAG_NAME, "description : " + description.getText().toString());
                Log.d(TAG_NAME, "serial : " + serial.getText().toString());
                Log.d(TAG_NAME, "status : " + status.isChecked());

                Datasource newDatasource = new Datasource();

                    /**
                     * update fields with no value will be deleted !
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

                    if (status.isChecked()) {
                        newDatasource.setStatus("activated");
                    } else {
                        newDatasource.setStatus("deactivated");
                    }

                    UpdateDatasourceOperation updateDatasourceOperation = new UpdateDatasourceOperation(
                            Model.instance.oapiKey,
                            Model.instance.key,
                            newDatasource,
                            new OperationCallback() {
                                @Override
                                public void process(Object object, Exception exception) {
                                    if (exception == null) {
                                        //getDatasources();
                                        Model.instance.currentDatasource = (Datasource)object; // update current Datasource
                                    } else {
                                        Errors.displayError(getActivity(), exception);
                                    }
                                }
                            });

                    updateDatasourceOperation.execute("");
                }
            });

        Button streamButton = (Button) view.findViewById(R.id.streams_button);

        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StreamActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
}