package com.example.iods_manage;



import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;



public class Net_Config_Activity extends Activity {
	//网络配置页面
	
	private EditText text_WebAddress, text_Port;
	private SeekBar Lock_seekBar;
	LinearLayout Backgroup;
	
	private DBHelper dbHlper;	//数据库操作对象
	
	private Context context = Net_Config_Activity.this;//定义Context对象
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_config);
        setTitle("网络配置");
        
        Backgroup = (LinearLayout)findViewById(R.id.TableLayout1);
        
        Show_Backgroup_PCS();

		
        
        dbHlper = new DBHelper(context);//获取数据库服务对象实例

		initi();
		
    }
    
    
    private void Show_Backgroup_PCS() {
		// 显示背景图案
    	switch (NMS_Communication.index_backgroup) {
		case 1:
			Backgroup.setBackgroundResource(R.drawable.backgroup_1);
			break;
		case 2:
			Backgroup.setBackgroundResource(R.drawable.backgroup_2);
			break;
		case 3:
			Backgroup.setBackgroundResource(R.drawable.backgroup_3);
			break;
		case 4:
			Backgroup.setBackgroundResource(R.drawable.backgroup_4);
			break;
		case 5:
			Backgroup.setBackgroundResource(R.drawable.backgroup_5);
			break;
		case 6:
			Backgroup.setBackgroundResource(R.drawable.backgroup_6);
			break;
		case 7:
			Backgroup.setBackgroundResource(R.drawable.backgroup_7);
			break;
		case 8:
			Backgroup.setBackgroundResource(R.drawable.backgroup_8);
			break;
		case 9:
			Backgroup.setBackgroundResource(R.drawable.backgroup_9);
			break;
		case 10:
			Backgroup.setBackgroundResource(R.drawable.backgroup_10);
			break;
		case 11:
			Backgroup.setBackgroundResource(R.drawable.backgroup_11);
			break;
		case 12:
			Backgroup.setBackgroundResource(R.drawable.backgroup_12);
			break;
		default:
			Backgroup.setBackgroundResource(0);
			break;
    	}
	}


	public void initi(){

		Button Net_Cancel_Btn, Net_OK_Btn, Last_pcs_Btn, Next_pcs_Btn;
		
		Net_Cancel_Btn = (Button)findViewById(R.id.btn_Net_Cancel);
		Net_OK_Btn = (Button)findViewById(R.id.btn_Net_OK);
		Last_pcs_Btn = (Button)findViewById(R.id.button1);
		Next_pcs_Btn = (Button)findViewById(R.id.button2);
		
		Net_OK_Btn.setOnClickListener(new ClickEvent());
		Net_Cancel_Btn.setOnClickListener(new ClickEvent());
		Last_pcs_Btn.setOnClickListener(new ClickEvent());
		Next_pcs_Btn.setOnClickListener(new ClickEvent());
		
		text_WebAddress = (EditText)findViewById(R.id.editText_Address);
		text_Port = (EditText)findViewById(R.id.editText_Space);
		
		text_WebAddress.setText(NMS_Communication.NMS_Address);
		text_Port.setText(String.valueOf(NMS_Communication.NMS_Port));
		
		Lock_seekBar = (SeekBar) findViewById(R.id.seekBar1);  
		
		Lock_seekBar.setProgress(UserLogin_Activity.Lock_Search_Scope - 1);
	}
    
	
	//自定义单击事件类
    class ClickEvent implements View.OnClickListener {    
		@Override    

		public void onClick(View v) {
			Cursor cursor;
			int temp_Int = 0;
			String temp_Str;
			
	    	switch(v.getId()){
	    	   case R.id.btn_Net_Cancel:
	    		   //配置取消按键
	    		   dbHlper.close();
	    		   finish();
	    		   
	    		   break;
	    		   
	    	   case R.id.btn_Net_OK:
	    		   //网络配置保存按键
	    		   NMS_Communication.NMS_Address = text_WebAddress.getText().toString();
	    		   NMS_Communication.NMS_Port = Integer.valueOf(text_Port.getText().toString());
	    		   
	    		   UserLogin_Activity.Lock_Search_Scope = Lock_seekBar.getProgress() + 1;
	    		   String Str_SeekBar = String.valueOf(UserLogin_Activity.Lock_Search_Scope);
	    		   
	    		   SQLiteDatabase db=dbHlper.getWritableDatabase();
	    		   
	    		   cursor=db.rawQuery("SELECT * FROM Basic_Config_Table",new String[]{});
	    		   if(cursor.moveToNext()){
	    			   temp_Int = 1;
	    		   }
			
	    		   temp_Str = String.valueOf(NMS_Communication.index_backgroup);
	    		   if (temp_Int == 1) {
	    			   db.execSQL("UPDATE Basic_Config_Table SET NMSAdrres = ?, NMSPort = ?, Search_Scope = ?, Breakgroup = ?", new String[]{text_WebAddress.getText().toString(), text_Port.getText().toString(), Str_SeekBar, temp_Str});
	    		   }
	    		   else {
	    			   String Str_sql = "INSERT INTO Basic_Config_Table (NMSAdrres, NMSPort, Search_Scope, Breakgroup) VALUES (?, ?, ?, ?)";
	    			   db.execSQL(Str_sql, new String[]{NMS_Communication.NMS_Address, text_Port.getText().toString(), Str_SeekBar, temp_Str});			
	    		   }
	    		   
	    		   //保存日志
	    		   DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "修改并保存网络配置", "");

	    		   db.close();
	    		   
	    		   dbHlper.close();
	    		   	    		   
	    		   finish();

	    		   break;
	    		   
	    	   case R.id.button1:
	    		   //配置取消按键
	    		   NMS_Communication.index_backgroup --;
	    		   if (NMS_Communication.index_backgroup < 0) {
	    			   NMS_Communication.index_backgroup = 12;
	    		   }
	    		   Show_Backgroup_PCS();
	    		   break;
	    		   
	    	   case R.id.button2:
	    		   //配置取消按键
	    		   NMS_Communication.index_backgroup ++;
	    		   if (NMS_Communication.index_backgroup > 12) {
	    			   NMS_Communication.index_backgroup = 0;
	    		   }
	    		   Show_Backgroup_PCS();
	    		   break;

	    	}
		}
	}
		
	protected void onDestroy() {

		super.onDestroy();
		dbHlper.close();

	}
	
}
