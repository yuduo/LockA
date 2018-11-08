package com.example.iods_network;

import de.greenrobot.event.EventBus;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

//�������ӹ㲥��
public class NetCheckReceiver extends BroadcastReceiver{

	//��дBroadcastReceiver��onReceive()����
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO �Զ����ɵķ������
		String action = intent.getAction();
		
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
			boolean isConnected = NetConnectionDetector.isNetworkConnected(context);//��ȡ��������״̬
			boolean isWifi = NetConnectionDetector.isWifiConnected(context);//��ȡ�Ƿ���wifi��������״̬�ı�ʶ
			boolean isMobile = NetConnectionDetector.isMobileConnected(context);//��ȡ�Ƿ����ƶ���������״̬�ı�ʶ
			
			System.out.println("����״̬��" + isConnected);
            System.out.println("wifi״̬��" + isWifi);
            System.out.println("�ƶ�����״̬��" + isMobile);
            System.out.println("�����������ͣ�" + NetConnectionDetector.getConnectedType(context));
            
            if (isConnected) {//�ж���������
            	Toast.makeText(context, "�Ѿ���������", Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new NetEvent(true));  //�ַ��¼����¼���NetEvent����ı�ʶ��������Ϊtrue               
            } else {//�ж�����������
            	Toast.makeText(context, "�Ѿ��Ͽ�����", Toast.LENGTH_LONG).show();
            	EventBus.getDefault().post(new NetEvent(false));  //�ַ��¼����¼���NetEvent ����ı�ʶ��������Ϊfalse
            }
			
		}
	}

}
