package com.example.btdemo.communication.messages;

import com.example.btdemo.communication.data.*;

public class Response {
	public static final short _HEADER_ = 0x9A;

	// #1
	public static DeviceInfo getDeviceInfoResponse(byte[] response) {
		int paraSize = 30;
		int cmdLength = paraSize + 3;
		int code = 0x90;
		DeviceInfo info = new DeviceInfo();

		if (!Utils.validCommand(response, cmdLength, code))
			return null;

		info.serialNumber = Utils.getString(response, 2, 10);
		info.btaddress = Utils.getSubarray(response, 12, 6);
		info.version = Utils.getInteger(response, 18, 4);
		info.deviceName = Utils.getString(response, 22, 10);

		return info;
	}

	// #2
	public static int commandResponse(byte[] response) {
		int paraSize = 1;
		int cmdLength = paraSize + 3;
		int code = 0x8F;

		if (!Utils.validCommand(response, cmdLength, code))
			return -1;

		return response[2];
	}

	// #4
	public static Date getTimeResponse(byte[] response) {
		int paraSize = 13;
		int cmdLength = paraSize + 3;
		int code = 0x93;

		if (!Utils.validCommand(response, cmdLength, code))
			return null;
		if (response[2] == 0)
			return null;

		Date date = new Date();
		int year = 2000;

		date.syear = year + response[3];
		date.smonth = response[4];
		date.sday = response[5];
		date.shour = response[6];
		date.sminute = response[7];
		date.ssecond = response[8];

		date.eyear = year + response[9];
		date.emonth = response[10];
		date.eday = response[11];
		date.ehour = response[12];
		date.eminute = response[13];
		date.esecond = response[14];

		return date;
	}

	// #45
	public static int getOperationStatusResponse(byte[] response) {
		int paraSize = 1;
		int cmdLength = paraSize + 3;
		int code = 0xBC;

		if (!Utils.validCommand(response, cmdLength, code))
			return -1;

		return response[2];
	}

}
