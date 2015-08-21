/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orange.datavenue.R;

/**
 * @author Stéphane SANDON
 */
public class Dialog {

	private Context mContext;
	private Activity mActivity;

	public Dialog(Activity activity) {
		mActivity = activity;
		mContext = activity.getApplicationContext();
	}
	
	public void create(int title, String text) {
		final android.app.Dialog dialog = new android.app.Dialog(mActivity);

		dialog.setContentView(R.layout.dialog);
		dialog.setTitle(title);
		dialog.setCancelable(false);
		
		TextView info = (TextView)dialog.findViewById(R.id.info_label);
		
		info.setText(text);
		
		Button infoButton = (Button) dialog.findViewById(R.id.info_button);
		infoButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});		
		dialog.show();
	}
}