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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class List_Checkup_Activity   extends Activity {
	//工单列表审查
	
	private Context context = List_Checkup_Activity.this;//定义Context对象
	
	double Get_Lock_Lat = 0, Get_Lock_Lng = 0;
	String Get_Lock_Name = ""; 

	static NMS_Communication Connect_NMS;
	
	String[] List_Lock_ID = new String[200];
	String[] List_Lock_Name = new String[200];
	String[] List_Lock_Lng = new String[200];
	String[] List_Lock_Lat = new String[200];
	int Lock_Index;
	
	boolean Check_Tag;
		
	private ListView Lock_ListView;
	private SimpleAdapter Lock_listAdapter;				//工单设备列表适配器
	private List<HashMap<String,String>> Lock_List;		//工单设备列表数据源
	
	DBHelper dbHelper ;//数据库服务对象
	
	String Lock_ID;				//锁ID
	String workID, workType;				//工单ID
	
	TextView Titale_View;
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.activity_make_newlock);
        
        Intent in=getIntent();
        workID=in.getStringExtra("workID");
        		
        setTitle(workID);		//显示工单ID
                
        dbHelper = new DBHelper(context);	//创建DBHlper对象实例
        
        initi();
	        
	}
	
	 public void initi(){
	    	
			Button Replay_This_Order, Show_Map, Get_New_Lock;
			
			Replay_This_Order = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
			Get_New_Lock = (Button)findViewById(R.id.Save_Config);
			Show_Map = (Button)findViewById(R.id.button1);
			
			Show_Map.setText("地图显示");
			Get_New_Lock.setText("审查驳回");
			Replay_This_Order.setText("审查通过");
			
			Replay_This_Order.setOnClickListener(new ClickEvent());
			Get_New_Lock.setOnClickListener(new ClickEvent());
			Show_Map.setOnClickListener(new ClickEvent());
						
			Lock_ListView=(ListView)findViewById(R.id.worklistView);
			
			Titale_View=(TextView)findViewById(R.id.Order_Title);
			
			String temp_Str = workID.substring(0, 6);
		        
			if (temp_Str.equals("INSERT")) {
				temp_Str = "工单完成量：";
			}
			else {
				temp_Str = "工单任务量：" + "； 工单完成量：";
			}
			Titale_View.setText(temp_Str);
			
			Flash_Lock_List();		//刷新锁列表
						
		}
	    
	    @SuppressWarnings("resource")
		private void Flash_Lock_List() {
			//刷新锁设备列表
	    	int temp_Int = 0;
	    	String Lock_Name = null, Lock_Numb = null, Order_Numb = null;
	    	String temp_Str = workID.substring(0, 6);
			
	    	Lock_List = new ArrayList<HashMap<String,String >>();
	    	
	    	SQLiteDatabase db=dbHelper.getWritableDatabase();
			Cursor cursor;  
			
			cursor=db.rawQuery("select * from Check_Data_Table  where Order_ID = ?", new String[]{workID});    			    
			if (cursor.moveToNext()){
				temp_Int = 1;
				Lock_Name = cursor.getString(3);
				Lock_Numb = cursor.getString(4);
				Order_Numb = cursor.getString(5);
			}
			cursor.close();

			if (temp_Int == 1) {
				Lock_Index = 0;
				cursor=db.rawQuery("select * from Check_Data_Table  where Order_ID = ?", new String[]{workID});    			    

			    while(cursor.moveToNext()){
		      
			    	Lock_Index++;
		    		HashMap<String,String > map=new HashMap<String,String>();
		    			    		
		    		Lock_Name = cursor.getString(2);
	    			List_Lock_Lng[Lock_Index] = cursor.getString(3);
	    			List_Lock_Lat[Lock_Index] = cursor.getString(4);  		
		    		
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
			    cursor.close();
			    
			    cursor=db.rawQuery("select * from Check_List_Table  where Order_ID = ?", new String[]{workID});    			    
				if (cursor.moveToNext()){
					Lock_Name = cursor.getString(3);
					temp_Int = 100 * Integer.valueOf(cursor.getString(4));
					Order_Numb = cursor.getString(5);
				}
				cursor.close();
			    	
			    temp_Str = workID.substring(0, 6);
			    if (temp_Str.equals("INSERT")) {

					temp_Str = "施工人：" + Lock_Name;
				}
				else {
					temp_Int = temp_Int / Integer.valueOf(Order_Numb);
					temp_Str = "施工人：" + Lock_Name + " ；  完成量：" + String.valueOf(temp_Int) + "％";
				}
				Titale_View.setText(temp_Str);
			}
			else {
				//下载工单数据		
				cursor=db.rawQuery("select * from Check_List_Table  where Order_ID = ?", new String[]{workID});    			    
				if (cursor.moveToNext()){
					temp_Str = cursor.getString(5);
				}
				cursor.close();

				if (temp_Str == null) {
					Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620B");
					Connect_NMS.Make_Socket_Connect();
				}
				else if (temp_Str.equals("0")) {
					Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620B");
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

					int i = 0;
				}
	    		
	    	});
		}
	    

		//自定义单击事件类
	  	class ClickEvent implements View.OnClickListener {    

	  		Intent intent = new Intent();   //创建Intent对象 

	  		public void onClick(View v) {
	  			
	  			switch(v.getId()){
		    	   case R.id.Save_Config:
		    		   //审查驳回
		    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620C B");
		    		   Connect_NMS.Make_Socket_Connect();
		    		   
		    		   break;
		    		   
		    	   case R.id.Apply_Insert_Tag_btn:
		    		   //审查通过
		    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620C P");
		    		   Connect_NMS.Make_Socket_Connect();

		    		   break;
		    		   
		    	   case R.id.button1:
		    		   //地图显示
		    		   intent.setClass(context, Map_Checkup_Activity.class);
		    		   intent.putExtra("workID", workID);
		    		   startActivity(intent);
		    		   
		    		   break;
	  			}  
	  		}
	  	}
	  	
	  	
	  	//定义Handler对象
	  	Handler mHandler = new Handler(){
	  		public void handleMessage(Message msg){
	  			String s, Str_Date, Str_Lng, Str_Lat, Str_ID, Str_Name;
	  			int i;
	  			AlertDialog dialog;

	  			switch(msg.what){
	  			case 0:
	  				//应答分析
	  				s=msg.obj.toString();
	  				Str_Date = s.substring(0, 6);
	  				
	  				if (Str_Date.equals("620B O")) {
	  					//是工单头数据
	  					Str_Date = s.substring(7);
	  					
	  					i = Str_Date.indexOf(" ");
	  					if (i > 0) {
	  						Str_ID = Str_Date.substring(0, i);
	  						Str_Date = Str_Date.substring(i + 1);
	  						
	  						i = Str_Date.indexOf(" ");
		  					if (i > 0) {
		  						Str_Name = Str_Date.substring(0, i);
		  						
		  						Str_Date = Str_Date.substring(i + 1);
		  						i = Str_Date.indexOf(" ");
			  					if (i > 0) {
			  						Str_Lat = Str_Date.substring(0, i);
			  						Str_Date = Str_Date.substring(i + 1);
			  						
			  						String temp_Str = Str_ID.substring(0, 6);
			  						
			  						SQLiteDatabase db=dbHelper.getWritableDatabase();
			  						db.execSQL("UPDATE Check_List_Table SET WorkerID = ?, Lock_Numb = ?, Oder_Numb = ?, Status = '1' WHERE Order_ID = ?", new String[]{Str_Name, Str_Date, Str_Lat, workID});		
			  						//添加工单时限，并更改工单状态
			  						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "下载待审查工单数据", workID);
			  						db.close();
			  				        
			  						if (temp_Str.equals("INSERT")) {
			  							temp_Str = "施工人：" + Str_Name;
			  						}
			  						else {
			  							i = 100 * Integer.valueOf(Str_Date);
			  							i = i / Integer.valueOf(Str_Lat);
			  							temp_Str = "施工人：" + Str_Name + " ；  完成量：" + String.valueOf(i) + "％";
			  						}
			  						Titale_View.setText(temp_Str);
			  					}
		  						
		  					}
		  					
	  					}
	  					else {
	  						
	  					}
	  				}
	  				else if (Str_Date.equals("620B L")) {
	  					//是工单锁数据
	  					Str_Lng = s.substring(7,18);
	  					Str_Lat = s.substring(18,28);
	  					Str_Name = s.substring(29);
	  					
	  					SQLiteDatabase db=dbHelper.getWritableDatabase();
						db.execSQL("INSERT INTO Check_Data_Table (Order_ID, Locker_Name, Locker_Lng, Locker_lat, Status) " +
								"VALUES (?, ?, ?, ?, ?)", new String[]{workID, Str_Name, Str_Lng, Str_Lat, "0"});
						db.close();
						
						Flash_Lock_List();		//刷新锁列表
	  				}
	  				else if (Str_Date.equals("620C F")) {
	  					//完成审查
	  					
	  					SQLiteDatabase db=dbHelper.getWritableDatabase();
  						db.execSQL("UPDATE Check_List_Table SET Status = '4' WHERE Order_ID = ?", new String[]{workID});		
  						
  						if (Check_Tag) {
  							Str_Lat = "工单审查通过";
  						}
  						else {
  							Str_Lat = "驳回待审查工单";
  						}
  						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, Str_Lat, workID);
  						
  						db.execSQL("DELETE FROM Check_List_Table WHERE Order_ID = ?", new String[]{workID});
  						db.execSQL("DELETE FROM Check_Data_Table WHERE Order_ID = ?", new String[]{workID});

  						db.close();
												
						dialog = new AlertDialog.Builder(context)
  							.setTitle("完成当前工单审查")			//设置对话框的标题
  							.setMessage(workID)
  							//设置对话框的按钮
  							.setPositiveButton("确定", null)
  							.create();
						dialog.show();
						
						Intent in=new Intent(); 
		  		    	in.setAction("Close_ListCheck"); 		//发送回单页面被关闭广播
		  		    	//in.putExtra("result", ""); 
		  		    	sendBroadcast(in);
		  		      		
		  		    	dbHelper.close();
		  		    	List_Checkup_Activity.this.finish();		//关闭当前窗口
	  				}
	  				else if (Str_Date.equals("620C E")) {
	  					//审查上传失败
	  					dialog = new AlertDialog.Builder(context)
	  						.setTitle("提示")			//设置对话框的标题
	  						.setMessage("审查结论上传失败，请重新上传。")
	  						//设置对话框的按钮
	  						.setPositiveButton("确定", null)
	  						.create();
	  					dialog.show();
	  				}
	  				break;
	  				
	  			case 2:
	  				s=msg.obj.toString();

	  				if (s.equals("Wait_Send_620B")) {
	  					Connect_NMS.Wait_Recive_TCP_Reply();	
	  			    	NMS_Communication.DownLoad_CheckData_620B(workID);		//发送下载待审查工单数据指令
	  				}
	  				else if (s.equals("Wait_Send_620C P")) {
	  					Check_Tag = true;
	  					Connect_NMS.Wait_Recive_TCP_Reply();	
	  			    	NMS_Communication. Report_Checkup_620C(workID, Check_Tag);	//发送审查通过指令
	  				}
	  				else if (s.equals("Wait_Send_620C B")) {
	  					Check_Tag = false;
	  					Connect_NMS.Wait_Recive_TCP_Reply();	
	  			    	NMS_Communication. Report_Checkup_620C(workID, Check_Tag);	//发送审查驳回指令
	  				}
	  				break;
	  			}
	  		}
	  	};

}
