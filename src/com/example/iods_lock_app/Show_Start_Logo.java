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

	Context context ;//Context����    
    DBHelper dbHelper ;//���ݿ�������
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_logo);
        setTitle("��ͨ���");
        
        context = this.getApplicationContext();	//���������Ķ���	
		dbHelper = new DBHelper(context);	//����DBHlper����ʵ��
				
		/*
		dbHelper.deleteTb(DBHelper.Basic_Config_Table);			//ɾ�����ݱ�
		dbHelper.deleteTb(DBHelper.Table_Log_Record);			//ɾ�����ݱ�
		dbHelper.deleteTb(DBHelper.User_Locker_List_Table);		//ɾ�����ݱ�
		dbHelper.deleteTb(DBHelper.User_Record_Table);			//ɾ�����ݱ�
		dbHelper.deleteTb(DBHelper.Table_Order_List);			//ɾ���������б�
		dbHelper.deleteTb(DBHelper.Table_Order_Data);			//ɾ���������ݱ�
		dbHelper.deleteTb(DBHelper.Table_Check_List);			//ɾ������鹤�����б�
		dbHelper.deleteTb(DBHelper.Table_Check_Data);			//ɾ������鹤�����ݱ�
		*/
		
		dbHelper.createTb(DBHelper.Create_Basic_Config_Table);		//�������ñ�
        dbHelper.createTb(DBHelper.Create_Log_Record);				//��־��¼��
        dbHelper.createTb(DBHelper.Create_User_Locker_Table);				//�û�Ͻ����ID��¼��
        dbHelper.createTb(DBHelper.Create_User_Record_Table);				//�û���¼��
        dbHelper.createTb(DBHelper.Create_Order_List_Table);			//�������б�
        dbHelper.createTb(DBHelper.Create_Order_Data_Table);			//�������ݱ�
        dbHelper.createTb(DBHelper.Create_Check_List_Table);			//����鹤�����б�
        dbHelper.createTb(DBHelper.Create_Check_Data_Table);			//����鹤�����ݱ�
                
        //�������ܵ�ַ��˿�
        SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from Basic_Config_Table",null);
		
		if(cursor.moveToNext()){
			NMS_Communication.NMS_Address =  cursor.getString(1);
			NMS_Communication.NMS_Port = Integer.valueOf(cursor.getInt(2));
			UserLogin_Activity.Lock_Search_Scope = Integer.valueOf(cursor.getInt(3));
			NMS_Communication.index_backgroup = Integer.valueOf(cursor.getInt(4));
		}
		else {
			NMS_Communication.NMS_Address =  "182.61.18.163";	//���ܻ��Žӷ����ȱʡ��ַ
			NMS_Communication.NMS_Port = 5002;					//���ܻ��Žӷ����ȱʡ�˿ں�
			UserLogin_Activity.Lock_Search_Scope = 3;			//������Χȱʡֵ
			NMS_Communication.index_backgroup = 0;				//����ͼ��
		}
		cursor.close();			
		db.close();
		
        new Handler().postDelayed(new Runnable(){     
		    public void run() {   
		    	Intent intent = new Intent();   //����Intent���� 
		    	Context context=Show_Start_Logo.this;
		    	
		    	intent.setClass(context, UserLogin_Activity.class);
		    	//intent.setClass(context, Open_Locker_Activity.class);		//TODO ��������ת
		    	Show_Start_Logo.this.finish(); 
	        	startActivity(intent);//��ת���û���¼
		    }     
		 }, 2000);			//��ʱ��ת����¼ҳ��
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
		//true��ʾ����ǩ
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		dbHelper.close();

	}	 

}
