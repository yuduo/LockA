package com.example.iods_lock_app;



import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


public class Show_Start_Logo extends ActionBarActivity {

	Context context ;//Context对象    
    DBHelper dbHelper ;//数据库服务对象
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_logo);
        setTitle("亨通光电");
        
        context = this.getApplicationContext();	//创建上下文对象	
		dbHelper = new DBHelper(context);	//创建DBHlper对象实例
				
		/*
		dbHelper.deleteTb(DBHelper.Basic_Config_Table);			//删除数据表
		dbHelper.deleteTb(DBHelper.Table_Log_Record);			//删除数据表
		dbHelper.deleteTb(DBHelper.User_Locker_List_Table);		//删除数据表
		dbHelper.deleteTb(DBHelper.User_Record_Table);			//删除数据表
		dbHelper.deleteTb(DBHelper.Table_Order_List);			//删除工单总列表
		dbHelper.deleteTb(DBHelper.Table_Order_Data);			//删除工单数据表
		dbHelper.deleteTb(DBHelper.Table_Check_List);			//删除待审查工单总列表
		dbHelper.deleteTb(DBHelper.Table_Check_Data);			//删除待审查工单数据表
		*/
		
		dbHelper.createTb(DBHelper.Create_Basic_Config_Table);		//基本配置表
        dbHelper.createTb(DBHelper.Create_Log_Record);				//日志记录表
        dbHelper.createTb(DBHelper.Create_User_Locker_Table);				//用户辖区锁ID记录表
        dbHelper.createTb(DBHelper.Create_User_Record_Table);				//用户记录表
        dbHelper.createTb(DBHelper.Create_Order_List_Table);			//工单总列表
        dbHelper.createTb(DBHelper.Create_Order_Data_Table);			//工单数据表
        dbHelper.createTb(DBHelper.Create_Check_List_Table);			//待审查工单总列表
        dbHelper.createTb(DBHelper.Create_Check_Data_Table);			//待审查工单数据表
                
        //调用网管地址与端口
        SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from Basic_Config_Table",null);
		
		if(cursor.moveToNext()){
			NMS_Communication.NMS_Address =  cursor.getString(1);
			NMS_Communication.NMS_Port = Integer.valueOf(cursor.getInt(2));
			UserLogin_Activity.Lock_Search_Scope = Integer.valueOf(cursor.getInt(3));
			NMS_Communication.index_backgroup = Integer.valueOf(cursor.getInt(4));
		}
		else {
			NMS_Communication.NMS_Address =  "182.61.18.163";	//网管或桥接服务的缺省地址
			NMS_Communication.NMS_Port = 5002;					//网管或桥接服务的缺省端口号
			UserLogin_Activity.Lock_Search_Scope = 3;			//搜锁范围缺省值
			NMS_Communication.index_backgroup = 0;				//背景图号
		}
		cursor.close();			
		db.close();
		
        new Handler().postDelayed(new Runnable(){     
		    public void run() {   
		    	Intent intent = new Intent();   //创建Intent对象 
		    	Context context=Show_Start_Logo.this;
		    	
		    	intent.setClass(context, UserLogin_Activity.class);
		    	//intent.setClass(context, Open_Locker_Activity.class);		//TODO 调试用跳转
		    	Show_Start_Logo.this.finish(); 
	        	startActivity(intent);//跳转到用户登录
		    }     
		 }, 2000);			//延时跳转到登录页面
    }
    
    Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			//None
			
		}

	};
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

	}	 

}
