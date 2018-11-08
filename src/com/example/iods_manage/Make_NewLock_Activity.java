package com.example.iods_manage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
import com.example.iods_common.Baidu_Map;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;



public class Make_NewLock_Activity  extends Activity {
	//新建锁施工
	private Make_NewLock_Activity mactivity;
	
	private Context context = Make_NewLock_Activity.this;//定义Context对象
	
	double Get_Lock_Lat = 0, Get_Lock_Lng = 0;
	String Get_Lock_Name = ""; 

	static NMS_Communication Connect_NMS;
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean BLE_Lock_Contrl_Tag = false;
	boolean Jast_Open_Tag, Send_BLE_Tag;
	
	int index_6105 = 0;
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
	String[] List_Lock_ID = new String[200];
	String[] List_Lock_Name = new String[200];
	String[] List_Lock_Lng = new String[200];
	String[] List_Lock_Lat = new String[200];
	boolean[] List_Lock_Finish = new boolean[200];
	int Lock_Index;
	
	private LocationClient mLocationClient;
    private LocationManager locationManager;  
    private String provider;
	private Double Your_Lng_C=120.0, Your_Lat_C=31.0;
	Double Scope_Lnglat = UserLogin_Activity.Scope_Limit;
	
	private ListView Lock_ListView;
	private SimpleAdapter Lock_listAdapter;				//新建锁设备列表适配器
	private List<HashMap<String,String>> Lock_List;		//新建锁设备列表数据源
	
	DBHelper dbHelper ;//数据库服务对象
	
	String Lock_ID, New_Lock_ID, Lock_imsi;				//锁ID
	String workID, workType, work_Status;				//工单ID
	
	int Int_Get_GPS_Tag = 0;
	
	Button Get_New_Lock;
	
	//广播参数
	private ServiceReceiver mReceiver;
	private String action="Close_BackOrder";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	SDKInitializer.initialize(getApplicationContext());
    	
        setContentView(R.layout.activity_make_newlock);
        
        Intent in=getIntent();
        workID=in.getStringExtra("workID");
        work_Status = in.getStringExtra("Status");
        
        mactivity = this;
        
        String temp_Str = workID.substring(0, 6);
        		
		if (temp_Str.equals("INSERT")) {
			workType = "新建";
			setTitle("新建锁施工：" + workID);
		}
		else if (temp_Str.equals("MAINTE")) {
			workType = "维护";
			setTitle("锁维护施工：" + workID);
		}
		else if (temp_Str.equals("REMOVE")) {
			workType = "拆除";
			setTitle("拆除施工：" + workID);
		}
		else if (temp_Str.equals("MOVETO")) {
			workType = "移机";
			setTitle("移机施工：" + workID);
		}
        
        dbHelper = new DBHelper(context);	//创建DBHlper对象实例
                
        //广播初始化
        mReceiver = new ServiceReceiver();
	    //实例化过滤器并设置要过滤的广播
	    IntentFilter mFilter = new IntentFilter();
	    mFilter.addAction(action);
	    registerReceiver(mReceiver, mFilter);

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
        locationManager.requestLocationUpdates(provider, 5000, 5, locationListener);  
        

        //TODO 回单调试
        Test_Backup_Order(workID);
    }
        
    private void Test_Backup_Order(String workID2) {
		// TODO 回单调试
    	/*
    	 * 	 * 1 _id：:记录序号
	 * Order_ID：工单ID
	 * Old_Locker_ID：		锁具ID
	 * Old_Locker_Name:	老锁名
	 * Old_Locker_Lng：	老经纬度
	 * Old_Locker_lat
	 * New_Locker_Name:	新锁名
	 * New_Locker_Lng：	新经纬度
	 * New_Locker_lat
	 * New_Locker_ID
	 * Status:			完工工单状态：0未完成，1完成
	 * New_Lock_imsi
    	 */
    	String[] Old_Locker_ID = new String[50];
    	String[] Old_Locker_Name = new String[50];
    	String[] Old_Locker_Lng = new String[50];
    	String[] Old_Locker_Lat = new String[50];
    	String[] New_Locker_Name = new String[50];
    	String[] New_Locker_Lng = new String[50];
    	String[] New_Locker_Lat = new String[50];
    	String[] New_Locker_ID = new String[50];
    	String[] Status = new String[50];
    	String[] New_Lock_imsi = new String[50];
    	int i = 0, j;
    	Double Double_Lng;
    	String temp_Str_1, temp_Str_2;
    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
    	
    	//db.execSQL("DELETE FROM Order_Data_Table WHERE New_Locker_ID <> ?", new String[]{"0D0F383633373033303336323835343330"});
		    	
		Cursor cursor;  
		cursor=db.rawQuery("select * from Order_Data_Table where Order_ID = ?", new String[]{workID});    	
	      
	    while(cursor.moveToNext()){
	    	j = cursor.getInt(0);
	    	Old_Locker_ID[i] = cursor.getString(2);
	    	Old_Locker_Name[i] = cursor.getString(3);
	    	Old_Locker_Lng[i] = cursor.getString(4);
	    	Old_Locker_Lat[i] = cursor.getString(5);
	    	New_Locker_Name[i] = cursor.getString(6);
	    	New_Locker_Lng[i] = cursor.getString(7);
	    	New_Locker_Lat[i] = cursor.getString(8);
	    	New_Locker_ID[i] = cursor.getString(9);
	    	Status[i] = cursor.getString(10);
	    	New_Lock_imsi[i] = cursor.getString(11);

	    	i++;
	    }
	    
	    temp_Str_1 = New_Locker_ID[0].substring(33);
	    temp_Str_2 = New_Locker_ID[0].substring(31,32);
	    
	    /*
	    Double_Lng = Double.valueOf(New_Locker_Lng[0]);
	    for (j = 1; j<40; j++) {
	    	New_Locker_Name[j] = New_Locker_Name[0] + "-" + String.valueOf(j);
	    	New_Locker_Lng[j] = String.valueOf(Double_Lng + j * 0.001);
	    	New_Locker_Lat[j] = New_Locker_Lat[0];
	    	i = j /10;
	    	New_Locker_ID[j] = New_Locker_ID[0].substring(0, 31) + String.valueOf(Integer.valueOf(temp_Str_2) + i) + "3" + String.valueOf(Integer.valueOf(temp_Str_1) + j%10);
	    	
	    	db.execSQL("INSERT INTO Order_Data_Table (Order_ID, New_Locker_Name, New_Locker_Lng, New_Locker_Lat, New_Locker_ID, Status, New_Lock_imsi) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)", new String[]{workID, New_Locker_Name[j], New_Locker_Lng[j], New_Locker_Lat[0], New_Locker_ID[j], Status[0], New_Lock_imsi[0]});
			
	    }
	    
	    db.close();
	    j = i;
		*/
	}

	public void initi(){
    	    	
		Button Replay_This_Order, Show_Map;
		
		Replay_This_Order = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		Get_New_Lock = (Button)findViewById(R.id.Save_Config);
		Show_Map = (Button)findViewById(R.id.button1);
		
		Replay_This_Order.setOnClickListener(new ClickEvent());
		Get_New_Lock.setOnClickListener(new ClickEvent());
		Show_Map.setOnClickListener(new ClickEvent());
		
		Get_New_Lock.setText("采集锁信息");
		Get_New_Lock.setTextColor(Color.rgb(0, 0, 0));
		
		Lock_ListView=(ListView)findViewById(R.id.worklistView);
		
		TextView Titale_View=(TextView)findViewById(R.id.Order_Title);
		
		String temp_Str = workID.substring(0, 6);
		
		if (temp_Str.equals("INSERT")) {
			temp_Str = "已完成新建锁列表";
		}
		else if (temp_Str.equals("MAINTE")) {
			temp_Str = "维护工单锁具列表";
		}
		else if (temp_Str.equals("REMOVE")) {
			temp_Str = "拆除工单锁具列表";
		}
		else if (temp_Str.equals("MOVETO")) {
			temp_Str = "移机工单锁具列表";
		}
		Titale_View.setText(temp_Str);
		
		Flash_Lock_List();		//刷新锁列表
					
	}
    
    @SuppressWarnings("resource")
	private void Flash_Lock_List() {
		//刷新锁设备列表
    	int temp_Int = 0;
    	String Lock_Name, Lock_Status;
    	String temp_Str = workID.substring(0, 6);
		
    	Lock_List = new ArrayList<HashMap<String,String >>();
    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		
		cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ?", new String[]{workID});    			    
		if (cursor.moveToNext()){
			temp_Int = 1;
		}
		cursor.close();

		if (temp_Int == 1) {
			Lock_Index = 0;
			cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ?", new String[]{workID});    			    

		    while(cursor.moveToNext()){
	      
		    	Lock_Index++;
	    		HashMap<String,String > map=new HashMap<String,String>();
	    			
	    		Lock_Status = cursor.getString(10);	    	

	    		if (Lock_Status.equals("0")) {
	    			//未完工设备取老设备名
	    			List_Lock_ID[Lock_Index] = cursor.getString(2);		//取未完工设备ID
	    			List_Lock_Finish[Lock_Index] = false;
	    			Lock_Name = cursor.getString(3);
	    			List_Lock_Lng[Lock_Index] = cursor.getString(4);
	    			List_Lock_Lat[Lock_Index] = cursor.getString(5);
	    			map.put("Status", "待处理");
	    		}
	    		else {
	    			//已完工设备取新设备名
	    			if (workID.substring(0, 6).equals("REMOVE")) {
	    				List_Lock_ID[Lock_Index] = cursor.getString(2);
	    			}
	    			else {
	    				List_Lock_ID[Lock_Index] = cursor.getString(9);		//取已完工设备ID
	    			}
	    				    			
	    			List_Lock_Finish[Lock_Index] = true;
	    			Lock_Name = cursor.getString(6);
	    			if (Lock_Name == null) {
	    				Lock_Name = cursor.getString(3);
	    			}
	    			List_Lock_Lng[Lock_Index] = cursor.getString(7);
	    			List_Lock_Lat[Lock_Index] = cursor.getString(8);
	    			map.put("Status", "完成");
	    		}    		
	    		
	    		if (Lock_Name == null) {
	    			Lock_Name = "未命名";
	    		}
	    		else if (Lock_Name.length() == 0) {
	    			Lock_Name = "未命名";
	    		}
	    		List_Lock_Name[Lock_Index] = Lock_Name;
	    		
	    		map.put("index", String.valueOf(Lock_Index));
	  	    	map.put("Name", Lock_Name);
	  	    	Lock_List.add(map);
	    	}
	      
		}
		else {
			//下载工单数据		
			cursor=db.rawQuery("select * from Order_List_Table  where Order_ID = ?", new String[]{workID});    			    
			if (cursor.moveToNext()){
				temp_Str = cursor.getString(5);
			}
			cursor.close();

			if (temp_Str == null) {
				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620E");
				Connect_NMS.Make_Socket_Connect();
			}
			else if (temp_Str.equals("0")) {
				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620E");
				Connect_NMS.Make_Socket_Connect();
			}
			
			   
		}

		db.close();
				
		String[] from=new String[]{"index", "Name", "Status"};
		int[] to=new int[]{R.id.textView1, R.id.textView4, R.id.textView3};
	  	Lock_listAdapter=new SimpleAdapter(context, Lock_List, R.layout.listitem_device, from, to);
	  	Lock_ListView.setAdapter(Lock_listAdapter);
	  	Lock_listAdapter.notifyDataSetChanged();
  	
	  	Lock_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				TextView tv=(TextView)view.findViewById(R.id.textView1);
				String Str_Lock = tv.getText().toString();
				
				int Int_temp = Integer.parseInt(Str_Lock );
				Str_Lock  = List_Lock_Name[Int_temp];
				
				final String Str_Lock_ID = List_Lock_ID[Int_temp];
				final String Str_Lock_Lng = List_Lock_Lng[Int_temp];
				final String Str_Lock_Lat = List_Lock_Lat[Int_temp];
				final String Str_Lock_Name = Str_Lock;
				final String Str_Title = workType;
				
				if (Str_Lock_ID == null) {
					return;
				}
				
				Jast_Open_Tag = List_Lock_Finish[Int_temp];
				//AlertDialog dialog;
				
				double Error_Lng_C = Your_Lng_C - Double.valueOf(Str_Lock_Lng);
	    		double Error_Lat_C = Your_Lat_C - Double.valueOf(Str_Lock_Lat);
	    		
    				 
	    		AlertDialog dialog_1;
	    		
				//dialog = new AlertDialog.Builder(context)
				if (Error_Lng_C > 2*Scope_Lnglat || Error_Lat_C > 2*Scope_Lnglat || Error_Lng_C < -2*Scope_Lnglat || Error_Lat_C < -2*Scope_Lnglat) {
					Lock_ID = List_Lock_ID[Int_temp];
					dialog_1 = new AlertDialog.Builder(context)
 				   		.setTitle("导航到锁设备位置")			//设置对话框的标题		
						.setMessage("锁设备名： " + Str_Lock_Name)
						//设置对话框的按钮
						.setPositiveButton("导航", new DialogInterface.OnClickListener() {
            				@Override
               				public void onClick(DialogInterface dialog, int i) {
            					Toast.makeText(context, "请等待，正在连接百度地图", Toast.LENGTH_SHORT).show();
        						//百度导航
        						new Baidu_Map().Market_BaiduMap(Str_Lock_Lng, Str_Lock_Lat, mactivity, "Baidu");				
            				}
                		})
								
						.create();
 				   	dialog_1.show();
 			   	}
 			   	else {
 			   		//间距符合要求
 			   		int temp_Int = 0;
 			   		if (! BLE_Get_Start_Tag) {
 			   			temp_Int = 1;
 			   		}
 			   		
 			   		if (Lock_ID == null) {
 			   			Lock_ID = Str_Lock_ID;
 			   		}
 			   		else if (Lock_ID.length() == 0) {
 			   			Lock_ID = Str_Lock_ID;
 			   		}			   		
 			   		
 			   		if (temp_Int == 1 && Get_New_Lock.getText().toString().equals("采集锁信息")) {
 			   			if (Str_Title.equals("维护")) {
 			   				dialog_1 = new AlertDialog.Builder(context)
 			   					.setTitle("提示")			//设置对话框的标题		
 			   					.setMessage("请先采集锁设备信息 ")
 			   					//设置对话框的按钮
 			   					.setPositiveButton("确定", null)
 			   					.setNegativeButton("无法读取设备信息",new DialogInterface.OnClickListener() {
 			   						@Override
 			   						public void onClick(DialogInterface dialog, int which) {
 			   							if (Str_Title.equals("维护")) {
 			   								AlertDialog dialog_1 = new AlertDialog.Builder(context)
	 			   								.setTitle("提示")			//设置对话框的标题
	 			   								.setMessage("完成锁体更换后，请读取新锁设备信息")
	 			   								//设置对话框的按钮
	 			   								.setPositiveButton("确定", null)
	 			   								.create();
 			   								dialog_1.show();
	 			   						
 			   								Get_New_Lock.setText("采集新锁信息");
	 			   							Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
 			   							}
 			   						}
 			   					})
			   						
 			   					.create();
 			   				dialog_1.show();
 			   			}
 			   			else {
 			   				dialog_1 = new AlertDialog.Builder(context)
 			   					.setTitle("提示")			//设置对话框的标题		
 			   					.setMessage("请先采集锁设备信息 ")
 			   					//设置对话框的按钮
 			   					.setPositiveButton("确定", null)
 			   					.create();
 			   				dialog_1.show();
 			   			}
 			   			
 			   		}
 			   		else {
 			   			//已连接蓝牙，并获取到锁设备ID
 			   			 			   			
 			   			AlertDialog.Builder dialog =  new   AlertDialog.Builder(Make_NewLock_Activity.this  ); 
 					
 			   			dialog.setTitle("请选取对下述锁具的操作").setMessage(Str_Lock);	//显示锁蓝牙设备名
 			   			//此对话框可设置多个按键
 			   			dialog.setPositiveButton(Str_Title, new DialogInterface.OnClickListener() {
 			   				//点击施工按钮
 			   				@Override
 			   				public void onClick(DialogInterface dialog, int i) {
 			   					AlertDialog dialog_1;
 			   					if (Str_Title.equals("维护")) {
 			   						dialog_1 = new AlertDialog.Builder(context)
 			   							.setTitle("提示")			//设置对话框的标题
 			   							.setMessage("完成锁体更换后，请读取新锁设备信息")
 			   							//设置对话框的按钮
 			   							.setPositiveButton("确定", null)
 			   							.create();
 			   						dialog_1.show();
 			   						
 			   						Get_New_Lock.setText("采集新锁信息");
 			   						Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
 			   					}
 			   					else if (Str_Title.equals("拆除") || Str_Title.equals("移机")) {
 			   						//联通蓝牙，并读锁设备ID
 			   		    		   Read_BLE_Lock_ID();  
 			   						 			   					
 			   					}
 			   				}
 			   			});
 			   			dialog.setNegativeButton("开锁", new DialogInterface.OnClickListener() {
 			   				@Override
 			   				public void onClick(DialogInterface dialog, int which) {
						    						
 			   					//具备临时性的完全权限
 			   					String Recive_Str = "123456 "+ Str_Lock_ID;
 			   					
 			   					Send_BLE_Tag = true;	//发送蓝牙指令标志置位
 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
				
 			   					if (! BLE_Lock_Contrl_Tag) {
 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context)
 			   							.setTitle("提示")			//设置对话框的标题
 			   							.setMessage("直联开锁失败")
 			   							//设置对话框的按钮
 			   							.setPositiveButton("确定", null)
 			   							.create();
 			   						dialog_1.show();
 			   					}
 			   					dialog.dismiss();
 			   				}
 			   			});
 			   			dialog.show();
 			   		}
 			   	}
			}
    		
    	});
	}
    

	//自定义单击事件类
  	class ClickEvent implements View.OnClickListener {    

  		Intent intent = new Intent();   //创建Intent对象 

  		public void onClick(View v) {
  			
  			switch(v.getId()){
	    	   case R.id.Save_Config:
	    		   //联通蓝牙，并读锁设备ID
	    		   Read_BLE_Lock_ID();  
	    		   
	    		   break;
	    		   
	    	   case R.id.Apply_Insert_Tag_btn:
	    		   //回单
	    		   intent.setClass(context, Updata_BackOrder_Activity.class);
	    		   intent.putExtra("workID", workID);
	    		   startActivity(intent);

	    		   break;
	    		   
	    	   case R.id.button1:
	    		   //地图显示
	    		   intent.setClass(context, Baidu_NewLock_Activity.class);
	    		   intent.putExtra("workID", workID);
	    		   intent.putExtra("Type", "Show");		//显示方式
	    		   startActivity(intent);
	    		   
	    		   break;
  			}  
  		}
  	}
  	
  	
  	//定义Handler对象
  	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			String s, Str_Date, Str_Numb, Str_Lng, Str_Lat, Str_ID, Str_Name;

  			switch(msg.what){
  			case 0:
  				//应答分析
  				s=msg.obj.toString();
  				Str_Date = s.substring(0, 6);
  				
  				if (Str_Date.equals("620E O")) {
  					//是工单时限数据
  					Str_Date = s.substring(7,16).trim();

  					if (Str_Date.indexOf(" ") < 0) {
  						//时限中没有空格
  						
  						SQLiteDatabase db=dbHelper.getWritableDatabase();
  						db.execSQL("UPDATE Order_List_Table SET dateLimit = ? WHERE Order_ID = ?", new String[]{Str_Date, workID});		
  						//添加工单时限，并更改工单状态
  						
  						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "下载工单数据", workID);
  						db.close();
  					}
  				}
  				else if (Str_Date.equals("620E L")) {
  					//是工单锁数据
  					Str_Lng = s.substring(7,18);
  					Str_Lat = s.substring(18,28);
  					Str_ID = s.substring(29,63);
  					Str_Name = s.substring(64);
  					
  					SQLiteDatabase db=dbHelper.getWritableDatabase();
					db.execSQL("INSERT INTO Order_Data_Table (Order_ID, Old_Locker_ID, Old_Locker_Name, Old_Locker_Lng, Old_Locker_lat, Status) " +
							"VALUES (?, ?, ?, ?, ?, ?)", new String[]{workID, Str_ID, Str_Name, Str_Lng, Str_Lat, "0"});
					db.close();
					
					Flash_Lock_List();		//刷新锁列表
  				}
  				else if (Str_Date.equals("6217 F") || Str_Date.equals("6217 E")) {
  					//是网管上报平台信息的回复
  					Str_Numb = s.substring(7);
  					Str_Name = workID.substring(0, 6);
  					Intent intent = new Intent();   //创建Intent对象 
  					
  					if (Str_Numb.equals("FF") || Str_Numb.equals("0B")) {
  						//平台注册失败
  						controller.close();		//先关闭蓝牙服务
  						BLE_Get_Start_Tag = false;
  						BLE_Lock_Contrl_Tag = false;
  						
  						AlertDialog dialog_1 = new AlertDialog.Builder(context)
							.setTitle("网管注册NB信息失败")			//设置对话框的标题
							.setMessage("请向网管上报NB注册失败情况，或另行进行设备的NB注册，然后才能施工")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog_1.show();

  					}
  					else if (Str_Numb.equals("00")) {
  						Int_Get_GPS_Tag = 0;			//清除标志位后，新建工单在收到6105应答后，并不进行完工数据保存
  						//完成网管的平台注册，启动设备的NB注册
  						BLE_Lock_Contrl_Tag = false;
  						index_6105 = 2;
  						Send_BLE_Tag = true;	//发送蓝牙指令标志置位
						BLE_Communication.Send_Command_6105(controller, 2);		//发送要求设备进行NB注册
						
  						if (Str_Name.equals("INSERT")) {
  							//以子Activity方式启动百度地图定位，以便回传锁定位位置
  							Int_Get_GPS_Tag = 0;
							intent.setClass(context, Baidu_NewLock_Activity.class);
							intent.putExtra("workID", workID);
							intent.putExtra("Type", "Position");	//定位方式
							startActivityForResult(intent, 1);
  						}
  						else if (Str_Name.equals("MAINTE")) {
  							BLE_Lock_Contrl_Tag = false;
  							
  						}
  					}
  					else if (Str_Numb.equals("44")) {
  						//设备已经注册
  						if (Str_Name.equals("INSERT")) {
  	  						//以子Activity方式启动百度地图定位，以便回传锁定位位置
  							intent.setClass(context, Baidu_NewLock_Activity.class);
  							intent.putExtra("workID", workID);
  							intent.putExtra("Type", "Position");	//定位方式
  							startActivityForResult(intent, 1);
  	  					}
  						else if (Str_Name.equals("MAINTE")) {
  							BLE_Lock_Contrl_Tag = false;
  							index_6105 = 1;
  							Send_BLE_Tag = true;	//发送蓝牙指令标志置位
  							BLE_Communication.Send_Command_6105(controller, 1);		//发送写设备配置标志指令
  						}
  					}
  				}
  				
  				break;
  				
  			case 2:
  				s=msg.obj.toString();

  				if (s.equals("Wait_Send_620E")) {
  					
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.DownLoad_OrderData_620E(workID);		//发送下载工单数据指令
  					
  				}
  				else if (s.equals("Wait_Send_6217 NEW")) {

  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.Send_NB_Information_6217(New_Lock_ID, Lock_imsi);		//发送上传NB信息指令
  				}
  				else if (s.equals("Wait_Send_6217 OLD")) {

  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.Send_NB_Information_6217(Lock_ID, Lock_imsi);		//发送上传NB信息指令

  				}
  				  				
  				break;
  				  				
  			}
  		}
  	};


	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i, temp_Int;
						
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
						                	.setTitle("读锁信息")			//设置对话框的标题
						                	.setMessage(BLE_Name[0])	//显示锁蓝牙设备名
						                	//设置对话框的按钮
						                	.setNegativeButton("取消", null)
						                	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_BLE_Lock_Start(0);
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
						                			Read_BLE_Lock_Start(which);
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
						                			Read_BLE_Lock_Start(which);
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
						                			Read_BLE_Lock_Start(which);
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
						                			Read_BLE_Lock_Start(which);
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
				//收到蓝牙应答
				
				AlertDialog dialog;
				String temp_Str, temp_Str_1, temp_Str_2 = null;
				
				if (Send_BLE_Tag) {
					Send_BLE_Tag = false;		//不接收多余的应答
					
					if (Recive_Str.substring(0, 7).equals("C610200") && ! BLE_Lock_Contrl_Tag) {
						//收到设备ID
						temp_Str_2 = Recive_Str.substring(7, 41);		//取通过蓝牙读到的设备ID
						temp_Str_1 = "Old";
						for (i = 1; i<=Lock_Index; i++) {
							if (temp_Str_2.equals(List_Lock_ID[i]) && List_Lock_Finish[i]) {
								temp_Str_1 = "Finish";
								break;
							}
						}
						
						if (temp_Str_1.equals("Finish")) {
							//已完工锁具，则仅提供开锁功能
							final String Str_Lock_ID = temp_Str_2;
							final Context context_1 = context;
							
							dialog = new AlertDialog.Builder(context)
	                		.setTitle("是否打开此锁 ？")			//设置对话框的标题
	                		.setMessage(BLE_Name[0])	//显示锁蓝牙设备名
	                		//设置对话框的按钮
	                		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	                			@Override
	                			public void onClick(DialogInterface dialog, int which) {
	                				controller.close();		//先关闭蓝牙服务
 			   						BLE_Get_Start_Tag = false;
 			   						BLE_Lock_Contrl_Tag = false;
	                			}
	                		})
	                		.setPositiveButton("开锁", new DialogInterface.OnClickListener() {
	                			@Override
	                			public void onClick(DialogInterface dialog, int which) {
	                				String Recive_Str = "123456 "+ Str_Lock_ID;
	                				
	                				Send_BLE_Tag = true;	//发送蓝牙指令标志置位
	 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
					
	 			   					if (! BLE_Lock_Contrl_Tag) {
	 			   						controller.close();		//先关闭蓝牙服务
	 			   						BLE_Get_Start_Tag = false;
	 			   						BLE_Lock_Contrl_Tag = false;
	 			   					
	 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context_1)
	 			   							.setTitle("提示")			//设置对话框的标题
	 			   							.setMessage("直联开锁失败")
	 			   							//设置对话框的按钮
	 			   							.setPositiveButton("确定", null)
	 			   							.create();
	 			   						dialog_1.show();
	 			   					}
	 			   					dialog.dismiss();
	                			}
	                		}).create();
							dialog.show();
						}
						else if ( work_Status.equals("已回单")){
							controller.close();		//先关闭蓝牙服务
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							
							dialog = new AlertDialog.Builder(context)
								.setTitle("非此工程锁具")			//设置对话框的标题
								.setMessage("您在此无权控制操作此锁 ！")
								//设置对话框的按钮
								.setPositiveButton("确定", null)
								.create();
							dialog.show();
						}
						else {
							//未完工锁具
							Int_Get_GPS_Tag = 0;
							BLE_Lock_Contrl_Tag = true;
							temp_Str_1 = workID.substring(0, 6);
							temp_Str = Recive_Str.substring(73);
							
							if (temp_Str_1.equals("MAINTE") && Get_New_Lock.getText().toString().equals("采集新锁信息")) {
								New_Lock_ID = Recive_Str.substring(7, 41);		//取通过蓝牙读到的设备ID
								Lock_imsi = Recive_Str.substring(41, 73);
								
								if ( temp_Str.equals("00")) {
									//提请上报NB信息
									Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6217 NEW");
									Connect_NMS.Make_Socket_Connect();
									
								}
								else if ( temp_Str.equals("6C")) {
									controller.close();		//先关闭蓝牙服务
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
									
									dialog = new AlertDialog.Builder(context)
										.setTitle("提示")			//设置对话框的标题
										.setMessage("这个锁具已经进行过施工配置。")
										//设置对话框的按钮
										.setPositiveButton("确定", null)
										.create();
									dialog.show();
								}
							}
							else {
								Lock_ID = Recive_Str.substring(7, 41);		//取通过蓝牙读到的设备ID
								Lock_imsi = Recive_Str.substring(41, 73);
								
								if (temp_Str.equals("00") && ! temp_Str_1.equals("REMOVE")) {
									if (temp_Str_1.equals("INSERT")) {
										//提请上报NB信息
										Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6217 OLD");
										Connect_NMS.Make_Socket_Connect();

									}
									else if (temp_Str_1.equals("MAINTE")) {
										controller.close();		//先关闭蓝牙服务
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
										
										dialog = new AlertDialog.Builder(context)
											.setTitle("提示")			//设置对话框的标题
											.setMessage("请选择要维护的锁具。")
											//设置对话框的按钮
											.setPositiveButton("确定", null)
											.create();
										dialog.show();
									}
									else {
										controller.close();		//先关闭蓝牙服务
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
										
										dialog = new AlertDialog.Builder(context)
											.setTitle("提示")			//设置对话框的标题
											.setMessage("此锁不是待施工锁设备！")
											//设置对话框的按钮
											.setPositiveButton("确定", null)
											.create();
										dialog.show();
									}
								}
								else if (temp_Str_1.equals("INSERT")) {
									controller.close();		//先关闭蓝牙服务
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
									
									dialog = new AlertDialog.Builder(context)
										.setTitle("提示")			//设置对话框的标题
										.setMessage("这个锁具已经进行过施工配置。")
										//设置对话框的按钮
										.setPositiveButton("确定", null)
										.create();
									dialog.show();
								}
								else {
									//其它施工，则先查看是否是施工要求的锁具
									temp_Int = 0;
									for (i = 1; i<=Lock_Index; i++) {
										if (Lock_ID.equals(List_Lock_ID[i])) {
											//工单中有这个锁具
											temp_Int = i;
											break;
										}
									}
									
									if (temp_Int == 0) {
										controller.close();		//先关闭蓝牙服务
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
										
										dialog = new AlertDialog.Builder(context)
											.setTitle("提示")			//设置对话框的标题
											.setMessage("此锁不是待施工锁设备！")
											//设置对话框的按钮
											.setPositiveButton("确定", null)
											.create();
										dialog.show();
									}
									else {
										//拆除和移机施工
										final String Str_Lock_Name = List_Lock_Name[temp_Int];
										final String Str_Lock_ID = List_Lock_ID[temp_Int];
										final String Str_Title = workType;
										final Context context_1 = context;
										AlertDialog.Builder dialog_1 =  new   AlertDialog.Builder(Make_NewLock_Activity.this  ); 
						 					
										Jast_Open_Tag = List_Lock_Finish[temp_Int];
										
										if (Jast_Open_Tag) {
											//已完工，则仅只能开锁
											dialog = new AlertDialog.Builder(context_1)
						                		.setTitle("是否打开此锁 ？")			//设置对话框的标题
						                		.setMessage(BLE_Name[0])	//显示锁蓝牙设备名
						                		//设置对话框的按钮
						                		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						                			@Override
						                			public void onClick(DialogInterface dialog, int which) {
						                				controller.close();		//先关闭蓝牙服务
						                				BLE_Get_Start_Tag = false;
						                				BLE_Lock_Contrl_Tag = false;
						                			}
						                		})
						                		.setPositiveButton("开锁", new DialogInterface.OnClickListener() {
						                			@Override
						                			public void onClick(DialogInterface dialog, int which) {
						                				String Recive_Str = "123456 "+ Str_Lock_ID;
						                				
						                				Send_BLE_Tag = true;	//发送蓝牙指令标志置位
						 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
										
						 			   					if (! BLE_Lock_Contrl_Tag) {
						 			   						controller.close();		//先关闭蓝牙服务
						 			   						BLE_Get_Start_Tag = false;
						 			   						BLE_Lock_Contrl_Tag = false;
						 			   				
						 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context_1)
						 			   							.setTitle("提示")			//设置对话框的标题
						 			   							.setMessage("直联开锁失败")
						 			   							//设置对话框的按钮
						 			   							.setPositiveButton("确定", null)
						 			   							.create();
						 			   						dialog_1.show();
						 			   					}
						 			   					dialog.dismiss();
						                			}
						                		}).create();
											dialog.show();
										}
										else {		
											//未完工，可在开锁与维护中进行选择
					 			   			dialog_1.setTitle("请选取对下述锁具的操作").setMessage(Str_Lock_Name);	//显示锁蓝牙设备名
					 			   			//此对话框可设置多个按键
					 			   			dialog_1.setPositiveButton(Str_Title, new DialogInterface.OnClickListener() {
					 			   				//点击施工按钮
					 			   				@Override
					 			   				public void onClick(DialogInterface dialog, int i) {
					 			   					AlertDialog dialog_1;
					 			   					if (Str_Title.equals("维护")) {
					 			   						controller.close();		//先关闭蓝牙服务
					 			   						BLE_Get_Start_Tag = false;
					 			   						BLE_Lock_Contrl_Tag = false;
					 			   				
					 			   						dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("提示")			//设置对话框的标题
					 			   							.setMessage("完成锁体更换后，请读取新锁设备信息")
					 			   							//设置对话框的按钮
					 			   							.setPositiveButton("确定", null)
					 			   							.create();
					 			   						dialog_1.show();
					 			   						
					 			   						Get_New_Lock.setText("采集新锁信息");
					 			   						Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
					 			   					}
					 			   					else if (Str_Title.equals("拆除")) {
					 			   						
					 			   						dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("您确定要拆除此锁 ？")			//设置对话框的标题
					 			   							.setMessage("锁设备名： " + Str_Lock_Name)
					 			   							//设置对话框的按钮
					 			   							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					 			    			   				@Override
					 			    			   				public void onClick(DialogInterface dialog, int which) {
					 			   						    		//保存拆机信息

					 			    			   					New_Lock_ID = "";
					 			    			   					
					 			            						int temp_Int = 0;
					 			            						SQLiteDatabase db=dbHelper.getWritableDatabase();
					 			            						
					 			            						db.execSQL("UPDATE Order_Data_Table SET Status = '1' WHERE Order_ID = ? AND Old_Locker_ID = ?", new String[]{workID, Lock_ID});
					 			            						
					 			            						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "完成对锁具的拆除操作", Str_Lock_Name);
					 			            						
					 			            						Send_BLE_Tag = true;	//发送蓝牙指令标志置位
					 			            						BLE_Communication.Send_Command_6105(controller, 0);		//发送锁设备配置重置指令
					 			            						
					 			            						Cursor cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Status = '0'", new String[]{workID});    			    
					 			            						if (cursor.moveToNext()){
					 			            							temp_Int = 1;
					 			            						}
					 			            						cursor.close();
					 			            						
					 			            						AlertDialog dialog_1;
					 			            						if (temp_Int == 0) {
					 			            							//全部完工
					 			            							db.execSQL("UPDATE Order_List_Table SET Status = '3' WHERE Order_ID = ?", new String[]{workID});		//更改工单总列表状态
					 			            						
					 			            							dialog_1 = new AlertDialog.Builder(context_1)
					 			            								.setTitle("当前工单全部完工")			//设置对话框的标题
					 			            								.setMessage("请尽快回单 ！")
					 			            								//设置对话框的按钮
					 			            								.setPositiveButton("确定", null)
					 			            								.create();
					 			            							dialog_1.show();
					 			            						
					 			            						}
					 			            						else {
					 			            							db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//更改工单总列表状态
					 			            						
					 			            							dialog_1 = new AlertDialog.Builder(context_1)
					 		            									.setTitle("拆除成功")			//设置对话框的标题
					 		            									.setMessage("当请处理下一个工单设备。")
					 		            									//设置对话框的按钮
					 		            									.setPositiveButton("确定", null)
					 		            									.create();
					 			            							dialog_1.show();
					 			            						}
					 			            						
					 			            						db.close();
					 			            						
					 			            						Flash_Lock_List();		//刷新锁列表
					 			    			   				}
					 			    			   			})
					 			   									
					 			   							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					 			    			   				@Override
					 			    			   				public void onClick(DialogInterface dialog, int which) {
					 			    			   					controller.close();		//先关闭蓝牙服务
					 			    			   					BLE_Get_Start_Tag = false;
					 			    			   					BLE_Lock_Contrl_Tag = false;
					 			    			   				}
					 			    			   			})
					 			   							.create();
					 			   						dialog_1.show();
					 			   					}
					 			   					else if (Str_Title.equals("移机")) {
					 			   						dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("设备移机： " + Str_Lock_Name)			//设置对话框的标题
					 			   							.setMessage("请将此锁设备安装到新位置后，重新进行地图定位")
					 			   							//设置对话框的按钮
					 			   							.setPositiveButton("确定", null)
					 			   							.create();
					 			   						dialog_1.show();
					 			   						
					 			   						//以子Activity方式启动百度地图定位，以便回传锁定位位置
					 			   						Intent intent = new Intent();   //创建Intent对象 
					 			   					
					 									intent.setClass(context_1, Baidu_NewLock_Activity.class);
					 									intent.putExtra("workID", workID);
					 									intent.putExtra("Type", "Position");	//定位方式
					 									startActivityForResult(intent, 1);
					 			   					}
					 			   				}
					 			   			});
					 			   			dialog_1.setNegativeButton("开锁", new DialogInterface.OnClickListener() {
					 			   				@Override
					 			   				public void onClick(DialogInterface dialog, int which) {
											    						
					 			   					//具备临时性的完全权限
					 			   					String Recive_Str = "123456 "+ Str_Lock_ID;
					 			   					
					 			   					Send_BLE_Tag = true;	//发送蓝牙指令标志置位
					 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
									
					 			   					if (! BLE_Lock_Contrl_Tag) {
					 			   						controller.close();		//先关闭蓝牙服务
					 			   						BLE_Get_Start_Tag = false;
					 			   						BLE_Lock_Contrl_Tag = false;
					 			   				
					 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("提示")			//设置对话框的标题
					 			   							.setMessage("直联开锁失败")
					 			   							//设置对话框的按钮
					 			   							.setPositiveButton("确定", null)
					 			   							.create();
					 			   						dialog_1.show();
					 			   					}
					 			   					dialog.dismiss();
					 			   				}
					 			   			});
					 			   			dialog_1.show();
										}
									}
								}
							}
						}
					}
					else if (Recive_Str.substring(0, 9).equals("C61050000") && ! BLE_Lock_Contrl_Tag) {
						//写锁配置成功，保存数据
						BLE_Lock_Contrl_Tag = true;
											
						temp_Str = workID.substring(0, 6);
						if (temp_Str.equals("INSERT") && Int_Get_GPS_Tag == 1 && index_6105 == 1) {
							SQLiteDatabase db=dbHelper.getWritableDatabase();
							
							db.execSQL("DELETE FROM Order_Data_Table WHERE Order_ID = ? AND New_Locker_ID = ?", new String[]{workID, Lock_ID});
							
							db.execSQL("INSERT INTO Order_Data_Table (Order_ID, New_Locker_ID, New_Locker_Name, New_Locker_Lng, New_Locker_lat, Status, New_Lock_imsi) " +
									"VALUES (?, ?, ?, ?, ?, ?, ?)", new String[]{workID, Lock_ID, Get_Lock_Name, String.valueOf(Get_Lock_Lng), String.valueOf(Get_Lock_Lat), "1", Lock_imsi});
							
							db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//更改工单总列表状态

							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "完成对锁具的新建配置", Get_Lock_Name);

							db.close();
							
							dialog = new AlertDialog.Builder(context)
								.setTitle("配置成功")			//设置对话框的标题
								.setMessage("当前锁设备配置完成，可继续配置其它锁具，或回单。")
								//设置对话框的按钮
								.setPositiveButton("确定", null)
								.create();
							dialog.show();
						}
						else if (temp_Str.equals("MAINTE") && index_6105 == 1) {
							temp_Int = 0;
							String str_lng = "0", str_lat = "0";
							
							SQLiteDatabase db=dbHelper.getWritableDatabase();
							Cursor cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Old_Locker_ID = ?", new String[]{workID, Lock_ID});    			    
							if (cursor.moveToNext()){
								Get_Lock_Name = cursor.getString(3);
								str_lng = cursor.getString(4);
								str_lat = cursor.getString(5);
							}
							cursor.close();

							db.execSQL("UPDATE Order_Data_Table SET Status = '1', New_Locker_Name = ?, New_Locker_Lng = ?, New_Locker_Lat = ?, New_Locker_ID = ?, New_Lock_imsi = ?" +
									" WHERE Order_ID = ? AND Old_Locker_ID = ?", new String[]{Get_Lock_Name, str_lng, str_lat, New_Lock_ID, Lock_imsi, workID, Lock_ID});
							
							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "完成对锁具的维护更新", Get_Lock_Name);
							
							temp_Int = 0;
							cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Status = '0'", new String[]{workID});    			    
							if (cursor.moveToNext()){
								temp_Int = 1;
							}
							cursor.close();

							if (temp_Int == 0) {
								//全部完工
								db.execSQL("UPDATE Order_List_Table SET Status = '3' WHERE Order_ID = ?", new String[]{workID});		//更改工单总列表状态
							}
							else {
								db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//更改工单总列表状态
							}
							
							db.close();
							
							if (temp_Int == 0) {
								//全部完工
								dialog = new AlertDialog.Builder(context)
									.setTitle("当前工单全部完工")			//设置对话框的标题
									.setMessage("请尽快回单 ！")
									//设置对话框的按钮
									.setPositiveButton("确定", null)
									.create();
								dialog.show();
							}
							else {
								dialog = new AlertDialog.Builder(context)
									.setTitle("锁维护成功")			//设置对话框的标题
									.setMessage("当请处理下一个工单设备。")
									//设置对话框的按钮
									.setPositiveButton("确定", null)
									.create();
								dialog.show();
							}
							
							Get_New_Lock.setText("采集锁信息");
							Get_New_Lock.setTextColor(Color.rgb(0, 0, 0));
							
						}
						else if (temp_Str.equals("MAINTE") && index_6105 == 2) {
							BLE_Lock_Contrl_Tag = false;
  							index_6105 = 1;
  							Send_BLE_Tag = true;	//发送蓝牙指令标志置位
  							BLE_Communication.Send_Command_6105(controller, 1);		//发送写设备配置标志指令
						}
						
						controller.close();
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
											
						Flash_Lock_List();		//刷新锁列表
						
					}
					else if (Recive_Str.substring(0, 7).equals("C610500") && ! Recive_Str.substring(7).equals("00")) {
						//写锁配置失败
						controller.close();		//先关闭蓝牙服务
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						Get_Lock_Lat = 0;
						Get_Lock_Lng = 0;
						Get_Lock_Name = ""; 
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("配置出错")			//设置对话框的标题
							.setMessage("当前锁设备配置失败，请重试 ！")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog.show();					
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000") && workID.substring(0, 6).equals("MAINTE") && !Jast_Open_Tag) {
						controller.close();		//先关闭蓝牙服务
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("请维修锁具")			//设置对话框的标题
							.setMessage("维修完成后请激活锁具，读取新锁具信息 ！")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog.show();

						Get_New_Lock.setText("采集新锁信息");
						Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
					}
					else if (!BLE_Lock_Contrl_Tag) {
						controller.close();		//先关闭蓝牙服务
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("提示")			//设置对话框的标题
							.setMessage("蓝牙通信故障 ！")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog.show();
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000")) {
						controller.close();			//关闭蓝牙连接
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
				}
				else {
					//多余的回复
					if (Recive_Str.substring(0, 7).equals("C610200")) {
						temp_Str_2 = Recive_Str.substring(7, 41);		//取通过蓝牙读到的设备ID
						temp_Str_1 = "Old";
						for (i = 1; i<=Lock_Index; i++) {
							if (temp_Str_2.equals(List_Lock_ID[i]) && List_Lock_Finish[i]) {
								temp_Str_1 = "Finish";
								break;
							}
						}
						
						if (temp_Str_1.equals("Finish")) {
							//已完工锁具，则仅提供开锁功能
							
						}
						else if ( work_Status.equals("已回单")){
							controller.close();		//先关闭蓝牙服务
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
						}
						else {
							//未完工锁具
							Int_Get_GPS_Tag = 0;
							BLE_Lock_Contrl_Tag = true;
							temp_Str_1 = workID.substring(0, 6);
							temp_Str = Recive_Str.substring(73);
							
							if (temp_Str_1.equals("MAINTE") && Get_New_Lock.getText().toString().equals("采集新锁信息")) {
								if ( temp_Str.equals("6C")) {
									controller.close();		//先关闭蓝牙服务
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
								}
							}
							else {
								
								if (temp_Str.equals("00")) {
									if (temp_Str_1.equals("INSERT")) {

									}
									else if (temp_Str_1.equals("MAINTE")) {
										controller.close();		//先关闭蓝牙服务
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
									}
									else {
										controller.close();		//先关闭蓝牙服务
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
									}
								}
								else if (temp_Str_1.equals("INSERT")) {
									controller.close();		//先关闭蓝牙服务
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
								}
								else {
									//其它施工，则先查看是否是施工要求的锁具
									temp_Int = 0;
									for (i = 1; i<=Lock_Index; i++) {
										if (Lock_ID.equals(List_Lock_ID[i])) {
											//工单中有这个锁具
											temp_Int = i;
											break;
										}
									}
									
									if (temp_Int == 0) {
										controller.close();		//先关闭蓝牙服务
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
									}
								}
							}
						}
					}
					else if (Recive_Str.substring(0, 7).equals("C610500")) {
						controller.close();			//关闭蓝牙连接
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000") && workID.substring(0, 6).equals("MAINTE") && !Jast_Open_Tag) {
						controller.close();			//关闭蓝牙连接
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
					else if (!BLE_Lock_Contrl_Tag) {
						controller.close();			//关闭蓝牙连接
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000")) {
						controller.close();			//关闭蓝牙连接
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
				}
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_LOSS_DEVICE_CHARACT)){
				//未能获取到蓝牙通信特征值
				if (controller != null) {
					controller.close();		//先关闭蓝牙服务
					BLE_Get_Start_Tag = false;
					BLE_Lock_Contrl_Tag = false;
				}
				
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
    
	public void Read_BLE_Lock_ID() {
		BLE_Get_Start_Tag = false;
		BLE_Lock_Contrl_Tag = false;
		controller.close();		//先关闭现有服务
		BLE_Get_Start_Tag = false;
		BLE_Lock_Contrl_Tag = false;
		
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
	
	
	
    private void Read_BLE_Lock_Start(int this_BLE_Numb) {
		//启动读锁ID指令发送
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
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
    		//百度站点定位成功
    		Uri horse = data.getData();

    		//调用的回传设备名与GPS坐标
			Get_Lock_Lat = data.getDoubleExtra("Lat", 0);
			Get_Lock_Lng = data.getDoubleExtra("Lng", 0);
			Get_Lock_Name = data.getStringExtra("Name");

			String temp_Str = workID.substring(0, 6);
			if (temp_Str.equals("MAINTE")) {
				//维护工单可通过地图选定待维护的设备ID (锁具损坏时，可能无法通过蓝牙读到锁具ID)
				Lock_ID = data.getStringExtra("Name");
				
				Get_New_Lock.setText("采集新锁信息");
				Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
			}
			else if (temp_Str.equals("INSERT")) {
				//蓝牙写锁配置标志
				Int_Get_GPS_Tag = 1;			//GPS定位标志置位
				Get_Lock_Name = data.getStringExtra("Name");
				BLE_Lock_Contrl_Tag = false;
				
				index_6105 = 1;
				Send_BLE_Tag = true;	//发送蓝牙指令标志置位
				BLE_Communication.Send_Command_6105(controller, 1);		//发送写设备配置标志指令
			}
			else if (temp_Str.equals("MOVETO")) {
				//完成移机，并保存数据
				Get_Lock_Name = data.getStringExtra("Name");
				int temp_Int = 0;
				SQLiteDatabase db=dbHelper.getWritableDatabase();

				db.execSQL("UPDATE Order_Data_Table SET Status = '1', New_Locker_Name = ?, New_Locker_Lng = ?, New_Locker_Lat = ?, New_Locker_ID = ?" +
						" WHERE Order_ID = ? AND Old_Locker_ID = ?", new String[]{Get_Lock_Name, String.valueOf(Get_Lock_Lng), String.valueOf(Get_Lock_Lat), Lock_ID, workID, Lock_ID});
				
				DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "完成对锁具的移机操作", Get_Lock_Name);
				
				Cursor cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Old_Locker_ID = ? AND Status = '0'", new String[]{workID, Lock_ID});    			    
				if (cursor.moveToNext()){
					temp_Int = 1;
				}
				cursor.close();
				
				AlertDialog dialog;
				if (temp_Int == 0) {
					//全部完工
					db.execSQL("UPDATE Order_List_Table SET Status = '3' WHERE Order_ID = ?", new String[]{workID});		//更改工单总列表状态
					
					dialog = new AlertDialog.Builder(context)
						.setTitle("当前工单全部完工")			//设置对话框的标题
						.setMessage("请尽快回单 ！")
						//设置对话框的按钮
						.setPositiveButton("确定", null)
						.create();
					dialog.show();
				}
				else {
					db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//更改工单总列表状态
				
					dialog = new AlertDialog.Builder(context)
						.setTitle("移机成功")			//设置对话框的标题
						.setMessage("当请处理下一个工单设备。")
						//设置对话框的按钮
						.setPositiveButton("确定", null)
						.create();
					dialog.show();
				}
				
				db.close();
				
			}
    	}
    	
    }
    
    class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Close_BackOrder")) {
				//完成回单，且页面被关闭
				dbHelper.close();
				
				Intent in=new Intent(); 
		    	in.setAction("Flash_Order_List"); 		//发送刷新工单列表广播
		    	sendBroadcast(in);
		    	
				finish();
			}
		}	
    }
    
    //显示经纬度信息  
   	private void showLocation(final Location location) {  

   		LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
		
		com.baidu.mapapi.utils.CoordinateConverter converter  = new com.baidu.mapapi.utils.CoordinateConverter();    
       	converter.from(CoordType.GPS);    
       	// sourceLatLng待转换坐标    	
       	converter.coord(ll);    
       	ll= converter.convert(); 

    	Your_Lng_C = ll.longitude;
    	Your_Lat_C = ll.latitude;
   		
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
    
	protected void onResume() {
//
		super.onResume();
		Flash_Lock_List();		//刷新锁列表
	}
    
	protected void onDestroy() {

		super.onDestroy();
		dbHelper.close();
		controller.close();

		unregisterReceiver(mReceiver);
	}

}
