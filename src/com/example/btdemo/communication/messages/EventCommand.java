package com.example.btdemo.communication.messages;

import com.example.btdemo.communication.data.AccelerationData;

public class EventCommand {
	public static AccelerationData noticeEvent(byte[] eventCommand) {
		int paraSize = 22;
		int cmdLength = paraSize + 3;
		int code = 0x80;

		if (!Utils.validCommand(eventCommand, cmdLength, code))
			return null;

		AccelerationData event = new AccelerationData();

		event.milisec = (eventCommand[6] << 24 + eventCommand[5] << 16 + eventCommand[4] << 8 + eventCommand[3]);
		event.Xadeta = (eventCommand[9] << 16 + eventCommand[8] << 8 + eventCommand[7]);
		event.Yadeta = (eventCommand[12] << 16 + eventCommand[11] << 8 + eventCommand[10]);
		event.Zadeta = (eventCommand[15] << 16 + eventCommand[14] << 8 + eventCommand[13]);
		event.Xvdeta = (eventCommand[18] << 16 + eventCommand[17] << 8 + eventCommand[16]);
		event.Yvdeta = (eventCommand[21] << 16 + eventCommand[20] << 8 + eventCommand[19]);
		event.Zvdeta = (eventCommand[24] << 16 + eventCommand[23] << 8 + eventCommand[22]);

		return event;
	}
}
