package com.example.iods_manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;



public class Project_Checkup_Activity    extends Activity {
	//���������ҳ��
	
	private ListView Order_ListView;
	private SimpleAdapter Order_listAdapter;				//�½����豸�б�������
	private List<HashMap<String,String>> OrderList;//�����б�����Դ

	private Spinner spinner1, spinner2;//��ѯspinner
	int selectedposition;//spinner2��ѡ���

	Context context=Project_Checkup_Activity.this;
	DBHelper dbHelper;
	
	static NMS_Communication Connect_NMS;
	
	//�㲥����
	private ServiceReceiver mReceiver;
	private String action="Close_ListCheck";
	
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.activity_work_order);
	     setTitle("��ͨ���");
	     
	     dbHelper = new DBHelper(context);//��ʼ�����ݿ����    
	     
	     //�㲥��ʼ��
	     mReceiver = new ServiceReceiver();
		 //ʵ����������������Ҫ���˵Ĺ㲥
		 IntentFilter mFilter = new IntentFilter();
		 mFilter.addAction(action);
		 registerReceiver(mReceiver, mFilter);
	     
	     initi();
	        
	     Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620A");
		 Connect_NMS.Make_Socket_Connect();
	}

	public void initi(){
    	spinner1 = (Spinner)findViewById(R.id.spinner_Select);//�ڶ��������˵����ɵ�һ�������˵���ѡ�������
		spinner2 = (Spinner)findViewById(R.id.spinner2);//��һ�������˵�		
		
		Button DownLoad_Btn;
		
		TextView Show_GPS_Title=(TextView)findViewById(R.id.Order_Title);
		Show_GPS_Title.setText("����鹤��");
		
		Order_ListView=(ListView)findViewById(R.id.worklistView);
		
		Button Apply_Insert_Tag_BTN = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		DownLoad_Btn = (Button)findViewById(R.id.Save_Config);
		DownLoad_Btn.setText("�б�����");
		DownLoad_Btn.setOnClickListener(new ClickEvent());
		
		Apply_Insert_Tag_BTN.setVisibility(View.INVISIBLE);			//����ʾ�˰���
		spinner1.setVisibility(View.INVISIBLE);			//����ʾ�˰���
		spinner2.setVisibility(View.INVISIBLE);			//����ʾ�˰���
		
		Flash_Order_List();		//ˢ�¹����б�
					
	}
    
    private void Flash_Order_List() {
		//ˢ�¹����б�
    	String Str_ID, temp_Str;
    	    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		cursor=db.rawQuery("select * from Check_List_Table where userID = ? AND (Status != '4' OR Status != '5')", new String[]{UserLogin_Activity.Login_User_ID});    	
	      
		OrderList = new ArrayList<HashMap<String,String >>();
	      
	    while(cursor.moveToNext()){
      
    		HashMap<String,String > map=new HashMap<String,String>();   		
    		
    		temp_Str = cursor.getString(6);		//ȡ����״̬
    		
    		if (temp_Str.equals("4") || temp_Str.equals("5")) {
    			continue;
    		}

    		Str_ID = cursor.getString(1);		//ȡ����ID
    		map.put("OrderID", Str_ID);
    		
    		temp_Str = Str_ID.substring(0, 6);
    		
    		if (temp_Str.equals("INSERT")) {
    			map.put("OrderType", "ʩ������");
    		}
    		else if (temp_Str.equals("MAINTE")) {
    			map.put("OrderType", "ά������");
    		}
    		else if (temp_Str.equals("MOVETO")) {
    			map.put("OrderType", "�ƻ�����");
    		}
    		if (temp_Str.equals("REMOVE")) {
    			map.put("OrderType", "�������");
    		}
    		
    		map.put("OrderLimite", "");
    		
    		OrderList.add(map);
    	}
      
		db.close();
		
		String[] from=new String[]{"OrderLimite", "OrderID", "OrderType"};
		int[] to=new int[]{R.id.textView1, R.id.textView4, R.id.textView3};
		Order_listAdapter=new SimpleAdapter(context, OrderList, R.layout.listitem_device,from, to);
	  	Order_ListView.setAdapter(Order_listAdapter);
	  	Order_listAdapter.notifyDataSetChanged();
  	
	  	Order_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent();   //����Intent���� 
				
				TextView tv=(TextView)view.findViewById(R.id.textView4);
				String Str_Order = tv.getText().toString();
				
	    		intent.setClass(context, List_Checkup_Activity.class);	
	    		intent.putExtra("workID", Str_Order);
	        	startActivity(intent);
		
			}
    		
    	});
	}
    
    
    class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Close_ListCheck")) {
				Flash_Order_List();		//ˢ�¹����б�
			}
		}	
    }
		

	//����Handler����
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			String s, temp_Str;
			int temp_Int = 0;

			switch(msg.what){
			case 0:
				//Ӧ�����
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("620A")) {
					temp_Str = s.substring(5);
					
					if (temp_Str.length() > 0) {
						//�й���ID
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						
						Cursor cursor=db.rawQuery("select * from Check_List_Table  where Order_ID = ?", new String[]{temp_Str});    			    
						if (cursor.moveToNext()){
							temp_Int = 1;
						}
						cursor.close();
						
						if (temp_Int == 0) {
							db.execSQL("INSERT INTO Check_List_Table (Order_ID, userID, Status) " +
									"VALUES (?, ?, '0')", new String[]{temp_Str, UserLogin_Activity.Login_User_ID});
						}
						db.close();
						
						Flash_Order_List();
					}
				}

				break;
			case 2:
				s=msg.obj.toString();

				if (s.equals("Wait_Send_620A")) {
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.DownLoad_CheckList_620A(UserLogin_Activity.Login_User_ID);
					
				}
				break;
			}
		}
	};

	
	
	

	
	@Override
	protected void onResume() {

		super.onResume();
		Flash_Order_List();		//ˢ�¹����б�
	}
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i("ǰ����", "onDestroy()");		
		dbHelper.close();
	}
	   


	public void onNewIntent(Intent intent) {
		//NFCUntils.NFC_onNewIntent(intent, nfcv, mHandler, true);
		//true��ʾ����ǩ
	}
	
	//�Զ��嵥���¼���
	class ClickEvent implements View.OnClickListener {    

		

		public void onClick(View v) {

	    	   switch(v.getId()){
	    	   case R.id.Save_Config:
	    		   //���ع����б�
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620A");
				   Connect_NMS.Make_Socket_Connect();
	    		   					
	    		   break;
	    		
	    	   }
		}
	}
		
}
