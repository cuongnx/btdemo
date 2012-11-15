package com.example.btdemo.communication.messages;

public class Command {
	public static final short _HEADER_ = 0x9A;

	// #1
	public static short[] retrieveDeviceInfoCommand() {
		final short cmdCode = 0x10;
		final int paraSize = 1;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = 0x00;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #2
	public static short[] setTimeCommand(int year, int month, int day,
			int hour, int min, int sec, int milisec) {
		final short cmdCode = 0x11;
		final int paraSize = 8;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) (year - 2000);
		msg[3] = (short) month;
		msg[4] = (short) day;
		msg[5] = (short) hour;
		msg[6] = (short) min;
		msg[7] = (short) sec;
		msg[8] = (short) (milisec & 0xFF);
		msg[9] = (short) ((milisec & 0xFF00) >> 8);

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #3
	public static short[] getTimeCommand() {
		final short cmdCode = 0x12;
		final int paraSize = 1;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = 0x00;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #4
	public static short[] startTimeCommand(int smode, int syear, int smonth,
			int sday, int shour, int smin, int ssec, int emode, int eyear,
			int emonth, int eday, int ehour, int emin, int esec) {
		final short cmdCode = 0x13;
		final int paraSize = 14;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) smode;
		msg[3] = (short) (syear - 2000);
		msg[4] = (short) smonth;
		msg[5] = (short) sday;
		msg[6] = (short) shour;
		msg[7] = (short) smin;
		msg[8] = (short) ssec;

		msg[9] = (short) emode;
		msg[10] = (short) (eyear - 2000);
		msg[11] = (short) emonth;
		msg[12] = (short) eday;
		msg[13] = (short) ehour;
		msg[14] = (short) emin;
		msg[15] = (short) esec;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #6
	public static short[] stopCommand() {
		final short cmdCode = 0x15;
		final int paraSize = 1;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = 0x00;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #7
	public static short[] setAccelerationMeasureCommand(int meas, int sent,
			int memo) {
		final short cmdCode = 0x16;
		final int paraSize = 3;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) meas;
		msg[3] = (short) sent;
		msg[4] = (short) memo;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #9
	public static short[] setGeomagnetismCommand(int meas, int sent, int memo) {
		final short cmdCode = 0x18;
		final int paraSize = 3;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) meas;
		msg[3] = (short) sent;
		msg[4] = (short) memo;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #11
	public static short[] setAtmosphereCommand(int meas, int sent, int memo) {
		final short cmdCode = 0x1A;
		final int paraSize = 3;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) meas;
		msg[3] = (short) sent;
		msg[4] = (short) memo;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #13
	public static short[] setBatteryCommand(int sent, int memo) {
		final short cmdCode = 0x1C;
		final int paraSize = 2;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) sent;
		msg[3] = (short) memo;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #15
	public static short[] setExdataCommand(int meas, int sent, int memo,
			int dsent, int dmemo) {
		final short cmdCode = 0x1E;
		final int paraSize = 5;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) meas;
		msg[3] = (short) sent;
		msg[4] = (short) memo;
		msg[5] = (short) dsent;
		msg[6] = (short) dmemo;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #17
	public static short[] setExcommunicationCommand(int meas, int sent, int memo) {
		final short cmdCode = 0x20;
		final int paraSize = 3;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) meas;
		msg[3] = (short) sent;
		msg[4] = (short) memo;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #19
	public static short[] setAccelerationRangeCommand(int range) {
		final short cmdCode = 0x22;
		final int paraSize = 1;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = (short) range;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}

	// #45
	public static short[] getOperationStatusCommand(int range) {
		final short cmdCode = 0x3C;
		final int paraSize = 1;

		final int cmdLength = paraSize + 3;
		short[] msg = new short[cmdLength];

		msg[0] = _HEADER_;
		msg[1] = cmdCode;

		msg[2] = 0x00;

		msg[cmdLength - 1] = 0x00;
		for (int i = 0; i < cmdLength - 1; ++i) {
			msg[cmdLength - 1] ^= msg[i];
		}

		return msg;
	}
}
