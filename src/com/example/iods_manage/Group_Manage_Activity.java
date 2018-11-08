package com.example.iods_manage;


import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class Group_Manage_Activity  extends Activity {
	//Ⱥ�����
	
	private Context context = Group_Manage_Activity.this;	//����Context����
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_group_manage);
	    setTitle("��ͨ���");
	    
	    LinearLayout Backgroup = (LinearLayout)findViewById(R.id.TableLayout1);

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
	     
	    Button User_Manage_Btn, Project_Checkup_Btn, Remote_Lock_Contral_Btn;
	    
	    User_Manage_Btn = (Button)findViewById(R.id.button2);
	    Project_Checkup_Btn = (Button)findViewById(R.id.button3);
	    Remote_Lock_Contral_Btn = (Button)findViewById(R.id.button1);
		
	    User_Manage_Btn.setOnClickListener(new ClickEvent());
	    Project_Checkup_Btn.setOnClickListener(new ClickEvent());
		Remote_Lock_Contral_Btn.setOnClickListener(new ClickEvent());
		
		TextView Show_informat=(TextView)findViewById(R.id.textView2);
		Show_informat.setVisibility(View.INVISIBLE);
	        
	}
	
	//�Զ��嵥���¼���
    class ClickEvent implements View.OnClickListener {    
		@Override    

		public void onClick(View v) {
			Intent intent = new Intent();   //����Intent���� 
			
	    	switch(v.getId()){
	    	   case R.id.button1:
	    		   //Զ�̿�������
	    		   intent.setClass(context, Remote_Lock_Contral_Activity.class);
	    		   startActivity(intent);
	    		   Group_Manage_Activity.this.finish(); 

	    		   break;
	    		   
	    	   case R.id.button3:
	    		   //������鰴��
	    		   intent.setClass(context, Project_Checkup_Activity.class);	    		  
	        	   startActivity(intent);//��ת�����������ӹ��ܽ���
	        	   Group_Manage_Activity.this.finish(); 


	    		   break;
	    		   
	    	   case R.id.button2:
	    		   //�û�������
	    		   intent.setClass(context, User_Manage_Activity.class);
	    		   startActivity(intent);
	    		   Group_Manage_Activity.this.finish(); 

	    		   break;

	    		
	    	}
		}
	}
		
	protected void onDestroy() {

		super.onDestroy();

	}

}
