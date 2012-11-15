package com.example.btdemo.communication.messages;

import com.example.btdemo.communication.data.AccelerationData;
import com.example.btdemo.communication.data.DataError;

public class EventCommand {
	public static AccelerationData accelerationEvent(byte[] eventCommand) {
		int paraSize = 22;
		int cmdLength = paraSize + 3;
		int code = 0x80;

		if (!Utils.validCommand(eventCommand, cmdLength, code))
			return null;

		AccelerationData event = new AccelerationData();
		long[] tmp = Utils.toLongArray(eventCommand);
		event.milisec = (tmp[5] << 24 | tmp[4] << 16 | tmp[3] << 8 | tmp[2]);
		long Xadeta = (tmp[8] << 16 | tmp[7] << 8 | tmp[6]);
		long Yadeta = (tmp[11] << 16 | tmp[10] << 8 | tmp[9]);
		long Zadeta = (tmp[14] << 16 | tmp[13] << 8 | tmp[12]);
		long Xvdeta = (tmp[17] << 16 | tmp[16] << 8 | tmp[15]);
		long Yvdeta = (tmp[20] << 16 | tmp[19] << 8 | tmp[18]);
		long Zvdeta = (tmp[23] << 16 | tmp[22] << 8 | tmp[21]);

		event.Xadeta = (int) (-(Xadeta & 0x800000) + (Xadeta & 0x7FFFFF));
		event.Yadeta = (int) (-(Yadeta & 0x800000) + (Yadeta & 0x7FFFFF));
		event.Zadeta = (int) (-(Zadeta & 0x800000) + (Zadeta & 0x7FFFFF));
		event.Xvdeta = (int) (-(Xvdeta & 0x800000) + (Xvdeta & 0x7FFFFF));
		event.Yvdeta = (int) (-(Yvdeta & 0x800000) + (Yvdeta & 0x7FFFFF));
		event.Zvdeta = (int) (-(Zvdeta & 0x800000) + (Zvdeta & 0x7FFFFF));

		return event;
	}

	public static DataError errorEvent(byte[] cmd) {
		int paraSize = 5;
		int cmdLength = paraSize + 3;
		int code = 0x87;

		if (!Utils.validCommand(cmd, cmdLength, code))
			return null;

		DataError event = new DataError();
		long[] tmp = Utils.toLongArray(Utils.getSubarray(cmd, 2, 4));
		event.milisec = (tmp[2] + tmp[3] << 8 + tmp[4] << 16 + tmp[5] << 24);
		event.errCode = cmd[5];

		return event;
	}

	public static boolean startEvent(byte[] cmd) {
		int paraSize = 1;
		int cmdLength = paraSize + 3;
		int code = 0x88;

		if (!Utils.validCommand(cmd, cmdLength, code))
			return false;

		if (cmd[2] == 0)
			return true;
		return false;
	}

	/*
	 * 0:計測停止コマンド及び終了時刻による終 1:OptionSW操作による終了 2:計測記録メモリフル終 3:バッテリ残量低下による終了,
	 * 100:開始エラー(計測対象無し) 101:開始エラー(拡張I2C異常)
	 */
	public static int stopEvent(byte[] cmd) {
		int paraSize = 1;
		int cmdLength = paraSize + 3;
		int code = 0x89;

		if (!Utils.validCommand(cmd, cmdLength, code))
			return -1;

		return cmd[2];
	}

}
