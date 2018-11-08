package com.example.iods_manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;



public class Project_Checkup_Activity    extends Activity {
	//工程审查主页面
	
	private ListView Order_ListView;
	private SimpleAdapter Order_listAdapter;				//新建锁设备列表适配器
	private List<HashMap<String,String>> OrderList;//工单列表数据源

	private Spinner spinner1, spinner2;//查询spinner
	int selectedposition;//spinner2的选择项；

	Context context=Project_Checkup_Activity.this;
	DBHelper dbHelper;
	
	static NMS_Communication Connect_NMS;
	
	//广播参数
	private ServiceReceiver mReceiver;
	private String action="Close_ListCheck";
	
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
	     
	     initi();
	        
	     Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620A");
		 Connect_NMS.Make_Socket_Connect();
	}

	public void initi(){
    	spinner1 = (Spinner)findViewById(R.id.spinner_Select);//第二个下拉菜单，由第一个下拉菜单的选择而更新
		spinner2 = (Spinner)findViewById(R.id.spinner2);//第一个下拉菜单		
		
		Button DownLoad_Btn;
		
		TextView Show_GPS_Title=(TextView)findViewById(R.id.Order_Title);
		Show_GPS_Title.setText("待审查工单");
		
		Order_ListView=(ListView)findViewById(R.id.worklistView);
		
		Button Apply_Insert_Tag_BTN = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		DownLoad_Btn = (Button)findViewById(R.id.Save_Config);
		DownLoad_Btn.setText("列表下载");
		DownLoad_Btn.setOnClickListener(new ClickEvent());
		
		Apply_Insert_Tag_BTN.setVisibility(View.INVISIBLE);			//不显示此按键
		spinner1.setVisibility(View.INVISIBLE);			//不显示此按键
		spinner2.setVisibility(View.INVISIBLE);			//不显示此按键
		
		Flash_Order_List();		//刷新工单列表
					
	}
    
    private void Flash_Order_List() {
		//刷新工单列表
    	String Str_ID, temp_Str;
    	    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		cursor=db.rawQuery("select * from Check_List_Table where userID = ? AND (Status != '4' OR Status != '5')", new String[]{UserLogin_Activity.Login_User_ID});    	
	      
		OrderList = new ArrayList<HashMap<String,String >>();
	      
	    while(cursor.moveToNext()){
      
    		HashMap<String,String > map=new HashMap<String,String>();   		
    		
    		temp_Str = cursor.getString(6);		//取工单状态
    		
    		if (temp_Str.equals("4") || temp_Str.equals("5")) {
    			continue;
    		}

    		Str_ID = cursor.getString(1);		//取工单ID
    		map.put("OrderID", Str_ID);
    		
    		temp_Str = Str_ID.substring(0, 6);
    		
    		if (temp_Str.equals("INSERT")) {
    			map.put("OrderType", "施工工单");
    		}
    		else if (temp_Str.equals("MAINTE")) {
    			map.put("OrderType", "维护工单");
    		}
    		else if (temp_Str.equals("MOVETO")) {
    			map.put("OrderType", "移机工单");
    		}
    		if (temp_Str.equals("REMOVE")) {
    			map.put("OrderType", "拆除工单");
    		}
    		
    		map.put("OrderLimite", "");
    		
    		OrderList.add(map);
    	}
      
		db.close();
		
		String[] from=new String[]{"OrderLimite", "OrderID", "OrderType"};
		int[] to=new int[]{R.id.textView1, R.id.textView4, R.id.textView3};
		Order_listAdapter=new SimpleAdapter(context, OrderList, R.layout.listitem_device,from, to);
	  	Order_ListView.setAdapter(Order_listAdapter);
	  	Order_listAdapter.notifyDataSetChanged();
  	
	  	Order_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent();   //创建Intent对象 
				
				TextView tv=(TextView)view.findViewById(R.id.textView4);
				String Str_Order = tv.getText().toString();
				
	    		intent.setClass(context, List_Checkup_Activity.class);	
	    		intent.putExtra("workID", Str_Order);
	        	startActivity(intent);
		
			}
    		
    	});
	}
    
    
    class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Close_ListCheck")) {
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
				
				if (temp_Str.equals("620A")) {
					temp_Str = s.substring(5);
					
					if (temp_Str.length() > 0) {
						//有工单ID
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						
						Cursor cursor=db.rawQuery("select * from Check_List_Table  where Order_ID = ?", new String[]{temp_Str});    			    
						if (cursor.moveToNext()){
							temp_Int = 1;
						}
						cursor.close();
						
						if (temp_Int == 0) {
							db.execSQL("INSERT INTO Check_List_Table (Order_ID, userID, Status) " +
									"VALUES (?, ?, '0')", new String[]{temp_Str, UserLogin_Activity.Login_User_ID});
						}
						db.close();
						
						Flash_Order_List();
					}
				}

				break;
			case 2:
				s=msg.obj.toString();

				if (s.equals("Wait_Send_620A")) {
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.DownLoad_CheckList_620A(UserLogin_Activity.Login_User_ID);
					
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
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620A");
				   Connect_NMS.Make_Socket_Connect();
	    		   					
	    		   break;
	    		
	    	   }
		}
	}
		
}
