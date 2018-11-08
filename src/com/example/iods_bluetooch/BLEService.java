package com.example.iods_bluetooch;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public class BLEService extends Service {
	BluetoothController bleCtrl;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int i;
			switch (msg.what) {
			case ConstantUtils.WM_BLE_CONNECTED_STATE_CHANGE:// 连接上某个设备的消息
				Bundle bundle = (Bundle) msg.obj;
				String address = bundle.getString("address");
				String name = bundle.getString("name");
				// 连接状态改变广播
				Bundle bundle1 = new Bundle();
				bundle1.putString("address", address);
				bundle1.putString("name", name);
				Intent intentDevice = new Intent(
						ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
				intentDevice.putExtras(bundle1);
				sendBroadcast(intentDevice);
				break;

			case ConstantUtils.WM_STOP_CONNECT:
				Intent stopConnect = new Intent(ConstantUtils.ACTION_STOP_CONNECT);
				sendBroadcast(stopConnect);
				break;

			case ConstantUtils.WM_STOP_SCAN_BLE:// 搜索1秒后停止搜索
				bleCtrl.stopScanBLE();
				
				break;
			case ConstantUtils.WM_UPDATE_BLE_LIST:// 回调发来的更新列表消息
				// 更新蓝牙列表广播
				Intent intent = new Intent(
						ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
				BluetoothDevice device = (BluetoothDevice) msg.obj;
				intent.putExtra("name", device.getName());
				intent.putExtra("address", device.getAddress());
				sendBroadcast(intent);
				break;

			case ConstantUtils.WM_RECEIVE_MSG_FROM_BLE:// 接受到蓝牙发送的消息
				String mes = (String) msg.obj;
				Intent mesDevice = new Intent(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
				mesDevice.putExtra("message", mes);
				sendBroadcast(mesDevice);
				break;
				
			case ConstantUtils.WM_GET_CHARACT:// 获取到蓝牙连接特征值
				//开始发送开锁指令

				Intent mesCHARACT = new Intent(ConstantUtils.ACTION_GET_DEVICE_CHARACT);
				mesCHARACT.putExtra("message", "Get_Charact");
				sendBroadcast(mesCHARACT);

				break;
				
			case ConstantUtils.WM_LOSS_CHARACT:// 获取蓝牙连接特征值失败

				Intent mesLossCHARACT = new Intent(ConstantUtils.ACTION_LOSS_DEVICE_CHARACT);
				mesLossCHARACT.putExtra("message", "Loss_Charact");
				sendBroadcast(mesLossCHARACT);

				break;

			}
		}
	};

	public void onStart(Intent intent, int startId) {
		bleCtrl = BluetoothController.getInstance();
		bleCtrl.setServiceHandler(handler);
	};

}
