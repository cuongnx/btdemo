package com.example.btdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import com.example.btdemo.communication.messages.*;
import com.example.btdemo.communication.data.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ConnectDeviceActivity extends Activity {

	private BluetoothAdapter btAdapter;
	private BluetoothSocket btSocket = null;

	private TextView deviceQuery;
	private TextView dataView;
	private Button disconnectButton;
	private Button stopButton;

	private Handler connectingHandler = new Handler();

	private volatile boolean connected = false;
	private volatile boolean measuring = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connected_device);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice btDevice = getIntent().getParcelableExtra(
				SearchDevicesActivity.EXTRA_BTDEVICE);

		deviceQuery = (TextView) findViewById(R.id.deviceQuery);
		deviceQuery.setText("Connecting...");

		dataView = (TextView) findViewById(R.id.dataView);

		disconnectButton = (Button) findViewById(R.id.disconnectButton);
		disconnectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (connected) {
					try {
						connected = false;
						btSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (measuring) {

				}
			}
		});

		(new ConnectThread(btDevice)).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connected_device, menu);
		return true;
	}

	public void publishText(final String msg) {
		connectingHandler.post(new Runnable() {
			public void run() {
				deviceQuery.append(msg);
			}
		});
	}

	class ConnectThread extends Thread {

		public ConnectThread(BluetoothDevice dev) {
			BluetoothSocket tmp = null;

			// UUID:00001101-0000-1000-8000-00805F9B34FB
			// SerialPortServiceClass_UUID

			/*
			 * try { Method m =
			 * btDevice.getClass().getMethod("createRfcommSocket", new Class[] {
			 * int.class }); tmp = (BluetoothSocket) m.invoke(btDevice,
			 * Integer.valueOf(1)); } catch (SecurityException e1) {
			 * e1.printStackTrace(); } catch (NoSuchMethodException e1) {
			 * e1.printStackTrace(); } catch (IllegalArgumentException e) {
			 * e.printStackTrace(); } catch (IllegalAccessException e) {
			 * e.printStackTrace(); } catch (InvocationTargetException e) {
			 * e.printStackTrace(); }
			 */

			try {
				tmp = dev.createRfcommSocketToServiceRecord(java.util.UUID
						.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			} catch (IOException e) {
				publishText(e.getMessage());
			}

			btSocket = tmp;
		}

		public void run() {
			btAdapter.cancelDiscovery();

			try {
				btSocket.connect();
				connected = true;

				connectingHandler.post(new Runnable() {
					public void run() {
						deviceQuery.append("\nConnected!\n");
					}
				});

				InputStream in = btSocket.getInputStream();
				OutputStream out = btSocket.getOutputStream();
				byte[] outBuffer = Utils.toByteArray(Command
						.retrieveDeviceInfoCommand());

				do {
					out.write(outBuffer);
					if (in.available() <= 0)
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				} while (in.available() <= 0);

				byte[] inBuffer = new byte[264];
				int n = in.read(inBuffer, 0, 33);

				DeviceInfo dev = Response.getDeviceInfoResponse(Utils
						.getSubarray(inBuffer, 0, n));
				if (dev == null) {
					publishText("null");
				} else {
					publishText("\nDevice Name: " + dev.deviceName);
					publishText("\nSerial Number: " + dev.serialNumber
							+ "\nBluetooth Address: ");
					for (byte x : dev.btaddress) {
						if (x < 16)
							publishText("0" + Integer.toHexString((short) x)
									+ ":");
						if (x < 0)
							publishText("0"
									+ Integer.toHexString((short) (x & 0xFF))
									+ ":");
					}
					publishText("\nVersion: " + Integer.toString(dev.version));
				}

				flushInputStream(in);

				/*
				 * outBuffer = Utils.toByteArray(Command.startTimeCommand(0, 12,
				 * 11, 13, 0, 0, 5, 0, 12, 11, 13, 0, 0, 30));
				 * out.write(outBuffer);
				 */

			} catch (IOException e) {
				publishText("\nConnection failed!\n" + e.getMessage());
				try {
					connected = false;
					btSocket.close();
				} catch (IOException e1) {
					publishText("\nConnection failed!\n" + e1.getMessage());
				}
			}
		}
	}

	public byte[] sendMessage(OutputStream out, byte[] cmd, InputStream in,
			int byteNum) throws IOException {
		do {
			out.write(cmd);
			if (in.available() <= 0)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		} while (in.available() <= 0);
		byte[] res = new byte[264];
		in.read(res);
		return Utils.getSubarray(res, 0, byteNum);
	}

	public void flushInputStream(InputStream in) throws IOException {
		while (in.available() > 0) {
			in.read(new byte[500]);
		}
	}
}
