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
	//������˵�ҳ��
	
	public static String Cable_ID_Ver = "0A";
	private ImageButton Lock_Contrl_Btn;//�������ƹ��ܰ�ť
	private ImageButton Work_guide_Btn;//����ʩ�����ܰ�ť
	private ImageButton PDA_manage_Btn;//�ն˹����ܰ�ť
	private ImageButton Group_Manage_Btn;//Ⱥ�����ť
	
	private NetCheckReceiver mReceiver;
		
	private Context context = Main_Activity.this;//����Context����
	//�������״̬�ı�Ĺ㲥

	public static boolean  isNet = false;
	private RelativeLayout noNetBar;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("��ͨ���");
		
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
				//�ޱ���ͼ��
				break;
		}
		
		//��ȡ��ͼ����ʵ��
		Lock_Contrl_Btn = (ImageButton)findViewById(R.id.order_manage_btn);
		Work_guide_Btn = (ImageButton)findViewById(R.id.work_guide_btn);
		PDA_manage_Btn = (ImageButton)findViewById(R.id.pda_manage_btn);
		Group_Manage_Btn = (ImageButton)findViewById(R.id.bt_connect_btn);

		//Ϊ�����ܰ�����ӵ����¼����� title3
		Lock_Contrl_Btn.setOnClickListener(new ClickEvent());
		Work_guide_Btn.setOnClickListener(new ClickEvent());
		PDA_manage_Btn.setOnClickListener(new ClickEvent());
		Group_Manage_Btn.setOnClickListener(new ClickEvent());	
		
		noNetBar= (RelativeLayout) findViewById(R.id.net_view_rl);//������ʶ��
		noNetBar.setVisibility(View.INVISIBLE);		//�ݲ���ʾ�˱�ʶ��
		
		
		TextView T_Group_Manage = (TextView)findViewById(R.id.title3);
		
		if (UserLogin_Activity.Login_User_Type.equals("Worker")) {
			Group_Manage_Btn.setVisibility(View.INVISIBLE);
			T_Group_Manage.setEnabled(false);
		}
		
		initReceiver();//ע������״̬�㲥
				
		if (UserLogin_Activity.Login_User_Type.equals("Reset")) {
			Intent intent = new Intent();   //����Intent���� 
			intent.setClass(context, PDA_Manage_Activity.class);
     	   	startActivity(intent);//��ת���ն˹����ܽ���
		}
	}
	
	//�Զ��嵥���¼���
	class ClickEvent implements View.OnClickListener {    
	       @Override    
	       public void onClick(View v) {
	    	   Intent intent = new Intent();   //����Intent���� 
	    	   switch(v.getId()){
	    	   case R.id.pda_manage_btn:
	    		   intent.setClass(context, PDA_Manage_Activity.class);
	        	   startActivity(intent);//��ת���ն˹����ܽ���
	    		   break;
	    	   case R.id.bt_connect_btn:
	    		   intent.setClass(context, Group_Manage_Activity.class);
	        	   startActivity(intent);//��תȺ��������
	    		   break;
	    	   case R.id.work_guide_btn:
	    		   intent.setClass(context, WorkOrderActivity.class);
	        	   startActivity(intent);//��ת������ʩ�����ܽ���
	    		   break;
	    	   case R.id.order_manage_btn:
	    		   intent.setClass(context, Open_Locker_Activity.class);	    		  
	        	   startActivity(intent);//��ת���������ƹ��ܽ���
	    		   break;
	    	   }
	       }
	}
	
	
	//���ذ����¼�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK://�������ذ���			
			new AlertDialog.Builder(Main_Activity.this)//�����Ի���
			.setTitle("�˳�ϵͳ")//���öԻ������
			.setIcon(android.R.drawable.ic_dialog_alert)//���ñ���ͼ��
			.setMessage("ȷ��Ҫ�˳���")//���öԻ�����Ϣ
			.setPositiveButton("��", new DialogInterface.OnClickListener() {//���ȷ����ť
				@Override
				public void onClick(DialogInterface dialog, int which) {
                    finish();//�˳���ǰ����                
				}				
			})
			.setNegativeButton("��", null)//���ȡ����ť
			.show();//��ʾ�Ի���
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
	   
    //��ʼ���㲥
    private void initReceiver() {
	    mReceiver = new NetCheckReceiver();
	    //ʵ����������������Ҫ���˵Ĺ㲥
	    IntentFilter mFilter = new IntentFilter();
	    mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
	    registerReceiver(mReceiver, mFilter);
	}
    
    //�����¼�����
	public void onEventMainThread(NetEvent event) {
		setNetState(event.isConnect);//�����¼����¼���NetEvent
	}

	//��������״̬��ʾ
	public void setNetState(boolean netState) {
		if (noNetBar != null) {
			noNetBar.setVisibility(netState ? View.GONE : View.VISIBLE);			

			isNet = netState;
			
			//����������״̬����ʾ�������¼�
			noNetBar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//���������ӣ�����������
					NetConnectionDetector.startToSettings(context);
				}
			});
		}
	}
	
	
}
