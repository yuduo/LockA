package com.example.iods_lock_app;





import com.example.iods_common.NMS_Communication;
import com.example.iods_manage.Group_Manage_Activity;
import com.example.iods_network.NetCheckReceiver;
import com.example.iods_network.NetConnectionDetector;
import com.example.iods_network.NetEvent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;


public class Main_Activity extends Activity {
	//软件主菜单页面
	
	public static String Cable_ID_Ver = "0A";
	private ImageButton Lock_Contrl_Btn;//门锁控制功能按钮
	private ImageButton Work_guide_Btn;//工程施工功能按钮
	private ImageButton PDA_manage_Btn;//终端管理功能按钮
	private ImageButton Group_Manage_Btn;//群组管理按钮
	
	private NetCheckReceiver mReceiver;
		
	private Context context = Main_Activity.this;//定义Context对象
	//检查网络状态改变的广播

	public static boolean  isNet = false;
	private RelativeLayout noNetBar;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("亨通光电");
		
		TableLayout Backgroup = (TableLayout)findViewById(R.id.TableLayout1);

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
		
		//获取视图对象实例
		Lock_Contrl_Btn = (ImageButton)findViewById(R.id.order_manage_btn);
		Work_guide_Btn = (ImageButton)findViewById(R.id.work_guide_btn);
		PDA_manage_Btn = (ImageButton)findViewById(R.id.pda_manage_btn);
		Group_Manage_Btn = (ImageButton)findViewById(R.id.bt_connect_btn);

		//为各功能按键添加单击事件监听 title3
		Lock_Contrl_Btn.setOnClickListener(new ClickEvent());
		Work_guide_Btn.setOnClickListener(new ClickEvent());
		PDA_manage_Btn.setOnClickListener(new ClickEvent());
		Group_Manage_Btn.setOnClickListener(new ClickEvent());	
		
		noNetBar= (RelativeLayout) findViewById(R.id.net_view_rl);//断网标识条
		noNetBar.setVisibility(View.INVISIBLE);		//暂不显示此标识条
		
		
		TextView T_Group_Manage = (TextView)findViewById(R.id.title3);
		
		if (UserLogin_Activity.Login_User_Type.equals("Worker")) {
			Group_Manage_Btn.setVisibility(View.INVISIBLE);
			T_Group_Manage.setEnabled(false);
		}
		
		initReceiver();//注册网络状态广播
				
		if (UserLogin_Activity.Login_User_Type.equals("Reset")) {
			Intent intent = new Intent();   //创建Intent对象 
			intent.setClass(context, PDA_Manage_Activity.class);
     	   	startActivity(intent);//跳转到终端管理功能界面
		}
	}
	
	//自定义单击事件类
	class ClickEvent implements View.OnClickListener {    
	       @Override    
	       public void onClick(View v) {
	    	   Intent intent = new Intent();   //创建Intent对象 
	    	   switch(v.getId()){
	    	   case R.id.pda_manage_btn:
	    		   intent.setClass(context, PDA_Manage_Activity.class);
	        	   startActivity(intent);//跳转到终端管理功能界面
	    		   break;
	    	   case R.id.bt_connect_btn:
	    		   intent.setClass(context, Group_Manage_Activity.class);
	        	   startActivity(intent);//跳转群组管理界面
	    		   break;
	    	   case R.id.work_guide_btn:
	    		   intent.setClass(context, WorkOrderActivity.class);
	        	   startActivity(intent);//跳转到工程施工功能界面
	    		   break;
	    	   case R.id.order_manage_btn:
	    		   intent.setClass(context, Open_Locker_Activity.class);	    		  
	        	   startActivity(intent);//跳转到开锁控制功能界面
	    		   break;
	    	   }
	       }
	}
	
	
	//返回按键事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK://单击返回按键			
			new AlertDialog.Builder(Main_Activity.this)//创建对话框
			.setTitle("退出系统")//设置对话框标题
			.setIcon(android.R.drawable.ic_dialog_alert)//设置标题图标
			.setMessage("确定要退出？")//设置对话框信息
			.setPositiveButton("是", new DialogInterface.OnClickListener() {//添加确定按钮
				@Override
				public void onClick(DialogInterface dialog, int which) {
                    finish();//退出当前界面                
				}				
			})
			.setNegativeButton("否", null)//添加取消按钮
			.show();//显示对话框
			return true;
		}		 
		return super.onKeyDown(keyCode, event);  		
	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();

	}
	
	
    public void onResume() {
    	super.onResume();

	}
	   
    //初始化广播
    private void initReceiver() {
	    mReceiver = new NetCheckReceiver();
	    //实例化过滤器并设置要过滤的广播
	    IntentFilter mFilter = new IntentFilter();
	    mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
	    registerReceiver(mReceiver, mFilter);
	}
    
    //接收事件处理
	public void onEventMainThread(NetEvent event) {
		setNetState(event.isConnect);//接收事件：事件类NetEvent
	}

	//设置网络状态显示
	public void setNetState(boolean netState) {
		if (noNetBar != null) {
			noNetBar.setVisibility(netState ? View.GONE : View.VISIBLE);			

			isNet = netState;
			
			//无网络连接状态条显示，单击事件
			noNetBar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//无网络连接，打开网络设置
					NetConnectionDetector.startToSettings(context);
				}
			});
		}
	}
	
	
}
