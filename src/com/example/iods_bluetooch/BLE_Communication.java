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
		//���Ͷ��豸����ָ��
		byte[] Command_6102 = new byte[13];
		
		//֡ͷ
		Command_6102[0] = 0x7E;
		Command_6102[1] = 0x00;
		Command_6102[2] = 0x10;
		Command_6102[3] = 0x01;
		Command_6102[4] = 0x00;		//С�˸�ʽ
		Command_6102[5] = 0x01;
		Command_6102[6] = 0x00;
		
		//������
		Command_6102[7] = 0x02;
		Command_6102[8] = 0x61;
		
		//״̬��
		Command_6102[9] = (byte) 0xFF;
		
		//����Ϣ��
		
		//CRC
		Command_6102[10] = (byte) 0x0F;
		Command_6102[11] = (byte) 0x67;
		
		//֡β��
		Command_6102[12] = (byte) 0x7E;
						
		BLE_controller.write(Command_6102);		//���Ͷ��豸����ָ��
	}
	
	public static void Send_Command_6103(BluetoothController BLE_controller, String Lock_ID) {
		//���Ͷ��豸����ָ��
		int i;
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(Lock_ID);

		//֡ͷ
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//С�˸�ʽ
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//������
		BluetoothController.RePly_Byte[7] = 0x03;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//״̬��
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//�����豸ID��CRC16ֵ
		
		//��Ϣ��
		BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//�豸ID�� CRC16ֵ
		BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//����CRC����ת��
		
		byte[] Send_Byte = new byte[temp_Int + 1];

		for (i = 0; i<=temp_Int; i++) {
			Send_Byte[i] = BluetoothController.RePly_Byte[i];
		}
		
		BluetoothController.getInstance().write(Send_Byte);			//���Ͷ���״ָ̬��
	}

	public static boolean Try_Open_BLE_Lock(BluetoothController BLE_controller, DBHelper dbHelper, String recive_Str, boolean Pass) {
		//���Ϳ���ָ�recive_Str�ĵ�7~41λλ���豸ID��PassΪtrueʱ����Ȩ�޿ɿ����������û�ʼ�տɿ������޿���Ȩʱ����false
		
		boolean BLE_Lock_Tag = false;
		int i;
		String temp_ID = recive_Str.substring(7, 41);
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(temp_ID);
				
		SQLiteDatabase db=dbHelper.getWritableDatabase();
		
		BLE_Lock_Tag = false;
		String temp_User = UserLogin_Activity.Login_User_ID;
		
		Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, temp_ID});
		if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin") || Pass){
			//�û���Ȩ�򳬼��û�
			
			BLE_Lock_Tag = true;

			//֡ͷ
			BluetoothController.RePly_Byte[0] = 0x7E;
			BluetoothController.RePly_Byte[1] = 0x00;
			BluetoothController.RePly_Byte[2] = 0x10;
			BluetoothController.RePly_Byte[3] = 0x01;
			BluetoothController.RePly_Byte[4] = 0x00;		//С�˸�ʽ
			BluetoothController.RePly_Byte[5] = 0x01;
			BluetoothController.RePly_Byte[6] = 0x00;
			
			//������
			BluetoothController.RePly_Byte[7] = 0x01;
			BluetoothController.RePly_Byte[8] = 0x61;
			
			//״̬��
			BluetoothController.RePly_Byte[9] = (byte) 0xFF;
			
			i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//�����豸ID��CRC16ֵ
			
			//��Ϣ��
			BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//�豸ID�� CRC16ֵ
			BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
			
			int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//����CRC����ת��
			
			byte[] Send_Byte = new byte[temp_Int + 1];

			for (i = 0; i<=temp_Int; i++) {
				Send_Byte[i] = BluetoothController.RePly_Byte[i];
			}
			
			BluetoothController.getInstance().write(Send_Byte);			//���Ϳ���ָ��
						
		}

		cursor.close();
		db.close();
				
		return BLE_Lock_Tag;
		//ֱ������
		
	}

	public static void Send_Command_6100(BluetoothController BLE_controller, String str_UUID) {
		//����д�豸UUIDָ��
		int i;
		
		//֡ͷ
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//С�˸�ʽ
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//������
		BluetoothController.RePly_Byte[7] = 0x00;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//״̬��
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(str_UUID);
		
		for (i = 0; i<16; i++) {
			BluetoothController.RePly_Byte[10 + i] = Device_ID[i];
		}
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(28);	//����CRC����ת��
		
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
		
		BLE_controller.write(Send_Byte);			//���ʹ���UUID���׸��а�
		
	}

	public static void Send_Command_6100_Second(BluetoothController BLE_controller) {
		//����д�豸ID�ĺ�һ���а�
		BLE_controller.write(Send_Second_Byte);			//����д�豸ID�ĺ�һ���а�
	}
	
	public static void Send_Command_6104(BluetoothController BLE_controller) {
		//���豸�汾��
		byte[] Send_Byte = new byte[13];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x04;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
		
		//CRC
		Send_Byte[10] = 0x04;
		Send_Byte[11] = 0x61;
		
		Send_Byte[12] = 0x7E;	//֡������
				
		BLE_controller.write(Send_Byte);			//���ʹ���UUID���׸��а�
				
	}

	
	public static void Send_Command_6105(BluetoothController BLE_controller, int Int_Tag) {
		//д���豸���ñ�־, Int_Tag=0Ϊ true�����־λ��Int_Tag=1д��־λ, Int_Tag=2Ҫ���豸��������NBע�ᣬȻ��д��־λ
		//Int_Tag=3������豸��ƽ̨ע�������ѯ
		//֡ͷ
		byte[] Send_Byte = new byte[14];
		int i;
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x05;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
		
		if (Int_Tag == 0) {
			Send_Byte[10] = 0x00;	//��Ϣ�壬�����־λ
		}
		else if (Int_Tag == 1) {
			Send_Byte[10] = 0x6C;	//��Ϣ�壬д��־λ
		}
		else if (Int_Tag == 2) {
			Send_Byte[10] = (byte) 0x93;	//��Ϣ�壬�����豸����NBע�ᣬ��д��־λ
		}
		else if (Int_Tag == 3) {
			Send_Byte[10] = (byte) 0x03;	//��Ϣ�壬��������豸ע�������ѯ
		}
		
		i = NMS_Communication.Cala_CRC(Send_Byte, 10);		//�����豸ID��CRC16ֵ
		//CRC
		Send_Byte[11] = (byte)(i & 0xFF);		//�豸ID�� CRC16ֵ
		Send_Byte[12] = (byte)(i >> 8);
		
		Send_Byte[13] = 0x7E;	//֡������
				
		BLE_controller.write(Send_Byte);			//���ʹ���UUID���׸��а�
				
	}

	public static void Read_Lock_Status(String locker_ID) {
		//���豸״̬
		
		int i;
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(locker_ID);
		
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//С�˸�ʽ
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//������
		BluetoothController.RePly_Byte[7] = 0x03;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//״̬��
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//�����豸ID��CRC16ֵ
		
		//��Ϣ��
		BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//�豸ID�� CRC16ֵ
		BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//����CRC����ת��
		
		byte[] Send_Byte = new byte[temp_Int + 1];
		
		for (i = 0; i<=temp_Int; i++) {
			Send_Byte[i] = BluetoothController.RePly_Byte[i];
		}
		
		BluetoothController.getInstance().write(Send_Byte);			//���Ϳ���ָ��
		
	}
	
	public static void Send_Command_6121(BluetoothController BLE_controller, String Str_Statuse) {
		//���豸�������ָ��
		byte[] Send_Byte = new byte[15];
		int i;
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x21;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
				
		Send_Byte[10] = (byte) Integer.parseInt(Str_Statuse.substring(0, 2), 16);
		Send_Byte[11] = (byte) Integer.parseInt(Str_Statuse.substring(2), 16);
		
		//CRC
		i = NMS_Communication.Cala_CRC(Send_Byte, 11);		//�����豸ID��CRC16ֵ
		
		//��Ϣ��
		Send_Byte[12] = (byte)(i & 0xFF);		//�豸ID�� CRC16ֵ
		Send_Byte[13] = (byte)(i >> 8);

		//֡β��
		Send_Byte[14] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//�������״ָ̬��
		
	}
	
	public static void Send_Command_6122(BluetoothController BLE_controller, int int_Tag) {
		//���������״ָ̬��, int_TagΪ0ʱ��ʾ�������״̬������Ϊ�˳�����״̬
		byte[] Send_Byte = new byte[15];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x22;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
				
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

		//֡β��
		Send_Byte[13] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//�������״ָ̬��
		
	}

	public static void Send_Command_6123(BluetoothController BLE_controller) {
		// �����豸����ָ��
		byte[] Send_Byte = new byte[13];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x23;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
				
		Send_Byte[10] = 63;
		Send_Byte[11] = 80;
		Send_Byte[12] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//����ָ��
	}
	
	public static void Send_Command_6124_1(BluetoothController BLE_controller, int Length) {
		// ���������������ָ��
		byte[] Send_Byte = new byte[17];
		int i;
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
				
		Send_Byte[10] = 1;	//������
		Send_Byte[11] = 1;
		
		i = Length % 1000;
		if (i > 0) {
			i = (int) (Length/1000) + 1;
		}
		else {
			i = (int) (Length/1000);
		}
		
		//֡����
		Send_Byte[12] = (byte)(i & 0xFF);
		Send_Byte[13] = (byte)(i >> 8);
		
		i = NMS_Communication.Cala_CRC(Send_Byte, 13);		//�����豸ID��CRC16ֵ
		//CRC
		
		Send_Byte[14] = (byte)(i & 0xFF);		//�豸ID�� CRC16ֵ
		Send_Byte[15] = (byte)(i >> 8);

		//֡β��
		Send_Byte[16] = 0x7E;
		
		BLE_controller.write(Send_Byte);
	}
	
	public static void Send_Command_6124_3(BluetoothController BLE_controller) {
		// ������������������ָ��
		byte[] Send_Byte = new byte[16];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
				
		Send_Byte[10] = 3;		//������
		Send_Byte[11] = 1;
		
		Send_Byte[12] = 0x00;
		
		//CRC
		Send_Byte[13] = 0x4D;		//�豸ID�� CRC16ֵ
		Send_Byte[14] = (byte) 0xDC;
		//֡β��
		Send_Byte[15] = 0x7E;
		
		BLE_controller.write(Send_Byte);
	}
	
	public static void Send_Command_6124_4(BluetoothController BLE_controller) {
		// ����������������ѯָ��
		byte[] Send_Byte = new byte[15];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
				
		Send_Byte[10] = 4;	//������
		Send_Byte[11] = 1;
		
		//CRC
		Send_Byte[12] = (byte) 0x86;
		Send_Byte[13] = 0x7A;
		//֡β��
		Send_Byte[14] = 0x7E;
		
		BLE_controller.write(Send_Byte);
	}
	public static void Send_Command_6124_2(BluetoothController BLE_controller, int index_Frame, byte[] Date_Byte) {
		// ���������������ָ��
		byte[] Send_Byte = new byte[13];
		
		Send_Byte[0] = 0x7E;
		Send_Byte[1] = 0x00;
		Send_Byte[2] = 0x10;
		Send_Byte[3] = 0x01;
		Send_Byte[4] = 0x00;		//С�˸�ʽ
		Send_Byte[5] = 0x01;
		Send_Byte[6] = 0x00;
		
		//������
		Send_Byte[7] = 0x24;
		Send_Byte[8] = 0x61;
		Send_Byte[9] = (byte) 0xFF;		//״̬��
				
		Send_Byte[10] = 63;
		Send_Byte[11] = 80;
		Send_Byte[12] = 0x7E;
				
		BLE_controller.write(Send_Byte);			//����ָ��
	}
	
	public static void Try_Open_BLE_Locker(BluetoothController BLE_controller, String Lock_ID) {
		//���Ϳ���ָ�Lock_IDΪ���豸ID
		int i;
		
		byte[] Device_ID =  DataAlgorithm.hexStringToBytes(Lock_ID);

		//֡ͷ
		BluetoothController.RePly_Byte[0] = 0x7E;
		BluetoothController.RePly_Byte[1] = 0x00;
		BluetoothController.RePly_Byte[2] = 0x10;
		BluetoothController.RePly_Byte[3] = 0x01;
		BluetoothController.RePly_Byte[4] = 0x00;		//С�˸�ʽ
		BluetoothController.RePly_Byte[5] = 0x01;
		BluetoothController.RePly_Byte[6] = 0x00;
		
		//������
		BluetoothController.RePly_Byte[7] = 0x01;
		BluetoothController.RePly_Byte[8] = 0x61;
		
		//״̬��
		BluetoothController.RePly_Byte[9] = (byte) 0xFF;
		
		i = NMS_Communication.Cala_CRC(Device_ID, 0, 16);		//�����豸ID��CRC16ֵ
		
		//��Ϣ��
		BluetoothController.RePly_Byte[10] = (byte)(i & 0xFF);		//�豸ID�� CRC16ֵ
		BluetoothController.RePly_Byte[11] = (byte)(i >> 8);
		
		int temp_Int = BluetoothController.CRC_And_Trans_7E(14);	//����CRC����ת��
		
		byte[] Send_Byte = new byte[temp_Int + 1];
		
		for (i = 0; i<=temp_Int; i++) {
			Send_Byte[i] = BluetoothController.RePly_Byte[i];
		}
		
		BluetoothController.getInstance().write(Send_Byte);			//���Ϳ���ָ��

		//ֱ������
		
	}
	
	

}
