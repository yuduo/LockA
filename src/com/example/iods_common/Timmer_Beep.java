package com.example.iods_common;


import android.content.Context;
import android.os.Handler;

public class Timmer_Beep {
	static int Counter_Time = 0;					//计数器
	
	private Context context;		//当前context对象
	private Handler handler;		//当前handler
	private boolean Run_Tag;
	
	public Timmer_Beep(Context context, Handler handler){
		//构造函数
		this.context=context;
		this.handler=handler;
	}
	
	public void Reset_Timmer() {
		//重启计时器
		Counter_Time = 0;
	}
	
	public void Close_Timmer() {
		//关闭定时器
		Run_Tag = false;
	}
	
	public int Get_Timmer() {
		return Counter_Time;
	}
	
	public void Wait_Interrupt_Infor() {
    	//启动接收TCP接收监听定时狗进程
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
					Thread.sleep(100);							//每100ms响一次
				} 
				catch(Exception e1){}
				
				Counter_Time++;									//计数器，以定时重启监测互联，相当于定时狗
				
				 switch (Counter_Time) {
	            	//确定查询工单类型
	            	case 10: 
	            		handler.obtainMessage(10, "1s").sendToTarget();		//传递1秒定时
	            	break;
	            	
	            	case 30: 
	            		handler.obtainMessage(10, "3s").sendToTarget();		//传递3秒定时
	            	break;
	            	
	            	case 50: 
	            		handler.obtainMessage(10, "5s").sendToTarget();		//传递3秒定时
	            	break;
	            	
	            	case 100: 
	            		handler.obtainMessage(10, "10s").sendToTarget();		//传递10秒定时
	            	break;
	            	
	            	case 300: 
	            		handler.obtainMessage(10, "30s").sendToTarget();		//传递30秒定时
	            	break;
	            	
	            	case 600: handler.obtainMessage(10, "60s").sendToTarget();		//传递1分钟定时
	            	break;
	            	
	            	case 1200: 
	            		handler.obtainMessage(10, "120s").sendToTarget();		//传递2分钟定时
	            	break;
	            	
	            	case 1800: 
	            		handler.obtainMessage(10, "180s").sendToTarget();		//传递3分钟定时
	            	break;
	            	
	            	case 3000: 
	            		handler.obtainMessage(10, "300s").sendToTarget();		//传递5分钟定时
	            	break;
	            	
	            	case 6000: 
	            		handler.obtainMessage(10, "600s").sendToTarget();		//传递10分钟定时
	            	break;
	            	
	            }
			}
		}
	}
}
