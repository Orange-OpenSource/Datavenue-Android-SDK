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
import com.orange.datavenue.client.model.Datasource;

import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class DatasourceAdapter extends ArrayAdapter<Datasource> {

    private static final String TAG_NAME = DatasourceAdapter.class.getSimpleName();

    private Context mContext;

    public DatasourceAdapter(Context context, int resource, int textViewResourceId, List<Datasource> objects) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
    }

    public void changeDataSet(List<Datasource> list) {
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
            convertView = inflater.inflate(R.layout.datasource_item, null);
            holder = new ViewHolder();
            holder.id = (TextView)convertView.findViewById(R.id.id);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.description = (TextView)convertView.findViewById(R.id.description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        final Datasource item = getItem(position);

        if (item != null) {
            // fill layout
            holder.id.setText(item.getId());
            holder.name.setText(item.getName());
            holder.description.setText(item.getDescription());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView id;
        TextView name;
        TextView description;
    }
}