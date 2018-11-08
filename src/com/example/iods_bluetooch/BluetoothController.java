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
 * 蓝牙控制类
 * 
 * @author wangdandan
 * 
 */
public class BluetoothController {
	private String deviceAddress;
	private String deviceName;

	private BluetoothAdapter bleAdapter;
	private Handler serviceHandler;// 服务句柄

	static BluetoothGatt bleGatt;// 连接
	static BluetoothGattCharacteristic bleGattCharacteristic;

	byte[] Byte_Commnd = new byte[100];
	static byte[] RePly_Byte = new byte[50];
	int Numb_Byte,  Total_Cell, Numb_Cell;		//已保存的数据长度, 切包总数， 切包序号
	/**
	 * 单例模式
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
	 * 初始化蓝牙
	 * 
	 * @return
	 */
	public boolean initBLE() {
		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
		// App.app可能会报错，清单文件中不要忘了配置application
		if (!App.app.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}
		// 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) App.app
				.getSystemService(Context.BLUETOOTH_SERVICE);
		bleAdapter = bluetoothManager.getAdapter();
		// 检查设备上是否支持蓝牙
		if (bleAdapter == null)
			return false;
		else
			return true;
	}

	/**
	 * 设置服务事件接收者
	 * 
	 * @return
	 */
	public void setServiceHandler(Handler handler) {
		// handler初始化在service中，用于逻辑和界面的沟通
		serviceHandler = handler;
	}

	/**
	 * 搜索蓝牙回调
	 */
	BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int arg1, byte[] arg2) {
			// device就是搜索到的设备
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
	 * 开始扫描蓝牙
	 */
	public void startScanBLE() {
		bleAdapter.startLeScan(bleScanCallback);
		if (serviceHandler != null)
			serviceHandler.sendEmptyMessageDelayed(
					ConstantUtils.WM_STOP_SCAN_BLE, 1000);
	}

	/**
	 * 停止扫描蓝牙设备
	 */
	public void stopScanBLE() {
		bleAdapter.stopLeScan(bleScanCallback);
	}

	/**
	 * 是否蓝牙打开
	 * 
	 * @return
	 */
	public boolean isBleOpen() {
		return bleAdapter.isEnabled();
	}

	/**
	 * 连接蓝牙设备
	 * 
	 * @param device
	 *            待连接的设备
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
		//关闭蓝牙连接
		if (bleGatt != null) {
			bleGatt.close();
		}
	}

	/**
	 * 与蓝牙通信回调
	 */
	public BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
		/**
		 * 收到消息
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
			RePly_Byte[4] = 0x00;		//小端格式
			RePly_Byte[5] = 0x01;
			RePly_Byte[6] = 0x00;

			RePly_Byte[9] = (byte) 0x6C;	//对切包的回复
			//帧尾符
			RePly_Byte[13] = (byte) 0x7E;
			
			//TODO
			byte[] arrayOfByte = paramAnonymousBluetoothGattCharacteristic.getValue();
			if (BluetoothController.this.serviceHandler != null) {
				temp_Int = 0;
				
				if (arrayOfByte.length < 3) {
					temp_Int = 0;
				}
				else if (arrayOfByte[0] == 0x7E && arrayOfByte[1] == 0x00 && arrayOfByte[2] == 0x10) {
					//有帧头
					Numb_Cell = 0;
					for (i = 1; i<arrayOfByte.length; i++) {
						//查看是否有帧尾
						if (arrayOfByte[i] == 0x7E) {
							//有帧尾
							temp_Int = i;
							break;
						}
					}
										
					if (temp_Int > 9) {
						//有帧尾的处理
												
						for (i = 0; i<= temp_Int; i++) {
							Byte_Commnd[i] = arrayOfByte[i];
						}
												
						temp_Int = 1;
					}
					else {
						//无帧尾的处理
						for (i = 0; i< arrayOfByte.length; i++) {
							Byte_Commnd[i] = arrayOfByte[i];
						}
						
						if (Numb_Cell == 0) {
							//有帧头时，是第一帧
							Numb_Cell = 1;
							Numb_Byte = arrayOfByte.length;
						}
						
						temp_Int = 0;		//无帧尾，则回复收到切包应答
						
						RePly_Byte[10] = (byte) Numb_Cell;	//回复切包的消息体：切包序号
						RePly_Byte[7] = Byte_Commnd[7];
						RePly_Byte[8] = Byte_Commnd[8];		//切包命令码
						
						temp_Int = CRC_And_Trans_7E(13);	//计算CRC16，并进行7D 7E的转义

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
						
						BluetoothController.getInstance().write(Send_Byte);		//发送对切包的接收回复
					}
				}
				else {
					//无帧头，
					temp_Int = 0;

					if (arrayOfByte[1] == arrayOfByte[0] && Numb_Cell > 0) {
						//完成切包的传送
						temp_Int = 1;
					}
					else if (arrayOfByte[1] > arrayOfByte[0]  && Numb_Cell > 0) {
						//出错
						temp_Int = -1;
					}
					else {
						if (Numb_Cell > 0) {
							//回复收到切包应答
							RePly_Byte[10] = arrayOfByte[1];	//回复切包的消息体：切包序号
							RePly_Byte[7] = Byte_Commnd[7];
							RePly_Byte[8] = Byte_Commnd[8];		//切包命令码
							
							temp_Int = CRC_And_Trans_7E(13);	//计算CRC16，并进行7D 7E的转义

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
							
							BluetoothController.getInstance().write(Send_Byte);		//发送对切包的接收回复
						}
						temp_Int = 0;
					}
					
					index_Frame = arrayOfByte[1];
					if (Numb_Cell > 0 && index_Frame == Numb_Cell + 1) {
						//续包						
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
				
				//temp_Int = 1完成切包的传送 		temp_Int = -1切包 传送出错
				
				if (temp_Int != 0) {
					temp_Int = 0;
					//查找帧尾
					for (i = 9; 9 < Byte_Commnd.length; i++) {
						if (Byte_Commnd[i] == 0x7E) {
							temp_Int = i;
							break;
						}
					}
				}
				
				if (Numb_Cell == 0 && temp_Int > 9) {
					int j, k;
					//找到帧尾，则进行转义和CRC计算
					//将7D 5E 还原成7E转成 7E
					i = 1;
					j = 0;
					while (j == 0) {
						if (Byte_Commnd[i] == 0x7D && Byte_Commnd[i+1] == 0x5E ) {
							//数据序列中有 7D 5E，则将后续数据全部前移一位，数据长度 - 1，并将 7D, 5E还原为7E
							Byte_Commnd[i] = 0x7E;
							for (k = i + 2; k <= temp_Int; k++) {
								Byte_Commnd[k - 1] = Byte_Commnd[k];
							}
							temp_Int--;
						}
						
						i ++;
						if (i >= temp_Int - 1)		j = 1;
					}
					
					//将7D 5D 还原成7E转成 7D
					i = 1;
					j = 0;
					while (j == 0) {
						if (Byte_Commnd[i] == 0x7D && Byte_Commnd[i+1] == 0x5D ) {
							//数据序列中有 7D 5D，则将后续数据全部前移一位，数据长度 - 1，并将 7D, 5D还原为7D
							Byte_Commnd[i] = 0x7D;
							for (k = i + 2; k <= temp_Int; k++) {
								Byte_Commnd[k - 1] = Byte_Commnd[k];
							}
							temp_Int--;
						}
						
						i ++;
						if (i >= temp_Int - 1)		j = 1;
					}
					
					i = NMS_Communication.Cala_CRC(Byte_Commnd, temp_Int - 3);		//计算CRC16
					if (Byte_Commnd[temp_Int - 2] == (byte)(i & 0xFF) && Byte_Commnd[temp_Int - 1] == (byte)(i >> 8)) {
						//CRC正确
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
						//仅读设备版本号需要将消息体转换成ASIIC码
						String res = new String(Send_Byte);
						temp_Str = temp_Str + res;
					}
					else {
						//其他指令若有消息体，均转成字符型16进制数字码
						temp_Str = temp_Str + DataAlgorithm.byteToHexString(Send_Byte, i - 10);
					}
					
					Message msg = new Message();
					msg.what = ConstantUtils.WM_RECEIVE_MSG_FROM_BLE;
					msg.obj = temp_Str.toUpperCase();
					BluetoothController.this.serviceHandler.sendMessage(msg);
				}
			}
			// 也可以先打印出来看看
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
		 * 连接状态改变WM_RECEIVE_MSG_FROM_BLE
		 */
		public void onConnectionStateChange(
				BluetoothGatt paramAnonymousBluetoothGatt, int oldStatus,
				int newStatus) {
			if (newStatus == 2)// 已连接状态，表明连接成功
			{
				Message msg = new Message();
				msg.what = ConstantUtils.WM_BLE_CONNECTED_STATE_CHANGE;
				Bundle bundle = new Bundle();
				bundle.putString("address", deviceAddress);
				bundle.putString("name", deviceName);
				msg.obj = bundle;
				serviceHandler.sendMessage(msg);
				paramAnonymousBluetoothGatt.discoverServices();
				// 连接到蓝牙后查找可以读写的服务，蓝牙有很多服务
				return;
			}
			if (newStatus == 0)// 断开连接或未连接成功
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
	 * 传输数据
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
	 * 传输数据
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
	 * 搜索服务
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
			//连接失败
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
		// 计算CRC，然后对传送数据中的7E数据进行转义处理，输入值为数据长度-1，输出为转义处理后的数据长度 -1
		int i, j, k, Tx_Length;

		//CRC校验
		i = NMS_Communication.Cala_CRC(RePly_Byte, Tx_Len - 3);
		RePly_Byte[Tx_Len - 2] = (byte) (i & 0xFF);				//低8位
		RePly_Byte[Tx_Len - 1] = (byte) (i >> 8);						//高8位

		//结束符
		RePly_Byte[Tx_Len] = 0x7E;
		
		//非结束符的0x7E 的转意处理
		Tx_Length = Tx_Len;
		
		//将7D转成 7D 5D
		i = 1;
		j = 0;
		while (j == 0) {
			if (RePly_Byte[i] == 0x7D ) {
				//数据序列中有 7D，则将后续数据全部后移一位，数据长度 + 1，并将 7E 转义为 7D, 5D
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
		
		//将7E转成 7D 5E
		i = 1;
		j = 0;
		while (j == 0) {
			if (RePly_Byte[i] == 0x7E ) {
				//数据序列中有 7E，则将后续数据全部后移一位，数据长度 + 1，并将 7E 转义为 7D, 5E
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
