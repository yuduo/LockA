package com.example.iods_manage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.example.iods_bluetooch.BLEService;
import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_bluetooch.BluetoothController;
import com.example.iods_bluetooch.ConstantUtils;
import com.example.iods_bluetooch.EntityDevice;
import com.example.iods_common.Baidu_Map;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;



public class Make_NewLock_Activity  extends Activity {
	//�½���ʩ��
	private Make_NewLock_Activity mactivity;
	
	private Context context = Make_NewLock_Activity.this;//����Context����
	
	double Get_Lock_Lat = 0, Get_Lock_Lng = 0;
	String Get_Lock_Name = ""; 

	static NMS_Communication Connect_NMS;
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean BLE_Lock_Contrl_Tag = false;
	boolean Jast_Open_Tag, Send_BLE_Tag;
	
	int index_6105 = 0;
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
	String[] List_Lock_ID = new String[200];
	String[] List_Lock_Name = new String[200];
	String[] List_Lock_Lng = new String[200];
	String[] List_Lock_Lat = new String[200];
	boolean[] List_Lock_Finish = new boolean[200];
	int Lock_Index;
	
	private LocationClient mLocationClient;
    private LocationManager locationManager;  
    private String provider;
	private Double Your_Lng_C=120.0, Your_Lat_C=31.0;
	Double Scope_Lnglat = UserLogin_Activity.Scope_Limit;
	
	private ListView Lock_ListView;
	private SimpleAdapter Lock_listAdapter;				//�½����豸�б�������
	private List<HashMap<String,String>> Lock_List;		//�½����豸�б�����Դ
	
	DBHelper dbHelper ;//���ݿ�������
	
	String Lock_ID, New_Lock_ID, Lock_imsi;				//��ID
	String workID, workType, work_Status;				//����ID
	
	int Int_Get_GPS_Tag = 0;
	
	Button Get_New_Lock;
	
	//�㲥����
	private ServiceReceiver mReceiver;
	private String action="Close_BackOrder";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	SDKInitializer.initialize(getApplicationContext());
    	
        setContentView(R.layout.activity_make_newlock);
        
        Intent in=getIntent();
        workID=in.getStringExtra("workID");
        work_Status = in.getStringExtra("Status");
        
        mactivity = this;
        
        String temp_Str = workID.substring(0, 6);
        		
		if (temp_Str.equals("INSERT")) {
			workType = "�½�";
			setTitle("�½���ʩ����" + workID);
		}
		else if (temp_Str.equals("MAINTE")) {
			workType = "ά��";
			setTitle("��ά��ʩ����" + workID);
		}
		else if (temp_Str.equals("REMOVE")) {
			workType = "���";
			setTitle("���ʩ����" + workID);
		}
		else if (temp_Str.equals("MOVETO")) {
			workType = "�ƻ�";
			setTitle("�ƻ�ʩ����" + workID);
		}
        
        dbHelper = new DBHelper(context);	//����DBHlper����ʵ��
                
        //�㲥��ʼ��
        mReceiver = new ServiceReceiver();
	    //ʵ����������������Ҫ���˵Ĺ㲥
	    IntentFilter mFilter = new IntentFilter();
	    mFilter.addAction(action);
	    registerReceiver(mReceiver, mFilter);

        initi();
        
        // ��ȡLocationClient
        mLocationClient = new LocationClient(this);
 
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);

        mLocationClient.start();
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
        provider = LocationManager.GPS_PROVIDER;
        //ʹ��getLastKnownLocation�Ϳ��Ի�ȡ����¼��ǰλ����Ϣ��Location������  
        //������showLocation()��ʾ��ǰ�豸��λ����Ϣ  
        //requestLocationUpdates��������λ�ü�����  
        //�˴���������ʱ����Ϊ5�룬��������5��  
        //Ҳ����˵ÿ��5�����ÿ�ƶ�5�ף�locationListener�л����һ��λ����Ϣ  
        Location location = locationManager.getLastKnownLocation(provider);  
        if (location != null) {  

        	showLocation(location);  
        }  
        String provider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(provider, 5000, 5, locationListener);  
        

        //TODO �ص�����
        Test_Backup_Order(workID);
    }
        
    private void Test_Backup_Order(String workID2) {
		// TODO �ص�����
    	/*
    	 * 	 * 1 _id��:��¼���
	 * Order_ID������ID
	 * Old_Locker_ID��		����ID
	 * Old_Locker_Name:	������
	 * Old_Locker_Lng��	�Ͼ�γ��
	 * Old_Locker_lat
	 * New_Locker_Name:	������
	 * New_Locker_Lng��	�¾�γ��
	 * New_Locker_lat
	 * New_Locker_ID
	 * Status:			�깤����״̬��0δ��ɣ�1���
	 * New_Lock_imsi
    	 */
    	String[] Old_Locker_ID = new String[50];
    	String[] Old_Locker_Name = new String[50];
    	String[] Old_Locker_Lng = new String[50];
    	String[] Old_Locker_Lat = new String[50];
    	String[] New_Locker_Name = new String[50];
    	String[] New_Locker_Lng = new String[50];
    	String[] New_Locker_Lat = new String[50];
    	String[] New_Locker_ID = new String[50];
    	String[] Status = new String[50];
    	String[] New_Lock_imsi = new String[50];
    	int i = 0, j;
    	Double Double_Lng;
    	String temp_Str_1, temp_Str_2;
    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
    	
    	//db.execSQL("DELETE FROM Order_Data_Table WHERE New_Locker_ID <> ?", new String[]{"0D0F383633373033303336323835343330"});
		    	
		Cursor cursor;  
		cursor=db.rawQuery("select * from Order_Data_Table where Order_ID = ?", new String[]{workID});    	
	      
	    while(cursor.moveToNext()){
	    	j = cursor.getInt(0);
	    	Old_Locker_ID[i] = cursor.getString(2);
	    	Old_Locker_Name[i] = cursor.getString(3);
	    	Old_Locker_Lng[i] = cursor.getString(4);
	    	Old_Locker_Lat[i] = cursor.getString(5);
	    	New_Locker_Name[i] = cursor.getString(6);
	    	New_Locker_Lng[i] = cursor.getString(7);
	    	New_Locker_Lat[i] = cursor.getString(8);
	    	New_Locker_ID[i] = cursor.getString(9);
	    	Status[i] = cursor.getString(10);
	    	New_Lock_imsi[i] = cursor.getString(11);

	    	i++;
	    }
	    
	    temp_Str_1 = New_Locker_ID[0].substring(33);
	    temp_Str_2 = New_Locker_ID[0].substring(31,32);
	    
	    /*
	    Double_Lng = Double.valueOf(New_Locker_Lng[0]);
	    for (j = 1; j<40; j++) {
	    	New_Locker_Name[j] = New_Locker_Name[0] + "-" + String.valueOf(j);
	    	New_Locker_Lng[j] = String.valueOf(Double_Lng + j * 0.001);
	    	New_Locker_Lat[j] = New_Locker_Lat[0];
	    	i = j /10;
	    	New_Locker_ID[j] = New_Locker_ID[0].substring(0, 31) + String.valueOf(Integer.valueOf(temp_Str_2) + i) + "3" + String.valueOf(Integer.valueOf(temp_Str_1) + j%10);
	    	
	    	db.execSQL("INSERT INTO Order_Data_Table (Order_ID, New_Locker_Name, New_Locker_Lng, New_Locker_Lat, New_Locker_ID, Status, New_Lock_imsi) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)", new String[]{workID, New_Locker_Name[j], New_Locker_Lng[j], New_Locker_Lat[0], New_Locker_ID[j], Status[0], New_Lock_imsi[0]});
			
	    }
	    
	    db.close();
	    j = i;
		*/
	}

	public void initi(){
    	    	
		Button Replay_This_Order, Show_Map;
		
		Replay_This_Order = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		Get_New_Lock = (Button)findViewById(R.id.Save_Config);
		Show_Map = (Button)findViewById(R.id.button1);
		
		Replay_This_Order.setOnClickListener(new ClickEvent());
		Get_New_Lock.setOnClickListener(new ClickEvent());
		Show_Map.setOnClickListener(new ClickEvent());
		
		Get_New_Lock.setText("�ɼ�����Ϣ");
		Get_New_Lock.setTextColor(Color.rgb(0, 0, 0));
		
		Lock_ListView=(ListView)findViewById(R.id.worklistView);
		
		TextView Titale_View=(TextView)findViewById(R.id.Order_Title);
		
		String temp_Str = workID.substring(0, 6);
		
		if (temp_Str.equals("INSERT")) {
			temp_Str = "������½����б�";
		}
		else if (temp_Str.equals("MAINTE")) {
			temp_Str = "ά�����������б�";
		}
		else if (temp_Str.equals("REMOVE")) {
			temp_Str = "������������б�";
		}
		else if (temp_Str.equals("MOVETO")) {
			temp_Str = "�ƻ����������б�";
		}
		Titale_View.setText(temp_Str);
		
		Flash_Lock_List();		//ˢ�����б�
					
	}
    
    @SuppressWarnings("resource")
	private void Flash_Lock_List() {
		//ˢ�����豸�б�
    	int temp_Int = 0;
    	String Lock_Name, Lock_Status;
    	String temp_Str = workID.substring(0, 6);
		
    	Lock_List = new ArrayList<HashMap<String,String >>();
    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		
		cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ?", new String[]{workID});    			    
		if (cursor.moveToNext()){
			temp_Int = 1;
		}
		cursor.close();

		if (temp_Int == 1) {
			Lock_Index = 0;
			cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ?", new String[]{workID});    			    

		    while(cursor.moveToNext()){
	      
		    	Lock_Index++;
	    		HashMap<String,String > map=new HashMap<String,String>();
	    			
	    		Lock_Status = cursor.getString(10);	    	

	    		if (Lock_Status.equals("0")) {
	    			//δ�깤�豸ȡ���豸��
	    			List_Lock_ID[Lock_Index] = cursor.getString(2);		//ȡδ�깤�豸ID
	    			List_Lock_Finish[Lock_Index] = false;
	    			Lock_Name = cursor.getString(3);
	    			List_Lock_Lng[Lock_Index] = cursor.getString(4);
	    			List_Lock_Lat[Lock_Index] = cursor.getString(5);
	    			map.put("Status", "������");
	    		}
	    		else {
	    			//���깤�豸ȡ���豸��
	    			if (workID.substring(0, 6).equals("REMOVE")) {
	    				List_Lock_ID[Lock_Index] = cursor.getString(2);
	    			}
	    			else {
	    				List_Lock_ID[Lock_Index] = cursor.getString(9);		//ȡ���깤�豸ID
	    			}
	    				    			
	    			List_Lock_Finish[Lock_Index] = true;
	    			Lock_Name = cursor.getString(6);
	    			if (Lock_Name == null) {
	    				Lock_Name = cursor.getString(3);
	    			}
	    			List_Lock_Lng[Lock_Index] = cursor.getString(7);
	    			List_Lock_Lat[Lock_Index] = cursor.getString(8);
	    			map.put("Status", "���");
	    		}    		
	    		
	    		if (Lock_Name == null) {
	    			Lock_Name = "δ����";
	    		}
	    		else if (Lock_Name.length() == 0) {
	    			Lock_Name = "δ����";
	    		}
	    		List_Lock_Name[Lock_Index] = Lock_Name;
	    		
	    		map.put("index", String.valueOf(Lock_Index));
	  	    	map.put("Name", Lock_Name);
	  	    	Lock_List.add(map);
	    	}
	      
		}
		else {
			//���ع�������		
			cursor=db.rawQuery("select * from Order_List_Table  where Order_ID = ?", new String[]{workID});    			    
			if (cursor.moveToNext()){
				temp_Str = cursor.getString(5);
			}
			cursor.close();

			if (temp_Str == null) {
				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620E");
				Connect_NMS.Make_Socket_Connect();
			}
			else if (temp_Str.equals("0")) {
				Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620E");
				Connect_NMS.Make_Socket_Connect();
			}
			
			   
		}

		db.close();
				
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
				
				final String Str_Lock_ID = List_Lock_ID[Int_temp];
				final String Str_Lock_Lng = List_Lock_Lng[Int_temp];
				final String Str_Lock_Lat = List_Lock_Lat[Int_temp];
				final String Str_Lock_Name = Str_Lock;
				final String Str_Title = workType;
				
				if (Str_Lock_ID == null) {
					return;
				}
				
				Jast_Open_Tag = List_Lock_Finish[Int_temp];
				//AlertDialog dialog;
				
				double Error_Lng_C = Your_Lng_C - Double.valueOf(Str_Lock_Lng);
	    		double Error_Lat_C = Your_Lat_C - Double.valueOf(Str_Lock_Lat);
	    		
    				 
	    		AlertDialog dialog_1;
	    		
				//dialog = new AlertDialog.Builder(context)
				if (Error_Lng_C > 2*Scope_Lnglat || Error_Lat_C > 2*Scope_Lnglat || Error_Lng_C < -2*Scope_Lnglat || Error_Lat_C < -2*Scope_Lnglat) {
					Lock_ID = List_Lock_ID[Int_temp];
					dialog_1 = new AlertDialog.Builder(context)
 				   		.setTitle("���������豸λ��")			//���öԻ���ı���		
						.setMessage("���豸���� " + Str_Lock_Name)
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
 			   	else {
 			   		//������Ҫ��
 			   		int temp_Int = 0;
 			   		if (! BLE_Get_Start_Tag) {
 			   			temp_Int = 1;
 			   		}
 			   		
 			   		if (Lock_ID == null) {
 			   			Lock_ID = Str_Lock_ID;
 			   		}
 			   		else if (Lock_ID.length() == 0) {
 			   			Lock_ID = Str_Lock_ID;
 			   		}			   		
 			   		
 			   		if (temp_Int == 1 && Get_New_Lock.getText().toString().equals("�ɼ�����Ϣ")) {
 			   			if (Str_Title.equals("ά��")) {
 			   				dialog_1 = new AlertDialog.Builder(context)
 			   					.setTitle("��ʾ")			//���öԻ���ı���		
 			   					.setMessage("���Ȳɼ����豸��Ϣ ")
 			   					//���öԻ���İ�ť
 			   					.setPositiveButton("ȷ��", null)
 			   					.setNegativeButton("�޷���ȡ�豸��Ϣ",new DialogInterface.OnClickListener() {
 			   						@Override
 			   						public void onClick(DialogInterface dialog, int which) {
 			   							if (Str_Title.equals("ά��")) {
 			   								AlertDialog dialog_1 = new AlertDialog.Builder(context)
	 			   								.setTitle("��ʾ")			//���öԻ���ı���
	 			   								.setMessage("���������������ȡ�����豸��Ϣ")
	 			   								//���öԻ���İ�ť
	 			   								.setPositiveButton("ȷ��", null)
	 			   								.create();
 			   								dialog_1.show();
	 			   						
 			   								Get_New_Lock.setText("�ɼ�������Ϣ");
	 			   							Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
 			   							}
 			   						}
 			   					})
			   						
 			   					.create();
 			   				dialog_1.show();
 			   			}
 			   			else {
 			   				dialog_1 = new AlertDialog.Builder(context)
 			   					.setTitle("��ʾ")			//���öԻ���ı���		
 			   					.setMessage("���Ȳɼ����豸��Ϣ ")
 			   					//���öԻ���İ�ť
 			   					.setPositiveButton("ȷ��", null)
 			   					.create();
 			   				dialog_1.show();
 			   			}
 			   			
 			   		}
 			   		else {
 			   			//����������������ȡ�����豸ID
 			   			 			   			
 			   			AlertDialog.Builder dialog =  new   AlertDialog.Builder(Make_NewLock_Activity.this  ); 
 					
 			   			dialog.setTitle("��ѡȡ���������ߵĲ���").setMessage(Str_Lock);	//��ʾ�������豸��
 			   			//�˶Ի�������ö������
 			   			dialog.setPositiveButton(Str_Title, new DialogInterface.OnClickListener() {
 			   				//���ʩ����ť
 			   				@Override
 			   				public void onClick(DialogInterface dialog, int i) {
 			   					AlertDialog dialog_1;
 			   					if (Str_Title.equals("ά��")) {
 			   						dialog_1 = new AlertDialog.Builder(context)
 			   							.setTitle("��ʾ")			//���öԻ���ı���
 			   							.setMessage("���������������ȡ�����豸��Ϣ")
 			   							//���öԻ���İ�ť
 			   							.setPositiveButton("ȷ��", null)
 			   							.create();
 			   						dialog_1.show();
 			   						
 			   						Get_New_Lock.setText("�ɼ�������Ϣ");
 			   						Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
 			   					}
 			   					else if (Str_Title.equals("���") || Str_Title.equals("�ƻ�")) {
 			   						//��ͨ�������������豸ID
 			   		    		   Read_BLE_Lock_ID();  
 			   						 			   					
 			   					}
 			   				}
 			   			});
 			   			dialog.setNegativeButton("����", new DialogInterface.OnClickListener() {
 			   				@Override
 			   				public void onClick(DialogInterface dialog, int which) {
						    						
 			   					//�߱���ʱ�Ե���ȫȨ��
 			   					String Recive_Str = "123456 "+ Str_Lock_ID;
 			   					
 			   					Send_BLE_Tag = true;	//��������ָ���־��λ
 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
				
 			   					if (! BLE_Lock_Contrl_Tag) {
 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context)
 			   							.setTitle("��ʾ")			//���öԻ���ı���
 			   							.setMessage("ֱ������ʧ��")
 			   							//���öԻ���İ�ť
 			   							.setPositiveButton("ȷ��", null)
 			   							.create();
 			   						dialog_1.show();
 			   					}
 			   					dialog.dismiss();
 			   				}
 			   			});
 			   			dialog.show();
 			   		}
 			   	}
			}
    		
    	});
	}
    

	//�Զ��嵥���¼���
  	class ClickEvent implements View.OnClickListener {    

  		Intent intent = new Intent();   //����Intent���� 

  		public void onClick(View v) {
  			
  			switch(v.getId()){
	    	   case R.id.Save_Config:
	    		   //��ͨ�������������豸ID
	    		   Read_BLE_Lock_ID();  
	    		   
	    		   break;
	    		   
	    	   case R.id.Apply_Insert_Tag_btn:
	    		   //�ص�
	    		   intent.setClass(context, Updata_BackOrder_Activity.class);
	    		   intent.putExtra("workID", workID);
	    		   startActivity(intent);

	    		   break;
	    		   
	    	   case R.id.button1:
	    		   //��ͼ��ʾ
	    		   intent.setClass(context, Baidu_NewLock_Activity.class);
	    		   intent.putExtra("workID", workID);
	    		   intent.putExtra("Type", "Show");		//��ʾ��ʽ
	    		   startActivity(intent);
	    		   
	    		   break;
  			}  
  		}
  	}
  	
  	
  	//����Handler����
  	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			String s, Str_Date, Str_Numb, Str_Lng, Str_Lat, Str_ID, Str_Name;

  			switch(msg.what){
  			case 0:
  				//Ӧ�����
  				s=msg.obj.toString();
  				Str_Date = s.substring(0, 6);
  				
  				if (Str_Date.equals("620E O")) {
  					//�ǹ���ʱ������
  					Str_Date = s.substring(7,16).trim();

  					if (Str_Date.indexOf(" ") < 0) {
  						//ʱ����û�пո�
  						
  						SQLiteDatabase db=dbHelper.getWritableDatabase();
  						db.execSQL("UPDATE Order_List_Table SET dateLimit = ? WHERE Order_ID = ?", new String[]{Str_Date, workID});		
  						//��ӹ���ʱ�ޣ������Ĺ���״̬
  						
  						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "���ع�������", workID);
  						db.close();
  					}
  				}
  				else if (Str_Date.equals("620E L")) {
  					//�ǹ���������
  					Str_Lng = s.substring(7,18);
  					Str_Lat = s.substring(18,28);
  					Str_ID = s.substring(29,63);
  					Str_Name = s.substring(64);
  					
  					SQLiteDatabase db=dbHelper.getWritableDatabase();
					db.execSQL("INSERT INTO Order_Data_Table (Order_ID, Old_Locker_ID, Old_Locker_Name, Old_Locker_Lng, Old_Locker_lat, Status) " +
							"VALUES (?, ?, ?, ?, ?, ?)", new String[]{workID, Str_ID, Str_Name, Str_Lng, Str_Lat, "0"});
					db.close();
					
					Flash_Lock_List();		//ˢ�����б�
  				}
  				else if (Str_Date.equals("6217 F") || Str_Date.equals("6217 E")) {
  					//�������ϱ�ƽ̨��Ϣ�Ļظ�
  					Str_Numb = s.substring(7);
  					Str_Name = workID.substring(0, 6);
  					Intent intent = new Intent();   //����Intent���� 
  					
  					if (Str_Numb.equals("FF") || Str_Numb.equals("0B")) {
  						//ƽ̨ע��ʧ��
  						controller.close();		//�ȹر���������
  						BLE_Get_Start_Tag = false;
  						BLE_Lock_Contrl_Tag = false;
  						
  						AlertDialog dialog_1 = new AlertDialog.Builder(context)
							.setTitle("����ע��NB��Ϣʧ��")			//���öԻ���ı���
							.setMessage("���������ϱ�NBע��ʧ������������н����豸��NBע�ᣬȻ�����ʩ��")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog_1.show();

  					}
  					else if (Str_Numb.equals("00")) {
  						Int_Get_GPS_Tag = 0;			//�����־λ���½��������յ�6105Ӧ��󣬲��������깤���ݱ���
  						//������ܵ�ƽ̨ע�ᣬ�����豸��NBע��
  						BLE_Lock_Contrl_Tag = false;
  						index_6105 = 2;
  						Send_BLE_Tag = true;	//��������ָ���־��λ
						BLE_Communication.Send_Command_6105(controller, 2);		//����Ҫ���豸����NBע��
						
  						if (Str_Name.equals("INSERT")) {
  							//����Activity��ʽ�����ٶȵ�ͼ��λ���Ա�ش�����λλ��
  							Int_Get_GPS_Tag = 0;
							intent.setClass(context, Baidu_NewLock_Activity.class);
							intent.putExtra("workID", workID);
							intent.putExtra("Type", "Position");	//��λ��ʽ
							startActivityForResult(intent, 1);
  						}
  						else if (Str_Name.equals("MAINTE")) {
  							BLE_Lock_Contrl_Tag = false;
  							
  						}
  					}
  					else if (Str_Numb.equals("44")) {
  						//�豸�Ѿ�ע��
  						if (Str_Name.equals("INSERT")) {
  	  						//����Activity��ʽ�����ٶȵ�ͼ��λ���Ա�ش�����λλ��
  							intent.setClass(context, Baidu_NewLock_Activity.class);
  							intent.putExtra("workID", workID);
  							intent.putExtra("Type", "Position");	//��λ��ʽ
  							startActivityForResult(intent, 1);
  	  					}
  						else if (Str_Name.equals("MAINTE")) {
  							BLE_Lock_Contrl_Tag = false;
  							index_6105 = 1;
  							Send_BLE_Tag = true;	//��������ָ���־��λ
  							BLE_Communication.Send_Command_6105(controller, 1);		//����д�豸���ñ�־ָ��
  						}
  					}
  				}
  				
  				break;
  				
  			case 2:
  				s=msg.obj.toString();

  				if (s.equals("Wait_Send_620E")) {
  					
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.DownLoad_OrderData_620E(workID);		//�������ع�������ָ��
  					
  				}
  				else if (s.equals("Wait_Send_6217 NEW")) {

  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.Send_NB_Information_6217(New_Lock_ID, Lock_imsi);		//�����ϴ�NB��Ϣָ��
  				}
  				else if (s.equals("Wait_Send_6217 OLD")) {

  					Connect_NMS.Wait_Recive_TCP_Reply();	
  			    	NMS_Communication.Send_NB_Information_6217(Lock_ID, Lock_imsi);		//�����ϴ�NB��Ϣָ��

  				}
  				  				
  				break;
  				  				
  			}
  		}
  	};


	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i, temp_Int;
						
			if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_UPDATE_DEVICE_LIST)) {
				String name = intent.getStringExtra("name");
				String address = intent.getStringExtra("address");
				
				if(name != null){
					i = 0;
					if (BLE_List_Total > 0) {
						for (i = 0; i < BLE_List_Total; i++) {
							if (BLE_Name[i].equals(name) && BLE_Address[i].equals(address)) {
								i = 10;
								break;
							}
						}
					}
					
					if (i < 5) {
						BLE_Name[BLE_List_Total] = name;
						BLE_Address[BLE_List_Total] = address;
						
						BLE_List_Total++;
						
						if (BLE_List_Total == 1) {
							final Context context_1 = context;
							
					        new Handler().postDelayed(new Runnable(){     
							    public void run() {   
							    	AlertDialog dialog;
							    	switch (BLE_List_Total) {
									case 1:
										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("������Ϣ")			//���öԻ���ı���
						                	.setMessage(BLE_Name[0])	//��ʾ�������豸��
						                	//���öԻ���İ�ť
						                	.setNegativeButton("ȡ��", null)
						                	.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_BLE_Lock_Start(0);
						                			dialog.dismiss();
						                		}
						                	}).create();
										dialog.show();
										break;
										
									case 2:
										final String items2[] = {BLE_Name[0], BLE_Name[1]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("��ѡ�����豸")	//���öԻ���ı���
						                	.setSingleChoiceItems(items2, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_BLE_Lock_Start(which);
						                			dialog.dismiss();
						                		}
						                	})
						                	.setNegativeButton("ȡ��", null)
						                	.create();
										dialog.show();
										break;
										
									case 3:
										final String items3[] = {BLE_Name[0], BLE_Name[1], BLE_Name[2]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("��ѡ�����豸")	//���öԻ���ı���
						                	.setSingleChoiceItems(items3, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_BLE_Lock_Start(which);
						                			dialog.dismiss();
						                		}
						                	})
						                	.setNegativeButton("ȡ��", null)
						                	.create();
										dialog.show();
										break;
										
									case 4:
										final String items4[] = {BLE_Name[0], BLE_Name[1], BLE_Name[2], BLE_Name[3]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("��ѡ�����豸")	//���öԻ���ı���
						                	.setSingleChoiceItems(items4, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_BLE_Lock_Start(which);
						                			dialog.dismiss();
						                		}
						                	})
						                	.setNegativeButton("ȡ��", null)
						                	.create();
										dialog.show();
										break;
										
									case 5:
										final String items5[] = {BLE_Name[0], BLE_Name[1], BLE_Name[2], BLE_Name[3], BLE_Name[4]};

										dialog = new AlertDialog.Builder(context_1)
						                	.setTitle("��ѡ�����豸")	//���öԻ���ı���
						                	.setSingleChoiceItems(items5, 1, new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Read_BLE_Lock_Start(which);
						                			dialog.dismiss();
						                		}
						                	})
						                	.setNegativeButton("ȡ��", null)
						                	.create();
										dialog.show();
										break;
									}
							    }     
							 }, 1000);			//��ʱ1�뵯���豸ѡ��
						}
					}
				}
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_GET_DEVICE_CHARACT)) {
				//��ȡ����������ֵ����ʼ����ͨ��ָ��
				if (!BLE_Get_Start_Tag) {
					BLE_Get_Start_Tag = true;
					
					Send_BLE_Tag = true;	//��������ָ���־��λ
					BLE_Communication.Send_Command_6102(controller);		//���Ͷ��豸IDָ��
				}
				
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE)){
				//connectedDevice.setText("���ӵ������ǣ�"+intent.getStringExtra("address"));
			}
			
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_STOP_CONNECT)){
				//connectedDevice.setText("");
				//toast("�����ѶϿ�");
			}
			
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE)){
				String Recive_Str = intent.getStringExtra("message");
				//�յ�����Ӧ��
				
				AlertDialog dialog;
				String temp_Str, temp_Str_1, temp_Str_2 = null;
				
				if (Send_BLE_Tag) {
					Send_BLE_Tag = false;		//�����ն����Ӧ��
					
					if (Recive_Str.substring(0, 7).equals("C610200") && ! BLE_Lock_Contrl_Tag) {
						//�յ��豸ID
						temp_Str_2 = Recive_Str.substring(7, 41);		//ȡͨ�������������豸ID
						temp_Str_1 = "Old";
						for (i = 1; i<=Lock_Index; i++) {
							if (temp_Str_2.equals(List_Lock_ID[i]) && List_Lock_Finish[i]) {
								temp_Str_1 = "Finish";
								break;
							}
						}
						
						if (temp_Str_1.equals("Finish")) {
							//���깤���ߣ�����ṩ��������
							final String Str_Lock_ID = temp_Str_2;
							final Context context_1 = context;
							
							dialog = new AlertDialog.Builder(context)
	                		.setTitle("�Ƿ�򿪴��� ��")			//���öԻ���ı���
	                		.setMessage(BLE_Name[0])	//��ʾ�������豸��
	                		//���öԻ���İ�ť
	                		.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
	                			@Override
	                			public void onClick(DialogInterface dialog, int which) {
	                				controller.close();		//�ȹر���������
 			   						BLE_Get_Start_Tag = false;
 			   						BLE_Lock_Contrl_Tag = false;
	                			}
	                		})
	                		.setPositiveButton("����", new DialogInterface.OnClickListener() {
	                			@Override
	                			public void onClick(DialogInterface dialog, int which) {
	                				String Recive_Str = "123456 "+ Str_Lock_ID;
	                				
	                				Send_BLE_Tag = true;	//��������ָ���־��λ
	 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
					
	 			   					if (! BLE_Lock_Contrl_Tag) {
	 			   						controller.close();		//�ȹر���������
	 			   						BLE_Get_Start_Tag = false;
	 			   						BLE_Lock_Contrl_Tag = false;
	 			   					
	 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context_1)
	 			   							.setTitle("��ʾ")			//���öԻ���ı���
	 			   							.setMessage("ֱ������ʧ��")
	 			   							//���öԻ���İ�ť
	 			   							.setPositiveButton("ȷ��", null)
	 			   							.create();
	 			   						dialog_1.show();
	 			   					}
	 			   					dialog.dismiss();
	                			}
	                		}).create();
							dialog.show();
						}
						else if ( work_Status.equals("�ѻص�")){
							controller.close();		//�ȹر���������
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							
							dialog = new AlertDialog.Builder(context)
								.setTitle("�Ǵ˹�������")			//���öԻ���ı���
								.setMessage("���ڴ���Ȩ���Ʋ������� ��")
								//���öԻ���İ�ť
								.setPositiveButton("ȷ��", null)
								.create();
							dialog.show();
						}
						else {
							//δ�깤����
							Int_Get_GPS_Tag = 0;
							BLE_Lock_Contrl_Tag = true;
							temp_Str_1 = workID.substring(0, 6);
							temp_Str = Recive_Str.substring(73);
							
							if (temp_Str_1.equals("MAINTE") && Get_New_Lock.getText().toString().equals("�ɼ�������Ϣ")) {
								New_Lock_ID = Recive_Str.substring(7, 41);		//ȡͨ�������������豸ID
								Lock_imsi = Recive_Str.substring(41, 73);
								
								if ( temp_Str.equals("00")) {
									//�����ϱ�NB��Ϣ
									Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6217 NEW");
									Connect_NMS.Make_Socket_Connect();
									
								}
								else if ( temp_Str.equals("6C")) {
									controller.close();		//�ȹر���������
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
									
									dialog = new AlertDialog.Builder(context)
										.setTitle("��ʾ")			//���öԻ���ı���
										.setMessage("��������Ѿ����й�ʩ�����á�")
										//���öԻ���İ�ť
										.setPositiveButton("ȷ��", null)
										.create();
									dialog.show();
								}
							}
							else {
								Lock_ID = Recive_Str.substring(7, 41);		//ȡͨ�������������豸ID
								Lock_imsi = Recive_Str.substring(41, 73);
								
								if (temp_Str.equals("00") && ! temp_Str_1.equals("REMOVE")) {
									if (temp_Str_1.equals("INSERT")) {
										//�����ϱ�NB��Ϣ
										Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6217 OLD");
										Connect_NMS.Make_Socket_Connect();

									}
									else if (temp_Str_1.equals("MAINTE")) {
										controller.close();		//�ȹر���������
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
										
										dialog = new AlertDialog.Builder(context)
											.setTitle("��ʾ")			//���öԻ���ı���
											.setMessage("��ѡ��Ҫά�������ߡ�")
											//���öԻ���İ�ť
											.setPositiveButton("ȷ��", null)
											.create();
										dialog.show();
									}
									else {
										controller.close();		//�ȹر���������
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
										
										dialog = new AlertDialog.Builder(context)
											.setTitle("��ʾ")			//���öԻ���ı���
											.setMessage("�������Ǵ�ʩ�����豸��")
											//���öԻ���İ�ť
											.setPositiveButton("ȷ��", null)
											.create();
										dialog.show();
									}
								}
								else if (temp_Str_1.equals("INSERT")) {
									controller.close();		//�ȹر���������
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
									
									dialog = new AlertDialog.Builder(context)
										.setTitle("��ʾ")			//���öԻ���ı���
										.setMessage("��������Ѿ����й�ʩ�����á�")
										//���öԻ���İ�ť
										.setPositiveButton("ȷ��", null)
										.create();
									dialog.show();
								}
								else {
									//����ʩ�������Ȳ鿴�Ƿ���ʩ��Ҫ�������
									temp_Int = 0;
									for (i = 1; i<=Lock_Index; i++) {
										if (Lock_ID.equals(List_Lock_ID[i])) {
											//���������������
											temp_Int = i;
											break;
										}
									}
									
									if (temp_Int == 0) {
										controller.close();		//�ȹر���������
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
										
										dialog = new AlertDialog.Builder(context)
											.setTitle("��ʾ")			//���öԻ���ı���
											.setMessage("�������Ǵ�ʩ�����豸��")
											//���öԻ���İ�ť
											.setPositiveButton("ȷ��", null)
											.create();
										dialog.show();
									}
									else {
										//������ƻ�ʩ��
										final String Str_Lock_Name = List_Lock_Name[temp_Int];
										final String Str_Lock_ID = List_Lock_ID[temp_Int];
										final String Str_Title = workType;
										final Context context_1 = context;
										AlertDialog.Builder dialog_1 =  new   AlertDialog.Builder(Make_NewLock_Activity.this  ); 
						 					
										Jast_Open_Tag = List_Lock_Finish[temp_Int];
										
										if (Jast_Open_Tag) {
											//���깤�����ֻ�ܿ���
											dialog = new AlertDialog.Builder(context_1)
						                		.setTitle("�Ƿ�򿪴��� ��")			//���öԻ���ı���
						                		.setMessage(BLE_Name[0])	//��ʾ�������豸��
						                		//���öԻ���İ�ť
						                		.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
						                			@Override
						                			public void onClick(DialogInterface dialog, int which) {
						                				controller.close();		//�ȹر���������
						                				BLE_Get_Start_Tag = false;
						                				BLE_Lock_Contrl_Tag = false;
						                			}
						                		})
						                		.setPositiveButton("����", new DialogInterface.OnClickListener() {
						                			@Override
						                			public void onClick(DialogInterface dialog, int which) {
						                				String Recive_Str = "123456 "+ Str_Lock_ID;
						                				
						                				Send_BLE_Tag = true;	//��������ָ���־��λ
						 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
										
						 			   					if (! BLE_Lock_Contrl_Tag) {
						 			   						controller.close();		//�ȹر���������
						 			   						BLE_Get_Start_Tag = false;
						 			   						BLE_Lock_Contrl_Tag = false;
						 			   				
						 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context_1)
						 			   							.setTitle("��ʾ")			//���öԻ���ı���
						 			   							.setMessage("ֱ������ʧ��")
						 			   							//���öԻ���İ�ť
						 			   							.setPositiveButton("ȷ��", null)
						 			   							.create();
						 			   						dialog_1.show();
						 			   					}
						 			   					dialog.dismiss();
						                			}
						                		}).create();
											dialog.show();
										}
										else {		
											//δ�깤�����ڿ�����ά���н���ѡ��
					 			   			dialog_1.setTitle("��ѡȡ���������ߵĲ���").setMessage(Str_Lock_Name);	//��ʾ�������豸��
					 			   			//�˶Ի�������ö������
					 			   			dialog_1.setPositiveButton(Str_Title, new DialogInterface.OnClickListener() {
					 			   				//���ʩ����ť
					 			   				@Override
					 			   				public void onClick(DialogInterface dialog, int i) {
					 			   					AlertDialog dialog_1;
					 			   					if (Str_Title.equals("ά��")) {
					 			   						controller.close();		//�ȹر���������
					 			   						BLE_Get_Start_Tag = false;
					 			   						BLE_Lock_Contrl_Tag = false;
					 			   				
					 			   						dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("��ʾ")			//���öԻ���ı���
					 			   							.setMessage("���������������ȡ�����豸��Ϣ")
					 			   							//���öԻ���İ�ť
					 			   							.setPositiveButton("ȷ��", null)
					 			   							.create();
					 			   						dialog_1.show();
					 			   						
					 			   						Get_New_Lock.setText("�ɼ�������Ϣ");
					 			   						Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
					 			   					}
					 			   					else if (Str_Title.equals("���")) {
					 			   						
					 			   						dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("��ȷ��Ҫ������� ��")			//���öԻ���ı���
					 			   							.setMessage("���豸���� " + Str_Lock_Name)
					 			   							//���öԻ���İ�ť
					 			   							.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					 			    			   				@Override
					 			    			   				public void onClick(DialogInterface dialog, int which) {
					 			   						    		//��������Ϣ

					 			    			   					New_Lock_ID = "";
					 			    			   					
					 			            						int temp_Int = 0;
					 			            						SQLiteDatabase db=dbHelper.getWritableDatabase();
					 			            						
					 			            						db.execSQL("UPDATE Order_Data_Table SET Status = '1' WHERE Order_ID = ? AND Old_Locker_ID = ?", new String[]{workID, Lock_ID});
					 			            						
					 			            						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "��ɶ����ߵĲ������", Str_Lock_Name);
					 			            						
					 			            						Send_BLE_Tag = true;	//��������ָ���־��λ
					 			            						BLE_Communication.Send_Command_6105(controller, 0);		//�������豸��������ָ��
					 			            						
					 			            						Cursor cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Status = '0'", new String[]{workID});    			    
					 			            						if (cursor.moveToNext()){
					 			            							temp_Int = 1;
					 			            						}
					 			            						cursor.close();
					 			            						
					 			            						AlertDialog dialog_1;
					 			            						if (temp_Int == 0) {
					 			            							//ȫ���깤
					 			            							db.execSQL("UPDATE Order_List_Table SET Status = '3' WHERE Order_ID = ?", new String[]{workID});		//���Ĺ������б�״̬
					 			            						
					 			            							dialog_1 = new AlertDialog.Builder(context_1)
					 			            								.setTitle("��ǰ����ȫ���깤")			//���öԻ���ı���
					 			            								.setMessage("�뾡��ص� ��")
					 			            								//���öԻ���İ�ť
					 			            								.setPositiveButton("ȷ��", null)
					 			            								.create();
					 			            							dialog_1.show();
					 			            						
					 			            						}
					 			            						else {
					 			            							db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//���Ĺ������б�״̬
					 			            						
					 			            							dialog_1 = new AlertDialog.Builder(context_1)
					 		            									.setTitle("����ɹ�")			//���öԻ���ı���
					 		            									.setMessage("���봦����һ�������豸��")
					 		            									//���öԻ���İ�ť
					 		            									.setPositiveButton("ȷ��", null)
					 		            									.create();
					 			            							dialog_1.show();
					 			            						}
					 			            						
					 			            						db.close();
					 			            						
					 			            						Flash_Lock_List();		//ˢ�����б�
					 			    			   				}
					 			    			   			})
					 			   									
					 			   							.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					 			    			   				@Override
					 			    			   				public void onClick(DialogInterface dialog, int which) {
					 			    			   					controller.close();		//�ȹر���������
					 			    			   					BLE_Get_Start_Tag = false;
					 			    			   					BLE_Lock_Contrl_Tag = false;
					 			    			   				}
					 			    			   			})
					 			   							.create();
					 			   						dialog_1.show();
					 			   					}
					 			   					else if (Str_Title.equals("�ƻ�")) {
					 			   						dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("�豸�ƻ��� " + Str_Lock_Name)			//���öԻ���ı���
					 			   							.setMessage("�뽫�����豸��װ����λ�ú����½��е�ͼ��λ")
					 			   							//���öԻ���İ�ť
					 			   							.setPositiveButton("ȷ��", null)
					 			   							.create();
					 			   						dialog_1.show();
					 			   						
					 			   						//����Activity��ʽ�����ٶȵ�ͼ��λ���Ա�ش�����λλ��
					 			   						Intent intent = new Intent();   //����Intent���� 
					 			   					
					 									intent.setClass(context_1, Baidu_NewLock_Activity.class);
					 									intent.putExtra("workID", workID);
					 									intent.putExtra("Type", "Position");	//��λ��ʽ
					 									startActivityForResult(intent, 1);
					 			   					}
					 			   				}
					 			   			});
					 			   			dialog_1.setNegativeButton("����", new DialogInterface.OnClickListener() {
					 			   				@Override
					 			   				public void onClick(DialogInterface dialog, int which) {
											    						
					 			   					//�߱���ʱ�Ե���ȫȨ��
					 			   					String Recive_Str = "123456 "+ Str_Lock_ID;
					 			   					
					 			   					Send_BLE_Tag = true;	//��������ָ���־��λ
					 			   					BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, true);		
									
					 			   					if (! BLE_Lock_Contrl_Tag) {
					 			   						controller.close();		//�ȹر���������
					 			   						BLE_Get_Start_Tag = false;
					 			   						BLE_Lock_Contrl_Tag = false;
					 			   				
					 			   						AlertDialog dialog_1 = new AlertDialog.Builder(context_1)
					 			   							.setTitle("��ʾ")			//���öԻ���ı���
					 			   							.setMessage("ֱ������ʧ��")
					 			   							//���öԻ���İ�ť
					 			   							.setPositiveButton("ȷ��", null)
					 			   							.create();
					 			   						dialog_1.show();
					 			   					}
					 			   					dialog.dismiss();
					 			   				}
					 			   			});
					 			   			dialog_1.show();
										}
									}
								}
							}
						}
					}
					else if (Recive_Str.substring(0, 9).equals("C61050000") && ! BLE_Lock_Contrl_Tag) {
						//д�����óɹ�����������
						BLE_Lock_Contrl_Tag = true;
											
						temp_Str = workID.substring(0, 6);
						if (temp_Str.equals("INSERT") && Int_Get_GPS_Tag == 1 && index_6105 == 1) {
							SQLiteDatabase db=dbHelper.getWritableDatabase();
							
							db.execSQL("DELETE FROM Order_Data_Table WHERE Order_ID = ? AND New_Locker_ID = ?", new String[]{workID, Lock_ID});
							
							db.execSQL("INSERT INTO Order_Data_Table (Order_ID, New_Locker_ID, New_Locker_Name, New_Locker_Lng, New_Locker_lat, Status, New_Lock_imsi) " +
									"VALUES (?, ?, ?, ?, ?, ?, ?)", new String[]{workID, Lock_ID, Get_Lock_Name, String.valueOf(Get_Lock_Lng), String.valueOf(Get_Lock_Lat), "1", Lock_imsi});
							
							db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//���Ĺ������б�״̬

							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "��ɶ����ߵ��½�����", Get_Lock_Name);

							db.close();
							
							dialog = new AlertDialog.Builder(context)
								.setTitle("���óɹ�")			//���öԻ���ı���
								.setMessage("��ǰ���豸������ɣ��ɼ��������������ߣ���ص���")
								//���öԻ���İ�ť
								.setPositiveButton("ȷ��", null)
								.create();
							dialog.show();
						}
						else if (temp_Str.equals("MAINTE") && index_6105 == 1) {
							temp_Int = 0;
							String str_lng = "0", str_lat = "0";
							
							SQLiteDatabase db=dbHelper.getWritableDatabase();
							Cursor cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Old_Locker_ID = ?", new String[]{workID, Lock_ID});    			    
							if (cursor.moveToNext()){
								Get_Lock_Name = cursor.getString(3);
								str_lng = cursor.getString(4);
								str_lat = cursor.getString(5);
							}
							cursor.close();

							db.execSQL("UPDATE Order_Data_Table SET Status = '1', New_Locker_Name = ?, New_Locker_Lng = ?, New_Locker_Lat = ?, New_Locker_ID = ?, New_Lock_imsi = ?" +
									" WHERE Order_ID = ? AND Old_Locker_ID = ?", new String[]{Get_Lock_Name, str_lng, str_lat, New_Lock_ID, Lock_imsi, workID, Lock_ID});
							
							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "��ɶ����ߵ�ά������", Get_Lock_Name);
							
							temp_Int = 0;
							cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Status = '0'", new String[]{workID});    			    
							if (cursor.moveToNext()){
								temp_Int = 1;
							}
							cursor.close();

							if (temp_Int == 0) {
								//ȫ���깤
								db.execSQL("UPDATE Order_List_Table SET Status = '3' WHERE Order_ID = ?", new String[]{workID});		//���Ĺ������б�״̬
							}
							else {
								db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//���Ĺ������б�״̬
							}
							
							db.close();
							
							if (temp_Int == 0) {
								//ȫ���깤
								dialog = new AlertDialog.Builder(context)
									.setTitle("��ǰ����ȫ���깤")			//���öԻ���ı���
									.setMessage("�뾡��ص� ��")
									//���öԻ���İ�ť
									.setPositiveButton("ȷ��", null)
									.create();
								dialog.show();
							}
							else {
								dialog = new AlertDialog.Builder(context)
									.setTitle("��ά���ɹ�")			//���öԻ���ı���
									.setMessage("���봦����һ�������豸��")
									//���öԻ���İ�ť
									.setPositiveButton("ȷ��", null)
									.create();
								dialog.show();
							}
							
							Get_New_Lock.setText("�ɼ�����Ϣ");
							Get_New_Lock.setTextColor(Color.rgb(0, 0, 0));
							
						}
						else if (temp_Str.equals("MAINTE") && index_6105 == 2) {
							BLE_Lock_Contrl_Tag = false;
  							index_6105 = 1;
  							Send_BLE_Tag = true;	//��������ָ���־��λ
  							BLE_Communication.Send_Command_6105(controller, 1);		//����д�豸���ñ�־ָ��
						}
						
						controller.close();
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
											
						Flash_Lock_List();		//ˢ�����б�
						
					}
					else if (Recive_Str.substring(0, 7).equals("C610500") && ! Recive_Str.substring(7).equals("00")) {
						//д������ʧ��
						controller.close();		//�ȹر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						Get_Lock_Lat = 0;
						Get_Lock_Lng = 0;
						Get_Lock_Name = ""; 
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("���ó���")			//���öԻ���ı���
							.setMessage("��ǰ���豸����ʧ�ܣ������� ��")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();					
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000") && workID.substring(0, 6).equals("MAINTE") && !Jast_Open_Tag) {
						controller.close();		//�ȹر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("��ά������")			//���öԻ���ı���
							.setMessage("ά����ɺ��뼤�����ߣ���ȡ��������Ϣ ��")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();

						Get_New_Lock.setText("�ɼ�������Ϣ");
						Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
					}
					else if (!BLE_Lock_Contrl_Tag) {
						controller.close();		//�ȹر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("��ʾ")			//���öԻ���ı���
							.setMessage("����ͨ�Ź��� ��")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000")) {
						controller.close();			//�ر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
				}
				else {
					//����Ļظ�
					if (Recive_Str.substring(0, 7).equals("C610200")) {
						temp_Str_2 = Recive_Str.substring(7, 41);		//ȡͨ�������������豸ID
						temp_Str_1 = "Old";
						for (i = 1; i<=Lock_Index; i++) {
							if (temp_Str_2.equals(List_Lock_ID[i]) && List_Lock_Finish[i]) {
								temp_Str_1 = "Finish";
								break;
							}
						}
						
						if (temp_Str_1.equals("Finish")) {
							//���깤���ߣ�����ṩ��������
							
						}
						else if ( work_Status.equals("�ѻص�")){
							controller.close();		//�ȹر���������
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
						}
						else {
							//δ�깤����
							Int_Get_GPS_Tag = 0;
							BLE_Lock_Contrl_Tag = true;
							temp_Str_1 = workID.substring(0, 6);
							temp_Str = Recive_Str.substring(73);
							
							if (temp_Str_1.equals("MAINTE") && Get_New_Lock.getText().toString().equals("�ɼ�������Ϣ")) {
								if ( temp_Str.equals("6C")) {
									controller.close();		//�ȹر���������
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
								}
							}
							else {
								
								if (temp_Str.equals("00")) {
									if (temp_Str_1.equals("INSERT")) {

									}
									else if (temp_Str_1.equals("MAINTE")) {
										controller.close();		//�ȹر���������
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
									}
									else {
										controller.close();		//�ȹر���������
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
									}
								}
								else if (temp_Str_1.equals("INSERT")) {
									controller.close();		//�ȹر���������
									BLE_Get_Start_Tag = false;
									BLE_Lock_Contrl_Tag = false;
								}
								else {
									//����ʩ�������Ȳ鿴�Ƿ���ʩ��Ҫ�������
									temp_Int = 0;
									for (i = 1; i<=Lock_Index; i++) {
										if (Lock_ID.equals(List_Lock_ID[i])) {
											//���������������
											temp_Int = i;
											break;
										}
									}
									
									if (temp_Int == 0) {
										controller.close();		//�ȹر���������
										BLE_Get_Start_Tag = false;
										BLE_Lock_Contrl_Tag = false;
									}
								}
							}
						}
					}
					else if (Recive_Str.substring(0, 7).equals("C610500")) {
						controller.close();			//�ر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000") && workID.substring(0, 6).equals("MAINTE") && !Jast_Open_Tag) {
						controller.close();			//�ر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
					else if (!BLE_Lock_Contrl_Tag) {
						controller.close();			//�ر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
					else if (Recive_Str.substring(0, 9).equals("C61010000")) {
						controller.close();			//�ر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
				}
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_LOSS_DEVICE_CHARACT)){
				//δ�ܻ�ȡ������ͨ������ֵ
				if (controller != null) {
					controller.close();		//�ȹر���������
					BLE_Get_Start_Tag = false;
					BLE_Lock_Contrl_Tag = false;
				}
				
				AlertDialog dialog = new AlertDialog.Builder(context)
            		.setTitle("����")			//���öԻ���ı���
            		.setMessage("����ֱ��ʧ�ܣ�")	//��ʾ�������豸��
            		//���öԻ���İ�ť
            		.setPositiveButton("ȷ��", null)
            		.create();
				dialog.show();
			}
		}
	}
    
	public void Read_BLE_Lock_ID() {
		BLE_Get_Start_Tag = false;
		BLE_Lock_Contrl_Tag = false;
		controller.close();		//�ȹر����з���
		BLE_Get_Start_Tag = false;
		BLE_Lock_Contrl_Tag = false;
		
		//��ʼ��������
		intentService = new Intent(context,BLEService.class);   
		startService(intentService);
		// ��ʼ������
		controller.initBLE();
		BLE_List_Total = 0;
				
		receiver=new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
		intentFilter.addAction(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
		intentFilter.addAction(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
		intentFilter.addAction(ConstantUtils.ACTION_STOP_CONNECT);
		intentFilter.addAction(ConstantUtils.ACTION_GET_DEVICE_CHARACT);
		intentFilter.addAction(ConstantUtils.ACTION_LOSS_DEVICE_CHARACT);
		registerReceiver(receiver, intentFilter);
		
		if(!controller.initBLE()){//�ֻ���֧������
			Toast.makeText(context, "�����ֻ���֧������",
					Toast.LENGTH_SHORT).show();
			return;//�ֻ���֧��������ɶҲ���ø��ˣ��ص���˯��ȥ��
		}
		if (!controller.isBleOpen()) {// ���������û�д�
			Toast.makeText(context, "�������",
					Toast.LENGTH_SHORT).show();
			return;
		}
		new GetDataTask().execute();// ��������
	}
	
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			if(controller.isBleOpen()){
				controller.startScanBLE();
			};// ��ʼɨ��
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
		}
	}
	
	
	
    private void Read_BLE_Lock_Start(int this_BLE_Numb) {
		//��������IDָ���
		EntityDevice BLE_temp = new EntityDevice();
		BLE_temp.setName(BLE_Name[this_BLE_Numb]);
		BLE_temp.setAddress(BLE_Address[this_BLE_Numb]);
		
		//��������
		controller.connect(BLE_temp);
		
		final BluetoothController controller_1 = controller;
        new Handler().postDelayed(new Runnable(){     
		    public void run() {   
		    	if (! controller_1.findGattCharacteristic()) {

		    		AlertDialog dialog = new AlertDialog.Builder(context)
            			.setTitle("����")			//���öԻ���ı���
            			.setMessage("����ֱ��ʧ�ܣ�")	//��ʾ�������豸��
            			.setPositiveButton("ȷ��", null)
            			.create();
		    		dialog.show();
		    	}
		    }     
		 }, 5000);			//��ʱ��������Ƿ����ӳɹ�

	}
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
    		//�ٶ�վ�㶨λ�ɹ�
    		Uri horse = data.getData();

    		//���õĻش��豸����GPS����
			Get_Lock_Lat = data.getDoubleExtra("Lat", 0);
			Get_Lock_Lng = data.getDoubleExtra("Lng", 0);
			Get_Lock_Name = data.getStringExtra("Name");

			String temp_Str = workID.substring(0, 6);
			if (temp_Str.equals("MAINTE")) {
				//ά��������ͨ����ͼѡ����ά�����豸ID (������ʱ�������޷�ͨ��������������ID)
				Lock_ID = data.getStringExtra("Name");
				
				Get_New_Lock.setText("�ɼ�������Ϣ");
				Get_New_Lock.setTextColor(Color.rgb(255, 0, 0));
			}
			else if (temp_Str.equals("INSERT")) {
				//����д�����ñ�־
				Int_Get_GPS_Tag = 1;			//GPS��λ��־��λ
				Get_Lock_Name = data.getStringExtra("Name");
				BLE_Lock_Contrl_Tag = false;
				
				index_6105 = 1;
				Send_BLE_Tag = true;	//��������ָ���־��λ
				BLE_Communication.Send_Command_6105(controller, 1);		//����д�豸���ñ�־ָ��
			}
			else if (temp_Str.equals("MOVETO")) {
				//����ƻ�������������
				Get_Lock_Name = data.getStringExtra("Name");
				int temp_Int = 0;
				SQLiteDatabase db=dbHelper.getWritableDatabase();

				db.execSQL("UPDATE Order_Data_Table SET Status = '1', New_Locker_Name = ?, New_Locker_Lng = ?, New_Locker_Lat = ?, New_Locker_ID = ?" +
						" WHERE Order_ID = ? AND Old_Locker_ID = ?", new String[]{Get_Lock_Name, String.valueOf(Get_Lock_Lng), String.valueOf(Get_Lock_Lat), Lock_ID, workID, Lock_ID});
				
				DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "��ɶ����ߵ��ƻ�����", Get_Lock_Name);
				
				Cursor cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Old_Locker_ID = ? AND Status = '0'", new String[]{workID, Lock_ID});    			    
				if (cursor.moveToNext()){
					temp_Int = 1;
				}
				cursor.close();
				
				AlertDialog dialog;
				if (temp_Int == 0) {
					//ȫ���깤
					db.execSQL("UPDATE Order_List_Table SET Status = '3' WHERE Order_ID = ?", new String[]{workID});		//���Ĺ������б�״̬
					
					dialog = new AlertDialog.Builder(context)
						.setTitle("��ǰ����ȫ���깤")			//���öԻ���ı���
						.setMessage("�뾡��ص� ��")
						//���öԻ���İ�ť
						.setPositiveButton("ȷ��", null)
						.create();
					dialog.show();
				}
				else {
					db.execSQL("UPDATE Order_List_Table SET Status = '2' WHERE Order_ID = ?", new String[]{workID});		//���Ĺ������б�״̬
				
					dialog = new AlertDialog.Builder(context)
						.setTitle("�ƻ��ɹ�")			//���öԻ���ı���
						.setMessage("���봦����һ�������豸��")
						//���öԻ���İ�ť
						.setPositiveButton("ȷ��", null)
						.create();
					dialog.show();
				}
				
				db.close();
				
			}
    	}
    	
    }
    
    class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Close_BackOrder")) {
				//��ɻص�����ҳ�汻�ر�
				dbHelper.close();
				
				Intent in=new Intent(); 
		    	in.setAction("Flash_Order_List"); 		//����ˢ�¹����б�㲥
		    	sendBroadcast(in);
		    	
				finish();
			}
		}	
    }
    
    //��ʾ��γ����Ϣ  
   	private void showLocation(final Location location) {  

   		LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
		
		com.baidu.mapapi.utils.CoordinateConverter converter  = new com.baidu.mapapi.utils.CoordinateConverter();    
       	converter.from(CoordType.GPS);    
       	// sourceLatLng��ת������    	
       	converter.coord(ll);    
       	ll= converter.convert(); 

    	Your_Lng_C = ll.longitude;
    	Your_Lat_C = ll.latitude;
   		
   	}
   	
   	//locationListener������3���������ֲ�̫�õõ��������ڴ�Ҳ����˵�ˣ�����Ȥ�Ŀ����Լ�ȥ�˽�һ��  
  	LocationListener locationListener = new LocationListener() {  

  		@Override  
  		public void onStatusChanged(String provider, int status, Bundle extras) {  
  		}  

  		@Override  
  		public void onProviderEnabled(String provider) {  
  		}  

  		@Override  
  		public void onProviderDisabled(String provider) {  
  		}  

  		@Override  
  		public void onLocationChanged(Location location) {  
  			// ���µ�ǰ�豸��λ����Ϣ  
  			showLocation(location);  
  		}  
  	}; 
    
	protected void onResume() {
//
		super.onResume();
		Flash_Lock_List();		//ˢ�����б�
	}
    
	protected void onDestroy() {

		super.onDestroy();
		dbHelper.close();
		controller.close();

		unregisterReceiver(mReceiver);
	}

}
