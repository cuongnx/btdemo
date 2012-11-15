package com.example.btdemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.btdemo.utils.CreateDialog;

public class SearchDevicesActivity extends Activity {

	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private List<BluetoothDevice> deviceList;
	private ArrayAdapter<String> listAdapter;

	private ListView listView;

	private SearchThread searchThread = new SearchThread();
	private Handler searchHandler = new Handler();

	private AlertDialog searchingDialog;
	private AlertDialog finishDialog;

	public final static String EXTRA_BTDEVICE = "com.example.btdemo.extra_btdevice";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_devices);

		listView = (ListView) findViewById(R.id.deviceList);

		deviceList = new ArrayList<BluetoothDevice>();
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1);

		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long id) {
				if (!Pattern.matches("(?i:.*(no).*(available).*)",
						listAdapter.getItem(pos)))
					startConnectActivity(pos);
			}
		});

		startSearch();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_search_devices, menu);
		return true;
	}

	public void startSearch() {
		searchThread.start();

		CreateDialog dialogBuilder2 = new CreateDialog();
		Object[] choice2 = { null, getResources().getString(R.string.ok), null };
		finishDialog = dialogBuilder2.showMessageDialog(this, "",
				getResources().getString(R.string.dialog_finish_search),
				choice2);

		CreateDialog dialogBuilder1 = new CreateDialog() {
			public void onNeutralButtonClick() {
				btAdapter.cancelDiscovery();
			}
		};
		Object[] choice1 = { null,
				getResources().getString(R.string.button_stop_search), null };
		searchingDialog = dialogBuilder1.showMessageDialog(this, "",
				getResources().getString(R.string.dialog_searching), choice1);
	}

	public void startConnectActivity(int pos) {
		Intent intent = new Intent(getApplicationContext(),
				ConnectDeviceActivity.class);
		intent.putExtra(EXTRA_BTDEVICE, deviceList.get(pos));
		this.startActivity(intent);
	}

	public String getDeviceInfo(BluetoothDevice dev) {
		return new String(dev.getName() + "\n\t" + dev.getAddress());
	}

	public void showDevices(List<BluetoothDevice> paired) {
		if (paired.isEmpty()) {
			listAdapter.add(getResources().getString(R.string.list_notfound));
		}
		for (int i = 0; i < paired.size(); ++i) {
			listAdapter.add(getDeviceInfo(paired.get(i)));
		}
	}

	class SearchThread extends Thread {

		private final BroadcastReceiver foundDevice = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (action.equals(BluetoothDevice.ACTION_FOUND)) {
					final BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					deviceList.add(device);

					searchHandler.post(new Runnable() {
						public void run() {
							listAdapter.add(getDeviceInfo(device));
						}
					});
				}
			}
		};

		private final BroadcastReceiver endDiscovery = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
					searchHandler.post(new Runnable() {
						public void run() {
							if (deviceList.isEmpty()) {
								listAdapter.add(getResources().getString(
										R.string.list_notfound));
							}
							searchingDialog.dismiss();
							finishDialog.show();
						}
					});
					unregisterReceiver(foundDevice);
					unregisterReceiver(endDiscovery);
				}
			}
		};

		public void run() {
			IntentFilter foundFilter = new IntentFilter(
					BluetoothDevice.ACTION_FOUND);
			registerReceiver(foundDevice, foundFilter);
			IntentFilter endFilter = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(endDiscovery, endFilter);
			btAdapter.startDiscovery();

			searchHandler.post(new Runnable() {
				public void run() {
					searchingDialog.show();
				}
			});
		}
	}

	public void searchPaired() {
		Set<BluetoothDevice> paired = btAdapter.getBondedDevices();
		List<BluetoothDevice> pairedList = new ArrayList<BluetoothDevice>(
				paired);

		showDevices(pairedList);
	}
}
