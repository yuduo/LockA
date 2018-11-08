package com.example.iods_manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;



import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class User_Manage_Activity   extends Activity {
	//�û�����
	Context context=User_Manage_Activity.this;
	
	static NMS_Communication Connect_NMS;
	
	private ListView User_ListView;
	private SimpleAdapter User_listAdapt;
	private List<HashMap<String,String>> UserList;
	
	int User_Index, User_Number;
	
	boolean Error_Tag = true;
	
	String[] User_ID = new String[100];			//�û�ID
	String[] User_Name = new String[100];		//�û���ʵ����
	String[] User_Type = new String[100];				//�û����ͣ�3��ʾ���ն˹����û���4��ʾ��ʩ���û�
	
	String Delete_User;
	
	DBHelper dbHelper;
	
	//�㲥����
	private ServiceReceiver mReceiver;
	private String action="Flash_UserList";
	
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_make_newlock);
	     setTitle("��ͨ���");
	     
	     dbHelper = new DBHelper(context);	//����DBHlper����ʵ��

	     //�㲥��ʼ��
	     mReceiver = new ServiceReceiver();
		 //ʵ����������������Ҫ���˵Ĺ㲥
		 IntentFilter mFilter = new IntentFilter();
		 mFilter.addAction(action);
		 registerReceiver(mReceiver, mFilter);
		    
	     initi();
	        
	}
	
	public void initi(){
    	
		Button Replay_This_Order, Show_Map, Insert_New_User;
		
		Replay_This_Order = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		Insert_New_User = (Button)findViewById(R.id.Save_Config);
		Show_Map = (Button)findViewById(R.id.button1);
		
		TextView Titale_View=(TextView)findViewById(R.id.Order_Title);
		Titale_View.setText("�û��б�");
		
		User_ListView=(ListView)findViewById(R.id.worklistView);
		UserList = new ArrayList<HashMap<String,String >>();
		
		Insert_New_User.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent();   //����Intent����
				intent.setClass(context, Chang_PassWd_Activity.class);	
				intent.putExtra("Mode", "Insert");
				intent.putExtra("Query", "");
				startActivity(intent);//�������������ģʽ��ת���������ҳ��
            }
        });
		
		Replay_This_Order.setVisibility(View.INVISIBLE);
		Show_Map.setVisibility(View.INVISIBLE);
		
		Insert_New_User.setText("������û�");		
		
		//����Ⱥ���û��б�			
		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6214");
		Connect_NMS.Make_Socket_Connect();
						
	}

	private void Add_User_List(int index) {
		//��ʾ�û��б�

		HashMap<String,String > map=new HashMap<String,String>();   	
		map.put("UserName", User_Name[index]);
		map.put("UserID", User_ID[index]);
		map.put("UserType", User_Type[index]);
		
		UserList.add(map);
		
		String[] from=new String[]{"UserName", "UserID", "UserType"};
		int[] to=new int[]{R.id.textView1, R.id.textView4, R.id.textView3};
		User_listAdapt=new SimpleAdapter(context, UserList, R.layout.listitem_device,from, to);
	  	User_ListView.setAdapter(User_listAdapt);
	  	User_listAdapt.notifyDataSetChanged();
  	
	  	User_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				TextView tv=(TextView)view.findViewById(R.id.textView4);
				final String Str_User = tv.getText().toString();
				
				tv=(TextView)view.findViewById(R.id.textView3);
				String Str_Type = tv.getText().toString();
				
				
				if (Str_Type.equals("����Ա") && ! Str_User.equals(UserLogin_Activity.Login_User_ID)) {
					AlertDialog dialog_1 = new AlertDialog.Builder(context)
						.setTitle("��ʾ")			//���öԻ���ı���
						.setMessage("����Ա��Ϣֻ���������ϴ���")
						//���öԻ���İ�ť
						.setNegativeButton("ȷ��", null)
						.create();
					dialog_1.show();
				}
				else {
					AlertDialog.Builder dialog =  new   AlertDialog.Builder(User_Manage_Activity.this  ); 
					
		   			dialog.setTitle("��ѡȡ�������û��Ĳ���").setMessage(Str_User);	//��ʾ�������豸��
		   			//�˶Ի�������ö������
		   			dialog.setPositiveButton("����", new DialogInterface.OnClickListener() {
		   				//���ʩ����ť
		   				@Override
		   				public void onClick(DialogInterface dialog, int i) {
		   					Intent intent = new Intent();   //����Intent����
		   					intent.setClass(context, Chang_PassWd_Activity.class);	
		   					intent.putExtra("Mode", "Change");
		   					intent.putExtra("Query", Str_User);
		   					startActivity(intent);//���޸��������ݵ�ģʽ��ת���������ҳ��
		   				}
		   			});
		   			
		   			
		   			if (! Str_User.equals(UserLogin_Activity.Login_User_ID)) {
		   				dialog.setNegativeButton("ɾ��", new DialogInterface.OnClickListener() {
			   				@Override
			   				public void onClick(DialogInterface dialog, int which) {		   					
			   					AlertDialog dialog_1 = new AlertDialog.Builder(context)
	   								.setTitle("��ȷ��Ҫɾ�������û� ��")			//���öԻ���ı���
	   								.setMessage(Str_User)
	   								//���öԻ���İ�ť
	   								.setNegativeButton("ȡ��", null)
	   								.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
	   									@Override
	   									public void onClick(DialogInterface dialog, int which) {
	   										Delete_User = Str_User;
	   										Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6216");
	   										Connect_NMS.Make_Socket_Connect();
	   									}
	   								}).create();
			   					dialog_1.show();
			   				}
			   			});
		   				dialog.setNeutralButton("Ͻ��", new DialogInterface.OnClickListener() {
			   				@Override
			   				public void onClick(DialogInterface dialog, int which) {	
			   					Intent intent = new Intent();
			   					intent.setClass(context, Chang_Popedom_Activity.class);	    
			   					intent.putExtra("User", Str_User);
			   					startActivity(intent);		//��תϽ����Ϣѡ���ܽ���
			   				}
			   			});
		   			}
		   			
		   			dialog.show();
				}
			}
    		
    	});
	}
	
	//����Handler����
  	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			String s, Str_Date, Str_Numb, Str_Type, Str_ID, Str_Name;
  			int i;

  			switch(msg.what){
  			case 0:
  				//Ӧ�����
  				s=msg.obj.toString();
  				Str_Date = s.substring(0, 4);
  				
  				if (Str_Date.equals("6214")) {
  					//���û��б�����
  					if (s.substring(5, 6).equals("E") && Error_Tag) {
  						Error_Tag = false;
  						AlertDialog dialog_1 = new AlertDialog.Builder(context)
							.setTitle("��ʾ")			//���öԻ���ı���
							.setMessage("��ȡ�û��б����")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog_1.show();
  					}
  					
  					Str_Date = s.substring(7, 8);
  					
  					if (Str_Date.equals("N")) {
  						Str_Date = s.substring(9);
  						User_Number = Integer.valueOf(Str_Date);
  						User_Index = 0;
  						
  						UserList.clear();

  					}
  					else if (Str_Date.equals("U")) {
  						Str_Date = s.substring(9);
  						
  						i = Str_Date.indexOf(" ");
  						Str_ID = Str_Date.substring(0, i);
  						
  						Str_Date = Str_Date.substring(i+1);
  						
  						i = Str_Date.indexOf(" ");
  						Str_Name = Str_Date.substring(0, i);
  						
  						Str_Date = Str_Date.substring(Str_Date.length() - 1);
  						
  						if (Str_Date.equals("3")) {
  							Str_Type = "����Ա";
  						}
  						else {
  							Str_Type = "����Ա";
  						}
  						
  						User_ID[User_Index] = Str_ID;
  						User_Name[User_Index] = Str_Name;
  						User_Type[User_Index] = Str_Type;
  						
  						Add_User_List(User_Index);
  						User_Index ++;
  						  						
  					}

  				}
  				else if (s.equals("6216 F 00")) {
  					//��ɾ���û��Ļظ�,�����¶�ȡ�û��б�
  					SQLiteDatabase db=dbHelper.getWritableDatabase();
					DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "ɾ���û��˺�", Delete_User);
					db.close();
  					
  					Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6214");
  					Connect_NMS.Make_Socket_Connect();
  				}
  				break;
  				
  			case 2:
  				s=msg.obj.toString();

  				if (s.equals("Wait_Send_6214")) {
  					//���Ͷ�ȡ�û��б�ָ��
  					Error_Tag = true;
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.DownLoad_OrderData_6214(UserLogin_Activity.Login_User_ID);		//�������ع�������ָ��
  				}
  				else if (s.equals("Wait_Send_6216")) {
  					//����ɾ���û�ָ��
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.Update_User_Inform_6216("", Delete_User, "", "", "", 0);
  				}
  				break;
  			}
  		}
  	};
  	
  	class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Flash_UserList")) {
				//����û���Ϣ����,�����¶�ȡ�û��б�
				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6214");
				Connect_NMS.Make_Socket_Connect();
			}
		}	
    }
  	
   
  	
  	protected void onResume() {

		super.onResume();

	}
    
	protected void onDestroy() {

		super.onDestroy();
		

	}
}
