package com.example.iods_lock_app;




import com.example.iods_bluetooch.BLEService;
import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_bluetooch.BluetoothController;
import com.example.iods_bluetooch.ConstantUtils;
import com.example.iods_bluetooch.EntityDevice;
import com.example.iods_common.NMS_Communication;
import com.example.iods_manage.Browse_Log_Activity;
import com.example.iods_manage.Chang_PassWd_Activity;
import com.example.iods_manage.Data_Settle_Activity;
import com.example.iods_manage.Net_Config_Activity;
import com.example.iods_manage.Update_Softwear_Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;






public class PDA_Manage_Activity extends Activity {
	//终端管理页面
	private Context context = PDA_Manage_Activity.this;//定义Context对象
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false, Send_BLE_Tag = false;
	boolean BLE_Second_Cell_Tag = false;
	
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
	//广播参数
	private ServiceReceiver mReceiver;
	private String action="getReceiver";
	private String action1="GPS"; 
		
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pda_manage);
        setTitle("亨通光电");

        LinearLayout Backgroup = (LinearLayout)findViewById(R.id.TableLayout1);

		switch (NMS_Communication.index_backgroup) {
			case 1:
				Backgroup.setBackgroundResource(R.drawable.backgroup_1);
				break;
			case 2:
				Backgroup.setBackgroundResource(R.drawable.backgroup_2);
				break;
			case 3:
				Backgroup.setBackgroundResource(R.drawable.backgroup_3);
				break;
			case 4:
				Backgroup.setBackgroundResource(R.drawable.backgroup_4);
				break;
			case 5:
				Backgroup.setBackgroundResource(R.drawable.backgroup_5);
				break;
			case 6:
				Backgroup.setBackgroundResource(R.drawable.backgroup_6);
				break;
			case 7:
				Backgroup.setBackgroundResource(R.drawable.backgroup_7);
				break;
			case 8:
				Backgroup.setBackgroundResource(R.drawable.backgroup_8);
				break;
			case 9:
				Backgroup.setBackgroundResource(R.drawable.backgroup_9);
				break;
			case 10:
				Backgroup.setBackgroundResource(R.drawable.backgroup_10);
				break;
			case 11:
				Backgroup.setBackgroundResource(R.drawable.backgroup_11);
				break;
			case 12:
				Backgroup.setBackgroundResource(R.drawable.backgroup_12);
				break;
			default:
				//无背景图案
				break;
		}
		
		//广播初始化
        mReceiver = new ServiceReceiver();
	    //实例化过滤器并设置要过滤的广播
	    IntentFilter mFilter = new IntentFilter();
	    mFilter.addAction(action);
	    mFilter.addAction(action1);
	    registerReceiver(mReceiver, mFilter);
		initi();
		
    }
    
    public void initi(){

		Button Clear_Data_Btn, Net_Config_Btn, Browse_Log_Btn, PDA_Help_Btn, Chang_PassWd_Btn;
		
		Clear_Data_Btn = (Button)findViewById(R.id.btn_Clear_Data);
		Net_Config_Btn = (Button)findViewById(R.id.btn_Net_Config);
		Browse_Log_Btn = (Button)findViewById(R.id.btn_Browse_Log);
		PDA_Help_Btn = (Button)findViewById(R.id.btn_PDA_Help);
		Chang_PassWd_Btn = (Button)findViewById(R.id.button1);
		
		Clear_Data_Btn.setOnClickListener(new ClickEvent());
		Net_Config_Btn.setOnClickListener(new ClickEvent());
		Browse_Log_Btn.setOnClickListener(new ClickEvent());
		PDA_Help_Btn.setOnClickListener(new ClickEvent());
		Chang_PassWd_Btn.setOnClickListener(new ClickEvent());
		
		if (UserLogin_Activity.Login_User_Type.equals("Admin") || UserLogin_Activity.Login_User_Type.equals("Reset")) {
			PDA_Help_Btn.setText(" 重置锁配置 ");
			Clear_Data_Btn.setText(" 设 备 升 级 ");
		}
		
		if (UserLogin_Activity.Login_User_Type.equals("Manager") || UserLogin_Activity.Login_User_Type.equals("Worker")) {
			Chang_PassWd_Btn.setEnabled(true);
		}
		else if (UserLogin_Activity.Login_User_Type.equals("Admin")) {
			Browse_Log_Btn.setEnabled(false);
		}
		else {
			Chang_PassWd_Btn.setEnabled(false);
			Browse_Log_Btn.setEnabled(false);
		}
	}
    
	
	//自定义单击事件类
    class ClickEvent implements View.OnClickListener {    
		@Override    

		public void onClick(View v) {
			Intent intent = new Intent();   //创建Intent对象 
			
	    	switch(v.getId()){
	    	   case R.id.btn_Clear_Data:
	    		   //响应数据清理按键
	    		   if (UserLogin_Activity.Login_User_Type.equals("Admin") || UserLogin_Activity.Login_User_Type.equals("Reset")) {
	    			   intent.setClass(context, Update_Softwear_Activity.class);	    		  
		        	   startActivity(intent);//跳转到设备软件升级界面
	    		   }
	    		   else {   	
	    			   intent.setClass(context, Data_Settle_Activity.class);	    		  
		        	   startActivity(intent);//跳转到数据清理功能界面
	    		   }
	    		   
	    		   break;
	    		   
	    	   case R.id.btn_Net_Config:
	    		   //响应网络配置按键
	    		   intent.setClass(context, Net_Config_Activity.class);	    		  
	        	   startActivity(intent);//跳转到读卡器连接功能界面

	    		   break;
	    		   
	    	   case R.id.btn_Browse_Log:
	    		   //响应日志查询按键
	    		   intent.setClass(context, Browse_Log_Activity.class);	    		  
	        	   startActivity(intent);//跳转到读卡器连接功能界面

	    		   break;
	    		   
	    	   case R.id.btn_PDA_Help:
	    		   //响应使用帮助按键
	    		   if (UserLogin_Activity.Login_User_Type.equals("Admin") || UserLogin_Activity.Login_User_Type.equals("Reset")) {
	    			   Scan_BLE_Lock();
	    		   }
	    		   else {   	
	    			   intent.setClass(context, User_Help_Activity.class);	//使用帮助
	    			   startActivity(intent);//使用帮助页面
	    		   }
	    		   break;
	    		   
	    	   case R.id.button1:
	    		   //响应更改密码界面
	    		   intent.setClass(context, Chang_PassWd_Activity.class);	
	    		   intent.putExtra("Mode", "Self");
	    		   intent.putExtra("Query", UserLogin_Activity.Login_User_ID);
	        	   startActivity(intent);//以修改自身数据的模式跳转到密码更改页面
	    		   
	    		   break;
	    		
	    	}
		}
	}
	
    
    public void Scan_BLE_Lock() {
		//扫描蓝牙锁
		BLE_Get_Start_Tag = false;
		BLE_Second_Cell_Tag = false;

		controller.close();		//先关闭现有服务
		
		//开始蓝牙服务
		intentService = new Intent(context,BLEService.class);   
		startService(intentService);
		// 初始化蓝牙
		controller.initBLE();
		BLE_List_Total = 0;
				
		receiver=new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
		intentFilter.addAction(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
		intentFilter.addAction(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
		intentFilter.addAction(ConstantUtils.ACTION_STOP_CONNECT);
		intentFilter.addAction(ConstantUtils.ACTION_GET_DEVICE_CHARACT);
		intentFilter.addAction(ConstantUtils.ACTION_LOSS_DEVICE_CHARACT);
		registerReceiver(receiver, intentFilter);
		
		if(!controller.initBLE()){//手机不支持蓝牙
			Toast.makeText(context, "您的手机不支持蓝牙",
					Toast.LENGTH_SHORT).show();
			return;//手机不支持蓝牙就啥也不用干了，关电脑睡觉去吧
		}
		if (!controller.isBleOpen()) {// 如果蓝牙还没有打开
			Toast.makeText(context, "请打开蓝牙",
					Toast.LENGTH_SHORT).show();
			return;
		}
		new GetDataTask().execute();// 搜索任务
	}
    
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			if(controller.isBleOpen()){
				controller.startScanBLE();
			};// 开始扫描
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
		}
	}
    
    public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i;
						
			if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_UPDATE_DEVICE_LIST)) {
				String name = intent.getStringExtra("name");
				String address = intent.getStringExtra("address");
				
				if(name != null){
					i = 0;
					if (BLE_List_Total > 0) {
						for (i = 0; i < BLE_List_Total; i++) {
							if (BLE_Name[i].equals(name) && BLE_Address[i].equals(address)) {
								i = 10;
								break;
							}
						}
					}
					
					if (i < 5) {
						BLE_Name[BLE_List_Total] = name;
						BLE_Address[BLE_List_Total] = address;
						
						BLE_List_Total++;
						
						if (BLE_List_Total == 1) {
							final Context context_1 = context;
							
					        new Handler().postDelayed(new Runnable(){     
							    public void run() {   
							    	AlertDialog dialog;
							    	switch (BLE_List_Total) {
									case 1:
										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("选择锁设备")			//设置对话框的标题
						                	.setMessage(BLE_Name[0])	//显示锁蓝牙设备名
						                	//设置对话框的按钮
						                	.setNegativeButton("取消", null)
						                	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_Lock_ID_Start(0);
						                			dialog.dismiss();
						                		}
						                	}).create();
										dialog.show();
										break;
										
									case 2:
										final String items2[] = {BLE_Name[0], BLE_Name[1]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("请选择锁设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items2, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_Lock_ID_Start(0);
						                			dialog.dismiss();
						                		}
												
						                	})
						                	.setNegativeButton("取消", null)
						                	.create();
										dialog.show();
										break;
										
									case 3:
										final String items3[] = {BLE_Name[0], BLE_Name[1], BLE_Name[2]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("请选择锁设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items3, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_Lock_ID_Start(0);
						                			dialog.dismiss();
						                		}
						                	})
						                	.setNegativeButton("取消", null)
						                	.create();
										dialog.show();
										break;
										
									case 4:
										final String items4[] = {BLE_Name[0], BLE_Name[1], BLE_Name[2], BLE_Name[3]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("请选择锁设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items4, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_Lock_ID_Start(0);
						                			dialog.dismiss();
						                		}
						                	})
						                	.setNegativeButton("取消", null)
						                	.create();
										dialog.show();
										break;
										
									case 5:
										final String items5[] = {BLE_Name[0], BLE_Name[1], BLE_Name[2], BLE_Name[3], BLE_Name[4]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("请选择锁设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items5, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_Lock_ID_Start(0);
						                			dialog.dismiss();
						                		}
						                	})
						                	.setNegativeButton("取消", null)
						                	.create();
										dialog.show();
										break;
									}

							    }     
							 }, 1000);			//延时1秒弹出设备选择窗
						}
					}
				}
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_GET_DEVICE_CHARACT)) {
				//获取到蓝牙特征值，开始发送通信指令
				if (!BLE_Get_Start_Tag) {
					BLE_Get_Start_Tag = true;

					Send_BLE_Tag = true;
					//BLE_Communication.Send_Command_6102(controller);	//发送读设备ID指令
					
					BLE_Communication.Send_Command_6105(controller, 0);		//发送锁设备配置重置指令
				}
				
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE)){
				//connectedDevice.setText("连接的蓝牙是："+intent.getStringExtra("address"));
			}
			
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_STOP_CONNECT)){
				//connectedDevice.setText("");
				//toast("连接已断开");
			}
			
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE)){
				String Recive_Str = intent.getStringExtra("message");
				AlertDialog dialog;
				
				if (Send_BLE_Tag) {
					Send_BLE_Tag = false;		//不接收多余的应答
					
					if (Recive_Str.substring(0, 7).equals("C610200") && ! BLE_Second_Cell_Tag) {
						//收到设备ID回复
						
						String temp_ID = Recive_Str.substring(7, 41);
						
						Send_BLE_Tag = true;
						BLE_Communication.Read_Lock_Status(temp_ID);	//发送读设备状态指令
						
						temp_ID = Recive_Str.substring(41, 73);
						temp_ID = Recive_Str.substring(73);
						BLE_Second_Cell_Tag = true;
					}
					else if (Recive_Str.substring(0, 7).equals("C610300")) {
						String temp_Str = Recive_Str.substring(7);
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("锁状态")			//设置对话框的标题
							.setMessage(temp_Str)
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog.show();
						
						BLE_Get_Start_Tag = false;
						controller.close();
					}
					else if (Recive_Str.equals("C61050000")) {
						
						BLE_Get_Start_Tag = false;
						controller.close();
						
						dialog = new AlertDialog.Builder(context)
	    					.setTitle("提示")			//设置对话框的标题
	    					.setMessage("已完成锁设备配置重置。")
	    					//设置对话框的按钮
	    					.setPositiveButton("确定", null)
	    					.create();
						dialog.show();

					}
					else if (Recive_Str.substring(0, 1).equals("E")) {
						BLE_Get_Start_Tag = false;
						controller.close();
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("配置设备出错")			//设置对话框的标题
							.setMessage("通信故障 ！")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog.show();
					}
					
				}
								
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_LOSS_DEVICE_CHARACT)){
				//未能获取到蓝牙通信特征值
				AlertDialog dialog = new AlertDialog.Builder(context)
            		.setTitle("警告")			//设置对话框的标题
            		.setMessage("蓝牙直联失败！")	//显示锁蓝牙设备名
            		//设置对话框的按钮
            		.setPositiveButton("确定", null)
            		.create();
				dialog.show();
			}
		}
	}
    
    
    class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {


		}
    	
    }
	
	
    private void Read_Lock_ID_Start(int this_BLE_Numb) {
		//启动读锁设备ID进程
    	EntityDevice BLE_temp = new EntityDevice();
		BLE_temp.setName(BLE_Name[this_BLE_Numb]);
		BLE_temp.setAddress(BLE_Address[this_BLE_Numb]);
		
		//蓝牙连接
		controller.connect(BLE_temp);
		
		final BluetoothController controller_1 = controller;
        new Handler().postDelayed(new Runnable(){     
		    public void run() {   
		    	if (! controller_1.findGattCharacteristic()) {
		    		boolean ft = controller_1.findGattCharacteristic();
		    		
		    		AlertDialog dialog = new AlertDialog.Builder(context)
            			.setTitle("警告")			//设置对话框的标题
            			.setMessage("蓝牙直联失败！")	//显示锁蓝牙设备名
            			.setPositiveButton("确定", null)
            			.create();
		    		dialog.show();
		    	}
		    }     
		 }, 5000);			//延时检查蓝牙是否连接成功
	}
    
	protected void onDestroy() {

		super.onDestroy();
		unregisterReceiver(mReceiver);
		controller.close();
		
	}
}
