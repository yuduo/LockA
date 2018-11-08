package com.example.iods_lock_app;


import java.util.ArrayList;
import java.util.List;


import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.example.iods_bluetooch.BLEService;
import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_bluetooch.BluetoothController;
import com.example.iods_bluetooch.ConstantUtils;
import com.example.iods_bluetooch.EntityDevice;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_manage.BaidumapActivity;
import com.example.iods_manage.Locker_Quert_Activity;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Open_Locker_Activity extends Activity {
	//开锁页面

    DBHelper dbHelper ;//数据库服务对象
    
	private Context context = Open_Locker_Activity.this;//定义Context对象
	
	TextView Show_GPS_Data;
	
	private Spinner Nearby_Locker_List;
	List<String> list_Name = new ArrayList<String>();
	int Selected_Lock_index = 0;
	
	private Double Last_Lng = 0.0, Last_Lat = 0.0;
	private Double Your_Lng_C = 0.0, Your_Lat_C = 0.0;
	
	static NMS_Communication Connect_NMS;
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean BLE_Lock_Contrl_Tag = false, Send_BLE_Tag;
	
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
	String[] Lock_Lng = new String[100];	//锁经度
	String[] Lock_Lat = new String[100];	//锁纬度
	String[] Lock_ID = new String[100];		//锁ID
	String[] Lock_Name = new String[100];	//锁名
	boolean[] Lock_Error = new boolean[100];//锁告警状态
	int Numb_Lock = 0, Total_Locks;			//锁数量
	
	String Locker_ID;
	
	Double Er_Lnglat = UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //搜锁范围
	Double Scope_Lnglat = UserLogin_Activity.Scope_Limit;
	
	private LocationClient mLocationClient;
    private LocationManager locationManager;  
    private String provider;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        
        setContentView(R.layout.activity_open_locker);
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
        dbHelper = new DBHelper(context);	//创建DBHlper对象实例
                
		initi();
		
	    // 获取LocationClient
        mLocationClient = new LocationClient(this);
 
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);

        mLocationClient.start();
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
        provider = LocationManager.GPS_PROVIDER;
        //使用getLastKnownLocation就可以获取到记录当前位置信息的Location对象了  
        //并且用showLocation()显示当前设备的位置信息  
        //requestLocationUpdates用于设置位置监听器  
        //此处监听器的时间间隔为5秒，距离间隔是5米  
        //也就是说每隔5秒或者每移动5米，locationListener中会更新一下位置信息  
        Location location = locationManager.getLastKnownLocation(provider);  
        if (location != null) {  

        	showLocation(location);  
        }  
        String provider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);  
    }
    
    public void initi(){
    	Button NB_Lock_Contral = (Button)findViewById(R.id.button1);		//网络开锁
    	Button Show_Baidu_GIS = (Button)findViewById(R.id.button2);			//地图查锁
    	Button BT_Lock_Contral = (Button)findViewById(R.id.button3);		//蓝牙开锁
    	Button Help_me_Open = (Button)findViewById(R.id.button4);			//开锁求助
    	Button Btn_Lock_Query = (Button)findViewById(R.id.button5);			//设备查询
    	
    	Show_GPS_Data=(TextView)findViewById(R.id.textView7);
    	
    	//为各功能按键添加单击事件监听
    	NB_Lock_Contral.setOnClickListener(new ClickEvent());
    	Show_Baidu_GIS.setOnClickListener(new ClickEvent());
    	BT_Lock_Contral.setOnClickListener(new ClickEvent());
    	Help_me_Open.setOnClickListener(new ClickEvent());	
    	Btn_Lock_Query.setOnClickListener(new ClickEvent());	
    		
    	Nearby_Locker_List = (Spinner)findViewById(R.id.spinner1);
    	
    	Nearby_Locker_List.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Selected_Lock_index = position;				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}
		});
    }
    
  
    
    //自定义单击事件类
    class ClickEvent implements View.OnClickListener {    
    	
	       @SuppressLint("NewApi") @Override    
	       public void onClick(View v) {

	    	   Intent in;
	    	   
	    	   switch(v.getId()){
	    	   case R.id.button1:
	    		   //对选取的锁设备进行网络开锁前，先建立TCP连接
	    		   
	    		   if (Lock_Lng[Selected_Lock_index] == null || Lock_Lat[Selected_Lock_index] == null) {
	    			   AlertDialog dialog_1 = new AlertDialog.Builder(context)
			   				.setTitle("尚未收到GPS信号")			//设置对话框的标题		
							.setMessage("您请等待获取到GPS数据")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
	    			   dialog_1.show();
	    		   }
	    		   else {
	    			   SQLiteDatabase db=dbHelper.getWritableDatabase();
		    		   String temp_User = UserLogin_Activity.Login_User_ID;
		    		   	    		   
		    		   final double Error_Lng_C = Your_Lng_C - Double.valueOf(Lock_Lng[Selected_Lock_index]);
		    		   final double Error_Lat_C = Your_Lat_C - Double.valueOf(Lock_Lat[Selected_Lock_index]);
	       				    		   		    		
		    		   Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, Lock_ID[Selected_Lock_index]});
		    		   if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin")){
		    			   //用户有权或超级用户，则检查距离
		    			   if (Error_Lng_C > Scope_Lnglat || Error_Lat_C > Scope_Lnglat || Error_Lng_C < -1 * Scope_Lnglat || Error_Lat_C < -1 * Scope_Lnglat) {
		    				   AlertDialog dialog_1 = new AlertDialog.Builder(context)
		    				   			.setTitle("C : " + String.valueOf(Error_Lng_C) + "; " + String.valueOf(Error_Lat_C))			//设置对话框的标题		
		    				   			
		   								//.setTitle("距离此锁太远")			//设置对话框的标题
		   								.setMessage("您恐怕无法激活锁具，完成开锁")
		   								//设置对话框的按钮
		   								.setPositiveButton("确定", null)
		   								.create();
		    				   dialog_1.show();
		    			   }
		    			   else {
		    				   //间距符合要求
		    				   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
		    				   Connect_NMS.Make_Socket_Connect();
		    			   }
		    		   }
		    		   else {
			    			
		    			   AlertDialog dialog = new AlertDialog.Builder(context)
		   						.setTitle("提示")			//设置对话框的标题
		   						.setMessage("您没有打开此锁的权限")
		   						//设置对话框的按钮
		   						.setPositiveButton("确定", null)
		   						.create();
		    			   dialog.show();
			    			
		    		   }
		    		   cursor.close();
		    		   db.close();
	    		   }    		   
		    	
	    		   break;
	    	   case R.id.button2:
	    		   //开启百度地图查找周边锁设备位置
	    		   in=new Intent(context, BaidumapActivity.class);	
	    		   in.putExtra("Mode", "Near");
	    		   if (Your_Lng_C == 0.0 || Your_Lat_C == 0.0) {
	    			   Your_Lng_C = 120.65456;		//亨通光网位置
	    			   Your_Lat_C = 31.20083;
	    		   }
    			   
	    		   in.putExtra("GPS_Lng", String.valueOf(Your_Lng_C));
	    		   in.putExtra("GPS_Lat", String.valueOf(Your_Lat_C));
	    		   startActivity(in);//修改为节点地图显示的测试类
	    		   break;
	    	   case R.id.button3:
	    		   //蓝牙开锁
	    		   Open_BLE_Lock();   	       
 
	    		   break;
	    	   case R.id.button4:
	    		   //发送开锁求助信息
	    		   if (Lock_Lng[Selected_Lock_index] == null || Lock_Lat[Selected_Lock_index] == null) {
	    			   AlertDialog dialog_1 = new AlertDialog.Builder(context)
		   				.setTitle("尚未收到GPS信号")			//设置对话框的标题		
						.setMessage("您请等待获取到GPS数据")
						//设置对话框的按钮
						.setPositiveButton("确定", null)
						.create();
	    			   dialog_1.show();
	    		   }
	    		   else{
	    			   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6203");
		    		   Connect_NMS.Make_Socket_Connect();
	    		   }
	    		   	    		   
	    		   break;
	    		   
	    	   case R.id.button5:
	    		   //锁具查询
	    		   in=new Intent(context, Locker_Quert_Activity.class);	
	    		   startActivity(in);//查询锁设备位置
	    		   
	    		   break;
	    	   }
	       }
	}
    
    //locationListener中其他3个方法新手不太用得到，笔者在此也不多说了，有兴趣的可以自己去了解一下  
  	LocationListener locationListener = new LocationListener() {  

  		@Override  
  		public void onStatusChanged(String provider, int status, Bundle extras) {  
  		}  

  		@Override  
  		public void onProviderEnabled(String provider) {  
  		}  

  		@Override  
  		public void onProviderDisabled(String provider) {  
  		}  

  		@Override  
  		public void onLocationChanged(Location location) {  
  			// 更新当前设备的位置信息  
  			showLocation(location);  
  		}  
  	}; 

    //显示经纬度信息  
   	private void showLocation(final Location location) {  
   		
   		double this_Lat = location.getLatitude();
   		double this_Lng = location.getLongitude();
   		double Show_Lng, Show_Lat;
   		
   		Show_Lng = ((int) (this_Lng * 1000000 + 0.5)) / 1000000.0;
   		Show_Lat = ((int) (this_Lat * 1000000 + 0.5)) / 1000000.0;

   		Show_GPS_Data.setText(String.valueOf(Show_Lng) + " ;  " + String.valueOf(Show_Lat));
   		
   		LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
		
		com.baidu.mapapi.utils.CoordinateConverter converter  = new com.baidu.mapapi.utils.CoordinateConverter();    
       	converter.from(CoordType.GPS);    
       	// sourceLatLng待转换坐标    	
       	converter.coord(ll);    
       	ll= converter.convert(); 
   		
       	this_Lat = ll.latitude;
    	this_Lng = ll.longitude;
    	Your_Lng_C = this_Lng;
    	Your_Lat_C = this_Lat;
   		
   		if (this_Lng - Last_Lng > Er_Lnglat || Last_Lng - this_Lng > Er_Lnglat || 
				this_Lat - Last_Lat > Er_Lnglat || Last_Lat - this_Lat > Er_Lnglat) {
			//大幅移动，则刷新锁定位信息
				
	    	Last_Lat = this_Lat;
			Last_Lng = this_Lng;
	    	
	    	Numb_Lock = 0;
	    	list_Name.clear();
	    	
	    	Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6206");
			Connect_NMS.Make_Socket_Connect();

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
		dbHelper.close();
		controller.close();

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
						                	.setTitle("直联开锁")			//设置对话框的标题
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
						                	.setTitle("请选择开锁设备")	//设置对话框的标题
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
						                	.setTitle("请选择开锁设备")	//设置对话框的标题
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
						                	.setTitle("请选择开锁设备")	//设置对话框的标题
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
						                	.setTitle("请选择开锁设备")	//设置对话框的标题
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
					BLE_Communication.Send_Command_6102(controller);		//发送读设备ID指令
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
					
					if (Recive_Str.subSequence(0, 7).equals("C610200") && ! BLE_Lock_Contrl_Tag) {
						//收到设备ID，进入开锁控制
						Locker_ID = Recive_Str.substring(7, 41);
						
						Send_BLE_Tag = true;	//发送蓝牙指令标志置位
						BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, false);		
						
						if (! BLE_Lock_Contrl_Tag) {
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							controller.close();			//关闭蓝牙连接
							
							dialog = new AlertDialog.Builder(context)
	        					.setTitle("提示")			//设置对话框的标题
	        					.setMessage("您没有打开此锁的权限")
	        					//设置对话框的按钮
	        					.setPositiveButton("确定", null)
	        					.create();
							dialog.show();
						}
					}
					else if (Recive_Str.subSequence(0, 7).equals("E610200")) {
						//读设备ID的应答校验出错
						controller.close();			//关闭蓝牙连接
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("直联开锁出错")			//设置对话框的标题
							.setMessage("通信故障 ！")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog.show();
					}
					else if (Recive_Str.substring(0, 7).equals("C610100") || Recive_Str.subSequence(0, 7).equals("E610100")) {
						//收到开锁应答
						Recive_Str = Recive_Str.substring(7);
						if (Recive_Str.equals("00")) {
							//开锁成功
							SQLiteDatabase db=dbHelper.getWritableDatabase();
							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "通过蓝牙开锁成功", Locker_ID);
							db.close();
							
							controller.close();			//关闭蓝牙连接
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							
							dialog = new AlertDialog.Builder(context)
	    						.setTitle("提示")			//设置对话框的标题
	    						.setMessage("直联开锁成功")
	    						//设置对话框的按钮
	    						.setPositiveButton("确定", null)
	    						.create();
							dialog.show();
						}
						else {
							//开锁失败
							controller.close();			//关闭蓝牙连接
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							
							dialog = new AlertDialog.Builder(context)
	        					.setTitle("提示")			//设置对话框的标题
	        					.setMessage("直联开锁失败")
	        					//设置对话框的按钮
	        					.setPositiveButton("确定", null)
	        					.create();
							dialog.show();
						}
						
					}
					else if (Recive_Str.subSequence(0, 7).equals("C610300")) {
						//TODO 收到查询锁设备状态的应答，需要将此状态上传网管
						
						Recive_Str = Recive_Str.substring(7);
						
					}
				}
				else {
					//多余的回复
					if (! Recive_Str.subSequence(0, 7).equals("C610200")) {
						controller.close();			//关闭蓝牙连接
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
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
	
	public void Open_BLE_Lock() {
		//开蓝牙锁
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
	
	Handler mHandler = new Handler(){
		@SuppressLint("HandlerLeak") public void handleMessage(Message msg){
			String s, temp_Str;
			
			switch(msg.what) {
			
			case 0:
				//应答分析
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("6206")) {
					if (s.substring(5, 6).equals("N")) {
						Total_Locks = Integer.valueOf(s.substring(7));
					}
					else {
						//是GIS查询的应答
						if (Numb_Lock < 100) {
							//只处理100把锁，多余的丢弃
							temp_Str = s.substring(5, 6);
							Lock_Error[Numb_Lock] = true;
							if (temp_Str.equals("W")) {
								//有告警
								Lock_Error[Numb_Lock] = false;
							}
							
							Lock_Lng[Numb_Lock] = s.substring(7, 17);
							Lock_Lat[Numb_Lock] = s.substring(18, 28);
							Lock_ID[Numb_Lock] = s.substring(29, 63);	
							Lock_Name[Numb_Lock] = s.substring(64);	
							
							list_Name.add(Lock_Name[Numb_Lock]);
							
							Numb_Lock++;
													
							final ArrayAdapter<String> sAdapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_Name);
							sAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //定义每一个item的样式
							Nearby_Locker_List.setAdapter(sAdapter1);
						}
					}
				}
				
				if (temp_Str.equals("6202")) {
					//是开锁控制的应答
					temp_Str = s.substring(5);
					
					if (temp_Str.equals("00")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
	            			.setTitle("提示")			//设置对话框的标题
	            			.setMessage("开锁成功 ！")	//显示锁蓝牙设备名
	            			//设置对话框的按钮
	            			.setPositiveButton("确定", null)
	            			.create();
						dialog.show();
						
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "通过蓝牙NB开锁成功", Lock_Name[Selected_Lock_index]);
						db.close();
					}
					else if (temp_Str.equals("01")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
            				.setTitle("提示")			//设置对话框的标题
            				.setMessage("正在开锁")	//显示锁蓝牙设备名
            				//设置对话框的按钮
            				.setPositiveButton("确定", null)
            				.create();
						dialog.show();
					}
					else if (temp_Str.equals("06")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
        					.setTitle("提示")			//设置对话框的标题
        					.setMessage("通信故障")	//显示锁蓝牙设备名
        					//设置对话框的按钮
        					.setPositiveButton("确定", null)
        					.create();
						dialog.show();
					}
					else {
						AlertDialog dialog = new AlertDialog.Builder(context)
        					.setTitle("提示")			//设置对话框的标题
        					.setMessage("开锁失败 ！")	//显示锁蓝牙设备名
        					//设置对话框的按钮
        					.setPositiveButton("确定", null)
        					.create();
						dialog.show();
					}
											
				}
				
				if (temp_Str.equals("6203")) {
					//是求助开锁的应答
					temp_Str = s.substring(5);
					
					if (temp_Str.equals("00")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
	            			.setTitle("提示")	
	            			.setMessage("求助信息发送成功 ！")
	            			//设置对话框的按钮
	            			.setPositiveButton("确定", null)
	            			.create();
						dialog.show();
						
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "发送远程开锁求助成功", Lock_Name[Selected_Lock_index]);
						db.close();
					}
					else {
						AlertDialog dialog = new AlertDialog.Builder(context)
            				.setTitle("提示")	
            				.setMessage("求助失败 ！")
            				//设置对话框的按钮
            				.setPositiveButton("确定", null)
            				.create();
						dialog.show();
					}
					
				}

				break;
				
			case 2:
				s=msg.obj.toString();

				if (s.equals("Wait_Send_6206")) {
					Numb_Lock = 0;
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.GIS_Query_6206(Your_Lat_C, Your_Lng_C, 3 * Er_Lnglat);

				}
				else if (s.equals("Wait_Send_6202")) {

					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Open_NB_Lock_6202(UserLogin_Activity.Login_User_ID, Lock_ID[Selected_Lock_index]);  
			    	
				}
				else if (s.equals("Wait_Send_6203")) {

					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Call_Open_Lock_6203(UserLogin_Activity.Login_User_ID, Lock_ID[Selected_Lock_index], Lock_Lng[Selected_Lock_index], Lock_Lat[Selected_Lock_index]);  
			    	
				}
				

				break;
			
			}
			
		}

	};
	
}
