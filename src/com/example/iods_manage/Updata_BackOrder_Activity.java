package com.example.iods_manage;



import java.util.Date;

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
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Updata_BackOrder_Activity extends Activity{
	//统一回单界面
	private String workID, Str_status;
	private Button Finish_Order_BTN;
	DBHelper dbHelper;
	Context context=Updata_BackOrder_Activity.this;
	TextView Order_Statu;
	static NMS_Communication Connect_NMS;
	
	int[] Send_Frame_Numb = new int[2];			//0位置保存当前帧号，从1开始；1位置保存当前要回传的指令总帧数
	
	protected void onCreate(Bundle savedInstanceState) {
		TextView OrderID, Activity_Title;
		Button Return_Order_BTN;
		String Str_Status = "0";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_backorder);
        setTitle("回单");
               
        Intent in=getIntent();
        workID=in.getStringExtra("workID");
        dbHelper = new DBHelper(context);//初始化数据库对象	
		//控件初始化
            	
		OrderID=(TextView)findViewById(R.id.Order_ID_3);
		Order_Statu=(TextView)findViewById(R.id.Order_Statu);
		Activity_Title = (TextView)findViewById(R.id.Write_Tag_PWD_Title);
    	
    	Return_Order_BTN = (Button)findViewById(R.id.btn_return_order);
    	Finish_Order_BTN = (Button)findViewById(R.id.btn_Finshin_order);

    	
    	Return_Order_BTN.setOnClickListener(new ClickEvent());
    	Finish_Order_BTN.setOnClickListener(new ClickEvent());
    	
    	OrderID.setText(workID);
    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		cursor=db.rawQuery("select * from Order_List_Table where Order_ID = ? AND status < 4 ORDER BY Order_Type", new String[]{workID});    	
	      
	    while(cursor.moveToNext()){
	    	Str_Status = cursor.getString(5);	//取工单状态
	    }
	    
	    switch(Str_Status) {
	    case "0":
	    	//未施工
	    	Return_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setText("回单");
			Order_Statu.setText("尚未施工");
	    	
	    	break;
	    	
	    case "1":
	    	//未施工
	    	Return_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setText("回单");
			Order_Statu.setText("尚未施工");
	    	
	    	break;
	    	
	    case "2":
	    	//部分施工
	    	Return_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setText("部分完成，强制回单");
			Order_Statu.setText("部分完成");
	    	break;
	    	
	    case "3":
	    	//完工
	    	Return_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setText("完工回单");
			Order_Statu.setText("全部完成");
	    	break;
	    	
	    default:
	    	//其它情况不能回单
	    	Return_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setEnabled(false);
			Order_Statu.setText("");
	    }
		
	    String temp_Str = workID.substring(0, 6);
		if (temp_Str.equals("MAINTE")) {
			Activity_Title.setText("维护工单");
		}
		else if (temp_Str.equals("INSERT")) {
			Activity_Title.setText("新建工单");
		}
		else if (temp_Str.equals("MOVETO")) {
			Activity_Title.setText("移机工单");
		}
		else if (temp_Str.equals("REMOVE")) {
			Activity_Title.setText("拆除工单");
		}
		
    }
	
	

	//自定义单击事件类
	class ClickEvent implements View.OnClickListener {    
		@Override    

		public void onClick(View v) {

	    	   //Intent intent = new Intent();   //创建Intent对象 
	    	   switch(v.getId()){
	    	   case R.id.btn_return_order:
	    		   //退单
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6211_R");
	   			   Connect_NMS.Make_Socket_Connect();

	    		   break;
	    		   
	    	   case R.id.btn_Finshin_order:
	    		   //回单
	    		   Send_Frame_Numb[0] = 0;
	    		   Send_Frame_Numb[1] = 0;
	  				
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6211_C");
	   			   Connect_NMS.Make_Socket_Connect();
	   			   
	    		   break;
	    	   }
	    	   
		}
	}
	
	
	//定义Handler对象
  	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			int temp_Int = 0, temp_Int_1 = 0;
  			String temp_Str, Str_Date;

  			switch(msg.what){
  			case 0:
  				//应答分析
  				String s=msg.obj.toString();
  				  				
  				if (s.equals("6211 F") || s.equals("6211 C")) {
  					//回单成功
  					
  					
  					
  					if (Str_status.equals("Finish")) {
  						//回单成功
  						if (s.equals("6211 C")) {
  							SQLiteDatabase db=dbHelper.getWritableDatabase();
  							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "回单被让步接受", workID);
  							db.close();
  							
  							AlertDialog dialog = new AlertDialog.Builder(context)
  	  							.setTitle("回单有错")			//设置对话框的标题
  	  							.setMessage("回单被让步接受 ！")	//显示锁蓝牙设备名
  	  							.setPositiveButton("确定", null)
  	  							.create();
  	                			
  							dialog.show();
  						}
  						
  						if (Send_Frame_Numb[0] < Send_Frame_Numb[1]) {
  							//有后续帧要发送
  							Str_status = "Finish";
  							Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6211_C");
  			   			   	Connect_NMS.Make_Socket_Connect();
  			   			   
  	  	  					//Connect_NMS.Wait_Recive_TCP_Reply();	
  	  	  					//Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//续传回单后续帧
  						}
  						else {	
  							//完成回单数据的发送，并被成功接收
  							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");// HH:mm:ss
  		  					//获取当前时间
  		  					Date date = new Date(System.currentTimeMillis());
  		  					Str_Date = simpleDateFormat.format(date);
  		  					
  							SQLiteDatabase db=dbHelper.getWritableDatabase();
  							db.execSQL("UPDATE Order_List_Table SET Status = '5', back_date = ?  WHERE Order_ID = ?", new String[]{Str_Date, workID});
  							
  							temp_Str = workID.substring(0, 6);
  							if (temp_Str.equals("INSERT")) {
  								if (s.equals("6211 C")) {
  									temp_Str = "完工让步回单";
  								}
  								else {
  									temp_Str = "完工回单";
  								}
  							}
  							else {
  								Cursor cursor=db.rawQuery("select count(*) from Order_Data_Table  where Order_ID = ?", new String[]{workID});    			    
  							    if (cursor.moveToNext()){
  							    	temp_Int = cursor.getInt(0);
  							    }
  							    cursor.close();
  							    
  							    cursor=db.rawQuery("select count(*) from Order_Data_Table  where Order_ID = ? and Status = '1'", new String[]{workID});    			    
							    if (cursor.moveToNext()){
							    	temp_Int_1 = 100 * cursor.getInt(0);
							    }
							    cursor.close();
							    temp_Int = temp_Int_1/temp_Int;
							    
							    if (s.equals("6211 C")) {
							    	temp_Str = "让步回单，完工率 " + String.valueOf(temp_Int) + "％";
  								}
  								else {
  									temp_Str = "回单，完工率 " + String.valueOf(temp_Int) + "％";
  								}
  							}
  							
  							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, temp_Str, workID);
  							db.close();
  							
  		  		    	   	Intent in=new Intent(); 
  		  		    	   	in.setAction("Close_BackOrder"); 		//发送回单页面被关闭广播
  		  		    	   	//in.putExtra("result", ""); 
  		  		    	   	sendBroadcast(in);
  		  		      		
  		  		    	   	dbHelper.close();
  		  		    	   	Updata_BackOrder_Activity.this.finish();		//关闭当前窗口
  		  					  		  					
  						}
  						
  					}
  					else if (Str_status.equals("Cancel")) {
  						//退单成功
  						SQLiteDatabase db=dbHelper.getWritableDatabase();
						db.execSQL("UPDATE Order_List_Table SET Status = '4' WHERE Order_ID = ?", new String[]{workID});
						
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "退单", workID);
						db.close();
  						
  	  		    	   	Intent in=new Intent(); 
  	  		    	   	in.setAction("Close_BackOrder"); 		//发送回单页面被关闭广播
  	  		    	   	//in.putExtra("result", ""); 
  	  		    	   	sendBroadcast(in);
  	  		      		
  	  		    	   	dbHelper.close();
  	  		    	   	Updata_BackOrder_Activity.this.finish();		//关闭当前窗口
  	  					
  					}
  				}
  				else if (s.equals("6211 E")) {
  					//回单失败
  					AlertDialog dialog = new AlertDialog.Builder(context)
  						.setTitle("提示")			//设置对话框的标题
  						.setMessage("回单失败，请重新回单 ！")	//显示锁蓝牙设备名
  						//设置对话框的按钮
  						.setPositiveButton("确定", null)
  						.create();
                			
  					dialog.show();
  				}
  				break;
  				  				
  			case 2:
  				s=msg.obj.toString();
  				
  				if (s.equals("Wait_Send_6211_C")) {
  					//回单
  					Str_status = "Finish";
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  					Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//完工回单
  					
  					//Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//完工回单
  					
  					//Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//完工回单
  					
  				}
  				else if (s.equals("Wait_Send_6211_R")) {
  					//退单
  					Str_status = "Cancel";
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  					Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, false, Send_Frame_Numb);		//退单
  					
  					temp_Int = 0;
  					
  				}
  				break;
  			}
  		}
  	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//unregisterReceiver(mReceiver);
		dbHelper.close();
	}	 
	
}
