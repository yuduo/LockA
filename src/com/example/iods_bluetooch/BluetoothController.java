package com.example.iods_bluetooch;

import java.util.Iterator;
import java.util.List;

import com.example.iods_common.DataAlgorithm;
import com.example.iods_common.NMS_Communication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * ����������
 * 
 * @author wangdandan
 * 
 */
public class BluetoothController {
	private String deviceAddress;
	private String deviceName;

	private BluetoothAdapter bleAdapter;
	private Handler serviceHandler;// ������

	static BluetoothGatt bleGatt;// ����
	static BluetoothGattCharacteristic bleGattCharacteristic;

	byte[] Byte_Commnd = new byte[100];
	static byte[] RePly_Byte = new byte[50];
	int Numb_Byte,  Total_Cell, Numb_Cell;		//�ѱ�������ݳ���, �а������� �а����
	/**
	 * ����ģʽ
	 */
	private static BluetoothController instance = null;

	private BluetoothController() {
	}

	public static BluetoothController getInstance() {
		if (instance == null)
			instance = new BluetoothController();
		return instance;
	}

	/**
	 * ��ʼ������
	 * 
	 * @return
	 */
	public boolean initBLE() {
		// ��鵱ǰ�ֻ��Ƿ�֧��ble ����,�����֧���˳�����
		// App.app���ܻᱨ���嵥�ļ��в�Ҫ��������application
		if (!App.app.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}
		// ��ʼ�� Bluetooth adapter, ͨ�������������õ�һ���ο�����������(API����������android4.3�����ϰ汾)
		final BluetoothManager bluetoothManager = (BluetoothManager) App.app
				.getSystemService(Context.BLUETOOTH_SERVICE);
		bleAdapter = bluetoothManager.getAdapter();
		// ����豸���Ƿ�֧������
		if (bleAdapter == null)
			return false;
		else
			return true;
	}

	/**
	 * ���÷����¼�������
	 * 
	 * @return
	 */
	public void setServiceHandler(Handler handler) {
		// handler��ʼ����service�У������߼��ͽ���Ĺ�ͨ
		serviceHandler = handler;
	}

	/**
	 * ���������ص�
	 */
	BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int arg1, byte[] arg2) {
			// device�������������豸
			String name = device.getName();
			if (name == null)
				return;
			if (BluetoothController.this.serviceHandler != null
					&& !name.isEmpty()) {
				Message msg = new Message();
				msg.what = ConstantUtils.WM_UPDATE_BLE_LIST;
				msg.obj = device;
				BluetoothController.this.serviceHandler.sendMessage(msg);
			}
		}
	};

	/**
	 * ��ʼɨ������
	 */
	public void startScanBLE() {
		bleAdapter.startLeScan(bleScanCallback);
		if (serviceHandler != null)
			serviceHandler.sendEmptyMessageDelayed(
					ConstantUtils.WM_STOP_SCAN_BLE, 1000);
	}

	/**
	 * ֹͣɨ�������豸
	 */
	public void stopScanBLE() {
		bleAdapter.stopLeScan(bleScanCallback);
	}

	/**
	 * �Ƿ�������
	 * 
	 * @return
	 */
	public boolean isBleOpen() {
		return bleAdapter.isEnabled();
	}

	/**
	 * ���������豸
	 * 
	 * @param device
	 *            �����ӵ��豸
	 */
	public void connect(EntityDevice device) {
		deviceAddress = device.getAddress();
		deviceName = device.getName();
		BluetoothDevice localBluetoothDevice = bleAdapter
				.getRemoteDevice(device.getAddress());
		if (bleGatt != null) {

			bleGatt.disconnect();
			bleGatt.close();
			bleGatt = null;
		}
		bleGatt = localBluetoothDevice.connectGatt(App.app, false,
				bleGattCallback);
		

	}
	
	@SuppressLint("NewApi") 
	public void close() {
		//�ر���������
		if (bleGatt != null) {
			bleGatt.close();
		}
	}

	/**
	 * ������ͨ�Żص�
	 */
	public BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
		/**
		 * �յ���Ϣ
		 */
		public void onCharacteristicChanged(
				BluetoothGatt paramAnonymousBluetoothGatt,
				BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic) {

			int i, temp_Int, index_Frame;
			String temp_Str;
			byte[] Send_Byte = null;
						
			RePly_Byte[0] = 0x7E;
			RePly_Byte[1] = 0x00;
			RePly_Byte[2] = 0x10;
			RePly_Byte[3] = 0x01;
			RePly_Byte[4] = 0x00;		//С�˸�ʽ
			RePly_Byte[5] = 0x01;
			RePly_Byte[6] = 0x00;

			RePly_Byte[9] = (byte) 0x6C;	//���а��Ļظ�
			//֡β��
			RePly_Byte[13] = (byte) 0x7E;
			
			//TODO
			byte[] arrayOfByte = paramAnonymousBluetoothGattCharacteristic.getValue();
			if (BluetoothController.this.serviceHandler != null) {
				temp_Int = 0;
				
				if (arrayOfByte.length < 3) {
					temp_Int = 0;
				}
				else if (arrayOfByte[0] == 0x7E && arrayOfByte[1] == 0x00 && arrayOfByte[2] == 0x10) {
					//��֡ͷ
					Numb_Cell = 0;
					for (i = 1; i<arrayOfByte.length; i++) {
						//�鿴�Ƿ���֡β
						if (arrayOfByte[i] == 0x7E) {
							//��֡β
							temp_Int = i;
							break;
						}
					}
										
					if (temp_Int > 9) {
						//��֡β�Ĵ���
												
						for (i = 0; i<= temp_Int; i++) {
							Byte_Commnd[i] = arrayOfByte[i];
						}
												
						temp_Int = 1;
					}
					else {
						//��֡β�Ĵ���
						for (i = 0; i< arrayOfByte.length; i++) {
							Byte_Commnd[i] = arrayOfByte[i];
						}
						
						if (Numb_Cell == 0) {
							//��֡ͷʱ���ǵ�һ֡
							Numb_Cell = 1;
							Numb_Byte = arrayOfByte.length;
						}
						
						temp_Int = 0;		//��֡β����ظ��յ��а�Ӧ��
						
						RePly_Byte[10] = (byte) Numb_Cell;	//�ظ��а�����Ϣ�壺�а����
						RePly_Byte[7] = Byte_Commnd[7];
						RePly_Byte[8] = Byte_Commnd[8];		//�а�������
						
						temp_Int = CRC_And_Trans_7E(13);	//����CRC16��������7D 7E��ת��

						if (temp_Int == 13) {
							Send_Byte = new byte[14];
						}
						else if (temp_Int == 14) {
							Send_Byte = new byte[15];
						}
						else if (temp_Int == 15) {
							Send_Byte = new byte[16];
						}
						
						for (i = 0; i<=temp_Int; i++) {
							Send_Byte[i] = RePly_Byte[i];
						}
						
						BluetoothController.getInstance().write(Send_Byte);		//���Ͷ��а��Ľ��ջظ�
					}
				}
				else {
					//��֡ͷ��
					temp_Int = 0;

					if (arrayOfByte[1] == arrayOfByte[0] && Numb_Cell > 0) {
						//����а��Ĵ���
						temp_Int = 1;
					}
					else if (arrayOfByte[1] > arrayOfByte[0]  && Numb_Cell > 0) {
						//����
						temp_Int = -1;
					}
					else {
						if (Numb_Cell > 0) {
							//�ظ��յ��а�Ӧ��
							RePly_Byte[10] = arrayOfByte[1];	//�ظ��а�����Ϣ�壺�а����
							RePly_Byte[7] = Byte_Commnd[7];
							RePly_Byte[8] = Byte_Commnd[8];		//�а�������
							
							temp_Int = CRC_And_Trans_7E(13);	//����CRC16��������7D 7E��ת��

							if (temp_Int == 13) {
								Send_Byte = new byte[14];
							}
							else if (temp_Int == 14) {
								Send_Byte = new byte[15];
							}
							else if (temp_Int == 15) {
								Send_Byte = new byte[16];
							}
							
							for (i = 0; i<=temp_Int; i++) {
								Send_Byte[i] = RePly_Byte[i];
							}
							
							BluetoothController.getInstance().write(Send_Byte);		//���Ͷ��а��Ľ��ջظ�
						}
						temp_Int = 0;
					}
					
					index_Frame = arrayOfByte[1];
					if (Numb_Cell > 0 && index_Frame == Numb_Cell + 1) {
						//����						
						for (i = 2; i < arrayOfByte.length; i++) {
							Byte_Commnd[Numb_Byte + i - 2] = arrayOfByte[i];
						}
						
						Numb_Byte = Numb_Byte + arrayOfByte.length - 2;
						
						if (temp_Int == 0) {
							Numb_Cell = arrayOfByte[1];
						}
						else {
							Numb_Cell = 0;
						}
					}
				}
				
				//temp_Int = 1����а��Ĵ��� 		temp_Int = -1�а� ���ͳ���
				
				if (temp_Int != 0) {
					temp_Int = 0;
					//����֡β
					for (i = 9; 9 < Byte_Commnd.length; i++) {
						if (Byte_Commnd[i] == 0x7E) {
							temp_Int = i;
							break;
						}
					}
				}
				
				if (Numb_Cell == 0 && temp_Int > 9) {
					int j, k;
					//�ҵ�֡β�������ת���CRC����
					//��7D 5E ��ԭ��7Eת�� 7E
					i = 1;
					j = 0;
					while (j == 0) {
						if (Byte_Commnd[i] == 0x7D && Byte_Commnd[i+1] == 0x5E ) {
							//������������ 7D 5E���򽫺�������ȫ��ǰ��һλ�����ݳ��� - 1������ 7D, 5E��ԭΪ7E
							Byte_Commnd[i] = 0x7E;
							for (k = i + 2; k <= temp_Int; k++) {
								Byte_Commnd[k - 1] = Byte_Commnd[k];
							}
							temp_Int--;
						}
						
						i ++;
						if (i >= temp_Int - 1)		j = 1;
					}
					
					//��7D 5D ��ԭ��7Eת�� 7D
					i = 1;
					j = 0;
					while (j == 0) {
						if (Byte_Commnd[i] == 0x7D && Byte_Commnd[i+1] == 0x5D ) {
							//������������ 7D 5D���򽫺�������ȫ��ǰ��һλ�����ݳ��� - 1������ 7D, 5D��ԭΪ7D
							Byte_Commnd[i] = 0x7D;
							for (k = i + 2; k <= temp_Int; k++) {
								Byte_Commnd[k - 1] = Byte_Commnd[k];
							}
							temp_Int--;
						}
						
						i ++;
						if (i >= temp_Int - 1)		j = 1;
					}
					
					i = NMS_Communication.Cala_CRC(Byte_Commnd, temp_Int - 3);		//����CRC16
					if (Byte_Commnd[temp_Int - 2] == (byte)(i & 0xFF) && Byte_Commnd[temp_Int - 1] == (byte)(i >> 8)) {
						//CRC��ȷ
						temp_Str = "C";
					}
					else {
						temp_Str = "E";
					}
					
					temp_Str = temp_Str + Integer.toHexString(Byte_Commnd[8]);
					
					if (Byte_Commnd[7] < 16) {
						temp_Str = temp_Str + "0" + Integer.toHexString(Byte_Commnd[7]);
					}
					else {
						temp_Str = temp_Str + Integer.toHexString(Byte_Commnd[7]);
					}
					
					if (Byte_Commnd[9] < 0) {
						temp_Str = temp_Str + Integer.toHexString(Byte_Commnd[9] + 256);
					}
					else if (Byte_Commnd[9] < 16) {
						temp_Str = temp_Str + "0" + Integer.toHexString(Byte_Commnd[9]);
					}
					else {
						temp_Str = temp_Str + Integer.toHexString(Byte_Commnd[9]);
					}
					
					Send_Byte = new byte[34];
					for (i = 10; i <= temp_Int-3; i++) {
						if (i - 10 >= 34) {
							break;
						}
						Send_Byte[i - 10] = Byte_Commnd[i];
					}
					
					if (Byte_Commnd[7] == 0x04 && Byte_Commnd[8] == 0x61) {
						//�����豸�汾����Ҫ����Ϣ��ת����ASIIC��
						String res = new String(Send_Byte);
						temp_Str = temp_Str + res;
					}
					else {
						//����ָ��������Ϣ�壬��ת���ַ���16����������
						temp_Str = temp_Str + DataAlgorithm.byteToHexString(Send_Byte, i - 10);
					}
					
					Message msg = new Message();
					msg.what = ConstantUtils.WM_RECEIVE_MSG_FROM_BLE;
					msg.obj = temp_Str.toUpperCase();
					BluetoothController.this.serviceHandler.sendMessage(msg);
				}
			}
			// Ҳ�����ȴ�ӡ��������
			Log.i("TEST",
					ConvertUtils.getInstance().bytesToHexString(arrayOfByte));
		}

		private String bytetostring(byte[] send_Byte) {
			// TODO Auto-generated method stub
			return null;
		}

		public void onCharacteristicRead(
				BluetoothGatt paramAnonymousBluetoothGatt,
				BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic,
				int paramAnonymousInt) {
		}

		public void onCharacteristicWrite(
				BluetoothGatt paramAnonymousBluetoothGatt,
				BluetoothGattCharacteristic paramAnonymousBluetoothGattCharacteristic,
				int paramAnonymousInt) {
		}

		/**
		 * ����״̬�ı�WM_RECEIVE_MSG_FROM_BLE
		 */
		public void onConnectionStateChange(
				BluetoothGatt paramAnonymousBluetoothGatt, int oldStatus,
				int newStatus) {
			if (newStatus == 2)// ������״̬���������ӳɹ�
			{
				Message msg = new Message();
				msg.what = ConstantUtils.WM_BLE_CONNECTED_STATE_CHANGE;
				Bundle bundle = new Bundle();
				bundle.putString("address", deviceAddress);
				bundle.putString("name", deviceName);
				msg.obj = bundle;
				serviceHandler.sendMessage(msg);
				paramAnonymousBluetoothGatt.discoverServices();
				// ���ӵ���������ҿ��Զ�д�ķ��������кܶ����
				return;
			}
			if (newStatus == 0)// �Ͽ����ӻ�δ���ӳɹ�
			{
				serviceHandler.sendEmptyMessage(ConstantUtils.WM_STOP_CONNECT);
				return;
			}
			paramAnonymousBluetoothGatt.disconnect();
			paramAnonymousBluetoothGatt.close();
			return;
		}

		public void onDescriptorRead(BluetoothGatt paramAnonymousBluetoothGatt,
				BluetoothGattDescriptor paramAnonymousBluetoothGattDescriptor,
				int paramAnonymousInt) {
		}

		public void onDescriptorWrite(
				BluetoothGatt paramAnonymousBluetoothGatt,
				BluetoothGattDescriptor paramAnonymousBluetoothGattDescriptor,
				int paramAnonymousInt) {
		}

		public void onReadRemoteRssi(BluetoothGatt paramAnonymousBluetoothGatt,
				int paramAnonymousInt1, int paramAnonymousInt2) {
		}

		public void onReliableWriteCompleted(
				BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt) {
		}

		public void onServicesDiscovered(
				BluetoothGatt paramAnonymousBluetoothGatt, int paramAnonymousInt) {
			BluetoothController.this.findService(paramAnonymousBluetoothGatt
					.getServices());
		}

	};

	/**
	 * ��������
	 * 
	 * @param byteArray
	 * @return
	 */
	public boolean write(byte byteArray[]) {
		if (bleGattCharacteristic == null)
			return false;
		if (bleGatt == null)
			return false;
		bleGattCharacteristic.setValue(byteArray);
		return bleGatt.writeCharacteristic(bleGattCharacteristic);
	}

	/**
	 * ��������
	 * 
	 * @param byteArray
	 * @return
	 */
	public boolean write(String str) {
		if (bleGattCharacteristic == null)
			return false;
		if (bleGatt == null)
			return false;
		bleGattCharacteristic.setValue(str);
		return bleGatt.writeCharacteristic(bleGattCharacteristic);
	}

	/**
	 * ��������
	 * 
	 * @param paramList
	 */
	public void findService(List<BluetoothGattService> paramList) {

		int temp_Int = 0;
		Iterator localIterator1 = paramList.iterator();
		
		while (localIterator1.hasNext()) {
			BluetoothGattService localBluetoothGattService = (BluetoothGattService) localIterator1
					.next();
			if (localBluetoothGattService.getUuid().toString()
					.equalsIgnoreCase(ConstantUtils.UUID_SERVER)) {
				List localList = localBluetoothGattService.getCharacteristics();
				Iterator localIterator2 = localList.iterator();
				while (localIterator2.hasNext()) {
					BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) localIterator2
							.next();
					if (localBluetoothGattCharacteristic.getUuid().toString()
							.equalsIgnoreCase(ConstantUtils.UUID_NOTIFY)) {
						bleGattCharacteristic = localBluetoothGattCharacteristic;
						
						Message msg = new Message();
						msg.what = ConstantUtils.WM_GET_CHARACT;
						serviceHandler.sendMessage(msg);
						temp_Int = 1;
						break;
					}
				}
				break;
			}

		}

		bleGatt.setCharacteristicNotification(bleGattCharacteristic, true);
		
		if (temp_Int == 0) {
			//����ʧ��
			Message msg = new Message();
			msg.what = ConstantUtils.WM_LOSS_CHARACT;
			serviceHandler.sendMessage(msg);
		}
	}
	
	public boolean findGattCharacteristic() {
		
		if (bleGattCharacteristic == null) {
			return false;
		}
		else {
			return true;
		}		
		
	}
	
	static int CRC_And_Trans_7E(int Tx_Len) {
		// ����CRC��Ȼ��Դ��������е�7E���ݽ���ת�崦������ֵΪ���ݳ���-1�����Ϊת�崦�������ݳ��� -1
		int i, j, k, Tx_Length;

		//CRCУ��
		i = NMS_Communication.Cala_CRC(RePly_Byte, Tx_Len - 3);
		RePly_Byte[Tx_Len - 2] = (byte) (i & 0xFF);				//��8λ
		RePly_Byte[Tx_Len - 1] = (byte) (i >> 8);						//��8λ

		//������
		RePly_Byte[Tx_Len] = 0x7E;
		
		//�ǽ�������0x7E ��ת�⴦��
		Tx_Length = Tx_Len;
		
		//��7Dת�� 7D 5D
		i = 1;
		j = 0;
		while (j == 0) {
			if (RePly_Byte[i] == 0x7D ) {
				//������������ 7D���򽫺�������ȫ������һλ�����ݳ��� + 1������ 7E ת��Ϊ 7D, 5D
				for (k = Tx_Length; k >= i; k--) {
					RePly_Byte[k + 1] = RePly_Byte[k];
				}
				Tx_Length++;
				RePly_Byte[i] = 0x7D;
				RePly_Byte[i + 1] = 0x5D;
				i ++;
			}
			
			i ++;
			if (i >= Tx_Length)		j = 1;
		}
		
		//��7Eת�� 7D 5E
		i = 1;
		j = 0;
		while (j == 0) {
			if (RePly_Byte[i] == 0x7E ) {
				//������������ 7E���򽫺�������ȫ������һλ�����ݳ��� + 1������ 7E ת��Ϊ 7D, 5E
				for (k = Tx_Length; k >= i; k--) {
					RePly_Byte[k + 1] = RePly_Byte[k];
				}
				Tx_Length++;
				RePly_Byte[i] = 0x7D;
				RePly_Byte[i + 1] = 0x5E;
				i ++;
			}
			
			i ++;
			if (i >= Tx_Length)		j = 1;
		}
		
		return Tx_Length;
	}

}
