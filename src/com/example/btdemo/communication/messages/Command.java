package com.example.btdemo.communication.messages;

public class Command {
	public static final short _HEADER_ = 0x9A;

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

	public static short[] measureCommand(int meas, int sent, int memo) {
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

	public static short[] accelerationCommand(int range) {
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

	public static short[] actCommand(int range) {
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
