package com.example.iods_manage;

import android.annotation.SuppressLint;
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
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iods_bluetooch.BLEService;
import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_bluetooch.BluetoothController;
import com.example.iods_bluetooch.ConstantUtils;
import com.example.iods_bluetooch.EntityDevice;
import com.example.iods_common.DataAlgorithm;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.Show_Start_Logo;
import com.example.iods_lock_app.UserLogin_Activity;
import com.example.iods_manage.Group_Manage_Activity.ClickEvent;



public class Update_Softwear_Activity  extends Activity{
	//设备软件升级
	
	Button BTN_Link_BT, BTN_To_Sleep, BTN_Lock_BT, BTN_Lock_NB;
	
	TextView Show_Lean_Status, Show_Water_Status, Show_Other_Status, Show_Low_Status;
	TextView Show_Open_A_Status, Show_Open_B_Status, Show_Lock_A_Status, Show_Lock_B_Status;
	TextView Show_New_Error, Show_Status_Error, Show_Help;
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean Send_BLE_Tag = false;
	
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
	String Locker_ID;
	
	String Softwear_Ver, Softwear_CRC;
	int int_Softwear_Len, int_This_Frame, int_This_byteLen, int_This_Cell;
	int index_Update;
	
	byte[] Softwear_byteData=new byte[30000];
	byte[] Softwear_Frame=new byte[1200];
	byte[] Softwear_Cell=new byte[20];
	
	TextView Show_informat;
	
	Button Project_Checkup_Btn, User_Manage_Btn;
	
	static NMS_Communication Connect_NMS;
	
	private Context context = Update_Softwear_Activity.this;//定义Context对象
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_group_manage);
	    setTitle("亨通光电");
	     
	    
		initi();
	}

	private void initi() {
		Button Remote_Lock_Contral_Btn;
	    
	    User_Manage_Btn = (Button)findViewById(R.id.button2);
	    Project_Checkup_Btn = (Button)findViewById(R.id.button3);
	    Remote_Lock_Contral_Btn = (Button)findViewById(R.id.button1);
	    
	    User_Manage_Btn.setText("下载升级软件");
	    Project_Checkup_Btn.setText("开始设备软件升级");
	    
	    User_Manage_Btn.setOnClickListener(new ClickEvent());
	    Project_Checkup_Btn.setOnClickListener(new ClickEvent());
	    //Project_Checkup_Btn.setEnabled(false);
	    //User_Manage_Btn.setEnabled(false);
				
		TextView Titale_View=(TextView)findViewById(R.id.Search_text_1);
		Show_informat=(TextView)findViewById(R.id.textView2);
		Titale_View.setText("设备软件升级");
		
		Show_informat.setText("正在下载升级软件");
		Remote_Lock_Contral_Btn.setVisibility(View.INVISIBLE);
		
		//下载升级软件
		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6224");
		Connect_NMS.Make_Socket_Connect();
	}
	
	class ClickEvent implements View.OnClickListener {    
    	
	       @SuppressLint("NewApi") @Override    
	       public void onClick(View v) {

	    	   switch(v.getId()){

	    	   case R.id.button3:
	    		   //启动设备软件升级	    		   
	    		   Open_BLE_Lock();
	    		   
	    		   break;
	   
	    	   case R.id.button2:
	    		   //下载升级软件
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6224");
	    		   Connect_NMS.Make_Socket_Connect();
						    		   
	    		   break;
	    	   }
	    	   
	       }
	}
	
	public void Open_BLE_Lock() {
		//连接蓝牙
		BLE_Get_Start_Tag = false;

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
	
	private void Open_BLE_Lock_Start(int this_BLE_Numb) {
		//启动开锁指令发送
		EntityDevice BLE_temp = new EntityDevice();
		BLE_temp.setName(BLE_Name[this_BLE_Numb]);
		BLE_temp.setAddress(BLE_Address[this_BLE_Numb]);
		
		//蓝牙连接
		controller.connect(BLE_temp);
		
		final BluetoothController controller_1 = controller;
        new Handler().postDelayed(new Runnable(){     
		    public void run() {   
		    	if (! controller_1.findGattCharacteristic()) {

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
	
	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i;
			String temp_Str_1, temp_Str_2;
						
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
						                	.setTitle("软件升级")			//设置对话框的标题
						                	.setMessage(BLE_Name[0])	//显示锁蓝牙设备名
						                	//设置对话框的按钮
						                	.setNegativeButton("取消", null)
						                	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Open_BLE_Lock_Start(0);
						                			dialog.dismiss();
						                		}
						                	}).create();
										dialog.show();
										break;
										
									case 2:
										final String items2[] = {BLE_Name[0], BLE_Name[1]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("请选择待升级设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items2, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Open_BLE_Lock_Start(which);
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
						                	.setTitle("请选择待升级设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items3, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Open_BLE_Lock_Start(which);
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
						                	.setTitle("请选择待升级设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items4, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Open_BLE_Lock_Start(which);
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
						                	.setTitle("请选择待升级设备")	//设置对话框的标题
						                	.setSingleChoiceItems(items5, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Open_BLE_Lock_Start(which);
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
					Send_BLE_Tag = true;	//发送蓝牙指令标志置位
					//先读取设备软件当前版本
					BLE_Communication.Send_Command_6104(controller);
					
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
					//TODO
					
					Recive_Str = Recive_Str.toUpperCase();
					
					if (Recive_Str.substring(0, 7).equals("C610400")) {
						if (Recive_Str.length() > 39) {
							Recive_Str = Recive_Str.substring(0, 39);
						}
						temp_Str_1 = Recive_Str.substring(23);
						temp_Str_2 = Softwear_Ver.substring(0, 3);
						String temp_Str_3 = Softwear_Ver.substring(3);
						
						Show_informat.setText("设备软件版本： " + temp_Str_1);
						
						if (temp_Str_1.substring(0, 3).equals(temp_Str_2)) {
							//软件版本兼容
							if(temp_Str_1.substring(3).compareTo(temp_Str_3) < 0) {
								//启动软件升级
								index_Update = 1;
								Send_BLE_Tag = true;	//发送蓝牙指令标志置位
								BLE_Communication.Send_Command_6124_1(controller, int_Softwear_Len);		//发送读设备ID指令
							}
							else {
								dialog = new AlertDialog.Builder(context)
									.setTitle("提示")			//设置对话框的标题
									.setMessage("升级软件版本不高，无需升级")
									//设置对话框的按钮
									.setPositiveButton("确定", null)
									.create();
								dialog.show();
							}
						}
						else {
							dialog = new AlertDialog.Builder(context)
    							.setTitle("提示")			//设置对话框的标题
    							.setMessage("软件版本不兼容，不能升级")
    							//设置对话框的按钮
    							.setPositiveButton("确定", null)
    							.create();
							dialog.show();
						}
					}
					else if (Recive_Str.substring(0, 5).equals("C6124")) {
						//升级指令的应答

						if (index_Update == 4) {
							//分析升级结果查询反馈
							if (Recive_Str.substring(0,9).equals("C61240004")) {
								//升级成功
								dialog = new AlertDialog.Builder(context)
    								.setTitle("成功")			//设置对话框的标题
    								.setMessage("设备软件升级成功 ！")
    								//设置对话框的按钮
    								.setPositiveButton("确定", null)
    								.create();
								dialog.show();
							}
							else {
								//升级失败
								Show_Dialog_Error();
							}
						}
						else if (index_Update == 3) {
							//延时启动升级结果查询指令
							if (Recive_Str.substring(0,9).equals("C61240003")) {
								Send_BLE_Tag = true;	//发送蓝牙指令标志置位
								index_Update = 4;
							}
							else {
								//升级失败
								Show_Dialog_Error();
							}
						}
						else if (index_Update == 2) {
							//发送下载完成指令
							if (Recive_Str.equals("C61240001")) {
								//蓝牙会反复发一组信号
								index_Update = 2;
								int_This_Frame = 1;		//第一帧
								int_This_Cell = 1;		//包序号初始化
								//组织第一帧升级软件数据
								Make_And_Send_Soft_Frame(0);
							}
							else if (Recive_Str.equals("C61240002")) {
								//是帧传输完成的应答
								i = int_Softwear_Len % 1000;
								if (i > 0) {
									i = (int) (int_Softwear_Len/1000) + 1;
								}
								else {
									i = (int) (int_Softwear_Len/1000);
								}
								
								if (int_This_Frame < i) {
									//组织后续帧升级软件数据
									int_This_Frame ++;
									int_This_Cell = 1;		//包序号初始化
									
									Make_And_Send_Soft_Frame(0);
								}
								else {
									//发送完成
									Send_BLE_Tag = true;	//发送蓝牙指令标志置位
									BLE_Communication.Send_Command_6124_3(controller);
									index_Update = 3;
								}
							}
							else if(Recive_Str.substring(5,7).equals("6C")) {
								//是对切包的应答
								temp_Str_1 = Recive_Str.substring(7);
								i = Integer.parseInt(temp_Str_1, 16);
								Make_And_Send_Soft_Frame(i);
							}
							else {
								//升级失败
								Show_Dialog_Error();
							}
						}
						else if (index_Update == 1) {
							//开始发送升级软件数据
							if (Recive_Str.substring(7).equals("01")) {
								index_Update = 2;
								int_This_Frame = 1;		//第一帧
								int_This_Cell = 1;		//包序号初始化
								//组织第一帧升级软件数据
								Make_And_Send_Soft_Frame(0);
							}
							else {
								//升级失败
								Show_Dialog_Error();
							}
						}
					}
				}
				
				
				
				
				//TODO
				
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

		private void Make_And_Send_Soft_Frame(int Last_Cell) {
			// TODO 生成软件下载数据帧，并发送程序，int_This_Frame为帧序号，int_This_Cell为包序号, int_This_byteLen保存当前帧长(含头尾的7E)
			//Last_Cell为上一个切包序号
			int i, j, k;
			
			if (Last_Cell + 1 != int_This_Cell) {
				return;
			}
			
			i = int_Softwear_Len/1000;
			if (int_Softwear_Len%1000 > 0) {
				i = i+1;
			}
			
			Show_informat.setText("设备软件升级进程： " + String.valueOf(int_This_Frame) + "/" + String.valueOf(i) + "  " + String.valueOf(Last_Cell));
			
			if (int_This_Cell == 1) {
				//首次进入，生成帧数据
				Softwear_Frame[0] = 0x7E;
				Softwear_Frame[1] = 0x00;
				Softwear_Frame[2] = 0x10;
				Softwear_Frame[3] = (byte)(int_This_Frame & 0xFF);;
				Softwear_Frame[4] = (byte)(int_This_Frame >> 8);		//小端格式的帧序号
				
				i = int_Softwear_Len % 1000;
				if (i > 0) {
					i = (int) (int_Softwear_Len/1000) + 1;
				}
				else {
					i = (int) (int_Softwear_Len/1000);
				}
				
				Softwear_Frame[5] = (byte)(i & 0xFF);
				Softwear_Frame[6] = (byte)(i >> 8);		//小端格式的帧总数
				
				//命令码
				Softwear_Frame[7] = 0x24;
				Softwear_Frame[8] = 0x61;
				Softwear_Frame[9] = (byte) 0xFF;		//状态码
				
				Softwear_Frame[10] = 2;		//请求类型码
				
				Softwear_Frame[11] = 1;		//对象类型
				
				Softwear_Frame[12] = Softwear_Frame[3];
				Softwear_Frame[13] = Softwear_Frame[4];		//升级包序号
				
				for (i = 0; i<1000; i++) {
					if (i + 1000 *(int_This_Frame - 1) <= int_Softwear_Len) {
						Softwear_Frame[14 + i] = Softwear_byteData[i + 1000*(int_This_Frame - 1)];
					}
					else {
						//完成全部下载软件数据的成帧处理
						break;
					}
				}
				int_This_byteLen = i + 14 + 2;		//未进行7D、7E转换的帧长
				
				//CRC校验
				i = NMS_Communication.Cala_CRC(Softwear_Frame, i + 13);
				Softwear_Frame[int_This_byteLen - 2] = (byte) (i & 0xFF);				//低8位
				Softwear_Frame[int_This_byteLen - 1] = (byte) (i >> 8);					//高8位
				
				//结束符
				Softwear_Frame[int_This_byteLen] = 0x7E;
												
				//非结束符的0x7E 的转意处理
				//将7D转成 7D 5D
				i = 1;
				j = 0;
				while (j == 0) {
					if (Softwear_Frame[i] == 0x7D ) {
						//数据序列中有 7D，则将后续数据全部后移一位，数据长度 + 1，并将 7E 转义为 7D, 5D
						for (k = int_This_byteLen; k >= i; k--) {
							Softwear_Frame[k + 1] = Softwear_Frame[k];
						}
						int_This_byteLen++;
						Softwear_Frame[i] = 0x7D;
						Softwear_Frame[i + 1] = 0x5D;
						i ++;
					}
					
					i ++;
					if (i >= int_This_byteLen)		j = 1;
				}
				
				//将7E转成 7D 5E
				i = 1;
				j = 0;
				while (j == 0) {
					if (Softwear_Frame[i] == 0x7E ) {
						//数据序列中有 7E，则将后续数据全部后移一位，数据长度 + 1，并将 7E 转义为 7D, 5E
						for (k = int_This_byteLen; k >= i; k--) {
							Softwear_Frame[k + 1] = Softwear_Frame[k];
						}
						int_This_byteLen++;
						Softwear_Frame[i] = 0x7D;
						Softwear_Frame[i + 1] = 0x5E;
						i ++;
					}
					
					i ++;
					if (i >= int_This_byteLen)		j = 1;
				}
				int_This_byteLen ++;	//完成帧数据生成
				
				//切第一个包，并发送
				for (i = 0; i<20; i++) {
					Softwear_Cell[i] = Softwear_Frame[i];
				}				
				int_This_Cell ++;
				
				Send_BLE_Tag = true;	//发送蓝牙指令标志置位
				
				controller.write(Softwear_Cell);		//发送数据包
			}
			else {
				//后续仅只发送切包数据
				j = int_This_byteLen - 2;
				if (j % 18 > 0) {
					i = j/18 + 1;
				}
				else {
					i = j/18;
				}				//获得当前帧的切包总数
				
				if (int_This_byteLen - (20 + 18*(int_This_Cell - 2)) > 18) {
					//包长20
					Softwear_Cell[0] = (byte) i;
					Softwear_Cell[1] = (byte) int_This_Cell;
					
					for (i = 0; i < 18; i++) {
						Softwear_Cell[i + 2] = Softwear_Frame[18 * (int_This_Cell-1) + 2 + i];
					}
					int_This_Cell ++;
					
					Send_BLE_Tag = true;	//发送蓝牙指令标志置位
					controller.write(Softwear_Cell);		//发送数据包
				}
				else {
					//剩余包长
					j = int_This_byteLen - (20 + 18*(int_This_Cell - 2)) + 2;
					byte[] Last_Softwear_Cell = new byte[j];
					
					Last_Softwear_Cell[0] = (byte) i;
					Last_Softwear_Cell[1] = (byte) int_This_Cell;
					
					for (i = 2; i<j; i++) {
						Last_Softwear_Cell[i] = Softwear_Frame[18 * (int_This_Cell-1) + i];
					}
					int_This_Cell ++;
					
					Send_BLE_Tag = true;	//发送蓝牙指令标志置位
					controller.write(Last_Softwear_Cell);		//发送数据包
				}
			}
		}

		private void Show_Dialog_Error() {
			AlertDialog dialog = new AlertDialog.Builder(context)
				.setTitle("提示")			//设置对话框的标题
				.setMessage("设备软件升级失败，请重新升级！")
				//设置对话框的按钮
				.setPositiveButton("确定", null)
				.create();
			dialog.show();
		}
	}
	
	 public void onResume() {
	    super.onResume();
	    	
	}
	 
	public void onNewIntent(Intent intent) {
			//NFCUntils.NFC_onNewIntent(intent, nfcv, mHandler, true);
			//true表示读标签
	}
		
	@Override
	protected void onDestroy() {

		super.onDestroy();
		controller.close();

	}
		
	
	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			String s, temp_Str;
  			int i, j;

  			switch(msg.what){
  			case 0:
  				//应答分析
  				s=msg.obj.toString();
  				temp_Str = s.substring(0, 6);
  				
  				if (temp_Str.equals("6224 F")) {
					//是下载的升级文件数据
					temp_Str = s.substring(7, 10);
					
					if (temp_Str.equals("Ver")) {
						//版本号数据
						Softwear_Ver = s.substring(10, 42);		//软件版本号
						
						byte[] temp_byte = new byte[16];
						temp_byte = DataAlgorithm.hexStringToBytes(Softwear_Ver);
						Softwear_Ver = new String(temp_byte);
						
						Softwear_CRC = s.substring(43, 47);		//软件CRC校验值
						
						int_Softwear_Len = Integer.valueOf(s.substring(48));	//软件长度
						
						int_This_Frame = 0;
						int_This_byteLen = 0;
						Softwear_byteData[0] = 0;	//此位置不存数据，以便于调用CRC计算程序
					}
					else {
						//升级数据
						temp_Str = s.substring(7);

						i = temp_Str.indexOf(" ");
						
						s = temp_Str.substring(0, i);	//当前帧号与总帧数
						Show_informat.setText("升级软件下载进程： " + s);
						
						temp_Str = temp_Str.substring(i + 1);	//软件数据
						byte[] Softwear_Frame_1 = DataAlgorithm.hexStringToBytes(temp_Str);
						int_This_Frame = temp_Str.length()/2;
						
						for (i = 0; i < int_This_Frame; i++) {
							Softwear_byteData[int_This_byteLen + i] = Softwear_Frame_1[i];
						}
						int_This_byteLen = int_This_byteLen + int_This_Frame;
						
						i = s.indexOf("/");
						//当前帧号
						int_This_Frame = Integer.valueOf(s.substring(0, i));
						
						i = Integer.valueOf(s.substring(i + 1));		//总帧数
						
						if (i == int_This_Frame && int_This_byteLen == int_Softwear_Len) {
							//最后一帧
							if (int_This_byteLen == int_Softwear_Len) {
								//帧长正确，则计算CRC
								i = NMS_Communication.Cala_CRC(Softwear_byteData, 0, int_This_byteLen - 1);
								
								temp_Str = Softwear_CRC.substring(0, 2);
								s =  Softwear_CRC.substring(2);
								
								if ((i & 0xFF) ==  Integer.parseInt(temp_Str, 16) && (i >> 8) == Integer.parseInt(s, 16)) {
									//CRC校验正确
									Show_informat.setText("完成软件下载，可以进行软件升级");
									Project_Checkup_Btn.setEnabled(true);
									User_Manage_Btn.setEnabled(true);
									
									AlertDialog dialog = new AlertDialog.Builder(context)
	  									.setTitle("提示")			//设置对话框的标题
	  									.setMessage("完成软件下载，可以启动软件升级")	//显示锁蓝牙设备名
	  									//设置对话框的按钮
	  									.setPositiveButton("确定", null)
	  									.create();
									dialog.show();

								}
								else {
									//CRC校验正确
									AlertDialog dialog = new AlertDialog.Builder(context)
		  								.setTitle("提示")			//设置对话框的标题
		  								.setMessage("软件下载出错，请重新下载")	//显示锁蓝牙设备名
		  								//设置对话框的按钮
		  								.setPositiveButton("确定", null)
		  								.create();
									dialog.show();
								}
							}
							else {
								//帧长错误
								AlertDialog dialog = new AlertDialog.Builder(context)
		  							.setTitle("提示")			//设置对话框的标题
		  							.setMessage("软件下载出错，请重新下载")	//显示锁蓝牙设备名
		  							//设置对话框的按钮
		  							.setPositiveButton("确定", null)
		  							.create();
								dialog.show();
							}
						}
					}						
				}
  				else {
  					//下载出错
  					AlertDialog dialog = new AlertDialog.Builder(context)
  						.setTitle("提示")			//设置对话框的标题
  						.setMessage("软件下载出错，请重新下载")	//显示锁蓝牙设备名
  						//设置对话框的按钮
  						.setPositiveButton("确定", null)
  						.create();
  					dialog.show();
  				}
  				
  				
  				break;
  				
  			case 2:
  				s=msg.obj.toString();

  				if (s.equals("Wait_Send_6224")) {
  					
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  					NMS_Communication.Download_Softwear_6224();	//下载升级软件
  					
  				}
  				break;
  			}
  		}
  	};

}
