package com.example.iods_common;


import android.content.Context;
import android.os.Handler;

public class Timmer_Beep {
	static int Counter_Time = 0;					//������
	
	private Context context;		//��ǰcontext����
	private Handler handler;		//��ǰhandler
	private boolean Run_Tag;
	
	public Timmer_Beep(Context context, Handler handler){
		//���캯��
		this.context=context;
		this.handler=handler;
	}
	
	public void Reset_Timmer() {
		//������ʱ��
		Counter_Time = 0;
	}
	
	public void Close_Timmer() {
		//�رն�ʱ��
		Run_Tag = false;
	}
	
	public int Get_Timmer() {
		return Counter_Time;
	}
	
	public void Wait_Interrupt_Infor() {
    	//��������TCP���ռ�����ʱ������
		Counter_Time = 0;
        Timmer_Beep_Thead Recive_TCP_Dog = new Timmer_Beep_Thead();
        Recive_TCP_Dog.start(); 	

    }
	
	@SuppressWarnings("unused")
	private class Timmer_Beep_Thead extends Thread {
		public void run() {
			Run_Tag = true;
			while (Run_Tag) {
				try {
					Thread.currentThread();
					Thread.sleep(100);							//ÿ100ms��һ��
				} 
				catch(Exception e1){}
				
				Counter_Time++;									//���������Զ�ʱ������⻥�����൱�ڶ�ʱ��
				
				 switch (Counter_Time) {
	            	//ȷ����ѯ��������
	            	case 10: 
	            		handler.obtainMessage(10, "1s").sendToTarget();		//����1�붨ʱ
	            	break;
	            	
	            	case 30: 
	            		handler.obtainMessage(10, "3s").sendToTarget();		//����3�붨ʱ
	            	break;
	            	
	            	case 50: 
	            		handler.obtainMessage(10, "5s").sendToTarget();		//����3�붨ʱ
	            	break;
	            	
	            	case 100: 
	            		handler.obtainMessage(10, "10s").sendToTarget();		//����10�붨ʱ
	            	break;
	            	
	            	case 300: 
	            		handler.obtainMessage(10, "30s").sendToTarget();		//����30�붨ʱ
	            	break;
	            	
	            	case 600: handler.obtainMessage(10, "60s").sendToTarget();		//����1���Ӷ�ʱ
	            	break;
	            	
	            	case 1200: 
	            		handler.obtainMessage(10, "120s").sendToTarget();		//����2���Ӷ�ʱ
	            	break;
	            	
	            	case 1800: 
	            		handler.obtainMessage(10, "180s").sendToTarget();		//����3���Ӷ�ʱ
	            	break;
	            	
	            	case 3000: 
	            		handler.obtainMessage(10, "300s").sendToTarget();		//����5���Ӷ�ʱ
	            	break;
	            	
	            	case 6000: 
	            		handler.obtainMessage(10, "600s").sendToTarget();		//����10���Ӷ�ʱ
	            	break;
	            	
	            }
			}
		}
	}
}
