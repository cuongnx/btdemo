package com.example.btdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

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

	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private InputStream in = null;
	private OutputStream out = null;

	private TextView deviceQuery;
	private TextView dataView;
	private Button connectButton;
	private Button startButton;

	private Handler connectHandler = new Handler();
	private volatile Thread flagCheck = null;
	private volatile MeasureThread bgMeasure = null;

	private volatile boolean connected = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connected_device);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		final BluetoothDevice btDevice = getIntent().getParcelableExtra(
				SearchDevicesActivity.EXTRA_BTDEVICE);

		deviceQuery = (TextView) findViewById(R.id.deviceQuery);

		dataView = (TextView) findViewById(R.id.dataView);

		connectButton = (Button) findViewById(R.id.connectButton);
		connectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (connected) {
					try {
						stopConnection();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					deviceQuery.setText(getResources().getString(
							R.string.separator)
							+ "\nConnecting...");
					(new ConnectThread(btDevice)).start();
				}
			}
		});

		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (connected && (in != null) && (out != null)) {
					if (bgMeasure == null) {
						(bgMeasure = new MeasureThread()).start();
					} else {
						byte[] outBuffer = Utils.toByteArray(Command
								.stopCommand());
						try {
							out.write(outBuffer);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		flagCheck = new Thread() {
			public void run() {
				while (Thread.currentThread().equals(flagCheck)) {
					if (bgMeasure == null) {
						connectHandler.post(new Runnable() {
							public void run() {
								startButton.setText(R.string.button_start);
							}
						});
					} else {
						connectHandler.post(new Runnable() {
							public void run() {
								startButton.setText(R.string.button_stop);
							}
						});
					}
					if (connected) {
						connectHandler.post(new Runnable() {
							public void run() {
								connectButton
										.setText(R.string.button_disconnect);
							}
						});
					} else {
						connectHandler.post(new Runnable() {
							public void run() {
								connectButton.setText(R.string.button_connect);
							}
						});
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		flagCheck.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connected_device, menu);
		return true;
	}

	public void publishText(final String msg) {
		connectHandler.post(new Runnable() {
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
				publishText("\nConnected!\n");

				in = btSocket.getInputStream();
				out = btSocket.getOutputStream();
				byte[] outBuffer = Utils.toByteArray(Command
						.retrieveDeviceInfoCommand());
				byte[] inBuffer = sendMessage(out, outBuffer, in, 33);

				DeviceInfo dev = Response.getDeviceInfoResponse(inBuffer);
				if (dev == null) {
					publishText("\nUnknown device");
				} else {
					publishText("\nDevice Name: " + dev.deviceName);
					publishText("\nSerial Number: " + dev.serialNumber
							+ "\nBluetooth Address: ");
					for (byte x : dev.btaddress) {
						if (x < 0)
							publishText(Integer.toHexString((short) (x & 0xFF))
									+ ":");
						else if (x < 16)
							publishText("0" + Integer.toHexString((short) x)
									+ ":");
						else
							publishText(Integer.toHexString((short) x) + ":");
					}
					publishText("\nVersion: "
							+ Integer.toString((int) dev.version)
							+ "\n\nSetting up device.....");
					setupDevice();
					publishText("--> Done\n"
							+ getResources().getString(R.string.separator));
					connected = true;
				}

				flushInputStream();
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

	public void stopConnection() throws IOException {
		in = null;
		out = null;
		btSocket.close();
		connected = false;
	}

	public boolean setupDevice() throws IOException {
		Calendar today = Calendar.getInstance();
		byte[] outBuffer;
		byte[] inBuffer = new byte[264];
		int done;

		// ==========
		outBuffer = Utils.toByteArray(Command.setTimeCommand(
				today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DATE), today.get(Calendar.HOUR_OF_DAY),
				today.get(Calendar.MINUTE), today.get(Calendar.SECOND),
				today.get(Calendar.MILLISECOND)));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		// ==========
		outBuffer = Utils.toByteArray(Command.setAtmosphereCommand(0, 0, 0));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		// ==========
		outBuffer = Utils.toByteArray(Command.setGeomagnetismCommand(0, 0, 0));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		// ==========
		outBuffer = Utils.toByteArray(Command.setBatteryCommand(0, 0));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		// ==========
		outBuffer = Utils.toByteArray(Command
				.setExcommunicationCommand(0, 0, 0));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		// ==========
		outBuffer = Utils.toByteArray(Command.setExdataCommand(0, 0, 0, 0, 0));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		// ==========
		outBuffer = Utils.toByteArray(Command.setAccelerationMeasureCommand(
				100, 1, 0));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		// ==========
		outBuffer = Utils.toByteArray(Command.setAccelerationRangeCommand(3));
		do {
			inBuffer = sendMessage(out, outBuffer, in, 4);
			done = Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
		} while (done != 0);

		return true;
	}

	class MeasureThread extends Thread {
		public void run() {
			try {
				byte[] outBuffer = Utils.toByteArray(Command.startTimeCommand(
						0, 12, 11, 13, 0, 0, 5, 0, 12, 11, 13, 0, 0, 10));
				byte[] inBuffer = sendMessage(out, outBuffer, in, 16);

				Date date = Response.getTimeResponse(inBuffer);
				if (date == null) {
					publishText("\n\nTime not set");
					bgMeasure = null;
				} else {
					publishText("\n\nStart time: "
							+ Integer.toString(date.syear) + "/"
							+ Integer.toString(date.smonth) + "/"
							+ Integer.toString(date.sday) + "\t"
							+ Integer.toString(date.shour) + ":"
							+ Integer.toString(date.sminute) + ":"
							+ Integer.toString(date.ssecond));
					publishText("\nEnd time: " + Integer.toString(date.eyear)
							+ "/" + Integer.toString(date.emonth) + "/"
							+ Integer.toString(date.eday) + "\t"
							+ Integer.toString(date.ehour) + ":"
							+ Integer.toString(date.eminute) + ":"
							+ Integer.toString(date.esecond));
					readData(in);
				}
			} catch (IOException e) {
				e.printStackTrace();
				publishText("\nConnection failed!\n" + e.getMessage());
			}
		}

		public void readData(InputStream in) throws IOException {
			byte[] inBuffer = new byte[28];
			flushInputStream();

			do {
				in.read(inBuffer, 0, 2);
				if (inBuffer[0] == (byte) 0x9A) {
					switch (inBuffer[1]) {
					case (byte) 0x80:
						in.read(inBuffer, 2, 23);

						final AccelerationData data = EventCommand
								.accelerationEvent(Utils.getSubarray(inBuffer,
										0, 25));

						if (data == null)
							break;

						connectHandler.post(new Runnable() {
							public void run() {
								dataView.setText("\nTimestamp: " + data.milisec);
								dataView.append("\n\tAcceleration");
								dataView.append("\n\t\tX: " + data.Xadeta
										+ "\n\t\tY: " + data.Yadeta
										+ "\n\t\tZ: " + data.Zadeta);
								dataView.append("\n\tAngular velocity");
								dataView.append("\n\t\tX: " + data.Xvdeta
										+ "\n\t\tY: " + data.Yvdeta
										+ "\n\t\tZ: " + data.Zvdeta);
							}
						});

						break;

					case (byte) 0x81:
						in.read(inBuffer, 2, 14);
						break;

					case (byte) 0x82:
						in.read(inBuffer, 2, 10);
						break;

					case (byte) 0x83:
						in.read(inBuffer, 2, 8);
						break;

					case (byte) 0x84:
						in.read(inBuffer, 2, 10);
						break;

					case (byte) 0x85:
						in.read(inBuffer, 2, 7);
						break;

					case (byte) 0x86:
						in.read(inBuffer, 2, 14);
						break;

					case (byte) 0x87:
						in.read(inBuffer, 2, 6);
						break;

					case (byte) 0x88:
						in.read(inBuffer, 2, 2);
						if (EventCommand.startEvent(Utils.getSubarray(inBuffer,
								0, 4))) {
						}
						break;

					case (byte) 0x89:
						in.read(inBuffer, 2, 2);
						publishText("\n--> Measurement ends with exit code "
								+ EventCommand.stopEvent(Utils.getSubarray(
										inBuffer, 0, 4)));
						bgMeasure = null;
						break;

					case (byte) 0x8F:
						in.read(inBuffer, 2, 2);
						break;

					default:
						// writeByte(inBuffer, 28);
					}
				}
			} while (Thread.currentThread().equals(bgMeasure));
		}
	}

	public byte[] sendMessage(OutputStream out, byte[] cmd, InputStream in,
			int byteNum) throws IOException {
		flushInputStream();
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

	public void flushInputStream() throws IOException {
		while (in.available() > 0) {
			in.skip(500);
		}
	}

	public void onDestroy() {
		flagCheck = null;
		bgMeasure = null;
		try {
			stopConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	public void writeByte(byte[] tmp, int n) {
		publishText("\n\n");
		for (int i = 0; i < n; ++i) {
			byte x = tmp[i];
			if (x < 0)
				publishText(Integer.toHexString((short) (x & 0xFF)) + ":");
			else if (x < 16)
				publishText("0" + Integer.toHexString((short) x) + ":");
			else
				publishText(Integer.toHexString((short) x) + ":");
		}
	}
}
