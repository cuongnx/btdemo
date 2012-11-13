package com.example.btdemo.communication.messages;

public class Utils {
	public static byte[] toByteArray(short[] msg) {
		byte[] x = new byte[msg.length];
		for (int i = 0; i < msg.length; ++i) {
			x[i] = (byte) msg[i];
		}
		return x;
	}

	public static byte[] getSubarray(byte[] msg, int offset, int length) {
		if ((offset + length) > msg.length)
			length = msg.length - offset;
		byte[] x = new byte[length];
		for (int i = 0; i < length; ++i) {
			x[i] = msg[i + offset];
		}

		return x;
	}

	public static byte[] reverse(byte[] msg) {
		byte[] x = new byte[msg.length];
		for (int i = 0; i < msg.length; ++i) {
			x[i] = msg[msg.length - i - 1];
		}
		return x;
	}

	public static int getInteger(byte[] num) {
		if (num.length > 4)
			return 0;

		int x = 0;
		for (int i = 0; i < num.length; ++i) {
			x += num[i] << (8 * i);
		}
		return x;
	}

	public static int getInteger(byte[] num, int offset, int length) {
		return getInteger(getSubarray(num, offset, length));
	}

	public static String getString(byte[] msg) {
		return new String(msg);
	}

	public static String getString(byte[] msg, int offset, int length) {
		return new String(getSubarray(msg, offset, length));
	}

	public static boolean checksum(byte[] cmd) {
		byte check = 0;
		for (byte x : cmd) {
			check ^= x;
		}
		if (check == 0)
			return true;
		else
			return false;
	}

	public static boolean validCommand(byte[] cmd, int cmdLength, int code) {
		if (cmd[0] != (byte) 0x9A)
			return false;
		if (!checksum(cmd))
			return false;
		if (cmd[1] != (byte) code)
			return false;
		if (cmd.length != cmdLength)
			return false;
		return true;
	}
}
