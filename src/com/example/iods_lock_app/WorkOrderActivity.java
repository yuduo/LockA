package com.example.iods_lock_app;

import com.example.iods_bluetooch.BLEService;
import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_bluetooch.BluetoothController;
import com.example.iods_bluetooch.ConstantUtils;
import com.example.iods_bluetooch.EntityDevice;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_manage.Make_NewLock_Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;




//列表显示类
public class WorkOrderActivity extends Activity {
	//工单管理页面
	
	//建立控件对象

	private ListView Order_ListView;
	private SimpleAdapter Order_listAdapter;				//新建锁设备列表适配器
	private List<HashMap<String,String>> OrderList;//工单列表数据源

	private Spinner spinner1, spinner2;//查询spinner
	int selectedposition;//spinner2的选择项；

	Context context=WorkOrderActivity.this;
	DBHelper dbHelper;
	
	static NMS_Communication Connect_NMS;
	
	//广播参数
	private ServiceReceiver mReceiver;
	private String action="Flash_Order_List";
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean BLE_Lock_Contrl_Tag = false;
	
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order);
        setTitle("亨通光电");
        dbHelper = new DBHelper(context);//初始化数据库对象    
        
        //广播初始化
        mReceiver = new ServiceReceiver();
	    //实例化过滤器并设置要过滤的广播
	    IntentFilter mFilter = new IntentFilter();
	    mFilter.addAction(action);
	    registerReceiver(mReceiver, mFilter);
	    
		//控件初始化
		initi();
		
    }
    
    public void initi(){
    	spinner1 = (Spinner)findViewById(R.id.spinner_Select);//第二个下拉菜单，由第一个下拉菜单的选择而更新
		spinner2 = (Spinner)findViewById(R.id.spinner2);//第一个下拉菜单		
		
		Button DownLoad_Btn;
		
		Order_ListView=(ListView)findViewById(R.id.worklistView);
		
		Button Apply_Insert_Tag_BTN = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		DownLoad_Btn = (Button)findViewById(R.id.Save_Config);
		
		DownLoad_Btn.setOnClickListener(new ClickEvent());
		Apply_Insert_Tag_BTN.setOnClickListener(new ClickEvent());
		
		Apply_Insert_Tag_BTN.setText("设备注册查询");
		
		//Apply_Insert_Tag_BTN.setVisibility(View.INVISIBLE);			//不显示此按键
		spinner1.setVisibility(View.INVISIBLE);			//不显示此按键
		spinner2.setVisibility(View.INVISIBLE);			//不显示此按键
		
		Flash_Order_List();		//刷新工单列表
		
		//下载工单列表
		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620D");
		Connect_NMS.Make_Socket_Connect();
			
	}
    

    @SuppressLint("NewApi") 
    private void Flash_Order_List() {
		//刷新工单列表
    	String Str_ID, temp_Str, temp_Str_1 = null, Str_Date;
    	    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");// HH:mm:ss
		//获取当前时间
		Date date = new Date(System.currentTimeMillis());
		Str_Date = simpleDateFormat.format(date);
		cursor=db.rawQuery("select * from Order_List_Table where userID = ? AND Status != '4' AND (Status != '5' OR back_date = ?) ORDER BY Order_Type", new String[]{UserLogin_Activity.Login_User_ID, Str_Date});    	
	     
		OrderList = new ArrayList<HashMap<String,String >>();
	      
	    while(cursor.moveToNext()){
      
    		HashMap<String,String > map=new HashMap<String,String>();   		
    		
    		Str_ID = cursor.getString(1);		//取工单ID
    		map.put("OrderID", Str_ID);
    		
    		temp_Str = Str_ID.substring(0, 6);	//截取工单类型
    		
    		if (temp_Str.equals("INSERT")) {
    			temp_Str_1 = "施工工单";
    		}
    		else if (temp_Str.equals("MAINTE")) {
    			temp_Str_1 = "维护工单";
    		}
    		else if (temp_Str.equals("MOVETO")) {
    			temp_Str_1 = "移机工单";
    		}
    		else if (temp_Str.equals("REMOVE")) {
    			temp_Str_1 = "拆除工单";
    		}
    		
    		temp_Str = cursor.getString(5);		//取工单状态
    		if (temp_Str.equals("5")) {
    			temp_Str_1 = "已回单";
    		}
    		else if (temp_Str.equals("4")) {
    			//不显示已退单工单
    			continue;
    		}
    		
    		map.put("OrderType", temp_Str_1);
    		
    		Str_Date = cursor.getString(4);		//取完工时限
    		if (Str_Date == null) {
    			Str_Date = "";
    		}
    		if (Str_Date.equals("0")) {
    			Str_Date = "";
    		}
    		map.put("OrderLimite", Str_Date);
    		
    		OrderList.add(map);
    	}
      
		db.close();
		
		String[] from=new String[]{"OrderID", "OrderLimite", "OrderType"};
		int[] to=new int[]{R.id.textView1, R.id.textView3, R.id.textView5};
		Order_listAdapter=new SimpleAdapter(context, OrderList, R.layout.listitem_order,from, to);
	  	Order_ListView.setAdapter(Order_listAdapter);
	  	Order_listAdapter.notifyDataSetChanged();
  	
	  	Order_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent();   //创建Intent对象 
				
				TextView tv=(TextView)view.findViewById(R.id.textView1);
				String Str_Order = tv.getText().toString();
				
				tv=(TextView)view.findViewById(R.id.textView5);
				String Str_Status = tv.getText().toString();
				
	    		intent.setClass(context, Make_NewLock_Activity.class);	
	    		intent.putExtra("workID", Str_Order);
	    		intent.putExtra("Status", Str_Status);
	        	startActivity(intent);
		
			}
    		
    	});
	}
    
    
    class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Flash_Order_List")) {
				//完成回单，施工页面被关闭
				Flash_Order_List();		//刷新工单列表

			}
		}	
    }
		

	//定义Handler对象
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			String s, temp_Str;
			int temp_Int = 0;

			switch(msg.what){
			case 0:
				//应答分析
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("620D")) {
					temp_Str = s.substring(5);
					
					if (temp_Str.length() > 0) {
						//有工单ID
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						
						Cursor cursor=db.rawQuery("select * from Order_List_Table  where Order_ID = ?", new String[]{temp_Str});    			    
						if (cursor.moveToNext()){
							temp_Int = 1;
						}
						cursor.close();
						
						if (temp_Int == 0) {
							db.execSQL("INSERT INTO Order_List_Table (Order_ID, Order_Type, userID, dateLimit, Status, flow) " +
									"VALUES (?, ?, ?, '', '0', 0)", new String[]{temp_Str, temp_Str.substring(0, 6), UserLogin_Activity.Login_User_ID});
						}
						db.close();
						
						Flash_Order_List();
					}
				}

				break;
			case 2:
				s=msg.obj.toString();

				if (s.equals("Wait_Send_620D")) {
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.DownLoad_OrderList_620D(UserLogin_Activity.Login_User_ID);
					
				}
				break;
			}
		}
	};

	
	
	

	
	@Override
	protected void onResume() {

		super.onResume();
		Flash_Order_List();		//刷新工单列表
	}
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i("前调用", "onDestroy()");		
		dbHelper.close();
		
		unregisterReceiver(mReceiver);
	}
	   


	public void onNewIntent(Intent intent) {
		//NFCUntils.NFC_onNewIntent(intent, nfcv, mHandler, true);
		//true表示读标签
	}
	
	//自定义单击事件类
	class ClickEvent implements View.OnClickListener {    

		

		public void onClick(View v) {

	    	   switch(v.getId()){
	    	   case R.id.Save_Config:
	    		   //下载工单列表
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620D");
				   Connect_NMS.Make_Socket_Connect();
	    		   					
	    		   break;
	    		   
	    	   case R.id.Apply_Insert_Tag_btn:
	    		   //查询设备BN平台注册情况
	    		   Open_BLE_Lock();  
	    		   					
	    		   break;
	    		
	    	   }
		}
	}
		
	
	public void Open_BLE_Lock() {
		//开蓝牙服务
		BLE_Get_Start_Tag = false;
		BLE_Lock_Contrl_Tag = false;
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
						                	.setTitle("查询设备")			//设置对话框的标题
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
						                	.setTitle("请选择查询的设备")	//设置对话框的标题
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
						                	.setTitle("请选择查询的设备")	//设置对话框的标题
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
						                	.setTitle("请选择查询的设备")	//设置对话框的标题
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
						                	.setTitle("请选择查询的设备")	//设置对话框的标题
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
					
					BLE_Communication.Send_Command_6105(controller, 3);		//发送写设备注册查询指令
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
				
				if (Recive_Str.substring(0, 7).equals("C610500")) {
					//收到设备注册情况查询
					if (Recive_Str.substring(7).equals("04")) {
						Recive_Str = "设备已成功完成在NB平台的注册";
					}
					else {
						Recive_Str = "设备尚未在NB平台上注册成功，可重新加电尝试再次注册。";
					}
					
					dialog = new AlertDialog.Builder(context)
						.setTitle("设备注册情况")			//设置对话框的标题
						.setMessage(Recive_Str)
						//设置对话框的按钮
						.setPositiveButton("确定", null)
						.create();
					dialog.show();

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
	
	
}
