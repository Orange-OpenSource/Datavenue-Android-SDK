/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue.utils;

import android.app.Activity;

import com.orange.datavenue.R;
import com.orange.datavenue.client.common.HTTPException;
import com.orange.datavenue.client.common.SDKException;

/**
 * @author Stéphane SANDON
 */
public class Errors {

    public static void displayError(Activity activity, Exception exception) {
        if (activity != null) {
            Dialog dialog = new Dialog(activity);
            StringBuilder errorMessage = new StringBuilder();

            if (exception instanceof HTTPException) {
                int code = ((HTTPException) exception).getDatavenueError().getCode();
                errorMessage.append(String.format("code : %1$d", code));

                String message = ((HTTPException) exception).getDatavenueError().getMessage();
                if (!"".equals(message)) {
                    errorMessage.append(String.format("\nmessage : %1$s", message));
                }

                String description = ((HTTPException) exception).getDatavenueError().getDescription();
                if (!"".equals(description)) {
                    errorMessage.append(String.format("\ndescription : %1$s", description));
                }

            } else if (exception instanceof SDKException) {
                errorMessage.append(((SDKException) exception).getMessage());
            }

            dialog.create(R.string.error, errorMessage.toString());
        }
    }

}
