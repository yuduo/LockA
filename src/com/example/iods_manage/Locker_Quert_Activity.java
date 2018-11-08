package com.example.iods_manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_common.Baidu_Map;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Locker_Quert_Activity extends Activity {
	//锁具查询页面
	private Context context = Locker_Quert_Activity.this;//定义Context对象
	
	private Locker_Quert_Activity mactivity;
	
	static NMS_Communication Connect_NMS;
	
	String[] List_Lock_Name = new String[200];
	String[] List_Lock_Lng = new String[200];
	String[] List_Lock_Lat = new String[200];
	int Lock_Index;
	
	String Str_Sel_Name, Str_Sel_Lng, Str_Sel_Lat, Str_Sel_ID, Selected_Tag;
	
	Double Er_Lnglat = UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //搜锁范围
	
	private ListView Lock_ListView;
	private SimpleAdapter Lock_listAdapter;				//新建锁设备列表适配器
	private List<HashMap<String,String>> Lock_List;		//新建锁设备列表数据源
	
	RadioButton RadioButton_Name;
	EditText Query_Str;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.activity_lock_query);
        
        Intent in=getIntent();

        mactivity = this;
        
        initi();
	        
	}
	
	 public void initi(){
		 Button Btn_Query_Lock = (Button)findViewById(R.id.button1);
		 
		 Btn_Query_Lock.setOnClickListener(new ClickEvent());
		 
		 Lock_ListView=(ListView)findViewById(R.id.worklistView);
		 
		 RadioButton_Name = (RadioButton)findViewById(R.id.radioButton2);  
		 RadioButton_Name.setChecked(true);
		 
		 Query_Str = (EditText)findViewById(R.id.editText1);	
	 }
	 
	 private void Flash_Lock_List() {
			//刷新锁设备列表
	    	int i;
			
	    	Lock_List = new ArrayList<HashMap<String,String >>();
	    	
	    	for (i = 0; i<Lock_Index; i++) {
	    		HashMap<String,String > map=new HashMap<String,String>();
	    		map.put("index", String.valueOf(i));
	  	    	map.put("Name", List_Lock_Name[i]);
	  	    	map.put("Status", "");
	  	    	Lock_List.add(map);
	    	}
	    			
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
					
					final String Str_Lock_Lng = List_Lock_Lng[Int_temp];
					final String Str_Lock_Lat = List_Lock_Lat[Int_temp];
		    		
					Str_Sel_Name = Str_Lock;
	    			if (RadioButton_Name.isChecked()) {
	    				
	    				Str_Lock = "锁设备名： " + Str_Lock;
	    			}
	    			else {
	    				Str_Lock = "锁设备资源编码： " + Str_Lock;
	    			}
	    			
		    		AlertDialog dialog_1;
		    		
		    		if (RadioButton_Name.isChecked() && UserLogin_Activity.Login_User_Type.equals("Admin")) {
		    			Str_Sel_Lng = Str_Lock_Lng;
		    			Str_Sel_Lat = Str_Lock_Lat;
		    			Str_Sel_ID = "";
		    			
		    			//取设备ID
		    			Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6206");
        				Connect_NMS.Make_Socket_Connect();
		    			
		    			dialog_1 = new AlertDialog.Builder(context)
                		.setTitle("请选择处理方式")
                		.setMessage("您需要进行远程开锁演示吗 ？ ")
                		//设置对话框的按钮
                		.setNegativeButton("远程开锁", new DialogInterface.OnClickListener() {
                			@Override
                			public void onClick(DialogInterface dialog, int which) {
                				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
                				Connect_NMS.Make_Socket_Connect();
                			}
                		})
                		.setPositiveButton("百度导航", new DialogInterface.OnClickListener() {
                			@Override
                			public void onClick(DialogInterface dialog, int i) {
				   				Toast.makeText(context, "请等待，正在连接百度地图", Toast.LENGTH_SHORT).show();
				   				//百度导航
				   				new Baidu_Map().Market_BaiduMap(Str_Lock_Lng, Str_Lock_Lat, mactivity, "Baidu");				
				   			}
                		}).create();
						dialog_1.show();
		    		}
		    		else {
		    			dialog_1 = new AlertDialog.Builder(context)
				   		.setTitle("导航到锁设备位置")			//设置对话框的标题		
				   		.setMessage(Str_Lock)
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
		    		
				}
	    		
	    	});
		}
	    




		//自定义单击事件类
	  	class ClickEvent implements View.OnClickListener {    

	  		Intent intent = new Intent();   //创建Intent对象 

	  		public void onClick(View v) {
	  			
	  			switch(v.getId()){
		    	   case R.id.button1:
		    		   //查询
		    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620F");
		    		   Connect_NMS.Make_Socket_Connect();
						
		    		   break;
	  			}  
	  		}
	  	}
	  	
	  	
	  	//定义Handler对象
	  	Handler mHandler = new Handler(){
	  		public void handleMessage(Message msg){
	  			String s, Str_Date, Str_Numb, Str_Lng, Str_Lat, Str_ID, Str_Name, temp_Str;
	  			int Total_Locks;

	  			switch(msg.what){
	  			case 0:
	  				//应答分析
	  				s=msg.obj.toString();
	  				Str_Date = s.substring(0, 6);
	  				
	  				if (Str_Date.equals("620F N")) {
	  					//是查询到的锁数量数据
	  					Str_Date = s.substring(7).trim();

	  				}
	  				else if (Str_Date.equals("620F L")) {
	  					//是锁数据
	  					Str_Lng = s.substring(7,18);
	  					Str_Lat = s.substring(18,28);
	  					Str_Name = s.substring(29);

	  					List_Lock_Name[Lock_Index] = Str_Name;
	  					List_Lock_Lng[Lock_Index] = Str_Lng;
	  					List_Lock_Lat[Lock_Index] = Str_Lat;
	  					Lock_Index ++;
						
						Flash_Lock_List();		//刷新锁列表
	  				}
	  				else if (s.substring(0, 4).equals("6202")) {
						//是开锁控制的应答
						temp_Str = s.substring(5);
						
						if (temp_Str.equals("00")) {
							AlertDialog dialog = new AlertDialog.Builder(context)
		            			.setTitle("提示")			//设置对话框的标题
		            			.setMessage("远程开锁成功 ！")	//显示锁蓝牙设备名
		            			//设置对话框的按钮
		            			.setPositiveButton("确定", null)
		            			.create();
							dialog.show();

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
	  				else if (s.substring(0, 4).equals("6206")) {
	  					if (s.substring(5, 6).equals("N")) {
							Total_Locks = Integer.valueOf(s.substring(7));
							Selected_Tag = "";
						}
						else {
							//是GIS查询的应答
							Str_Lng = s.substring(7, 17);
							Str_Lat = s.substring(18, 28);
							Str_ID = s.substring(29, 63);	
							Str_Name = s.substring(64);	
							
							if (Str_Name.equals(Str_Sel_Name)) {
								if (Selected_Tag.length() == 0) {
									Str_Sel_ID = Str_ID;
								}

								if (Str_Lng.equals(Str_Sel_Lng) && Str_Lat.equals(Str_Sel_Lat)) {
									Str_Sel_ID = "Selected";
								}
							}
						}
	  				}
	  				
	  				break;
	  				
	  			case 2:
	  				s=msg.obj.toString();

	  				if (s.equals("Wait_Send_620F")) {
	  					Lock_Index = 0;
	  					Connect_NMS.Wait_Recive_TCP_Reply();	
	  			    	NMS_Communication.DownLoad_Lock_Query_620F(Query_Str.getText().toString(), RadioButton_Name.isChecked());		//发送下载工单数据指令
	  					
	  				}
	  				else if (s.equals("Wait_Send_6202")) {
	  					if (Str_Sel_ID.length() > 0) {
	  						Connect_NMS.Wait_Recive_TCP_Reply();	
					    	NMS_Communication.Open_NB_Lock_6202(UserLogin_Activity.Login_User_ID, Str_Sel_ID);  
	  					}
	  					else {
	  						AlertDialog dialog_1 = new AlertDialog.Builder(context)
	   							.setTitle("提示")			//设置对话框的标题
	   							.setMessage("尚未获取到设备ID，请再次尝试远程开锁")
	   							.setPositiveButton("远程开锁", new DialogInterface.OnClickListener() {
	   								@Override
	   								public void onClick(DialogInterface dialog, int which) {
	   									Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
	   									Connect_NMS.Make_Socket_Connect();
	   								}
	   							})
	   							.create();
	   						dialog_1.show();
	  					}						
					}
	  				else if (s.equals("Wait_Send_6206")) {
						
						Connect_NMS.Wait_Recive_TCP_Reply();	
				    	NMS_Communication.GIS_Query_6206(Double.valueOf(Str_Sel_Lat), Double.valueOf(Str_Sel_Lng), Er_Lnglat);

					}
	  				
	  				break;
	  			}
	  		}
	  	};
}
