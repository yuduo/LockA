package com.example.iods_manage;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.iods_common.DBHelper;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


public class Data_Settle_Activity extends Activity {
	//��������ҳ��
	
	private DBHelper dbHlper;	//���ݿ��������
	private TextView T_Result;
	private int data_Total, Settle_Time;
	
	private Context context = Data_Settle_Activity.this;//����Context����
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_settle);
        setTitle("��������");
        
        dbHlper = new DBHelper(context);//��ȡ���ݿ�������ʵ��

		initi();
		
    }
    public void initi(){

		Button Settle_Cancel_Btn, Settle_OK_Btn;
		TextView T_Order, T_Log;
		Cursor cursor;
		
		int temp_Int = 0, temp_Int_1 = 0;
		
		String End_time;
		long This_DaysMili, twoDaysAgoMili, Browse_End;
		
		Settle_Time = 1;
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
		Date now=new Date();
		This_DaysMili = now.getTime();
		Browse_End = 100;
		twoDaysAgoMili=This_DaysMili-24*1000*60*60 * Browse_End;
		Date twodaysago=new Date((long) twoDaysAgoMili);
		End_time = sDateFormat.format(twodaysago);
				
		Settle_Cancel_Btn = (Button)findViewById(R.id.button2);
		Settle_OK_Btn = (Button)findViewById(R.id.button1);
		
		Settle_OK_Btn.setOnClickListener(new ClickEvent());
		Settle_Cancel_Btn.setOnClickListener(new ClickEvent());
		
		T_Order = (TextView)findViewById(R.id.textView3);
		T_Log = (TextView)findViewById(R.id.textView11);
		T_Result = (TextView)findViewById(R.id.textView8);
		
		SQLiteDatabase db=dbHlper.getWritableDatabase();
		   
    	cursor=db.rawQuery("select count(_id) from Table_Log_Record",new String[]{});
    	if(cursor.moveToNext()){
    		temp_Int = cursor.getInt(0);
    	}
    	cursor.close(); 
    	T_Log.setText("��־��" + temp_Int + " ����");
    	temp_Int_1 = temp_Int; 
    	
    	cursor=db.rawQuery("select count(_id) from Order_List_Table WHERE Status = '4' OR Status = '5'",new String[]{});
    	if(cursor.moveToNext()){
    		temp_Int = cursor.getInt(0);
    	}
    	cursor.close(); 
    	T_Order.setText("������" + temp_Int + " ����");
    	data_Total = temp_Int * 10 + temp_Int_1;
    	
    	
    	cursor=db.rawQuery("select count(_id) from Order_List_Table WHERE (Status = '4' OR Status = '5') AND dateLimit < ?",new String[]{End_time});
    	if(cursor.moveToNext()){
    		temp_Int_1 = cursor.getInt(0);
    	}
    	cursor.close(); 
    	
    	cursor=db.rawQuery("select count(_id) from Table_Log_Record WHERE Date_Time < ?",new String[]{End_time});
    	if(cursor.moveToNext()){
    		temp_Int_1 = 10 * temp_Int_1 + cursor.getInt(0);
    	}
    	cursor.close(); 
    	
    	db.close();
    	temp_Int_1 = 100 * temp_Int_1 / data_Total;
    	T_Result.setText("������� " + temp_Int_1 + " % �����ݡ�");
    	
		Spinner spinner  = (Spinner) findViewById(R.id.spinner_Select);
		
		List<String> list1 = new ArrayList<String>();
		list1.add(0, "һ����ǰ");
		list1.add(1, "������ǰ");
		list1.add(2, "һ����ǰ");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list1);    
		//��������Ϊ���������������б�����ʱ�Ĳ˵���ʽ��    
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
		//���Ĳ�������������ӵ������б���    
		spinner.setAdapter(adapter); 
		
		spinner.setSelection(1);
		
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				String End_time;
				long This_DaysMili, twoDaysAgoMili, Browse_End;
				Cursor cursor;
				
				int temp_Int_1 = 0;
				Settle_Time = position;
				
				SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    		   
	    		Date now=new Date();
	    		This_DaysMili = now.getTime();
	    		   
				if (position == 0) {
					//����һ����ǰ������
					Browse_End = 30;
				}
				else if (position == 1) {
					//����������ǰ������
					Browse_End = 100;
				}
				else if (position == 2) {
					//����һ����ǰ������
					Browse_End = 365;
				}
				else {
					return;
				}
				
				twoDaysAgoMili=This_DaysMili-24*1000*60*60 * Browse_End;
				Date twodaysago=new Date((long) twoDaysAgoMili);
	    		End_time = sDateFormat.format(twodaysago);
	    		
	    		SQLiteDatabase db=dbHlper.getWritableDatabase();
	    		
	    		cursor=db.rawQuery("select count(_id) from Order_List_Table WHERE (Status = '4' OR Status = '5') AND dateLimit < ?",new String[]{End_time});
	        	if(cursor.moveToNext()){
	        		temp_Int_1 = cursor.getInt(0);
	        	}
	        	cursor.close(); 
	        	
	        	cursor=db.rawQuery("select count(_id) from Table_Log_Record WHERE Date_Time < ?",new String[]{End_time});
	        	if(cursor.moveToNext()){
	        		temp_Int_1 = 10 * temp_Int_1 + cursor.getInt(0);
	        	}
	        	cursor.close(); 
	        	
	        	db.close();
	        	temp_Int_1 = 100 * temp_Int_1 / data_Total;
	        	T_Result.setText("������� " + temp_Int_1 + " % �����ݡ�");
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//�Զ����ɵķ������
				
			}

  
		});    

	}
    
	
	//�Զ��嵥���¼���
		class ClickEvent implements View.OnClickListener {    
			@Override    

			public void onClick(View v) {
				Cursor cursor;
				int temp_Int = 0;
				String temp_Str, order_ID;
				String End_time;
				long This_DaysMili, twoDaysAgoMili, Browse_End = 0;
				
		    	switch(v.getId()){
		    	   case R.id.button2:
		    		   //����ȡ������
		    		   dbHlper.close();
		    		   finish();
		    		   
		    		   break;
		    		   
		    	   case R.id.button1:
		    		   //����ȷ������
		    		   
		    		   SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    		   
		    		   Date now=new Date();
		    		   This_DaysMili = now.getTime();
			    		
		    		   SQLiteDatabase db=dbHlper.getWritableDatabase();
		    		   		    		   
		    		   if (Settle_Time == 0) {
		    			   temp_Str = "һ����ǰ";
		    			   Browse_End = 30;
		    		   }
		    		   else if (Settle_Time == 1) {
		    			   temp_Str = "������ǰ";
		    			   Browse_End = 100;
		    		   }
		    		   else if (Settle_Time == 0) {
		    			   temp_Str = "һ��ǰ";
		    			   Browse_End = 365;
		    		   }
		    		   else {
		    			   temp_Str = "��";
		    		   }
		    		   
		    		   twoDaysAgoMili=This_DaysMili-24*1000*60*60 * Browse_End;
		    		   Date twodaysago=new Date((long) twoDaysAgoMili);
		    		   End_time = sDateFormat.format(twodaysago);
		    		   
		    		   db.execSQL("delete from Table_Log_Record WHERE Date_Time < ?", new String[]{End_time});
		    		   
		    		   cursor=db.rawQuery("select * from Order_List_Table WHERE (Status = '4' OR Status = '5') AND dateLimit < ?",new String[]{End_time});
		    		   while (cursor.moveToNext()){
		    			   order_ID = cursor.getString(2);
		    			   
		    			   db.execSQL("DELETE FROM Order_List_Table WHERE Order_ID = ?", new String[]{order_ID});
		    			   db.execSQL("DELETE FROM Order_Data_Table WHERE Order_ID = ?", new String[]{order_ID});

		    		   }
		    		   cursor.close(); 
		    		   
		    		   db.execSQL("delete from Order_List_Table WHERE (Status = '4' OR Status = '5') AND dateLimit < ?",new String[]{End_time});
		    		    
		    		   //������־
		    		   DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "�����ʷ����", temp_Str);

		    		   db.close();
		    		   
		    		   dbHlper.close();
		    		   
		    		   finish();

		    		   break;
		    		
		    	}
			}
		}
	
	
}
