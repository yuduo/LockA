package com.example.iods_manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_common.Baidu_Map;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Locker_Quert_Activity extends Activity {
	//���߲�ѯҳ��
	private Context context = Locker_Quert_Activity.this;//����Context����
	
	private Locker_Quert_Activity mactivity;
	
	static NMS_Communication Connect_NMS;
	
	String[] List_Lock_Name = new String[200];
	String[] List_Lock_Lng = new String[200];
	String[] List_Lock_Lat = new String[200];
	int Lock_Index;
	
	String Str_Sel_Name, Str_Sel_Lng, Str_Sel_Lat, Str_Sel_ID, Selected_Tag;
	
	Double Er_Lnglat = UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //������Χ
	
	private ListView Lock_ListView;
	private SimpleAdapter Lock_listAdapter;				//�½����豸�б�������
	private List<HashMap<String,String>> Lock_List;		//�½����豸�б�����Դ
	
	RadioButton RadioButton_Name;
	EditText Query_Str;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.activity_lock_query);
        
        Intent in=getIntent();

        mactivity = this;
        
        initi();
	        
	}
	
	 public void initi(){
		 Button Btn_Query_Lock = (Button)findViewById(R.id.button1);
		 
		 Btn_Query_Lock.setOnClickListener(new ClickEvent());
		 
		 Lock_ListView=(ListView)findViewById(R.id.worklistView);
		 
		 RadioButton_Name = (RadioButton)findViewById(R.id.radioButton2);  
		 RadioButton_Name.setChecked(true);
		 
		 Query_Str = (EditText)findViewById(R.id.editText1);	
	 }
	 
	 private void Flash_Lock_List() {
			//ˢ�����豸�б�
	    	int i;
			
	    	Lock_List = new ArrayList<HashMap<String,String >>();
	    	
	    	for (i = 0; i<Lock_Index; i++) {
	    		HashMap<String,String > map=new HashMap<String,String>();
	    		map.put("index", String.valueOf(i));
	  	    	map.put("Name", List_Lock_Name[i]);
	  	    	map.put("Status", "");
	  	    	Lock_List.add(map);
	    	}
	    			
			String[] from=new String[]{"index", "Name", "Status"};
			int[] to=new int[]{R.id.textView1, R.id.textView4, R.id.textView3};
		  	Lock_listAdapter=new SimpleAdapter(context, Lock_List, R.layout.listitem_device, from, to);
		  	Lock_ListView.setAdapter(Lock_listAdapter);
		  	Lock_listAdapter.notifyDataSetChanged();
	  	
		  	Lock_ListView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					TextView tv=(TextView)view.findViewById(R.id.textView1);
					String Str_Lock = tv.getText().toString();
					
					int Int_temp = Integer.parseInt(Str_Lock );
					Str_Lock  = List_Lock_Name[Int_temp];
					
					final String Str_Lock_Lng = List_Lock_Lng[Int_temp];
					final String Str_Lock_Lat = List_Lock_Lat[Int_temp];
		    		
					Str_Sel_Name = Str_Lock;
	    			if (RadioButton_Name.isChecked()) {
	    				
	    				Str_Lock = "���豸���� " + Str_Lock;
	    			}
	    			else {
	    				Str_Lock = "���豸��Դ���룺 " + Str_Lock;
	    			}
	    			
		    		AlertDialog dialog_1;
		    		
		    		if (RadioButton_Name.isChecked() && UserLogin_Activity.Login_User_Type.equals("Admin")) {
		    			Str_Sel_Lng = Str_Lock_Lng;
		    			Str_Sel_Lat = Str_Lock_Lat;
		    			Str_Sel_ID = "";
		    			
		    			//ȡ�豸ID
		    			Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6206");
        				Connect_NMS.Make_Socket_Connect();
		    			
		    			dialog_1 = new AlertDialog.Builder(context)
                		.setTitle("��ѡ����ʽ")
                		.setMessage("����Ҫ����Զ�̿�����ʾ�� �� ")
                		//���öԻ���İ�ť
                		.setNegativeButton("Զ�̿���", new DialogInterface.OnClickListener() {
                			@Override
                			public void onClick(DialogInterface dialog, int which) {
                				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
                				Connect_NMS.Make_Socket_Connect();
                			}
                		})
                		.setPositiveButton("�ٶȵ���", new DialogInterface.OnClickListener() {
                			@Override
                			public void onClick(DialogInterface dialog, int i) {
				   				Toast.makeText(context, "��ȴ����������Ӱٶȵ�ͼ", Toast.LENGTH_SHORT).show();
				   				//�ٶȵ���
				   				new Baidu_Map().Market_BaiduMap(Str_Lock_Lng, Str_Lock_Lat, mactivity, "Baidu");				
				   			}
                		}).create();
						dialog_1.show();
		    		}
		    		else {
		    			dialog_1 = new AlertDialog.Builder(context)
				   		.setTitle("���������豸λ��")			//���öԻ���ı���		
				   		.setMessage(Str_Lock)
				   		//���öԻ���İ�ť
				   		.setPositiveButton("����", new DialogInterface.OnClickListener() {
				   			@Override
				   			public void onClick(DialogInterface dialog, int i) {
				   				Toast.makeText(context, "��ȴ����������Ӱٶȵ�ͼ", Toast.LENGTH_SHORT).show();
				   				//�ٶȵ���
				   				new Baidu_Map().Market_BaiduMap(Str_Lock_Lng, Str_Lock_Lat, mactivity, "Baidu");				
				   			}
				   		})		
				   		.create();
		    			dialog_1.show();
		    		}
		    		
				}
	    		
	    	});
		}
	    




		//�Զ��嵥���¼���
	  	class ClickEvent implements View.OnClickListener {    

	  		Intent intent = new Intent();   //����Intent���� 

	  		public void onClick(View v) {
	  			
	  			switch(v.getId()){
		    	   case R.id.button1:
		    		   //��ѯ
		    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620F");
		    		   Connect_NMS.Make_Socket_Connect();
						
		    		   break;
	  			}  
	  		}
	  	}
	  	
	  	
	  	//����Handler����
	  	Handler mHandler = new Handler(){
	  		public void handleMessage(Message msg){
	  			String s, Str_Date, Str_Numb, Str_Lng, Str_Lat, Str_ID, Str_Name, temp_Str;
	  			int Total_Locks;

	  			switch(msg.what){
	  			case 0:
	  				//Ӧ�����
	  				s=msg.obj.toString();
	  				Str_Date = s.substring(0, 6);
	  				
	  				if (Str_Date.equals("620F N")) {
	  					//�ǲ�ѯ��������������
	  					Str_Date = s.substring(7).trim();

	  				}
	  				else if (Str_Date.equals("620F L")) {
	  					//��������
	  					Str_Lng = s.substring(7,18);
	  					Str_Lat = s.substring(18,28);
	  					Str_Name = s.substring(29);

	  					List_Lock_Name[Lock_Index] = Str_Name;
	  					List_Lock_Lng[Lock_Index] = Str_Lng;
	  					List_Lock_Lat[Lock_Index] = Str_Lat;
	  					Lock_Index ++;
						
						Flash_Lock_List();		//ˢ�����б�
	  				}
	  				else if (s.substring(0, 4).equals("6202")) {
						//�ǿ������Ƶ�Ӧ��
						temp_Str = s.substring(5);
						
						if (temp_Str.equals("00")) {
							AlertDialog dialog = new AlertDialog.Builder(context)
		            			.setTitle("��ʾ")			//���öԻ���ı���
		            			.setMessage("Զ�̿����ɹ� ��")	//��ʾ�������豸��
		            			//���öԻ���İ�ť
		            			.setPositiveButton("ȷ��", null)
		            			.create();
							dialog.show();

						}
						else if (temp_Str.equals("01")) {
							AlertDialog dialog = new AlertDialog.Builder(context)
	            				.setTitle("��ʾ")			//���öԻ���ı���
	            				.setMessage("���ڿ���")	//��ʾ�������豸��
	            				//���öԻ���İ�ť
	            				.setPositiveButton("ȷ��", null)
	            				.create();
							dialog.show();
						}
						else if (temp_Str.equals("06")) {
							AlertDialog dialog = new AlertDialog.Builder(context)
	        					.setTitle("��ʾ")			//���öԻ���ı���
	        					.setMessage("ͨ�Ź���")	//��ʾ�������豸��
	        					//���öԻ���İ�ť
	        					.setPositiveButton("ȷ��", null)
	        					.create();
							dialog.show();
						}
						else {
							AlertDialog dialog = new AlertDialog.Builder(context)
	        					.setTitle("��ʾ")			//���öԻ���ı���
	        					.setMessage("����ʧ�� ��")	//��ʾ�������豸��
	        					//���öԻ���İ�ť
	        					.setPositiveButton("ȷ��", null)
	        					.create();
							dialog.show();
						}
												
					}
	  				else if (s.substring(0, 4).equals("6206")) {
	  					if (s.substring(5, 6).equals("N")) {
							Total_Locks = Integer.valueOf(s.substring(7));
							Selected_Tag = "";
						}
						else {
							//��GIS��ѯ��Ӧ��
							Str_Lng = s.substring(7, 17);
							Str_Lat = s.substring(18, 28);
							Str_ID = s.substring(29, 63);	
							Str_Name = s.substring(64);	
							
							if (Str_Name.equals(Str_Sel_Name)) {
								if (Selected_Tag.length() == 0) {
									Str_Sel_ID = Str_ID;
								}

								if (Str_Lng.equals(Str_Sel_Lng) && Str_Lat.equals(Str_Sel_Lat)) {
									Str_Sel_ID = "Selected";
								}
							}
						}
	  				}
	  				
	  				break;
	  				
	  			case 2:
	  				s=msg.obj.toString();

	  				if (s.equals("Wait_Send_620F")) {
	  					Lock_Index = 0;
	  					Connect_NMS.Wait_Recive_TCP_Reply();	
	  			    	NMS_Communication.DownLoad_Lock_Query_620F(Query_Str.getText().toString(), RadioButton_Name.isChecked());		//�������ع�������ָ��
	  					
	  				}
	  				else if (s.equals("Wait_Send_6202")) {
	  					if (Str_Sel_ID.length() > 0) {
	  						Connect_NMS.Wait_Recive_TCP_Reply();	
					    	NMS_Communication.Open_NB_Lock_6202(UserLogin_Activity.Login_User_ID, Str_Sel_ID);  
	  					}
	  					else {
	  						AlertDialog dialog_1 = new AlertDialog.Builder(context)
	   							.setTitle("��ʾ")			//���öԻ���ı���
	   							.setMessage("��δ��ȡ���豸ID�����ٴγ���Զ�̿���")
	   							.setPositiveButton("Զ�̿���", new DialogInterface.OnClickListener() {
	   								@Override
	   								public void onClick(DialogInterface dialog, int which) {
	   									Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
	   									Connect_NMS.Make_Socket_Connect();
	   								}
	   							})
	   							.create();
	   						dialog_1.show();
	  					}						
					}
	  				else if (s.equals("Wait_Send_6206")) {
						
						Connect_NMS.Wait_Recive_TCP_Reply();	
				    	NMS_Communication.GIS_Query_6206(Double.valueOf(Str_Sel_Lat), Double.valueOf(Str_Sel_Lng), Er_Lnglat);

					}
	  				
	  				break;
	  			}
	  		}
	  	};
}
