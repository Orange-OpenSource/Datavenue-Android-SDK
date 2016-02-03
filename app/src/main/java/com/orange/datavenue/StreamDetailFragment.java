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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orange.datavenue.client.model.Callback;
import com.orange.datavenue.client.model.Stream;
import com.orange.datavenue.client.model.Unit;
import com.orange.datavenue.model.Model;
import com.orange.datavenue.operation.UpdateStreamOperation;
import com.orange.datavenue.utils.Errors;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Stéphane SANDON
 */
public class StreamDetailFragment extends Fragment {

    private static final String TAG_NAME = StreamDetailFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stream_detail_fragment_layout, container, false);

        getActivity().setTitle(R.string.stream);
        final LinearLayout callbackLayout = (LinearLayout) view.findViewById(R.id.callback_layout);
        final TextView id = (TextView) view.findViewById(R.id.id);
        final EditText name = (EditText) view.findViewById(R.id.name);
        final EditText description = (EditText) view.findViewById(R.id.description);
        final EditText unit = (EditText) view.findViewById(R.id.unit);
        final EditText symbol = (EditText) view.findViewById(R.id.symbol);
        final EditText callback = (EditText) view.findViewById(R.id.callback);
        final EditText latitude = (EditText) view.findViewById(R.id.latitude);
        final EditText longitude = (EditText) view.findViewById(R.id.longitude);
        final TextView created = (TextView) view.findViewById(R.id.created);
        final TextView updated = (TextView) view.findViewById(R.id.updated);

        /**
         * Set fields with current data
         */
        id.setText(Model.instance.currentStream.getId());
        name.setText(Model.instance.currentStream.getName());
        description.setText(Model.instance.currentStream.getDescription());
        if (Model.instance.currentStream.getUnit() != null) {
            unit.setText(Model.instance.currentStream.getUnit().getName());
            symbol.setText(Model.instance.currentStream.getUnit().getSymbol());
        }

        if (Model.instance.currentStream.getCallback() != null) {
            callback.setText(Model.instance.currentStream.getCallback().getUrl().toString());
        }

        if (Model.instance.currentStream.getLocation() != null) {
            if (Model.instance.currentStream.getLocation().length >= 2) {
                latitude.setText("" + Model.instance.currentStream.getLocation()[0]);
                longitude.setText("" + Model.instance.currentStream.getLocation()[1]);
            }
        }

        created.setText(Model.instance.currentStream.getCreated());
        updated.setText(Model.instance.currentStream.getUpdated());

        Button actionButton = (Button) view.findViewById(R.id.update_button);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_NAME, "name : " + name.getText().toString());
                Log.d(TAG_NAME, "description : " + description.getText().toString());

                Stream newStream = new Stream();

                    /**
                     * update fields with no value will be deleted !
                     */
                    newStream.setId(Model.instance.currentStream.getId()); // set the previous ID
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

                    UpdateStreamOperation updateStreamOperation = new UpdateStreamOperation(
                            Model.instance.oapiKey,
                            Model.instance.key,
                            Model.instance.currentDatasource,
                            newStream,
                            new OperationCallback() {
                                @Override
                                public void process(Object object, Exception exception) {
                                    if (exception == null) {
                                        Model.instance.currentStream = (Stream)object; // update current Datasource
                                    } else {
                                        Errors.displayError(getActivity(), exception);
                                    }
                                }
                            });

                    updateStreamOperation.execute("");
                }
            });

        Button valueButton = (Button) view.findViewById(R.id.value_button);

        valueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ValueActivity.class);
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