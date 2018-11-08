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
	//用户登录页面
	
	public static Double error_Limit = 0.0001;		//GPS坐标容差
	public static Double Scope_Limit = 0.0002;		//GPS范围限定
	public static int Lock_Search_Scope;
	
	//static Socket_Recive User_Login_Accept;
	static Timmer_Beep Interrupt_User_Login;
	//static Make_TCP_Connect Connect_TCP;
	static NMS_Communication Connect_NMS;
	
	static int Lock_Numb_User;			//刷新的用户锁数
	static String Lock_Flash_Data;		//用户锁数刷新时间
		
	EditText mUsername;  //用户名、用户密码输入框
	EditText mPassword;
	CheckBox mShowPwd;//显示密码勾选框
	Button mLoginBtn, mExitBtn;         //登录、取消按钮
    
    Intent intent;
    
    //静态变量获取用户登录参数：用户名、密码、用户类型
    public static String nameValue, Login_User_ID;
    public static String pwdValue;
	public static String Login_User_Type;
	
	private Context context = UserLogin_Activity.this;	//定义Context对象

    DBHelper dbHelper ;//数据库服务对象
    SQLiteDatabase dbr;
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_login);
		
		setTitle("亨通光电");
		
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
				//无背景图案
				break;
		}
		
		//context = this.getApplicationContext();	//创建上下文对象	
		
		dbHelper = new DBHelper(context);	//创建DBHlper对象实例
		SQLiteDatabase db=dbHelper.getWritableDatabase();
      
		//创建视图对象实例
		mUsername = (EditText)findViewById(R.id.User_id);	//用户名输入框		
		mPassword = (EditText)findViewById(R.id.User_PWD);	//用户密码输入框		
		mShowPwd = (CheckBox)findViewById(R.id.checkBox_Show_pwd);	//显示密码单选框
		mLoginBtn = (Button)findViewById(R.id.Login_Btn); //登录按钮
		mExitBtn = (Button)findViewById(R.id.Exit_Btn);//退出按钮
		
		//为Button控件添加单击事件监听
		mLoginBtn.setOnClickListener(new clickEvent());
		mExitBtn.setOnClickListener(new clickEvent());
		
		String temp_User = "";
		
		Cursor cursor=db.rawQuery("SELECT * FROM User_Record_Table WHERE Last_Tag = ?",new String[]{"T"});
		if(cursor.moveToNext()){
			temp_User = cursor.getString(1);
		}
		cursor.close();
		db.close();

		//TODO 开发调试用，设置admin用户登录
		mUsername.setText(temp_User);  			//iODH			Reset				debug
		//mUsername.setText("iODH");  
		//mPassword.setText("hengtong_GD"); 		//hengtong_GD	Clean_HT_Locker		test
		
		intent = new Intent();   //创建Intent对象 
		
		//显示密码单选框选中和取消选中事件操作 
		mShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//监听密码显示方式
				if (mShowPwd.isChecked()){//单选框处于选中状态
					// 将文本框的内容设置成明文显示 
					mPassword.setTransformationMethod(HideReturnsTransformationMethod .getInstance()); 
				} else{//单选框取消选中
					// 将文本框内容设置成密文的方式显示 
					mPassword.setTransformationMethod(PasswordTransformationMethod .getInstance()); 
				}
			}			
		});	
		
	}
	
	//定义单击事件类
	class clickEvent implements View.OnClickListener{

		public void onClick(View v){
			
			
			if(v == mLoginBtn){//单击登录按钮
				nameValue = mUsername.getText().toString();
				pwdValue = mPassword.getText().toString();	
				Login_User_ID = nameValue;
				
				if (nameValue.length() == 0 || pwdValue.length() == 0) {
					return;
				}
				
				if (nameValue.equals("iODH") && pwdValue.equals("hengtong_GD")) {
					//超级用户登录，跳转到主页面
					Login_User_Type = "Admin";
					
					intent.setClass(context, Main_Activity.class);
		        	startActivity(intent);		//跳转到主菜单界面
		        	
		            //UserLogin_Activity.this.finish(); 
		            return;
				}
				else if (nameValue.equals("Reset") || nameValue.equals("Clean_HT_Locker")) {
					Login_User_Type = "Reset";
					
					intent.setClass(context, Main_Activity.class);
		        	startActivity(intent);		//跳转到设备调试界面
		        	
		            //UserLogin_Activity.this.finish(); 
		            return;
				}
				else {
					//TODO 先建立TCP连接
					Interrupt_User_Login = new Timmer_Beep(context, mHandler);		//创建中断用户登录计时对象
					
					Interrupt_User_Login.Wait_Interrupt_Infor();		//启动监听接收信号的定时中断
					
					Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6201");
					Connect_NMS.Make_Socket_Connect();

				}				
			}
			
			if(v == mExitBtn){//点击退出按钮
				new AlertDialog.Builder(UserLogin_Activity.this)//创建对话框
				.setTitle("退出")//设置对话框标题
				.setIcon(android.R.drawable.ic_dialog_alert)//设置标题图标
				.setMessage("确定要退出？")//设置对话框信息
				.setPositiveButton("是", new DialogInterface.OnClickListener() {//添加确定按钮
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//退出当前界面
						UserLogin_Activity.this.finish();
						System.exit(0);
						android.os.Process.killProcess(getTaskId());

					}
				})
				.setNegativeButton("否", null)//添加取消按钮
				.show();//显示对话框
			}
		}
	}
	
	
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			String s, temp_Str;
			int i;
			
			switch(msg.what) {
			
			case 0:
				//用户登录分析
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("6201")) {
					//是用户登录应答
					temp_Str = s.substring(5, 6);
					
					if (Lock_Numb_User < 10) {
						temp_Str = s.substring(5, 6);
					}
					
					switch(temp_Str){
					case "U" :
						//用户信息
						temp_Str = s.substring(7, 8);
						if (temp_Str.equals("3")) {
							//终端管理用户登录
							Login_User_Type = "Manager";
						}
						else if (temp_Str.equals("4")) {
							//终端施工用户登录
							Login_User_Type = "Worker";
						}
						else {
							//非法登录
							Login_User_Type = "";
						}

						
						if (Login_User_Type.length() > 5) {
							//合法登录
							temp_Str = s.substring(9);
							Lock_Numb_User = Integer.valueOf(temp_Str);		//刷新的用户辖区锁数量
							
							Cursor cursor = dbr.rawQuery("select * from User_Record_Table where UserID = ?",new String[]{nameValue});

							i = 0;
					    	if(cursor.moveToNext()){
					    		//查看是否有记录
					    		i = 1;
						    }
					    	cursor.close();
					    	
					    	if (i == 0) {
					    		//新用户，则记录此用户信息
					    		dbr.execSQL("UPDATE User_Record_Table SET Last_Tag = '' ", new String[]{});		//清除登录标记
					    		
								dbr.execSQL("INSERT INTO User_Record_Table (UserID, UserPassWd, UserType, Flash_Date, Last_Tag) " +
										"VALUES (?, ?, ?, ?, ?)", new String[]{nameValue, pwdValue, Login_User_Type, "00000000", "T"});
					    	}
					    	else {
					    		//老用户，则进行登录标记
					    		dbr.execSQL("UPDATE User_Record_Table SET Last_Tag = '' ", new String[]{});		//清除登录标记
					    		//清除登录标记和用户权限
					    		dbr.execSQL("UPDATE User_Record_Table SET Last_Tag = 'T', UserType = ? WHERE UserID = ?", new String[]{Login_User_Type, nameValue});	
					    	}
					    	
					    	if (Lock_Numb_User == 0 ) {
					    		DBHelper.Save_Log_inDB(dbr, nameValue, "用户正常登录", "");
								
								//有用户记录，则跳转到主界面  
								intent.setClass(context, Main_Activity.class);
					        	startActivity(intent);//跳转到施工导引功能界面
					      
					            UserLogin_Activity.this.finish(); 
					    	}
						}
						else {
							//若是以前的合法用户变成了非法用户，则删除数据库中的合法用户记录，以避免其具有脱管登录的权限
							dbr.execSQL("DELETE FROM User_Record_Table WHERE UserID = ? and UserPassWd = ?", new String[]{nameValue, pwdValue});
														
							//保存日志
							DBHelper.Save_Log_inDB(dbr, nameValue, "非法用户登录", "");
							
							intent.setClass(context, PDA_Manage_Activity.class);
				        	startActivity(intent);//跳转到终端管理功能界面
						}
						
						break;
						
					case "D" :
						//刷新日期信息
						temp_Str = s.substring(7);
						dbr.execSQL("UPDATE User_Record_Table SET Flash_Date = ? WHERE UserID = ?", new String[]{temp_Str, nameValue});		//刷新用户数据更新时间
						
						//清除用户辖区锁记录数据
						dbr.execSQL("DELETE FROM User_Locker_Table WHERE UserID = ?", new String[]{nameValue});
  		    		   
						break;
						
					case "L" :	
						//锁ID信息
						temp_Str = s.substring(7);
						//添加用户辖区锁ID
						dbr.execSQL("INSERT INTO User_Locker_Table (UserID, Locker_ID) VALUES (?, ?)", new String[]{nameValue, temp_Str});
			    	
						Lock_Numb_User--;
						
						if (Lock_Numb_User == 0) {
							DBHelper.Save_Log_inDB(dbr, nameValue, "用户正常登录", "");
							
							//有用户记录，则跳转到主界面  
							intent.setClass(context, Main_Activity.class);
				        	startActivity(intent);//跳转到施工导引功能界面
				      				        	
				            UserLogin_Activity.this.finish(); 
						}
						
						break;
						
					case "R" :	
						//有校验错的锁ID信息
						temp_Str = s.substring(7);
						//清除用户刷新日期信息
						dbr.execSQL("UPDATE User_Record_Table SET Flash_Date = '00000000' WHERE UserID = ?", new String[]{nameValue});
						
						//添加用户辖区锁ID
						dbr.execSQL("INSERT INTO User_Locker_Table (UserID, Locker_ID) VALUES (?, ?)", new String[]{nameValue, temp_Str});
			    	
						Lock_Numb_User--;			
						
						if (Lock_Numb_User == 0) {
							DBHelper.Save_Log_inDB(dbr, nameValue, "用户正常登录", "");
							
							//有用户记录，则跳转到主界面  
							intent.setClass(context, Main_Activity.class);
				        	startActivity(intent);//跳转到施工导引功能界面
				      
				            UserLogin_Activity.this.finish(); 
						}
						break;
												
					}
						
				}

				break;
				
			case 1:
				s=msg.obj.toString();

				if(s.equals("No_Net")) {
					//未联网处理

					No_NetWork_Operat();
					
					Interrupt_User_Login.Close_Timmer();
				}
				break;
				
			case 2:
				s=msg.obj.toString();
				
				if (s.equals("联网失败")) {
					AlertDialog dialog = new AlertDialog.Builder(context)
	   					.setTitle("提示")			//设置对话框的标题		
	   					.setMessage("网管连接失败，断网登陆。")
	   					//设置对话框的按钮
	   					.setPositiveButton("确定", null)
	   					.create();
	   				dialog.show();
				}
				else {
					SQLiteDatabase db=dbHelper.getWritableDatabase();
			    	
					Cursor cursor = db.rawQuery("select * from User_Record_Table where UserID = ?",new String[]{nameValue});

					String UserFlash_Date = "00000000";
			    	if(cursor.moveToNext()){
			    		//取用户数据刷新时间
			    		UserFlash_Date = cursor.getString(4);
				    }
			    	cursor.close();
			    	db.close();
			    	
			    	if (dbr != null) {
			    		dbr.close();
			    	}
			    	
			    	dbr=dbHelper.getWritableDatabase();
			    			    	
			    	Connect_NMS.Wait_Recive_TCP_Reply();	
			    	Connect_NMS.User_Login_6201(nameValue, pwdValue, UserFlash_Date);		//发送用户登录上报指令

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
		//未联网处理
		int temp_Int;
		Cursor cursor;
		
		Toast.makeText(context, "网管连接不畅，脱网运行", Toast.LENGTH_LONG).show();
		
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
			//保存日志
			DBHelper.Save_Log_inDB(db, nameValue, "用户脱网登录", "");
			
			//有用户记录，则跳转到主界面  
			intent.setClass(context, Main_Activity.class);
        	startActivity(intent);//跳转到施工导引功能界面
            //Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);  
            UserLogin_Activity.this.finish(); 
            //UserLoginActivity.this.startActivity(intent);
		}
		else {
			//登录用户非法，仅能使用终端管理功能
			mUsername.setText("");  
			mPassword.setText(""); 	
			
			//保存日志
			DBHelper.Save_Log_inDB(db, nameValue, "非法用户登录", "");
			
			intent.setClass(context, PDA_Manage_Activity.class);
        	startActivity(intent);//跳转到终端管理功能界面

		}
		db.close();
	}
	
	
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
		//unregisterReceiver(mReceiver);
		if (dbr != null) {
    		dbr.close();
    	}
		
		dbHelper.close();

		Interrupt_User_Login.Close_Timmer();

	}	 
	
	
		
		
}
