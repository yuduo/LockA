package com.example.iods_bluetooch;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.iods_common.DBHelper;
import com.example.iods_common.DataAlgorithm;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.UserLogin_Activity;

public class BLE_Communication {
	
	static byte[] Send_Second_Byte = null;

	public static void Send_Command_6102(BluetoothController BLE_controller) {
		//发送读设备编码指令
		byte[] Command_6102 = new byte[13];
		
		//帧头
		Command_6102[0] = 0x7E;
		Command_6102[1] = 0x00;
		Command_6102[2] = 0x10;
		Command_6102[3] = 0x01;
		Command_6102[4] = 0x00;		//小端格式
		Command_6102[5] = 0x01;
		Command_6102[6] = 0x00;
		
		//命令码
		Command_6102[7] = 0x02;
		Command_6102[8] = 0x61;
		
		//状态码
		Command_6102[9] = (byte) 0xFF;
		
		//无消息体
		
		//CRC
		Command_6102[10] = (byte) 0x0F;
		Command_6102[11] = (byte) 0x67;
		
		//帧尾符
		Command_6102[12] = (byte) 0x7E;
						
		BLE_controller.write(Command_6102);		//发送读设备编码指令
	}
	
	public static void Send_Command_6103(BluetoothController BLE_controller, String Lock_ID) {
		//发送读设备编码指令
		int i;
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(Lock_ID);

		//帧头
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//小端格式
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//命令码
		BluetoothController.RePly_Byte[7] = 0x03;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//状态码
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//计算设备ID的CRC16值
		
		//消息体
		BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//设备ID的 CRC16值
		BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//计算CRC，并转义
		
		byte[] Send_Byte = new byte[temp_Int + 1];

		for (i = 0; i<=temp_Int; i++) {
			Send_Byte[i] = BluetoothController.RePly_Byte[i];
		}
		
		BluetoothController.getInstance().write(Send_Byte);			//发送读锁状态指令
	}

	public static boolean Try_Open_BLE_Lock(BluetoothController BLE_controller, DBHelper dbHelper, String recive_Str, boolean Pass) {
		//发送开锁指令，recive_Str的第7~41位位锁设备ID，Pass为true时无视权限可开锁，超级用户始终可开锁。无开锁权时返回false
		
		boolean BLE_Lock_Tag = false;
		int i;
		String temp_ID = recive_Str.substring(7, 41);
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(temp_ID);
				
		SQLiteDatabase db=dbHelper.getWritableDatabase();
		
		BLE_Lock_Tag = false;
		String temp_User = UserLogin_Activity.Login_User_ID;
		
		Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, temp_ID});
		if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin") || Pass){
			//用户有权或超级用户
			
			BLE_Lock_Tag = true;

			//帧头
			BluetoothController.RePly_Byte[0] = 0x7E;
			BluetoothController.RePly_Byte[1] = 0x00;
			BluetoothController.RePly_Byte[2] = 0x10;
			BluetoothController.RePly_Byte[3] = 0x01;
			BluetoothController.RePly_Byte[4] = 0x00;		//小端格式
			BluetoothController.RePly_Byte[5] = 0x01;
			BluetoothController.RePly_Byte[6] = 0x00;
			
			//命令码
			BluetoothController.RePly_Byte[7] = 0x01;
			BluetoothController.RePly_Byte[8] = 0x61;
			
			//状态码
			BluetoothController.RePly_Byte[9] = (byte) 0xFF;
			
			i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//计算设备ID的CRC16值
			
			//消息体
			BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//设备ID的 CRC16值
			BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
			
			int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//计算CRC，并转义
			
			byte[] Send_Byte = new byte[temp_Int + 1];

			for (i = 0; i<=temp_Int; i++) {
				Send_Byte[i] = BluetoothController.RePly_Byte[i];
			}
			
			BluetoothController.getInstance().write(Send_Byte);			//发送开锁指令
						
		}

		cursor.close();
		db.close();
				
		return BLE_Lock_Tag;
		//直联开锁
		
	}

	public static void Send_Command_6100(BluetoothController BLE_controller, String str_UUID) {
		//发送写设备UUID指令
		int i;
		
		//帧头
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//小端格式
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//命令码
		BluetoothController.RePly_Byte[7] = 0x00;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//状态码
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(str_UUID);
		
		for (i = 0; i<16; i++) {
			BluetoothController.RePly_Byte[10 + i] = Device_ID[i];
		}
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(28);	//计算CRC，并转义
		
		byte[] Send_Byte = new byte[20];
		
		for (i = 0; i<20; i++) {
			Send_Byte[i] = BluetoothController.RePly_Byte[i];
		}
		
		Send_Second_Byte = new byte[temp_Int - 17];
		Send_Second_Byte[0] = 0x02;
		Send_Second_Byte[1] = 0x02;
		for (i = 20; i <= temp_Int; i++) {
			Send_Second_Byte[i - 18] = BluetoothController.RePly_Byte[i];
		}
		
		BLE_controller.write(Send_Byte);			//发送传送UUID的首个切包
		
	}

	public static void Send_Command_6100_Second(BluetoothController BLE_controller) {
		//发送写设备ID的后一个切包
		BLE_controller.write(Send_Second_Byte);			//发送写设备ID的后一个切包
	}
	
	public static void Send_Command_6104(BluetoothController BLE_controller) {
		//度设备版本号
		byte[] Send_Byte = new byte[13];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x04;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
		
		//CRC
		Send_Byte[10] = 0x04;
		Send_Byte[11] = 0x61;
		
		Send_Byte[12] = 0x7E;	//帧结束符
				
		BLE_controller.write(Send_Byte);			//发送传送UUID的首个切包
				
	}

	
	public static void Send_Command_6105(BluetoothController BLE_controller, int Int_Tag) {
		//写锁设备配置标志, Int_Tag=0为 true清除标志位，Int_Tag=1写标志位, Int_Tag=2要求设备立即进行NB注册，然后写标志位
		//Int_Tag=3则进行设备的平台注册情况查询
		//帧头
		byte[] Send_Byte = new byte[14];
		int i;
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x05;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
		
		if (Int_Tag == 0) {
			Send_Byte[10] = 0x00;	//消息体，清除标志位
		}
		else if (Int_Tag == 1) {
			Send_Byte[10] = 0x6C;	//消息体，写标志位
		}
		else if (Int_Tag == 2) {
			Send_Byte[10] = (byte) 0x93;	//消息体，提请设备进行NB注册，并写标志位
		}
		else if (Int_Tag == 3) {
			Send_Byte[10] = (byte) 0x03;	//消息体，提请进行设备注册情况查询
		}
		
		i = NMS_Communication.Cala_CRC(Send_Byte, 10);		//计算设备ID的CRC16值
		//CRC
		Send_Byte[11] = (byte)(i & 0xFF);		//设备ID的 CRC16值
		Send_Byte[12] = (byte)(i >> 8);
		
		Send_Byte[13] = 0x7E;	//帧结束符
				
		BLE_controller.write(Send_Byte);			//发送传送UUID的首个切包
				
	}

	public static void Read_Lock_Status(String locker_ID) {
		//读设备状态
		
		int i;
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(locker_ID);
		
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//小端格式
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//命令码
		BluetoothController.RePly_Byte[7] = 0x03;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//状态码
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//计算设备ID的CRC16值
		
		//消息体
		BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//设备ID的 CRC16值
		BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//计算CRC，并转义
		
		byte[] Send_Byte = new byte[temp_Int + 1];
		
		for (i = 0; i<=temp_Int; i++) {
			Send_Byte[i] = BluetoothController.RePly_Byte[i];
		}
		
		BluetoothController.getInstance().write(Send_Byte);			//发送开锁指令
		
	}
	
	public static void Send_Command_6121(BluetoothController BLE_controller, String Str_Statuse) {
		//锁设备调测控制指令
		byte[] Send_Byte = new byte[15];
		int i;
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x21;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
				
		Send_Byte[10] = (byte) Integer.parseInt(Str_Statuse.substring(0, 2), 16);
		Send_Byte[11] = (byte) Integer.parseInt(Str_Statuse.substring(2), 16);
		
		//CRC
		i = NMS_Communication.Cala_CRC(Send_Byte, 11);		//计算设备ID的CRC16值
		
		//消息体
		Send_Byte[12] = (byte)(i & 0xFF);		//设备ID的 CRC16值
		Send_Byte[13] = (byte)(i >> 8);

		//帧尾符
		Send_Byte[14] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//进入调测状态指令
		
	}
	
	public static void Send_Command_6122(BluetoothController BLE_controller, int int_Tag) {
		//读进入调测状态指令, int_Tag为0时表示进入调试状态，否则为退出调测状态
		byte[] Send_Byte = new byte[15];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x22;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
				
		if (int_Tag == 0) {
			Send_Byte[10] = 0;
			Send_Byte[11] = 15;
			Send_Byte[12] = 36;
		}
		else {
			Send_Byte[10] = (byte) 0xFF;
			Send_Byte[11] = -1;
			Send_Byte[12] = 58;
		}

		//帧尾符
		Send_Byte[13] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//进入调测状态指令
		
	}

	public static void Send_Command_6123(BluetoothController BLE_controller) {
		// 发送设备休眠指令
		byte[] Send_Byte = new byte[13];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x23;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
				
		Send_Byte[10] = 63;
		Send_Byte[11] = 80;
		Send_Byte[12] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//休眠指令
	}
	
	public static void Send_Command_6124_1(BluetoothController BLE_controller, int Length) {
		// 发送启动软件升级指令
		byte[] Send_Byte = new byte[17];
		int i;
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
				
		Send_Byte[10] = 1;	//类型码
		Send_Byte[11] = 1;
		
		i = Length % 1000;
		if (i > 0) {
			i = (int) (Length/1000) + 1;
		}
		else {
			i = (int) (Length/1000);
		}
		
		//帧数量
		Send_Byte[12] = (byte)(i & 0xFF);
		Send_Byte[13] = (byte)(i >> 8);
		
		i = NMS_Communication.Cala_CRC(Send_Byte, 13);		//计算设备ID的CRC16值
		//CRC
		
		Send_Byte[14] = (byte)(i & 0xFF);		//设备ID的 CRC16值
		Send_Byte[15] = (byte)(i >> 8);

		//帧尾符
		Send_Byte[16] = 0x7E;
		
		BLE_controller.write(Send_Byte);
	}
	
	public static void Send_Command_6124_3(BluetoothController BLE_controller) {
		// 发送软件升级下载完成指令
		byte[] Send_Byte = new byte[16];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
				
		Send_Byte[10] = 3;		//类型码
		Send_Byte[11] = 1;
		
		Send_Byte[12] = 0x00;
		
		//CRC
		Send_Byte[13] = 0x4D;		//设备ID的 CRC16值
		Send_Byte[14] = (byte) 0xDC;
		//帧尾符
		Send_Byte[15] = 0x7E;
		
		BLE_controller.write(Send_Byte);
	}
	
	public static void Send_Command_6124_4(BluetoothController BLE_controller) {
		// 发送软件升级结果查询指令
		byte[] Send_Byte = new byte[15];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
				
		Send_Byte[10] = 4;	//类型码
		Send_Byte[11] = 1;
		
		//CRC
		Send_Byte[12] = (byte) 0x86;
		Send_Byte[13] = 0x7A;
		//帧尾符
		Send_Byte[14] = 0x7E;
		
		BLE_controller.write(Send_Byte);
	}
	public static void Send_Command_6124_2(BluetoothController BLE_controller, int index_Frame, byte[] Date_Byte) {
		// 发送升级软件数据指令
		byte[] Send_Byte = new byte[13];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//小端格式
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//命令码
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//状态码
				
		Send_Byte[10] = 63;
		Send_Byte[11] = 80;
		Send_Byte[12] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//休眠指令
	}
	
	public static void Try_Open_BLE_Locker(BluetoothController BLE_controller, String Lock_ID) {
		//发送开锁指令，Lock_ID为锁设备ID
		int i;
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(Lock_ID);

		//帧头
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//小端格式
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//命令码
		BluetoothController.RePly_Byte[7] = 0x01;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//状态码
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//计算设备ID的CRC16值
		
		//消息体
		BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//设备ID的 CRC16值
		BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//计算CRC，并转义
		
		byte[] Send_Byte = new byte[temp_Int + 1];
		
		for (i = 0; i<=temp_Int; i++) {
			Send_Byte[i] = BluetoothController.RePly_Byte[i];
		}
		
		BluetoothController.getInstance().write(Send_Byte);			//发送开锁指令

		//直联开锁
		
	}
	
	

}
