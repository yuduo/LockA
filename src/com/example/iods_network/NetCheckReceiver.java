package com.example.iods_network;

import de.greenrobot.event.EventBus;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

//网络连接广播类
public class NetCheckReceiver extends BroadcastReceiver{

	//重写BroadcastReceiver的onReceive()方法
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自动生成的方法存根
		String action = intent.getAction();
		
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
			boolean isConnected = NetConnectionDetector.isNetworkConnected(context);//获取网络连接状态
			boolean isWifi = NetConnectionDetector.isWifiConnected(context);//获取是否是wifi网络连接状态的标识
			boolean isMobile = NetConnectionDetector.isMobileConnected(context);//获取是否是移动网络连接状态的标识
			
			System.out.println("网络状态：" + isConnected);
            System.out.println("wifi状态：" + isWifi);
            System.out.println("移动网络状态：" + isMobile);
            System.out.println("网络连接类型：" + NetConnectionDetector.getConnectedType(context));
            
            if (isConnected) {//判断网络连接
            	Toast.makeText(context, "已经连接网络", Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new NetEvent(true));  //分发事件：事件类NetEvent对象的标识变量设置为true               
            } else {//判断无网络连接
            	Toast.makeText(context, "已经断开网络", Toast.LENGTH_LONG).show();
            	EventBus.getDefault().post(new NetEvent(false));  //分发事件：事件类NetEvent 对象的标识变量设置为false
            }
			
		}
	}

}
