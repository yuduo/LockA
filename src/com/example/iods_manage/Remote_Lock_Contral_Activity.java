package com.example.iods_manage;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;


public class Remote_Lock_Contral_Activity    extends Activity {
	//远程开锁控制
	
	private Spinner Nearby_Locker_List;
	List<String> list_Name = new ArrayList<String>();
	int Selected_Lock_index;
	
	String[] Lock_Lng = new String[100];	//锁经度
	String[] Lock_Lat = new String[100];	//锁纬度
	String[] Lock_ID = new String[100];		//锁ID
	String[] Lock_Name = new String[100];	//锁名
	boolean[] Lock_Error = new boolean[100];//锁告警状态
	int Numb_Lock = 0, Send_Locks;			//锁数量
	
	Double Db_Lng = 0.0, Db_Lat = 0.0;
	String Help_Lock;
	
	DBHelper dbHelper ;//数据库服务对象
	
	private Context context = Remote_Lock_Contral_Activity.this;//定义Context对象
	
	static NMS_Communication Connect_NMS;
	
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_open_locker);
	     setTitle("亨通光电");
	     
	     dbHelper = new DBHelper(context);	//创建DBHlper对象实例
	     
	     initi();
	        
	}
	
	public void initi(){
    	Button NB_Lock_Contral = (Button)findViewById(R.id.button1);		//网络开锁
    	Button Show_Baidu_GIS = (Button)findViewById(R.id.button2);			//地图查锁
    	Button BT_Lock_Contral = (Button)findViewById(R.id.button3);		//蓝牙开锁
    	Button Help_me_Open = (Button)findViewById(R.id.button4);			//开锁求助
    	Button Btn_Lock_Query = (Button)findViewById(R.id.button5);			//设备查询
    	
    	TextView Show_GPS_Data=(TextView)findViewById(R.id.textView7);
    	TextView Show_GPS_Title=(TextView)findViewById(R.id.textView4);
    	
    	//为各功能按键添加单击事件监听
    	Show_Baidu_GIS.setOnClickListener(new ClickEvent());
    	Help_me_Open.setOnClickListener(new ClickEvent());	
    	
    	Help_me_Open.setText("远程开锁");
    	NB_Lock_Contral.setVisibility(View.INVISIBLE);
    	BT_Lock_Contral.setVisibility(View.INVISIBLE);
    	Show_GPS_Data.setVisibility(View.INVISIBLE);
    	Show_GPS_Title.setVisibility(View.INVISIBLE);
    	Btn_Lock_Query.setVisibility(View.INVISIBLE);
    		
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
    	
    	Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6213");
		Connect_NMS.Make_Socket_Connect();
    }
	
	
	//自定义单击事件类
    class ClickEvent implements View.OnClickListener {    
    	
	       @SuppressLint("NewApi") @Override    
	       public void onClick(View v) {

	    	   switch(v.getId()){
	    	   case R.id.button2:
	    		   //开启百度地图查找中心点附近锁设备位置
	    		   if (Send_Locks > 0) {
	    			   Intent in=new Intent(context, BaidumapActivity.class);	
		    		   in.putExtra("Mode", "Remote");

		    		   in.putExtra("GPS_Lng", Lock_Lng[Selected_Lock_index]);
		    		   in.putExtra("GPS_Lat", Lock_Lat[Selected_Lock_index]);
		    		   
		    		   startActivity(in);//修改为节点地图显示的测试类
	    		   }
	    		   
	    		   break;

	    	   case R.id.button4:
	    		   //发送远程开锁指令
	    		   if (Send_Locks > 0) {
	    			   SQLiteDatabase db=dbHelper.getWritableDatabase();
		    		   String temp_User = UserLogin_Activity.Login_User_ID;
		    		   	 		   		    		
		    		   Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, Lock_ID[Selected_Lock_index]});
		    		   if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin")){
		    			   //用户有权或超级用户
		    			   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
	    				   Connect_NMS.Make_Socket_Connect();
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
	    	   }
	       }
	}
    
  //定义Handler对象
  	Handler mHandler = new Handler(){
  		@SuppressLint({ "HandlerLeak", "NewApi" }) public void handleMessage(Message msg){
  			String s, Str_Date, Str_Lat = null;
  			AlertDialog dialog;

  			switch(msg.what){
  			case 0:
  				//应答分析
  				s=msg.obj.toString();
  				Str_Date = s.substring(0, 4);
  				
  				if (Str_Date.equals("6213")) {
  					if (s.substring(5, 6).equals("E")) {
  						dialog = new AlertDialog.Builder(context)
  							.setTitle("提示")			//设置对话框的标题
  							.setMessage("通信故障！")	//显示锁蓝牙设备名
  							//设置对话框的按钮
  							.setPositiveButton("确定", null)
  							.create();
  						dialog.show();
  					}
  					
  					if (s.substring(7, 8).equals("N")) {
  						Send_Locks = Integer.valueOf(s.substring(9));
  						Numb_Lock = 0;
  						
  						Selected_Lock_index = 0;
  						
  						if (Send_Locks == 0) {
  							dialog = new AlertDialog.Builder(context)
  								.setTitle("提示")			//设置对话框的标题
  								.setMessage("没有开锁求助请求！")	//显示锁蓝牙设备名
  								//设置对话框的按钮
  								.setPositiveButton("确定", null)
  								.create();
  							dialog.show();
  						}
  					}
  					else if (s.substring(7, 8).equals("D")) {
  						Str_Date = s.substring(9);
  						
  						if (Str_Date.length() > 57) {
  							Lock_Lng[Numb_Lock] = Str_Date.substring(0, 10);
  							Lock_Lat[Numb_Lock] = Str_Date.substring(11, 21);
  							Lock_ID[Numb_Lock] = Str_Date.substring(22, 56);	
  							Lock_Name[Numb_Lock] = Str_Date.substring(57);	
  							
  							list_Name.add(Lock_Name[Numb_Lock]);
								
  							final ArrayAdapter<String> sAdapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_Name);
  							sAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //定义每一个item的样式
  							Nearby_Locker_List.setAdapter(sAdapter1);

  							Numb_Lock++;
  							
  						}
  						else {
  							dialog = new AlertDialog.Builder(context)
	  							.setTitle("提示")			//设置对话框的标题
	  							.setMessage("通信故障！")	//显示锁蓝牙设备名
	  							//设置对话框的按钮
	  							.setPositiveButton("确定", null)
	  							.create();
							dialog.show();
  						}
  					}
  				}
  				else if (Str_Date.equals("6202")) {
					//是开锁控制的应答
  					Str_Date = s.substring(5);
					
					if (Str_Date.equals("00")) {
						dialog = new AlertDialog.Builder(context)
	            			.setTitle("提示")			//设置对话框的标题
	            			.setMessage("开锁成功 ！")	//显示锁蓝牙设备名
	            			//设置对话框的按钮
	            			.setPositiveButton("确定", null)
	            			.create();
						dialog.show();
						

				    	SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "远程协助开锁成功", Lock_Name[Selected_Lock_index]);
						db.close();
						
					}
					else if (Str_Date.equals("01")) {
						dialog = new AlertDialog.Builder(context)
            				.setTitle("提示")			//设置对话框的标题
            				.setMessage("正在开锁")	//显示锁蓝牙设备名
            				//设置对话框的按钮
            				.setPositiveButton("确定", null)
            				.create();
						dialog.show();
					}
					else if (Str_Date.equals("06")) {
						dialog = new AlertDialog.Builder(context)
        					.setTitle("提示")			//设置对话框的标题
        					.setMessage("通信故障")	//显示锁蓝牙设备名
        					//设置对话框的按钮
        					.setPositiveButton("确定", null)
        					.create();
						dialog.show();
					}
					else {
						dialog = new AlertDialog.Builder(context)
        					.setTitle("提示")			//设置对话框的标题
        					.setMessage("开锁失败 ！")	//显示锁蓝牙设备名
        					//设置对话框的按钮
        					.setPositiveButton("确定", null)
        					.create();
						dialog.show();
					}
					
				}
  				break;
  				
  			case 2:
  				s=msg.obj.toString();
  				
  				if (s.equals("Wait_Send_6213")) {
  					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Read_Help_Lock_6213(UserLogin_Activity.Login_User_ID);  
			    	
  				}
  				else if (s.equals("Wait_Send_6202")) {
  					//远程开锁
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Open_NB_Lock_6202(UserLogin_Activity.Login_User_ID, Lock_ID[Selected_Lock_index]);  
			    	
				}
  				else if (s.equals("Wait_Send_6206")) {
					Numb_Lock = 0;
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.GIS_Query_6206(Db_Lat, Db_Lng, 100*UserLogin_Activity.error_Limit);

				}
  				
  				break;
  			}
  		}

		
  	};
  	
  	@Override
	protected void onResume() {

		super.onResume();

	}
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i("前调用", "onDestroy()");		
		dbHelper.close();
	}
  	
}
