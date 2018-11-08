package com.example.iods_network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

//��������״̬�����
public class NetConnectionDetector {
	
	/**
	 * �ж��Ƿ�����������
	 * @param context :Context�����ø����Activity
	 * @return boolean��true���������ӣ�false������������
	 */
	public static boolean isNetworkConnected(Context context) { 
		if (context != null) { 
			//��ȡConnectivityManager����
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE); 
			//��ȡNetworkInfo����
			NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo(); 
			if (netInfo != null) { 
				return netInfo.isAvailable();//������������״̬
			}
		} 
		return false; 
	}
	
	/**
	 * �ж����������Ƿ�WIFI����
	 * @param context ��Context�����ø����Activity
	 * @return boolean��true����wifi�������ӣ�false������wifi��������
	 */
	public static boolean isWifiConnected(Context context) { 
		if (context != null) {
			//��ȡConnectivityManager����
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE); 
			//��ȡNetworkInfo����
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (mWiFiNetworkInfo != null) { 
				return mWiFiNetworkInfo.isAvailable(); //�����Ƿ�WIFI��������״̬��ʶ
			} 
		} 
		return false; 
	}
	
	/**
	 * �ж����������Ƿ�MOBILE����
	 * @param context ��Context�����ø����Activity
	 * @return boolean��true����wifi�������ӣ�false������wifi��������
	 */
	public static boolean isMobileConnected(Context context) { 
		if (context != null) { 
			//��ȡConnectivityManager����
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			//��ȡNetworkInfo����
			NetworkInfo mMobileNetworkInfo = mConnectivityManager .getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
			if (mMobileNetworkInfo != null) { 
				return mMobileNetworkInfo.isAvailable(); //�����Ƿ�MOBILE��������״̬��ʶ
			} 
		} 
		return false; 
	}
	
	/**
	 * ��ȡ��ǰ�������ӵ�������Ϣ
	 * @param context��Context�����ø����Activity
	 * @return int�� -1��û�����磻 1��WIFI���磻2��wap���磻3��net����
	 */
	public static int getConnectedType(Context context) { 
		if (context != null) {
			//��ȡConnectivityManager����
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			//��ȡNetworkInfo����
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 		
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType(); //���ص�ǰ������������
			} 
		} 
		return -1; 
	}
	
	/**
	 * ��������壬����wifi��������
	 * @param paramContext ��Context�����ø����Activity
	 */
	public static void startToSettings(Context paramContext) {
        if (paramContext == null)
            return;
        try {
        	//�ж��ֻ�ϵͳ�İ汾����API����10 ������3.0�����ϰ汾 
            if (Build.VERSION.SDK_INT > 10) {
                paramContext.startActivity(new Intent( "android.settings.SETTINGS"));
                return;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            return;
        }
        //��������壬����wifi��������
        paramContext.startActivity(new Intent( "android.settings.WIRELESS_SETTINGS"));
    }
	

}
