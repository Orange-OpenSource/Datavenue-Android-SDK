/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue.utils;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class IntentHelper {

    public static final String TAG_NAME = IntentHelper.class.getSimpleName();

    public static Data getIntentData(Intent intent) {
        Data data = null;

        Uri uri = intent.getData();

        if (uri != null) {
            data = new Data();

            Log.d(TAG_NAME, "Intent received !");
            Log.d(TAG_NAME, "uri=" + uri.toString());
            data.ope = uri.getQueryParameter("ope");
            data.key = uri.getQueryParameter("key");

            List<String> segments = uri.getPathSegments();

            for (String segment: segments) {
                if ("accounts".equals(segment)) {
                    data.isAccount = true;
                    data.isDatasource = false;
                    data.isStream = false;
                } else if ("datasources".equals(segment)) {
                    data.isAccount = false;
                    data.isDatasource = true;
                    data.isStream = false;
                } else if ("streams".equals(segment)) {
                    data.isAccount = false;
                    data.isDatasource = false;
                    data.isStream = true;
                } else {
                    if (data.isAccount) {
                        data.accountId = segment;
                    } else if (data.isDatasource) {
                        data.datasourceId = segment;
                    } else if (data.isStream) {
                        data.streamId = segment;
                    }
                }
            }

            Log.d(TAG_NAME, "accountId=" + data.accountId);
            Log.d(TAG_NAME, "datasourceId=" + data.datasourceId);
            Log.d(TAG_NAME, "streamId=" + data.streamId);
        }

        return data;
    }

    public static class Data {
        public String ope = "";
        public String key = "";
        public String accountId = "";
        public String datasourceId = "";
        public String streamId = "";

        public boolean isAccount = false;
        public boolean isDatasource = false;
        public boolean isStream = false;
    }
}
