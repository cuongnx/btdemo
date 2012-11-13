package com.example.btdemo;

import com.example.btdemo.utils.CreateDialog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button enableButton;

	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

	private final BroadcastReceiver btStatusChanged = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent
						.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				if (state == BluetoothAdapter.STATE_ON) {
					enableButton.setText("Disable Bluetooth");
					turnedOnDialog();
				} else if (state == BluetoothAdapter.STATE_OFF) {
					enableButton.setText("Enable Bluetooth");
					turnedOffDialog();
				}
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		enableButton = (Button) this.findViewById(R.id.enableButton);

		if (btAdapter == null) {
			CreateDialog dialogBuilder = new CreateDialog();
			Object[] choice = { null, "OK", null };
			dialogBuilder
					.showMessageDialog(
							this,
							"Error!",
							"Your device does not support Bluetooth!\nThe application is now exiting!",
							choice);
			System.exit(1);
		}

		if (btAdapter.isEnabled()) {
			enableButton.setText("Disable Bluetooth");
		}

		IntentFilter filter = new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(btStatusChanged, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onEnableButtonClicked(View btn) {
		if (!btAdapter.isEnabled()) {
			enableBluetooth();
		} else {
			btAdapter.disable();
		}
	}

	public void enableBluetooth() {
		int REQUEST_ENABLE_BT = 1991;
		Intent enableBTIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1991) {
			if (resultCode == RESULT_CANCELED) {
				turnedOffDialog();
			}
		}
	}

	public void turnedOnDialog() {
		CreateDialog dialogBuilder = new CreateDialog();
		Object[] choice = { null, "OK", null };
		dialogBuilder.showMessageDialog(this, "",
				"Bluetooth is now turned on!", choice).show();
	}

	public void turnedOffDialog() {
		CreateDialog dialogBuilder = new CreateDialog();
		Object[] choice = { null, "OK", null };
		dialogBuilder.showMessageDialog(this, "", "Bluetooth is off!", choice).show();
	}

	public void onSearchButtonClicked(View btn) {
		if (!btAdapter.isEnabled()) {
			enableBluetooth();
		} else {
			Intent intent = new Intent(this, SearchDevicesActivity.class);
			this.startActivity(intent);
		}
	}

	public void onDestroy() {
		unregisterReceiver(btStatusChanged);
		super.onDestroy();
	}
}
