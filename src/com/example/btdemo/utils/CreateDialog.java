package com.example.btdemo.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CreateDialog {
	public AlertDialog showMessageDialog(Context context, String title,
			String msg, Object[] obj) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

		dialogBuilder.setTitle(title);
		dialogBuilder.setMessage(msg);

		if (obj[0] != null) {
			dialogBuilder.setPositiveButton((String) obj[0],
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							onPositiveButtonClick();
						}
					});
		}

		if (obj[1] != null) {
			dialogBuilder.setNeutralButton((String) obj[1],
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							onNeutralButtonClick();
						}
					});
		}
		if (obj[2] != null) {
			dialogBuilder.setNegativeButton((String) obj[2],
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							onNegativeButtonClick();
						}
					});
		}

		return dialogBuilder.create();
	}

	public void onNegativeButtonClick() {
	}

	public void onNeutralButtonClick() {
	}

	public void onPositiveButtonClick() {
	}
}
