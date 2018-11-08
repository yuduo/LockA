package com.example.iods_manage;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;


public class Remote_Lock_Contral_Activity    extends Activity {
	//Զ�̿�������
	
	private Spinner Nearby_Locker_List;
	List<String> list_Name = new ArrayList<String>();
	int Selected_Lock_index;
	
	String[] Lock_Lng = new String[100];	//������
	String[] Lock_Lat = new String[100];	//��γ��
	String[] Lock_ID = new String[100];		//��ID
	String[] Lock_Name = new String[100];	//����
	boolean[] Lock_Error = new boolean[100];//���澯״̬
	int Numb_Lock = 0, Send_Locks;			//������
	
	Double Db_Lng = 0.0, Db_Lat = 0.0;
	String Help_Lock;
	
	DBHelper dbHelper ;//���ݿ�������
	
	private Context context = Remote_Lock_Contral_Activity.this;//����Context����
	
	static NMS_Communication Connect_NMS;
	
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_open_locker);
	     setTitle("��ͨ���");
	     
	     dbHelper = new DBHelper(context);	//����DBHlper����ʵ��
	     
	     initi();
	        
	}
	
	public void initi(){
    	Button NB_Lock_Contral = (Button)findViewById(R.id.button1);		//���翪��
    	Button Show_Baidu_GIS = (Button)findViewById(R.id.button2);			//��ͼ����
    	Button BT_Lock_Contral = (Button)findViewById(R.id.button3);		//��������
    	Button Help_me_Open = (Button)findViewById(R.id.button4);			//��������
    	Button Btn_Lock_Query = (Button)findViewById(R.id.button5);			//�豸��ѯ
    	
    	TextView Show_GPS_Data=(TextView)findViewById(R.id.textView7);
    	TextView Show_GPS_Title=(TextView)findViewById(R.id.textView4);
    	
    	//Ϊ�����ܰ�����ӵ����¼�����
    	Show_Baidu_GIS.setOnClickListener(new ClickEvent());
    	Help_me_Open.setOnClickListener(new ClickEvent());	
    	
    	Help_me_Open.setText("Զ�̿���");
    	NB_Lock_Contral.setVisibility(View.INVISIBLE);
    	BT_Lock_Contral.setVisibility(View.INVISIBLE);
    	Show_GPS_Data.setVisibility(View.INVISIBLE);
    	Show_GPS_Title.setVisibility(View.INVISIBLE);
    	Btn_Lock_Query.setVisibility(View.INVISIBLE);
    		
    	Nearby_Locker_List = (Spinner)findViewById(R.id.spinner1);
    	
    	Nearby_Locker_List.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Selected_Lock_index = position;				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}
		});
    	
    	Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6213");
		Connect_NMS.Make_Socket_Connect();
    }
	
	
	//�Զ��嵥���¼���
    class ClickEvent implements View.OnClickListener {    
    	
	       @SuppressLint("NewApi") @Override    
	       public void onClick(View v) {

	    	   switch(v.getId()){
	    	   case R.id.button2:
	    		   //�����ٶȵ�ͼ�������ĵ㸽�����豸λ��
	    		   if (Send_Locks > 0) {
	    			   Intent in=new Intent(context, BaidumapActivity.class);	
		    		   in.putExtra("Mode", "Remote");

		    		   in.putExtra("GPS_Lng", Lock_Lng[Selected_Lock_index]);
		    		   in.putExtra("GPS_Lat", Lock_Lat[Selected_Lock_index]);
		    		   
		    		   startActivity(in);//�޸�Ϊ�ڵ��ͼ��ʾ�Ĳ�����
	    		   }
	    		   
	    		   break;

	    	   case R.id.button4:
	    		   //����Զ�̿���ָ��
	    		   if (Send_Locks > 0) {
	    			   SQLiteDatabase db=dbHelper.getWritableDatabase();
		    		   String temp_User = UserLogin_Activity.Login_User_ID;
		    		   	 		   		    		
		    		   Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, Lock_ID[Selected_Lock_index]});
		    		   if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin")){
		    			   //�û���Ȩ�򳬼��û�
		    			   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
	    				   Connect_NMS.Make_Socket_Connect();
		    		   }
		    		   else {
			    			
		    			   AlertDialog dialog = new AlertDialog.Builder(context)
		   						.setTitle("��ʾ")			//���öԻ���ı���
		   						.setMessage("��û�д򿪴�����Ȩ��")
		   						//���öԻ���İ�ť
		   						.setPositiveButton("ȷ��", null)
		   						.create();
		    			   dialog.show();
			    			
		    		   }
		    		   cursor.close();
		    		   db.close();
	    		   }
	    		   	    		   
	    		   break;
	    	   }
	       }
	}
    
  //����Handler����
  	Handler mHandler = new Handler(){
  		@SuppressLint({ "HandlerLeak", "NewApi" }) public void handleMessage(Message msg){
  			String s, Str_Date, Str_Lat = null;
  			AlertDialog dialog;

  			switch(msg.what){
  			case 0:
  				//Ӧ�����
  				s=msg.obj.toString();
  				Str_Date = s.substring(0, 4);
  				
  				if (Str_Date.equals("6213")) {
  					if (s.substring(5, 6).equals("E")) {
  						dialog = new AlertDialog.Builder(context)
  							.setTitle("��ʾ")			//���öԻ���ı���
  							.setMessage("ͨ�Ź��ϣ�")	//��ʾ�������豸��
  							//���öԻ���İ�ť
  							.setPositiveButton("ȷ��", null)
  							.create();
  						dialog.show();
  					}
  					
  					if (s.substring(7, 8).equals("N")) {
  						Send_Locks = Integer.valueOf(s.substring(9));
  						Numb_Lock = 0;
  						
  						Selected_Lock_index = 0;
  						
  						if (Send_Locks == 0) {
  							dialog = new AlertDialog.Builder(context)
  								.setTitle("��ʾ")			//���öԻ���ı���
  								.setMessage("û�п�����������")	//��ʾ�������豸��
  								//���öԻ���İ�ť
  								.setPositiveButton("ȷ��", null)
  								.create();
  							dialog.show();
  						}
  					}
  					else if (s.substring(7, 8).equals("D")) {
  						Str_Date = s.substring(9);
  						
  						if (Str_Date.length() > 57) {
  							Lock_Lng[Numb_Lock] = Str_Date.substring(0, 10);
  							Lock_Lat[Numb_Lock] = Str_Date.substring(11, 21);
  							Lock_ID[Numb_Lock] = Str_Date.substring(22, 56);	
  							Lock_Name[Numb_Lock] = Str_Date.substring(57);	
  							
  							list_Name.add(Lock_Name[Numb_Lock]);
								
  							final ArrayAdapter<String> sAdapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_Name);
  							sAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //����ÿһ��item����ʽ
  							Nearby_Locker_List.setAdapter(sAdapter1);

  							Numb_Lock++;
  							
  						}
  						else {
  							dialog = new AlertDialog.Builder(context)
	  							.setTitle("��ʾ")			//���öԻ���ı���
	  							.setMessage("ͨ�Ź��ϣ�")	//��ʾ�������豸��
	  							//���öԻ���İ�ť
	  							.setPositiveButton("ȷ��", null)
	  							.create();
							dialog.show();
  						}
  					}
  				}
  				else if (Str_Date.equals("6202")) {
					//�ǿ������Ƶ�Ӧ��
  					Str_Date = s.substring(5);
					
					if (Str_Date.equals("00")) {
						dialog = new AlertDialog.Builder(context)
	            			.setTitle("��ʾ")			//���öԻ���ı���
	            			.setMessage("�����ɹ� ��")	//��ʾ�������豸��
	            			//���öԻ���İ�ť
	            			.setPositiveButton("ȷ��", null)
	            			.create();
						dialog.show();
						

				    	SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "Զ��Э�������ɹ�", Lock_Name[Selected_Lock_index]);
						db.close();
						
					}
					else if (Str_Date.equals("01")) {
						dialog = new AlertDialog.Builder(context)
            				.setTitle("��ʾ")			//���öԻ���ı���
            				.setMessage("���ڿ���")	//��ʾ�������豸��
            				//���öԻ���İ�ť
            				.setPositiveButton("ȷ��", null)
            				.create();
						dialog.show();
					}
					else if (Str_Date.equals("06")) {
						dialog = new AlertDialog.Builder(context)
        					.setTitle("��ʾ")			//���öԻ���ı���
        					.setMessage("ͨ�Ź���")	//��ʾ�������豸��
        					//���öԻ���İ�ť
        					.setPositiveButton("ȷ��", null)
        					.create();
						dialog.show();
					}
					else {
						dialog = new AlertDialog.Builder(context)
        					.setTitle("��ʾ")			//���öԻ���ı���
        					.setMessage("����ʧ�� ��")	//��ʾ�������豸��
        					//���öԻ���İ�ť
        					.setPositiveButton("ȷ��", null)
        					.create();
						dialog.show();
					}
					
				}
  				break;
  				
  			case 2:
  				s=msg.obj.toString();
  				
  				if (s.equals("Wait_Send_6213")) {
  					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Read_Help_Lock_6213(UserLogin_Activity.Login_User_ID);  
			    	
  				}
  				else if (s.equals("Wait_Send_6202")) {
  					//Զ�̿���
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Open_NB_Lock_6202(UserLogin_Activity.Login_User_ID, Lock_ID[Selected_Lock_index]);  
			    	
				}
  				else if (s.equals("Wait_Send_6206")) {
					Numb_Lock = 0;
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.GIS_Query_6206(Db_Lat, Db_Lng, 100*UserLogin_Activity.error_Limit);

				}
  				
  				break;
  			}
  		}

		
  	};
  	
  	@Override
	protected void onResume() {

		super.onResume();

	}
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i("ǰ����", "onDestroy()");		
		dbHelper.close();
	}
  	
}
