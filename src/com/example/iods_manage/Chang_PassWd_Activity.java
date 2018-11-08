package com.example.iods_manage;


import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Chang_PassWd_Activity   extends Activity {
	//�����û���Ϣ����
	CheckBox ShowPwd;//��ʾ���빴ѡ��
    EditText Old_Password, New_Password, Check_Password, New_TelePhone;
    TextView Show_Name, TextV_Old, TextV_New, TexeV_Check, TexeV_Phone;
    
    String  Str_New, User_passwd, Work_Mode, User_Name, User_ID, Query_ID;
    
    static NMS_Communication Connect_NMS;
    
    private Context context = Chang_PassWd_Activity.this;//����Context����
    
    DBHelper dbHelper ;//���ݿ�������
	
	 protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_chang_passwd);
	     setTitle("��ͨ���");
	     
	     Intent in=getIntent();
	     Work_Mode=in.getStringExtra("Mode");		//����ģʽ��"Insert"������û���"Self"�޸��Լ���Ϣ��"Change"�޸�������Ϣ
	     Query_ID=in.getStringExtra("Query");
	     
	     initi();
	 }
	 
	 public void initi(){
	     Button OK_Btn;
	     
	     dbHelper = new DBHelper(context);	//����DBHlper����ʵ��
	     
	     Old_Password = (EditText)findViewById(R.id.editText1);
	     New_Password = (EditText)findViewById(R.id.editText2);
	     Check_Password = (EditText)findViewById(R.id.editText3);
	     ShowPwd = (CheckBox)findViewById(R.id.checkBox1);
	     New_TelePhone = (EditText)findViewById(R.id.editText4);
	     OK_Btn = (Button)findViewById(R.id.button1);

	     Show_Name=(TextView)findViewById(R.id.Search_text_1);
	     TextV_Old=(TextView)findViewById(R.id.textView3);
	     TextV_New=(TextView)findViewById(R.id.textView2);
	     TexeV_Check=(TextView)findViewById(R.id.textView6);
	     TexeV_Phone=(TextView)findViewById(R.id.textView7);
	     
	     if (Work_Mode.equals("Insert") || Work_Mode.equals("Change")) {
	    	 if (Work_Mode.equals("Insert")) {
	    		 Show_Name.setText("����²����û�");
	    	 }
	    	 else {
	    		 Show_Name.setText("�޸��û���Ϣ");
	    		 Old_Password.setEnabled(false);	//�û�ID���ɱ༭
	    	 }

		     TextV_Old.setText("�û�ID��");
		     TextV_New.setText("�û����룺");
		     TexeV_Check.setText("��ʵ������");
		     TexeV_Phone.setText("�û��绰��");
		     
		     // ���ı�����������ó�������ʾ 
		     Old_Password.setTransformationMethod(HideReturnsTransformationMethod .getInstance());
		     Check_Password.setTransformationMethod(HideReturnsTransformationMethod .getInstance()); 
		     
		     ShowPwd.setVisibility(View.INVISIBLE);		//����ʾ
	     }
	     else {
	    	 Show_Name.setText("�޸��û���Ϣ");
	    	 TextV_Old.setText("�����������룺");
		     TextV_New.setText("���������룺");
		     TexeV_Check.setText("ȷ�������룺");
		     TexeV_Phone.setText("���ĵ绰��");
		     
		     // ���ı����������ó����ĵķ�ʽ��ʾ 
		     Old_Password.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
		     New_Password.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
		     Check_Password.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
		     ShowPwd.setVisibility(View.VISIBLE);		//��ʾ
	     }
	        
	     ShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					//����������ʾ��ʽ
					if (ShowPwd.isChecked()){//��ѡ����ѡ��״̬
						// ���ı�����������ó�������ʾ 
						Old_Password.setTransformationMethod(HideReturnsTransformationMethod .getInstance());
						New_Password.setTransformationMethod(HideReturnsTransformationMethod .getInstance()); 
						Check_Password.setTransformationMethod(HideReturnsTransformationMethod .getInstance()); 
					} else{//��ѡ��ȡ��ѡ��
						// ���ı����������ó����ĵķ�ʽ��ʾ 
						Old_Password.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
						New_Password.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
						Check_Password.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
					}
				}			
		});	
	     
	     
	     OK_Btn.setOnClickListener(new OnClickListener() {

	    	 public void onClick(View v) {
	    		 String Str_Old, Str_Check, Str_Phone;
	    		 AlertDialog dialog;
	    		 
	    		 Str_Old = Old_Password.getText().toString();
	    		 Str_New = New_Password.getText().toString();
	    		 Str_Check = Check_Password.getText().toString();
	    		 Str_Phone  = New_TelePhone.getText().toString();
	    		 
	    		 if (Work_Mode.equals("Insert") || Work_Mode.equals("Change")) {
	    			 if (Str_Old.length() == 0 || Str_New.length() == 0 || Str_Check.length() == 0 || Str_Phone.length() == 0) {
	    				 dialog = new AlertDialog.Builder(context)
							.setTitle("��ʾ")			//���öԻ���ı���
							.setMessage("������δ��д�")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();
	    			 }
	    			 else {
	    				 Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6216");
	    				 Connect_NMS.Make_Socket_Connect();
	    			 }
	    		 }
	    		 else {
	    			 if (Str_New.equals(Str_Old)) {
		    			 dialog = new AlertDialog.Builder(context)
							.setTitle("��ʾ")			//���öԻ���ı���
							.setMessage("�����벻������������ͬ��")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();
						
						return;
		    		 }
		    		 
		    		 if (! Str_New.equals(Str_Check)) {
		    			 dialog = new AlertDialog.Builder(context)
							.setTitle("��ʾ")			//���öԻ���ı���
							.setMessage("��������������벻һ�¡�")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();
						
						return;
		    		 }
		    		 
		    		 if (User_passwd.equals(Str_Old)) {
			    		//�����ܽ���TCP����
			    		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6216");
			    		Connect_NMS.Make_Socket_Connect();
		    		 }
		    		 else {
		    			dialog = new AlertDialog.Builder(context)
							.setTitle("��ʾ")			//���öԻ���ı���
							.setMessage("�������������")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();
		    		 }
	    		 }
	    	 }

	     });

	 }
	 
	 //����Handler����
	 Handler mHandler = new Handler(){
		 public void handleMessage(Message msg){
	  			String s, Str_Temp, Str_Temp_1;
	  			int i;

	  			switch(msg.what){
	  			case 0:
	  				//Ӧ�����
	  				s=msg.obj.toString();
	  				Str_Temp = s.substring(0, 6);
	  				
	  				if (Str_Temp.equals("6216 F")) {
	  					if (s.substring(7).equals("00")) {
	  					//����û���Ϣ����
		  					SQLiteDatabase db=dbHelper.getWritableDatabase();
	  						db.execSQL("UPDATE User_Record_Table SET UserPassWd = ?  WHERE UserID = ?", new String[]{Str_New, UserLogin_Activity.Login_User_ID});		
	  						
	  						if (Work_Mode.equals("Self")) {
	  							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "�޸��û�������Ϣ", "");
	  						}
	  						else if (Work_Mode.equals("Change")) {
	  							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "�����û���Ϣ", User_ID);
	  						}
	  						else  if (Work_Mode.equals("Insert")) {
	  							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "������û�", User_ID);
	  						}
	  						
	  						db.close();
	  							  						
	  						Intent in=new Intent(); 
  		  		    	   	in.setAction("Flash_UserList"); 		//����ˢ���û��б�㲥
  		  		    	   	//in.putExtra("result", ""); 
  		  		    	   	sendBroadcast(in);
  		  		    	   	
  		  		    	   	finish();
	  					}
	  					else if (s.substring(7).equals("11")) {
	  						AlertDialog dialog = new AlertDialog.Builder(context)
	  							.setTitle("��ʾ")			//���öԻ���ı���
	  							.setMessage("����ĵ绰�����ѱ�ռ�á��������롣")
	  							//���öԻ���İ�ť
	  							.setPositiveButton("ȷ��", null)
	  							.create();
	  						dialog.show();	  							  					
	  					}
	  					else if (Work_Mode.equals("Self") || Work_Mode.equals("Change")) {
	  						AlertDialog dialog = new AlertDialog.Builder(context)
	  							.setTitle("��ʾ")			//���öԻ���ı���
	  							.setMessage("�û���Ϣ����ʧ��")
	  							//���öԻ���İ�ť
	  							.setPositiveButton("ȷ��", null)
	  							.create();
	  						dialog.show();
	  					}
	  					else if (Work_Mode.equals("Insert")) {
	  						AlertDialog dialog = new AlertDialog.Builder(context)
	  							.setTitle("��ʾ")			//���öԻ���ı���
	  							.setMessage("������û�ʧ��")
	  							//���öԻ���İ�ť
	  							.setPositiveButton("ȷ��", null)
	  							.create();
	  						dialog.show();
	  					}
	  				}
	  				else if (Str_Temp.equals("6207 F")) {
	  					Str_Temp = s.substring(7);
	  					i = Str_Temp.indexOf(" ");
	  					if (i > 0) {
	  						Str_Temp_1 = Str_Temp.substring(0, i);
	  						User_ID = Str_Temp_1;
	  						User_passwd = Str_Temp.substring(i+1);
  							i = User_passwd.lastIndexOf(" ");
  							if (i > 0) {
  								Str_Temp_1 = User_passwd.substring(i + 1);
  								User_Name = Str_Temp_1;
  									  								
  								Str_Temp_1 = User_passwd.substring(0, i);
  								
  								i = Str_Temp_1.lastIndexOf(" ");
	  							if (i > 0) {
	  								Str_Temp_1 = Str_Temp_1.substring(i+1);
	  								User_passwd = User_passwd.substring(0, i); 	
	  								New_TelePhone.setText(Str_Temp_1);
	  								if (Work_Mode.equals("Self")) {
	  									Show_Name.setText("���� " + User_Name + " ����Ϣ");					
	  								}
	  								else {
	  									Old_Password.setText(User_ID);
	  								    New_Password.setText(User_passwd);
	  								    Check_Password.setText(User_Name);
	  								}
	  							}
	  							else {
	  								AlertDialog dialog = new AlertDialog.Builder(context)
	  					 			.setTitle("��ʾ")			//���öԻ���ı���
	  					 			.setMessage("ͨ�ų��� ��")
	  					 			//���öԻ���İ�ť
	  					 			.setPositiveButton("ȷ��", null)
	  					 			.create();
	  								dialog.show();
	  								finish();
	  							}
  							}
  							else {
  								AlertDialog dialog = new AlertDialog.Builder(context)
  					 			.setTitle("��ʾ")			//���öԻ���ı���
  					 			.setMessage("ͨ�ų��� ��")
  					 			//���öԻ���İ�ť
  					 			.setPositiveButton("ȷ��", null)
  					 			.create();
  								dialog.show();
  								finish();
  							}
	  					}
	  					else {
	  						AlertDialog dialog = new AlertDialog.Builder(context)
					 			.setTitle("��ʾ")			//���öԻ���ı���
					 			.setMessage("ͨ�ų��� ��")
					 			//���öԻ���İ�ť
					 			.setPositiveButton("ȷ��", null)
					 			.create();
								dialog.show();
								finish();
	  					}
	  				}
	  				else {
	  					AlertDialog dialog = new AlertDialog.Builder(context)
				 			.setTitle("��ʾ")			//���öԻ���ı���
				 			.setMessage("ͨ�ų��� ��")
				 			//���öԻ���İ�ť
				 			.setPositiveButton("ȷ��", null)
				 			.create();
							dialog.show();
							finish();
	  				}
	  					  				
	  				break;
	  				
	  			case 2:
	  				s=msg.obj.toString();

	  				if (s.equals("Wait_Send_6216")) {
	  					if (Work_Mode.equals("Self")) {
	  						Str_Temp = New_TelePhone.getText().toString();
		  					Connect_NMS.Wait_Recive_TCP_Reply();	
		  			    	NMS_Communication.Update_User_Inform_6216("", UserLogin_Activity.Login_User_ID, Str_New, Str_Temp, User_Name, 6);		//���͸����û���Ϣָ��
	  					}
	  					else {
	  						Str_New = New_Password.getText().toString();
	  						User_ID = Old_Password.getText().toString();
	  						User_Name = Check_Password.getText().toString();
	  						Str_Temp = New_TelePhone.getText().toString();

	  						if (Work_Mode.equals("Change") || Work_Mode.equals("Insert")) {
	  							i = 6;
	  						}
	  						else {
	  							i = 0;
	  						}
	  						Connect_NMS.Wait_Recive_TCP_Reply();	
	  						NMS_Communication.Update_User_Inform_6216(UserLogin_Activity.Login_User_ID, User_ID, Str_New, Str_Temp, User_Name, i);		//���͸��»�����û���Ϣָ��
	  					}
	  				}
	  				else if (s.equals("Wait_Send_6207")) {
	  					Connect_NMS.Wait_Recive_TCP_Reply();
	  					NMS_Communication.Read_User_Inform_6207(Query_ID);		//����ȡ�û���Ϣָ��
	  			    	
	  				}
	  				break;
	  			}
	  		}

		 	
	  	};
	  	
	  	protected void onResume() {

			super.onResume();
			
			if (Work_Mode.equals("Self") || Work_Mode.equals("Change")) {
		    	 //ȡ�û���Ϣ
		    	 Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6207");
			     Connect_NMS.Make_Socket_Connect();
		    }
		}
	    
		protected void onDestroy() {

			super.onDestroy();
			dbHelper.close();

		}
}
