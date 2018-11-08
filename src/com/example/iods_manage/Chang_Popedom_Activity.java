package com.example.iods_manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;

import android.annotation.SuppressLint;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Chang_Popedom_Activity   extends Activity {
	//修改辖区信息界面
	
	static NMS_Communication Connect_NMS;
    
    private Context context = Chang_Popedom_Activity.this;//定义Context对象
    
    DBHelper dbHelper ;//数据库服务对象
    
    String[] Popedom_ID = new String[1000];
    String[] Popedom_Name = new String[1000];
    int[] Popedom_Type = new int[1000];
    boolean[] Popedom_Select = new boolean[1000];
    int index_Popedom = 0, Numb_Popedom;
    
    boolean Error_Tag = true;
    
    String User_ID, Download_ID;
    
    int[] Send_Frame_Numb = new int[2];			//0位置保存当前帧号，从1开始；1位置保存当前要回传的指令总帧数
    
    private ListView User_ListView;
	private SimpleAdapter User_listAdapt;
	private List<HashMap<String,String>> UserList;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_newlock);
	    setTitle("亨通光电");
	    
	    Intent in=getIntent();
	    User_ID=in.getStringExtra("User");	
	     
	    dbHelper = new DBHelper(context);	//创建DBHlper对象实例
	    
	    initi();
	}

	private void initi() {

		Button Replay_This_Order, Show_Map, Insert_New_User;
		
		Replay_This_Order = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		Insert_New_User = (Button)findViewById(R.id.Save_Config);
		Show_Map = (Button)findViewById(R.id.button1);
		
		TextView Titale_View=(TextView)findViewById(R.id.Order_Title);
		Titale_View.setText("请选择用户 " + User_ID +" 的辖区范畴");
		
		User_ListView=(ListView)findViewById(R.id.worklistView);
		UserList = new ArrayList<HashMap<String,String >>();
		
		Insert_New_User.setText("数据提交");	
		Insert_New_User.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	//提交辖区选择结果	
            	Send_Frame_Numb[0] = 0;
            	Send_Frame_Numb[1] = 0;
            	
        		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6212");
        		Connect_NMS.Make_Socket_Connect();
            }
        });
		
		Replay_This_Order.setVisibility(View.INVISIBLE);
		Show_Map.setVisibility(View.INVISIBLE);
		
		//下载群组辖区列表	
		Download_ID = UserLogin_Activity.Login_User_ID;
		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6215");
		Connect_NMS.Make_Socket_Connect();
	}
	
	
	//定义Handler对象
  	Handler mHandler = new Handler(){
  		@SuppressLint("HandlerLeak") public void handleMessage(Message msg){
  			String s, Str_Date, Str_Type, Str_ID, Str_Name;
  			int i, j;

  			switch(msg.what){
  			case 0:
  				//应答分析
  				s=msg.obj.toString();
  				Str_Date = s.substring(0, 4);
  				
  				if (Str_Date.equals("6215")) {
  					if (s.substring(5, 6).equals("E") && Error_Tag) {
  						Error_Tag = false;
  						AlertDialog dialog = new AlertDialog.Builder(context)
  							.setTitle("提示")			//设置对话框的标题
  							.setMessage("通信出错。")
  							//设置对话框的按钮
  							.setPositiveButton("确定", null)
  							.create();
  						dialog.show();
  					}
  					
  					if (s.substring(7, 8).equals("N")) {
  						Numb_Popedom = Integer.valueOf(s.substring(9));		//辖区范畴数
  						
  						if (Numb_Popedom == 0 && Download_ID.length() == 0) {
  							//用户辖区为空，则直接显示列表
  							Flash_Popedom_List();
  						}
  					}
  					else if (s.substring(7, 8).equals("P")) {
  						//辖区数据
  						Str_Date = s.substring(11);
  						Str_Type = s.substring(9, 10);
  						
  						Str_ID = Str_Date.substring(0, 16);		
  						Str_Name = Str_Date.substring(17);
  						
  						if (Download_ID.length() == 0) {
  	  						//是用户辖区数据
  							j = 0;
  	  						for (i = 0; i<index_Popedom; i++) {
  	  							if (Popedom_ID[i].equals(Str_ID) && 
  	  									Popedom_Type[i] == Integer.valueOf(Str_Type) && 
  	  									Popedom_Name[i].equals(Str_Name)) {
  	  								Popedom_Select[i] = true;	//选中
  	  							}
  	  							
  	  							if (Popedom_Select[i]) {
  	  								j++;	//累计已被选中的数量
  	  							}
  	  						}
  	  						
  	  						if (j >= Numb_Popedom ) {
  	  							//TODO 刷新辖区列表
  	  							Flash_Popedom_List();
  	  						}
  	  					}
  	  					else {
  	  						//是管理员辖区数据
  	  						Popedom_ID[index_Popedom] = Str_ID;
  	  						Popedom_Type[index_Popedom] = Integer.valueOf(Str_Type);
  	  						Popedom_Name[index_Popedom] = Str_Name;
  	  						Popedom_Select[index_Popedom] = false;
  	  						
  	  						index_Popedom++;
  	  						
  	  						if (index_Popedom >= Numb_Popedom) {
  	  							//查询用户辖区
  	  							Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6215");
  	  							Connect_NMS.Make_Socket_Connect();
  	  						}
  	  					}
  					}
  					
  				}
  				else if (s.equals("6212 F")) {
  					SQLiteDatabase db=dbHelper.getWritableDatabase();
					DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "更改用户管辖范畴", User_ID);
					db.close();
  					
					Intent in=new Intent(); 
	  		    	in.setAction("Flash_UserList"); 		//发送刷新用户列表广播
	  		    	   	//in.putExtra("result", ""); 
	  		    	sendBroadcast(in);
	  		    	   	
  					finish();		//完成更新
  				}
  				else if (s.subSequence(0, 6).equals("6212 E")) {
  					AlertDialog dialog = new AlertDialog.Builder(context)
						.setTitle("提示")			//设置对话框的标题
						.setMessage("数据提交出错 ！")
						//设置对话框的按钮
						.setPositiveButton("确定", null)
						.create();
  					dialog.show();
  				}
  				break;
  				
  			case 2:
  				s=msg.obj.toString();
  				
  				if (s.equals("Wait_Send_6215")) {
  					//发送读取用户列表指令
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.DownLoad_PopedomData_6215(Download_ID);		//发送下载辖区数据指令
  			    	
  					if (Download_ID.equals(UserLogin_Activity.Login_User_ID)) {
  						Download_ID = User_ID;
  					}
  					else {
  						Download_ID = "";
  					}
  				}
  				else if (s.equals("Wait_Send_6212")) {
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  					Send_Frame_Numb = NMS_Communication.Update_PopedomData_6212(User_ID, index_Popedom, Popedom_Name, Popedom_ID, Popedom_Type, Popedom_Select, Send_Frame_Numb);		//上传辖区数据
  				}
  				
  				break;
  			}
  		}

		
  	};
  	
  	private void Flash_Popedom_List() {
		//刷新辖区表
  		int i, j; 		

  		UserList.clear();
  		
  		for (i = 0; i < index_Popedom; i++) {
  			HashMap<String,String > map=new HashMap<String,String>();
  			
  			map.put("ID", String.valueOf(i));
  			
  			j = Popedom_Type[i];
  			if (j == 1) {
  				map.put("Type", "子网");
  			}
  			else if (j == 2) {
  				map.put("Type", "业务");
  			}
  			if (j == 0) {
  				map.put("Type", "锁");
  			}
  			
  			if (Popedom_Select[i]) {
  				map.put("Name", "*选中*** " + Popedom_Name[i]);
  			}
  			else {
  				map.put("Name", Popedom_Name[i]);
  			}
  			
  			UserList.add(map);
  		}
		
		String[] from=new String[]{"ID", "Name", "Type"};
		int[] to=new int[]{R.id.textView1, R.id.textView4, R.id.textView3};
		User_listAdapt=new SimpleAdapter(context, UserList, R.layout.listitem_device,from, to);
	  	User_ListView.setAdapter(User_listAdapt);
	  	User_listAdapt.notifyDataSetChanged();
  	
	  	User_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int i, j, k;

				TextView tv=(TextView)view.findViewById(R.id.textView4);
				String Str_User = tv.getText().toString();
								
				i = position;
				
				if (i < index_Popedom) {
					AlertDialog.Builder dialog =  new   AlertDialog.Builder(Chang_Popedom_Activity.this  ); 
					
		   			dialog.setTitle("请选取对下述范畴的操作").setMessage(Str_User);	//显示锁蓝牙设备名
		   			//此对话框可设置多个按键
		   			dialog.setPositiveButton("取消", null);
		   			final int index_click = i;
					if (Popedom_Select[i]) {
						dialog.setNegativeButton("去除选中", new DialogInterface.OnClickListener() {
			   				@Override
			   				public void onClick(DialogInterface dialog, int which) {		   					
			   					Popedom_Select[index_click] = false;
			   					Flash_Popedom_List();
			   				}
			   			});
					}
					else {
						dialog.setNegativeButton("选取", new DialogInterface.OnClickListener() {
			   				@Override
			   				public void onClick(DialogInterface dialog, int which) {		   					
			   					Popedom_Select[index_click] = true;
			   					Flash_Popedom_List();
			   				}
			   			});
					}
					dialog.show();
				}
			}
    	});
		
	}
  	
  	protected void onResume() {

		super.onResume();

	}
    
	protected void onDestroy() {

		super.onDestroy();
		dbHelper.close();	

	}
}
