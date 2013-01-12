package com.example.btdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.btdemo.communication.data.AccelerationData;
import com.example.btdemo.communication.data.Date;
import com.example.btdemo.communication.data.DeviceInfo;
import com.example.btdemo.communication.messages.Command;
import com.example.btdemo.communication.messages.EventCommand;
import com.example.btdemo.communication.messages.Response;
import com.example.btdemo.communication.messages.Utils;
import com.example.btdemo.customview.FishView;
import com.example.btdemo.customview.FishingRod;

public class ConnectDeviceActivity extends Activity {

	private static final int GAME_DURATION = 60;

	private static final int ACCELERATION_RANGE = 3;
	private static final int ANGULAR_VELOCITY_RANGE = 3;
	private static final int SAMPLING_INTERVAL = 100;
	private static final int SAMPLING_NUMBER = 1;
	private static final int ROTATE_THRES_VAL = 130000;
	private static final int CAST_THRES_VAL = 5000;
	private static final int CAST_MAX_THRES_VAL = 30000;

	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private InputStream in = null;
	private OutputStream out = null;

	private TextView deviceQuery;
	private RelativeLayout layout;
	private Button connectButton;
	private Button startButton;
	private FishingRod rodView;
	private FishView fish[] = new FishView[3];

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
		layout = (RelativeLayout) findViewById(R.id.layout);
		rodView = new FishingRod(this);
		layout.addView(rodView);

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
					if (!bgMeasure.isRunning()) {
						(bgMeasure = new MeasureThread()).start();
					} else {
						stopMeasure();
					}
				}
			}
		});

		bgMeasure = new MeasureThread();

		flagCheck = new Thread() {
			public void run() {
				while (Thread.currentThread().equals(flagCheck)) {
					if (!bgMeasure.isRunning()) {
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
						Thread.sleep(1000);
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
				publishText("--> Connected!\n");

				in = btSocket.getInputStream();
				out = btSocket.getOutputStream();
				byte[] outBuffer = Utils.toByteArray(Command
						.retrieveDeviceInfoCommand());
				byte[] inBuffer = sendMessage(out, outBuffer, in, 33);
				Log.i("Setting device", "finish retrieveDeviceInfo");

				DeviceInfo dev = Response.getDeviceInfoResponse(inBuffer);
				if (dev == null) {
					publishText("\nUnknown device");
				} else {
					/*
					 * publishText("\nDevice Name: " + dev.deviceName);
					 * publishText("\nSerial Number: " + dev.serialNumber +
					 * "\nBluetooth Address: "); for (byte x : dev.btaddress) {
					 * if (x < 0) publishText(Integer.toHexString((short) (x &
					 * 0xFF)) + ":"); else if (x < 16) publishText("0" +
					 * Integer.toHexString((short) x) + ":"); else
					 * publishText(Integer.toHexString((short) x) + ":"); }
					 * publishText("\nVersion: " + Integer.toString((int)
					 * dev.version));
					 */
					publishText("Setting up device.....");
					setupDevice();
					publishText("--> Done!\n"
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

		public boolean setupDevice() throws IOException {
			Calendar today = Calendar.getInstance();
			byte[] outBuffer;
			byte[] inBuffer = new byte[264];
			int done;

			// ==========
			/*
			 * outBuffer = Utils.toByteArray(Command.setTimeCommand(
			 * today.get(Calendar.YEAR), today.get(Calendar.MONTH),
			 * today.get(Calendar.DATE), today.get(Calendar.HOUR_OF_DAY),
			 * today.get(Calendar.MINUTE), today.get(Calendar.SECOND),
			 * today.get(Calendar.MILLISECOND))); do { inBuffer =
			 * sendMessage(out, outBuffer, in, 4); done =
			 * Response.commandResponse(Utils.getSubarray(inBuffer, 0, 4));
			 * Log.i("debug",done==0?"a":"b"); } while (done != 0);
			 * Log.i("Setting device", "finish setTime");
			 */

			// ==========
			outBuffer = Utils
					.toByteArray(Command.setAtmosphereCommand(0, 0, 0));
			do {
				inBuffer = sendMessage(out, outBuffer, in, 4);
				done = Response.commandResponse(Utils.getSubarray(inBuffer, 0,
						4));
			} while (done != 0);
			Log.i("Setting device", "finish setAtmosphere");

			// ==========
			outBuffer = Utils.toByteArray(Command.setGeomagnetismCommand(0, 0,
					0));
			do {
				inBuffer = sendMessage(out, outBuffer, in, 4);
				done = Response.commandResponse(Utils.getSubarray(inBuffer, 0,
						4));
			} while (done != 0);
			Log.i("Setting device", "finish setGeomagnetism");

			// ==========
			outBuffer = Utils.toByteArray(Command.setBatteryCommand(0, 0));
			do {
				inBuffer = sendMessage(out, outBuffer, in, 4);
				done = Response.commandResponse(Utils.getSubarray(inBuffer, 0,
						4));
			} while (done != 0);
			Log.i("Setting device", "finish setBattery");

			// ==========
			outBuffer = Utils.toByteArray(Command.setExcommunicationCommand(0,
					0, 0));
			do {
				inBuffer = sendMessage(out, outBuffer, in, 4);
				done = Response.commandResponse(Utils.getSubarray(inBuffer, 0,
						4));
			} while (done != 0);
			Log.i("Setting device", "finish setExcommunication");

			// ==========
			outBuffer = Utils.toByteArray(Command.setExdataCommand(0, 0, 0, 0,
					0));
			do {
				inBuffer = sendMessage(out, outBuffer, in, 4);
				done = Response.commandResponse(Utils.getSubarray(inBuffer, 0,
						4));
			} while (done != 0);
			Log.i("Setting device", "finish setExdata");

			// ==========
			outBuffer = Utils.toByteArray(Command
					.setAccelerationMeasureCommand(SAMPLING_INTERVAL,
							SAMPLING_NUMBER, 0));
			do {
				inBuffer = sendMessage(out, outBuffer, in, 4);
				done = Response.commandResponse(Utils.getSubarray(inBuffer, 0,
						4));
			} while (done != 0);
			Log.i("Setting device", "finish setAccelerationMeasure");

			// ==========
			outBuffer = Utils.toByteArray(Command
					.setAccelerationRangeCommand(ACCELERATION_RANGE));
			do {
				inBuffer = sendMessage(out, outBuffer, in, 4);
				done = Response.commandResponse(Utils.getSubarray(inBuffer, 0,
						4));
			} while (done != 0);
			Log.i("Setting device", "finish setAccelerationRange");

			return true;
		}

	}

	public void stopConnection() throws IOException {
		while (bgMeasure.isRunning()) {
			stopMeasure();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		in = null;
		out = null;
		btSocket.close();
		connected = false;
	}

	public void stopMeasure() {
		byte[] outBuffer = Utils.toByteArray(Command.stopCommand());
		try {
			out.write(outBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class MeasureThread extends Thread {
		private volatile boolean running;

		public MeasureThread() {
			super();
			running = false;
		}

		public void run() {
			try {
				int hour = GAME_DURATION / 3600;
				int min = (GAME_DURATION - 3600 * hour) / 60;
				int sec = (GAME_DURATION - 3600 * hour - 60 * min) / 60;
				byte[] outBuffer = Utils.toByteArray(Command.startTimeCommand(
						0, 12, 11, 13, 0, 0, 1, 0, 12, 11, 13, hour, min, sec));
				byte[] inBuffer = sendMessage(out, outBuffer, in, 16);

				Date date = Response.getTimeResponse(inBuffer);
				if (date == null) {
					publishText("\n\nTime not set");
					setRunning(false);
				} else {
					/*
					 * publishText("\n\nStart time: " +
					 * Integer.toString(date.syear) + "/" +
					 * Integer.toString(date.smonth) + "/" +
					 * Integer.toString(date.sday) + "\t" +
					 * Integer.toString(date.shour) + ":" +
					 * Integer.toString(date.sminute) + ":" +
					 * Integer.toString(date.ssecond));
					 * publishText("\nEnd time: " + Integer.toString(date.eyear)
					 * + "/" + Integer.toString(date.emonth) + "/" +
					 * Integer.toString(date.eday) + "\t" +
					 * Integer.toString(date.ehour) + ":" +
					 * Integer.toString(date.eminute) + ":" +
					 * Integer.toString(date.esecond));
					 */
					readData(in);
				}
			} catch (IOException e) {
				e.printStackTrace();
				publishText("\nConnection failed!\n" + e.getMessage());
			}
		}

		public void readData(InputStream in) throws IOException {
			running = true;
			(new FishCreateThread()).start();

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
						processData(data);
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
						setRunning(false);
						break;

					case (byte) 0x8F:
						in.read(inBuffer, 2, 2);
						break;

					default:
						// writeByte(inBuffer, 28);
					}
				}
			} while (running && Thread.currentThread().equals(bgMeasure));
		}

		public void setRunning(boolean flag) {
			running = flag;
		}

		public boolean isRunning() {
			return running;
		}

		public void processData(final AccelerationData data) {
			final int dAngle = calcAngle(data.Xvdeta, data.Yvdeta, data.Zvdeta);
			final double dist = calcDistance(data.milisec, data.Xadeta,
					data.Yadeta, data.Zadeta);

			connectHandler.post(new Runnable() {
				public void run() {
					if (dAngle != 0) {
						rodView.rotate(dAngle);
					}
					if (dist != 0) {
						rodView.castLine(dist);
					}
				}
			});
		}

		public int calcAngle(int x, int y, int z) {
			double interpolator;
			int max = FishingRod.MAX_ROD_ANGLE;
			int min = FishingRod.MIN_ROD_ANGLE;
			interpolator = (float) x / ROTATE_THRES_VAL * 2 * (max - min);

			int ang = (int) (-interpolator);
			return ang;
		}

		int current_max_x = 999999;
		int current_min_x = -999999;

		public double calcDistance(long time, int x, int y, int z) {
			double dist = 0;

			if (current_min_x <= -999999) {
				if (x < CAST_THRES_VAL) {
					current_min_x = x;
				}
			} else {
				if (x < current_min_x) {
					current_min_x = x;
				} else {
					if (current_max_x >= 999999) {
						current_max_x = x;
					} else {
						if (current_max_x < x) {
							current_max_x = x;
						} else {
							if (current_max_x > CAST_THRES_VAL) {
								dist = rodView.getMaxDistance()
										* (current_max_x - current_min_x)
										/ CAST_MAX_THRES_VAL;
								current_max_x = 999999;
								current_min_x = -999999;
							}
						}
					}
				}
			}

			return dist;
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
		if (btSocket != null)
			try {
				stopConnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
		super.onDestroy();
	}

	class FishCreateThread extends Thread {
		int count;

		public FishCreateThread() {
			super();
		}

		public void run() {
			while (bgMeasure.isRunning()) {
				connectHandler.post(new Runnable() {
					public void run() {
						createFish();
					}
				});
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void createFish() {
		FishView fish = new FishView(this, 0, (float) 0.1);
		layout.addView(fish);
		fish.startAnim(new PointF(1000, 200), new PointF(-100, 200), 6000);
		Log.i("createfish", "fish created");
	}
}
