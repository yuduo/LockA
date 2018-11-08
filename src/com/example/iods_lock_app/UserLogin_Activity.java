package com.example.iods_lock_app;





import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_common.Timmer_Beep;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

@SuppressLint("HandlerLeak")
public class UserLogin_Activity extends Activity {
	//�û���¼ҳ��
	
	public static Double error_Limit = 0.0001;		//GPS�����ݲ�
	public static Double Scope_Limit = 0.0002;		//GPS��Χ�޶�
	public static int Lock_Search_Scope;
	
	//static Socket_Recive User_Login_Accept;
	static Timmer_Beep Interrupt_User_Login;
	//static Make_TCP_Connect Connect_TCP;
	static NMS_Communication Connect_NMS;
	
	static int Lock_Numb_User;			//ˢ�µ��û�����
	static String Lock_Flash_Data;		//�û�����ˢ��ʱ��
		
	EditText mUsername;  //�û������û����������
	EditText mPassword;
	CheckBox mShowPwd;//��ʾ���빴ѡ��
	Button mLoginBtn, mExitBtn;         //��¼��ȡ����ť
    
    Intent intent;
    
    //��̬������ȡ�û���¼�������û��������롢�û�����
    public static String nameValue, Login_User_ID;
    public static String pwdValue;
	public static String Login_User_Type;
	
	private Context context = UserLogin_Activity.this;	//����Context����

    DBHelper dbHelper ;//���ݿ�������
    SQLiteDatabase dbr;
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_login);
		
		setTitle("��ͨ���");
		
		LinearLayout Backgroup = (LinearLayout)findViewById(R.id.LinearLayout1);

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
				//�ޱ���ͼ��
				break;
		}
		
		//context = this.getApplicationContext();	//���������Ķ���	
		
		dbHelper = new DBHelper(context);	//����DBHlper����ʵ��
		SQLiteDatabase db=dbHelper.getWritableDatabase();
      
		//������ͼ����ʵ��
		mUsername = (EditText)findViewById(R.id.User_id);	//�û��������		
		mPassword = (EditText)findViewById(R.id.User_PWD);	//�û����������		
		mShowPwd = (CheckBox)findViewById(R.id.checkBox_Show_pwd);	//��ʾ���뵥ѡ��
		mLoginBtn = (Button)findViewById(R.id.Login_Btn); //��¼��ť
		mExitBtn = (Button)findViewById(R.id.Exit_Btn);//�˳���ť
		
		//ΪButton�ؼ���ӵ����¼�����
		mLoginBtn.setOnClickListener(new clickEvent());
		mExitBtn.setOnClickListener(new clickEvent());
		
		String temp_User = "";
		
		Cursor cursor=db.rawQuery("SELECT * FROM User_Record_Table WHERE Last_Tag = ?",new String[]{"T"});
		if(cursor.moveToNext()){
			temp_User = cursor.getString(1);
		}
		cursor.close();
		db.close();

		//TODO ���������ã�����admin�û���¼
		mUsername.setText(temp_User);  			//iODH			Reset				debug
		//mUsername.setText("iODH");  
		//mPassword.setText("hengtong_GD"); 		//hengtong_GD	Clean_HT_Locker		test
		
		intent = new Intent();   //����Intent���� 
		
		//��ʾ���뵥ѡ��ѡ�к�ȡ��ѡ���¼����� 
		mShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//����������ʾ��ʽ
				if (mShowPwd.isChecked()){//��ѡ����ѡ��״̬
					// ���ı�����������ó�������ʾ 
					mPassword.setTransformationMethod(HideReturnsTransformationMethod .getInstance()); 
				} else{//��ѡ��ȡ��ѡ��
					// ���ı����������ó����ĵķ�ʽ��ʾ 
					mPassword.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
				}
			}			
		});	
		
	}
	
	//���嵥���¼���
	class clickEvent implements View.OnClickListener{

		public void onClick(View v){
			
			
			if(v == mLoginBtn){//������¼��ť
				nameValue = mUsername.getText().toString();
				pwdValue = mPassword.getText().toString();	
				Login_User_ID = nameValue;
				
				if (nameValue.length() == 0 || pwdValue.length() == 0) {
					return;
				}
				
				if (nameValue.equals("iODH") && pwdValue.equals("hengtong_GD")) {
					//�����û���¼����ת����ҳ��
					Login_User_Type = "Admin";
					
					intent.setClass(context, Main_Activity.class);
		        	startActivity(intent);		//��ת�����˵�����
		        	
		            //UserLogin_Activity.this.finish(); 
		            return;
				}
				else if (nameValue.equals("Reset") || nameValue.equals("Clean_HT_Locker")) {
					Login_User_Type = "Reset";
					
					intent.setClass(context, Main_Activity.class);
		        	startActivity(intent);		//��ת���豸���Խ���
		        	
		            //UserLogin_Activity.this.finish(); 
		            return;
				}
				else {
					//TODO �Ƚ���TCP����
					Interrupt_User_Login = new Timmer_Beep(context, mHandler);		//�����ж��û���¼��ʱ����
					
					Interrupt_User_Login.Wait_Interrupt_Infor();		//�������������źŵĶ�ʱ�ж�
					
					Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6201");
					Connect_NMS.Make_Socket_Connect();

				}				
			}
			
			if(v == mExitBtn){//����˳���ť
				new AlertDialog.Builder(UserLogin_Activity.this)//�����Ի���
				.setTitle("�˳�")//���öԻ������
				.setIcon(android.R.drawable.ic_dialog_alert)//���ñ���ͼ��
				.setMessage("ȷ��Ҫ�˳���")//���öԻ�����Ϣ
				.setPositiveButton("��", new DialogInterface.OnClickListener() {//���ȷ����ť
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//�˳���ǰ����
						UserLogin_Activity.this.finish();
						System.exit(0);
						android.os.Process.killProcess(getTaskId());

					}
				})
				.setNegativeButton("��", null)//���ȡ����ť
				.show();//��ʾ�Ի���
			}
		}
	}
	
	
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			String s, temp_Str;
			int i;
			
			switch(msg.what) {
			
			case 0:
				//�û���¼����
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("6201")) {
					//���û���¼Ӧ��
					temp_Str = s.substring(5, 6);
					
					if (Lock_Numb_User < 10) {
						temp_Str = s.substring(5, 6);
					}
					
					switch(temp_Str){
					case "U" :
						//�û���Ϣ
						temp_Str = s.substring(7, 8);
						if (temp_Str.equals("3")) {
							//�ն˹����û���¼
							Login_User_Type = "Manager";
						}
						else if (temp_Str.equals("4")) {
							//�ն�ʩ���û���¼
							Login_User_Type = "Worker";
						}
						else {
							//�Ƿ���¼
							Login_User_Type = "";
						}

						
						if (Login_User_Type.length() > 5) {
							//�Ϸ���¼
							temp_Str = s.substring(9);
							Lock_Numb_User = Integer.valueOf(temp_Str);		//ˢ�µ��û�Ͻ��������
							
							Cursor cursor = dbr.rawQuery("select * from User_Record_Table where UserID = ?",new String[]{nameValue});

							i = 0;
					    	if(cursor.moveToNext()){
					    		//�鿴�Ƿ��м�¼
					    		i = 1;
						    }
					    	cursor.close();
					    	
					    	if (i == 0) {
					    		//���û������¼���û���Ϣ
					    		dbr.execSQL("UPDATE User_Record_Table SET Last_Tag = '' ", new String[]{});		//�����¼���
					    		
								dbr.execSQL("INSERT INTO User_Record_Table (UserID, UserPassWd, UserType, Flash_Date, Last_Tag) " +
										"VALUES (?, ?, ?, ?, ?)", new String[]{nameValue, pwdValue, Login_User_Type, "00000000", "T"});
					    	}
					    	else {
					    		//���û�������е�¼���
					    		dbr.execSQL("UPDATE User_Record_Table SET Last_Tag = '' ", new String[]{});		//�����¼���
					    		//�����¼��Ǻ��û�Ȩ��
					    		dbr.execSQL("UPDATE User_Record_Table SET Last_Tag = 'T', UserType = ? WHERE UserID = ?", new String[]{Login_User_Type, nameValue});	
					    	}
					    	
					    	if (Lock_Numb_User == 0 ) {
					    		DBHelper.Save_Log_inDB(dbr, nameValue, "�û�������¼", "");
								
								//���û���¼������ת��������  
								intent.setClass(context, Main_Activity.class);
					        	startActivity(intent);//��ת��ʩ���������ܽ���
					      
					            UserLogin_Activity.this.finish(); 
					    	}
						}
						else {
							//������ǰ�ĺϷ��û�����˷Ƿ��û�����ɾ�����ݿ��еĺϷ��û���¼���Ա���������ѹܵ�¼��Ȩ��
							dbr.execSQL("DELETE FROM User_Record_Table WHERE UserID = ? and UserPassWd = ?", new String[]{nameValue, pwdValue});
														
							//������־
							DBHelper.Save_Log_inDB(dbr, nameValue, "�Ƿ��û���¼", "");
							
							intent.setClass(context, PDA_Manage_Activity.class);
				        	startActivity(intent);//��ת���ն˹����ܽ���
						}
						
						break;
						
					case "D" :
						//ˢ��������Ϣ
						temp_Str = s.substring(7);
						dbr.execSQL("UPDATE User_Record_Table SET Flash_Date = ? WHERE UserID = ?", new String[]{temp_Str, nameValue});		//ˢ���û����ݸ���ʱ��
						
						//����û�Ͻ������¼����
						dbr.execSQL("DELETE FROM User_Locker_Table WHERE UserID = ?", new String[]{nameValue});
  		    		   
						break;
						
					case "L" :	
						//��ID��Ϣ
						temp_Str = s.substring(7);
						//����û�Ͻ����ID
						dbr.execSQL("INSERT INTO User_Locker_Table (UserID, Locker_ID) VALUES (?, ?)", new String[]{nameValue, temp_Str});
			    	
						Lock_Numb_User--;
						
						if (Lock_Numb_User == 0) {
							DBHelper.Save_Log_inDB(dbr, nameValue, "�û�������¼", "");
							
							//���û���¼������ת��������  
							intent.setClass(context, Main_Activity.class);
				        	startActivity(intent);//��ת��ʩ���������ܽ���
				      				        	
				            UserLogin_Activity.this.finish(); 
						}
						
						break;
						
					case "R" :	
						//��У������ID��Ϣ
						temp_Str = s.substring(7);
						//����û�ˢ��������Ϣ
						dbr.execSQL("UPDATE User_Record_Table SET Flash_Date = '00000000' WHERE UserID = ?", new String[]{nameValue});
						
						//����û�Ͻ����ID
						dbr.execSQL("INSERT INTO User_Locker_Table (UserID, Locker_ID) VALUES (?, ?)", new String[]{nameValue, temp_Str});
			    	
						Lock_Numb_User--;			
						
						if (Lock_Numb_User == 0) {
							DBHelper.Save_Log_inDB(dbr, nameValue, "�û�������¼", "");
							
							//���û���¼������ת��������  
							intent.setClass(context, Main_Activity.class);
				        	startActivity(intent);//��ת��ʩ���������ܽ���
				      
				            UserLogin_Activity.this.finish(); 
						}
						break;
												
					}
						
				}

				break;
				
			case 1:
				s=msg.obj.toString();

				if(s.equals("No_Net")) {
					//δ��������

					No_NetWork_Operat();
					
					Interrupt_User_Login.Close_Timmer();
				}
				break;
				
			case 2:
				s=msg.obj.toString();
				
				if (s.equals("����ʧ��")) {
					AlertDialog dialog = new AlertDialog.Builder(context)
	   					.setTitle("��ʾ")			//���öԻ���ı���		
	   					.setMessage("��������ʧ�ܣ�������½��")
	   					//���öԻ���İ�ť
	   					.setPositiveButton("ȷ��", null)
	   					.create();
	   				dialog.show();
				}
				else {
					SQLiteDatabase db=dbHelper.getWritableDatabase();
			    	
					Cursor cursor = db.rawQuery("select * from User_Record_Table where UserID = ?",new String[]{nameValue});

					String UserFlash_Date = "00000000";
			    	if(cursor.moveToNext()){
			    		//ȡ�û�����ˢ��ʱ��
			    		UserFlash_Date = cursor.getString(4);
				    }
			    	cursor.close();
			    	db.close();
			    	
			    	if (dbr != null) {
			    		dbr.close();
			    	}
			    	
			    	dbr=dbHelper.getWritableDatabase();
			    			    	
			    	Connect_NMS.Wait_Recive_TCP_Reply();	
			    	Connect_NMS.User_Login_6201(nameValue, pwdValue, UserFlash_Date);		//�����û���¼�ϱ�ָ��

				}
				
				break;
				
			case 10:
				s=msg.obj.toString();

				if(s.equals("3s")) {
					
					No_NetWork_Operat();
					Interrupt_User_Login.Close_Timmer();
				}
				break;
			
			}
			
		}

	};
			
	

	private void No_NetWork_Operat() {
		//δ��������
		int temp_Int;
		Cursor cursor;
		
		Toast.makeText(context, "�������Ӳ�������������", Toast.LENGTH_LONG).show();
		
		SQLiteDatabase db=dbHelper.getWritableDatabase();
		
		cursor = db.rawQuery("select * from User_Record_Table where UserID = ? and UserPassWd = ?", new String[]{nameValue, pwdValue});

		temp_Int = 0;
		Login_User_Type = "";
		if(cursor.moveToNext()){
			temp_Int = 1;
			Login_User_Type = cursor.getString(3);
		}
		cursor.close();
					
		if (nameValue.length() == 0 && pwdValue.length() == 0) {
			temp_Int = 0;
		}
		
		if (temp_Int == 1) {
			//������־
			DBHelper.Save_Log_inDB(db, nameValue, "�û�������¼", "");
			
			//���û���¼������ת��������  
			intent.setClass(context, Main_Activity.class);
        	startActivity(intent);//��ת��ʩ���������ܽ���
            //Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);  
            UserLogin_Activity.this.finish(); 
            //UserLoginActivity.this.startActivity(intent);
		}
		else {
			//��¼�û��Ƿ�������ʹ���ն˹�����
			mUsername.setText("");  
			mPassword.setText(""); 	
			
			//������־
			DBHelper.Save_Log_inDB(db, nameValue, "�Ƿ��û���¼", "");
			
			intent.setClass(context, PDA_Manage_Activity.class);
        	startActivity(intent);//��ת���ն˹����ܽ���

		}
		db.close();
	}
	
	
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
		//unregisterReceiver(mReceiver);
		if (dbr != null) {
    		dbr.close();
    	}
		
		dbHelper.close();

		Interrupt_User_Login.Close_Timmer();

	}	 
	
	
		
		
}
