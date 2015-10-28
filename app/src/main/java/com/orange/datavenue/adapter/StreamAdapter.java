/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue.adapter;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orange.datavenue.R;
import com.orange.datavenue.client.common.ApiInvoker;
import com.orange.datavenue.client.common.SDKException;
import com.orange.datavenue.client.model.Stream;
import com.orange.datavenue.client.model.Unit;

import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class StreamAdapter extends ArrayAdapter<Stream> {

    private static final String TAG_NAME = StreamAdapter.class.getSimpleName();

    private Context mContext;

    public StreamAdapter(Context context, int resource, int textViewResourceId, List<Stream> objects) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
    }

    public void changeDataSet(List<Stream> list) {
        Log.d(TAG_NAME, "changeDataSet()");
        clear();
        addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.stream_item, null);
            holder = new ViewHolder();
            holder.id = (TextView)convertView.findViewById(R.id.id);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.description = (TextView)convertView.findViewById(R.id.description);
            holder.unit = (TextView)convertView.findViewById(R.id.unit);
            holder.symbol = (TextView)convertView.findViewById(R.id.symbol);
            holder.created = (TextView)convertView.findViewById(R.id.created);
            holder.updated = (TextView)convertView.findViewById(R.id.updated);
            holder.latitude = (TextView)convertView.findViewById(R.id.latitude);
            holder.longitude = (TextView)convertView.findViewById(R.id.longitude);
            holder.last_value = (TextView)convertView.findViewById(R.id.last_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        final Stream item = getItem(position);

        if (item != null) {
            // fill layout
            holder.id.setText(item.getId());
            holder.name.setText(item.getName());
            holder.description.setText(item.getDescription());

            Unit unit = item.getUnit();

            holder.unit.setText("");
            holder.symbol.setText("");

            if (unit != null) {
                if (unit.getName() != null) {
                    holder.unit.setText(unit.getName());

                }

                if (unit.getSymbol() != null) {
                    holder.symbol.setText(unit.getSymbol());
                }
            }

            holder.created.setText(item.getCreated());
            holder.updated.setText(item.getUpdated());
            Double[] location = item.getLocation();

            if (location != null) {
                if (location.length >= 2) {
                    holder.latitude.setText(String.format("%1$f", location[0]));
                    holder.longitude.setText(String.format("%1$f", location[1]));
                }
            } else {
                holder.latitude.setText("");
                holder.longitude.setText("");
            }

            try {
                String jsonLastValue = ApiInvoker.serialize(item.getLastValue());

                if (!"\"\"".equals(jsonLastValue)) {
                    holder.last_value.setText(jsonLastValue);
                } else {
                    holder.last_value.setText("");
                }

            } catch(SDKException e) {
                Log.e(TAG_NAME, e.toString());
            }

        }

        return convertView;
    }

    static class ViewHolder {
        TextView id;
        TextView name;
        TextView description;
        TextView unit;
        TextView symbol;
        TextView created;
        TextView updated;
        TextView latitude;
        TextView longitude;
        TextView last_value;
    }
}
