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
	//ͳһ�ص�����
	private String workID, Str_status;
	private Button Finish_Order_BTN;
	DBHelper dbHelper;
	Context context=Updata_BackOrder_Activity.this;
	TextView Order_Statu;
	static NMS_Communication Connect_NMS;
	
	int[] Send_Frame_Numb = new int[2];			//0λ�ñ��浱ǰ֡�ţ���1��ʼ��1λ�ñ��浱ǰҪ�ش���ָ����֡��
	
	protected void onCreate(Bundle savedInstanceState) {
		TextView OrderID, Activity_Title;
		Button Return_Order_BTN;
		String Str_Status = "0";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_backorder);
        setTitle("�ص�");
               
        Intent in=getIntent();
        workID=in.getStringExtra("workID");
        dbHelper = new DBHelper(context);//��ʼ�����ݿ����	
		//�ؼ���ʼ��
            	
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
	    	Str_Status = cursor.getString(5);	//ȡ����״̬
	    }
	    
	    switch(Str_Status) {
	    case "0":
	    	//δʩ��
	    	Return_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setText("�ص�");
			Order_Statu.setText("��δʩ��");
	    	
	    	break;
	    	
	    case "1":
	    	//δʩ��
	    	Return_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setText("�ص�");
			Order_Statu.setText("��δʩ��");
	    	
	    	break;
	    	
	    case "2":
	    	//����ʩ��
	    	Return_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setText("������ɣ�ǿ�ƻص�");
			Order_Statu.setText("�������");
	    	break;
	    	
	    case "3":
	    	//�깤
	    	Return_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setEnabled(true);
			Finish_Order_BTN.setText("�깤�ص�");
			Order_Statu.setText("ȫ�����");
	    	break;
	    	
	    default:
	    	//����������ܻص�
	    	Return_Order_BTN.setEnabled(false);
			Finish_Order_BTN.setEnabled(false);
			Order_Statu.setText("");
	    }
		
	    String temp_Str = workID.substring(0, 6);
		if (temp_Str.equals("MAINTE")) {
			Activity_Title.setText("ά������");
		}
		else if (temp_Str.equals("INSERT")) {
			Activity_Title.setText("�½�����");
		}
		else if (temp_Str.equals("MOVETO")) {
			Activity_Title.setText("�ƻ�����");
		}
		else if (temp_Str.equals("REMOVE")) {
			Activity_Title.setText("�������");
		}
		
    }
	
	

	//�Զ��嵥���¼���
	class ClickEvent implements View.OnClickListener {    
		@Override    

		public void onClick(View v) {

	    	   //Intent intent = new Intent();   //����Intent���� 
	    	   switch(v.getId()){
	    	   case R.id.btn_return_order:
	    		   //�˵�
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6211_R");
	   			   Connect_NMS.Make_Socket_Connect();

	    		   break;
	    		   
	    	   case R.id.btn_Finshin_order:
	    		   //�ص�
	    		   Send_Frame_Numb[0] = 0;
	    		   Send_Frame_Numb[1] = 0;
	  				
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6211_C");
	   			   Connect_NMS.Make_Socket_Connect();
	   			   
	    		   break;
	    	   }
	    	   
		}
	}
	
	
	//����Handler����
  	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			int temp_Int = 0, temp_Int_1 = 0;
  			String temp_Str, Str_Date;

  			switch(msg.what){
  			case 0:
  				//Ӧ�����
  				String s=msg.obj.toString();
  				  				
  				if (s.equals("6211 F") || s.equals("6211 C")) {
  					//�ص��ɹ�
  					
  					
  					
  					if (Str_status.equals("Finish")) {
  						//�ص��ɹ�
  						if (s.equals("6211 C")) {
  							SQLiteDatabase db=dbHelper.getWritableDatabase();
  							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "�ص����ò�����", workID);
  							db.close();
  							
  							AlertDialog dialog = new AlertDialog.Builder(context)
  	  							.setTitle("�ص��д�")			//���öԻ���ı���
  	  							.setMessage("�ص����ò����� ��")	//��ʾ�������豸��
  	  							.setPositiveButton("ȷ��", null)
  	  							.create();
  	                			
  							dialog.show();
  						}
  						
  						if (Send_Frame_Numb[0] < Send_Frame_Numb[1]) {
  							//�к���֡Ҫ����
  							Str_status = "Finish";
  							Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6211_C");
  			   			   	Connect_NMS.Make_Socket_Connect();
  			   			   
  	  	  					//Connect_NMS.Wait_Recive_TCP_Reply();	
  	  	  					//Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//�����ص�����֡
  						}
  						else {	
  							//��ɻص����ݵķ��ͣ������ɹ�����
  							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");// HH:mm:ss
  		  					//��ȡ��ǰʱ��
  		  					Date date = new Date(System.currentTimeMillis());
  		  					Str_Date = simpleDateFormat.format(date);
  		  					
  							SQLiteDatabase db=dbHelper.getWritableDatabase();
  							db.execSQL("UPDATE Order_List_Table SET Status = '5', back_date = ?  WHERE Order_ID = ?", new String[]{Str_Date, workID});
  							
  							temp_Str = workID.substring(0, 6);
  							if (temp_Str.equals("INSERT")) {
  								if (s.equals("6211 C")) {
  									temp_Str = "�깤�ò��ص�";
  								}
  								else {
  									temp_Str = "�깤�ص�";
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
							    	temp_Str = "�ò��ص����깤�� " + String.valueOf(temp_Int) + "��";
  								}
  								else {
  									temp_Str = "�ص����깤�� " + String.valueOf(temp_Int) + "��";
  								}
  							}
  							
  							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, temp_Str, workID);
  							db.close();
  							
  		  		    	   	Intent in=new Intent(); 
  		  		    	   	in.setAction("Close_BackOrder"); 		//���ͻص�ҳ�汻�رչ㲥
  		  		    	   	//in.putExtra("result", ""); 
  		  		    	   	sendBroadcast(in);
  		  		      		
  		  		    	   	dbHelper.close();
  		  		    	   	Updata_BackOrder_Activity.this.finish();		//�رյ�ǰ����
  		  					  		  					
  						}
  						
  					}
  					else if (Str_status.equals("Cancel")) {
  						//�˵��ɹ�
  						SQLiteDatabase db=dbHelper.getWritableDatabase();
						db.execSQL("UPDATE Order_List_Table SET Status = '4' WHERE Order_ID = ?", new String[]{workID});
						
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "�˵�", workID);
						db.close();
  						
  	  		    	   	Intent in=new Intent(); 
  	  		    	   	in.setAction("Close_BackOrder"); 		//���ͻص�ҳ�汻�رչ㲥
  	  		    	   	//in.putExtra("result", ""); 
  	  		    	   	sendBroadcast(in);
  	  		      		
  	  		    	   	dbHelper.close();
  	  		    	   	Updata_BackOrder_Activity.this.finish();		//�رյ�ǰ����
  	  					
  					}
  				}
  				else if (s.equals("6211 E")) {
  					//�ص�ʧ��
  					AlertDialog dialog = new AlertDialog.Builder(context)
  						.setTitle("��ʾ")			//���öԻ���ı���
  						.setMessage("�ص�ʧ�ܣ������»ص� ��")	//��ʾ�������豸��
  						//���öԻ���İ�ť
  						.setPositiveButton("ȷ��", null)
  						.create();
                			
  					dialog.show();
  				}
  				break;
  				  				
  			case 2:
  				s=msg.obj.toString();
  				
  				if (s.equals("Wait_Send_6211_C")) {
  					//�ص�
  					Str_status = "Finish";
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  					Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//�깤�ص�
  					
  					//Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//�깤�ص�
  					
  					//Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, true, Send_Frame_Numb);		//�깤�ص�
  					
  				}
  				else if (s.equals("Wait_Send_6211_R")) {
  					//�˵�
  					Str_status = "Cancel";
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  					Send_Frame_Numb = NMS_Communication.BackOrder_6211(workID, dbHelper, false, Send_Frame_Numb);		//�˵�
  					
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
