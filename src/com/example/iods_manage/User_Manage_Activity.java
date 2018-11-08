package com.example.iods_manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;



import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class User_Manage_Activity   extends Activity {
	//用户管理
	Context context=User_Manage_Activity.this;
	
	static NMS_Communication Connect_NMS;
	
	private ListView User_ListView;
	private SimpleAdapter User_listAdapt;
	private List<HashMap<String,String>> UserList;
	
	int User_Index, User_Number;
	
	boolean Error_Tag = true;
	
	String[] User_ID = new String[100];			//用户ID
	String[] User_Name = new String[100];		//用户真实姓名
	String[] User_Type = new String[100];				//用户类型：3表示是终端管理用户，4表示是施工用户
	
	String Delete_User;
	
	DBHelper dbHelper;
	
	//广播参数
	private ServiceReceiver mReceiver;
	private String action="Flash_UserList";
	
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_make_newlock);
	     setTitle("亨通光电");
	     
	     dbHelper = new DBHelper(context);	//创建DBHlper对象实例

	     //广播初始化
	     mReceiver = new ServiceReceiver();
		 //实例化过滤器并设置要过滤的广播
		 IntentFilter mFilter = new IntentFilter();
		 mFilter.addAction(action);
		 registerReceiver(mReceiver, mFilter);
		    
	     initi();
	        
	}
	
	public void initi(){
    	
		Button Replay_This_Order, Show_Map, Insert_New_User;
		
		Replay_This_Order = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		Insert_New_User = (Button)findViewById(R.id.Save_Config);
		Show_Map = (Button)findViewById(R.id.button1);
		
		TextView Titale_View=(TextView)findViewById(R.id.Order_Title);
		Titale_View.setText("用户列表");
		
		User_ListView=(ListView)findViewById(R.id.worklistView);
		UserList = new ArrayList<HashMap<String,String >>();
		
		Insert_New_User.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent();   //创建Intent对象
				intent.setClass(context, Chang_PassWd_Activity.class);	
				intent.putExtra("Mode", "Insert");
				intent.putExtra("Query", "");
				startActivity(intent);//以添加新下属的模式跳转到密码更改页面
            }
        });
		
		Replay_This_Order.setVisibility(View.INVISIBLE);
		Show_Map.setVisibility(View.INVISIBLE);
		
		Insert_New_User.setText("添加新用户");		
		
		//下载群组用户列表			
		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6214");
		Connect_NMS.Make_Socket_Connect();
						
	}

	private void Add_User_List(int index) {
		//显示用户列表

		HashMap<String,String > map=new HashMap<String,String>();   	
		map.put("UserName", User_Name[index]);
		map.put("UserID", User_ID[index]);
		map.put("UserType", User_Type[index]);
		
		UserList.add(map);
		
		String[] from=new String[]{"UserName", "UserID", "UserType"};
		int[] to=new int[]{R.id.textView1, R.id.textView4, R.id.textView3};
		User_listAdapt=new SimpleAdapter(context, UserList, R.layout.listitem_device,from, to);
	  	User_ListView.setAdapter(User_listAdapt);
	  	User_listAdapt.notifyDataSetChanged();
  	
	  	User_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				TextView tv=(TextView)view.findViewById(R.id.textView4);
				final String Str_User = tv.getText().toString();
				
				tv=(TextView)view.findViewById(R.id.textView3);
				String Str_Type = tv.getText().toString();
				
				
				if (Str_Type.equals("管理员") && ! Str_User.equals(UserLogin_Activity.Login_User_ID)) {
					AlertDialog dialog_1 = new AlertDialog.Builder(context)
						.setTitle("提示")			//设置对话框的标题
						.setMessage("管理员信息只能在网管上处理")
						//设置对话框的按钮
						.setNegativeButton("确定", null)
						.create();
					dialog_1.show();
				}
				else {
					AlertDialog.Builder dialog =  new   AlertDialog.Builder(User_Manage_Activity.this  ); 
					
		   			dialog.setTitle("请选取对下述用户的操作").setMessage(Str_User);	//显示锁蓝牙设备名
		   			//此对话框可设置多个按键
		   			dialog.setPositiveButton("更新", new DialogInterface.OnClickListener() {
		   				//点击施工按钮
		   				@Override
		   				public void onClick(DialogInterface dialog, int i) {
		   					Intent intent = new Intent();   //创建Intent对象
		   					intent.setClass(context, Chang_PassWd_Activity.class);	
		   					intent.putExtra("Mode", "Change");
		   					intent.putExtra("Query", Str_User);
		   					startActivity(intent);//以修改下属数据的模式跳转到密码更改页面
		   				}
		   			});
		   			
		   			
		   			if (! Str_User.equals(UserLogin_Activity.Login_User_ID)) {
		   				dialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {
			   				@Override
			   				public void onClick(DialogInterface dialog, int which) {		   					
			   					AlertDialog dialog_1 = new AlertDialog.Builder(context)
	   								.setTitle("您确定要删除下述用户 ？")			//设置对话框的标题
	   								.setMessage(Str_User)
	   								//设置对话框的按钮
	   								.setNegativeButton("取消", null)
	   								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	   									@Override
	   									public void onClick(DialogInterface dialog, int which) {
	   										Delete_User = Str_User;
	   										Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6216");
	   										Connect_NMS.Make_Socket_Connect();
	   									}
	   								}).create();
			   					dialog_1.show();
			   				}
			   			});
		   				dialog.setNeutralButton("辖区", new DialogInterface.OnClickListener() {
			   				@Override
			   				public void onClick(DialogInterface dialog, int which) {	
			   					Intent intent = new Intent();
			   					intent.setClass(context, Chang_Popedom_Activity.class);	    
			   					intent.putExtra("User", Str_User);
			   					startActivity(intent);		//跳转辖区信息选择功能界面
			   				}
			   			});
		   			}
		   			
		   			dialog.show();
				}
			}
    		
    	});
	}
	
	//定义Handler对象
  	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			String s, Str_Date, Str_Numb, Str_Type, Str_ID, Str_Name;
  			int i;

  			switch(msg.what){
  			case 0:
  				//应答分析
  				s=msg.obj.toString();
  				Str_Date = s.substring(0, 4);
  				
  				if (Str_Date.equals("6214")) {
  					//是用户列表数据
  					if (s.substring(5, 6).equals("E") && Error_Tag) {
  						Error_Tag = false;
  						AlertDialog dialog_1 = new AlertDialog.Builder(context)
							.setTitle("提示")			//设置对话框的标题
							.setMessage("读取用户列表出错。")
							//设置对话框的按钮
							.setPositiveButton("确定", null)
							.create();
						dialog_1.show();
  					}
  					
  					Str_Date = s.substring(7, 8);
  					
  					if (Str_Date.equals("N")) {
  						Str_Date = s.substring(9);
  						User_Number = Integer.valueOf(Str_Date);
  						User_Index = 0;
  						
  						UserList.clear();

  					}
  					else if (Str_Date.equals("U")) {
  						Str_Date = s.substring(9);
  						
  						i = Str_Date.indexOf(" ");
  						Str_ID = Str_Date.substring(0, i);
  						
  						Str_Date = Str_Date.substring(i+1);
  						
  						i = Str_Date.indexOf(" ");
  						Str_Name = Str_Date.substring(0, i);
  						
  						Str_Date = Str_Date.substring(Str_Date.length() - 1);
  						
  						if (Str_Date.equals("3")) {
  							Str_Type = "管理员";
  						}
  						else {
  							Str_Type = "操作员";
  						}
  						
  						User_ID[User_Index] = Str_ID;
  						User_Name[User_Index] = Str_Name;
  						User_Type[User_Index] = Str_Type;
  						
  						Add_User_List(User_Index);
  						User_Index ++;
  						  						
  					}

  				}
  				else if (s.equals("6216 F 00")) {
  					//是删除用户的回复,则重新读取用户列表
  					SQLiteDatabase db=dbHelper.getWritableDatabase();
					DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "删除用户账号", Delete_User);
					db.close();
  					
  					Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6214");
  					Connect_NMS.Make_Socket_Connect();
  				}
  				break;
  				
  			case 2:
  				s=msg.obj.toString();

  				if (s.equals("Wait_Send_6214")) {
  					//发送读取用户列表指令
  					Error_Tag = true;
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.DownLoad_OrderData_6214(UserLogin_Activity.Login_User_ID);		//发送下载工单数据指令
  				}
  				else if (s.equals("Wait_Send_6216")) {
  					//发送删除用户指令
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.Update_User_Inform_6216("", Delete_User, "", "", "", 0);
  				}
  				break;
  			}
  		}
  	};
  	
  	class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Flash_UserList")) {
				//完成用户信息更新,则重新读取用户列表
				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6214");
				Connect_NMS.Make_Socket_Connect();
			}
		}	
    }
  	
   
  	
  	protected void onResume() {

		super.onResume();

	}
    
	protected void onDestroy() {

		super.onDestroy();
		

	}
}
