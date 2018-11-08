package com.example.iods_network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

//网络连接状态检查类
public class NetConnectionDetector {
	
	/**
	 * 判断是否有网络连接
	 * @param context :Context，调用该类的Activity
	 * @return boolean，true：网络连接；false：无网络连接
	 */
	public static boolean isNetworkConnected(Context context) { 
		if (context != null) { 
			//获取ConnectivityManager对象
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE); 
			//获取NetworkInfo对象
			NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo(); 
			if (netInfo != null) { 
				return netInfo.isAvailable();//返回网络连接状态
			}
		} 
		return false; 
	}
	
	/**
	 * 判断网络连接是否WIFI网络
	 * @param context ：Context，调用该类的Activity
	 * @return boolean，true：是wifi网络连接；false：不是wifi网络连接
	 */
	public static boolean isWifiConnected(Context context) { 
		if (context != null) {
			//获取ConnectivityManager对象
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE); 
			//获取NetworkInfo对象
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (mWiFiNetworkInfo != null) { 
				return mWiFiNetworkInfo.isAvailable(); //返回是否WIFI网络连接状态标识
			} 
		} 
		return false; 
	}
	
	/**
	 * 判断网络连接是否MOBILE网络
	 * @param context ：Context，调用该类的Activity
	 * @return boolean，true：是wifi网络连接；false：不是wifi网络连接
	 */
	public static boolean isMobileConnected(Context context) { 
		if (context != null) { 
			//获取ConnectivityManager对象
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			//获取NetworkInfo对象
			NetworkInfo mMobileNetworkInfo = mConnectivityManager .getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
			if (mMobileNetworkInfo != null) { 
				return mMobileNetworkInfo.isAvailable(); //返回是否MOBILE网络连接状态标识
			} 
		} 
		return false; 
	}
	
	/**
	 * 获取当前网络连接的类型信息
	 * @param context：Context，调用该类的Activity
	 * @return int： -1：没有网络； 1：WIFI网络；2：wap网络；3：net网络
	 */
	public static int getConnectedType(Context context) { 
		if (context != null) {
			//获取ConnectivityManager对象
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			//获取NetworkInfo对象
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 		
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType(); //返回当前网络连接类型
			} 
		} 
		return -1; 
	}
	
	/**
	 * 打开设置面板，设置wifi网络连接
	 * @param paramContext ：Context，调用该类的Activity
	 */
	public static void startToSettings(Context paramContext) {
        if (paramContext == null)
            return;
        try {
        	//判断手机系统的版本，即API大于10 ，就是3.0或以上版本 
            if (Build.VERSION.SDK_INT > 10) {
                paramContext.startActivity(new Intent( "android.settings.SETTINGS"));
                return;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            return;
        }
        //打开设置面板，设置wifi网络连接
        paramContext.startActivity(new Intent( "android.settings.WIRELESS_SETTINGS"));
    }
	

}
