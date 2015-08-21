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
import com.orange.datavenue.client.model.Value;

import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class ValueAdapter extends ArrayAdapter<Value> {

    private static final String TAG_NAME = ValueAdapter.class.getSimpleName();

    private Context mContext;

    public ValueAdapter(Context context, int resource, int textViewResourceId, List<Value> objects) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
    }

    public void changeDataSet(List<Value> list) {
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
            convertView = inflater.inflate(R.layout.value_item, null);
            holder = new ViewHolder();
            holder.id = (TextView)convertView.findViewById(R.id.id);
            holder.at = (TextView)convertView.findViewById(R.id.at);
            holder.value = (TextView)convertView.findViewById(R.id.value);
            holder.metadata = (TextView)convertView.findViewById(R.id.metadata);
            holder.latitude = (TextView)convertView.findViewById(R.id.latitude);
            holder.longitude = (TextView)convertView.findViewById(R.id.longitude);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        final Value item = getItem(position);

        if (item != null) {
            holder.id.setText(item.getId());
            holder.at.setText(item.getAt());

            try {
                String jsonValue = ApiInvoker.serialize(item.getValue());
                holder.value.setText(jsonValue);
            } catch(SDKException e) {
                Log.e(TAG_NAME, e.toString());
            }

            if (item.getMetadata() != null) {
                try {
                    String jsonValue = ApiInvoker.serialize(item.getMetadata());
                    holder.metadata.setText(jsonValue);
                } catch(SDKException e) {
                    Log.e(TAG_NAME, e.toString());
                }
            } else {
                holder.metadata.setText("");
            }

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
        }

        return convertView;
    }

    static class ViewHolder {
        TextView id;
        TextView at;
        TextView value;
        TextView metadata;
        TextView latitude;
        TextView longitude;
    }
}