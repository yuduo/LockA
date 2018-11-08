package com.example.iods_bluetooch;

public class ConstantUtils {
	//消息类型
	public final static int WM_STOP_SCAN_BLE=1;
	public final static int WM_UPDATE_BLE_LIST=2;
	//蓝牙连接状态改变
	public final static int  WM_BLE_CONNECTED_STATE_CHANGE=3;
	//接受到蓝牙发来的消息
	public final static int WM_RECEIVE_MSG_FROM_BLE=4;
	//断开连接或未连接成功
	public final static int WM_STOP_CONNECT=5;
	
	//获取到蓝牙连接特征值
	public final static int WM_GET_CHARACT=6;
	
	//获取蓝牙连接特征值失败
	public final static int WM_LOSS_CHARACT=7;
	
	//intent的action们
	public final static String ACTION_UPDATE_DEVICE_LIST="action.update.device.list";//更新设备列表
	public final static String  ACTION_CONNECTED_ONE_DEVICE="action.connected.one.device";//连接上某个设备时发的广播
	public final static String ACTION_RECEIVE_MESSAGE_FROM_DEVICE="action.receive.message";
	public final static String ACTION_STOP_CONNECT="action.stop.connect";
	public final static String ACTION_GET_DEVICE_CHARACT="action.get.device.charact";	//获取到设备连接特征值
	public final static String ACTION_LOSS_DEVICE_CHARACT="action.loss.device.charact";	//获取设备连接特征值失败
	
	//UUID	
		public final static  String UUID_SERVER="0000ffe0-0000-1000-8000-00805f9b34fb";
		public final static  String UUID_NOTIFY="0000ffe1-0000-1000-8000-00805f9b34fb";

}
