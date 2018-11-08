package com.example.iods_lock_app;


import java.util.ArrayList;
import java.util.List;


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
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_manage.BaidumapActivity;
import com.example.iods_manage.Locker_Quert_Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Open_Locker_Activity extends Activity {
	//����ҳ��

    DBHelper dbHelper ;//���ݿ�������
    
	private Context context = Open_Locker_Activity.this;//����Context����
	
	TextView Show_GPS_Data;
	
	private Spinner Nearby_Locker_List;
	List<String> list_Name = new ArrayList<String>();
	int Selected_Lock_index = 0;
	
	private Double Last_Lng = 0.0, Last_Lat = 0.0;
	private Double Your_Lng_C = 0.0, Your_Lat_C = 0.0;
	
	static NMS_Communication Connect_NMS;
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean BLE_Lock_Contrl_Tag = false, Send_BLE_Tag;
	
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
	String[] Lock_Lng = new String[100];	//������
	String[] Lock_Lat = new String[100];	//��γ��
	String[] Lock_ID = new String[100];		//��ID
	String[] Lock_Name = new String[100];	//����
	boolean[] Lock_Error = new boolean[100];//���澯״̬
	int Numb_Lock = 0, Total_Locks;			//������
	
	String Locker_ID;
	
	Double Er_Lnglat = UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //������Χ
	Double Scope_Lnglat = UserLogin_Activity.Scope_Limit;
	
	private LocationClient mLocationClient;
    private LocationManager locationManager;  
    private String provider;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        
        setContentView(R.layout.activity_open_locker);
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
        dbHelper = new DBHelper(context);	//����DBHlper����ʵ��
                
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
        locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);  
    }
    
    public void initi(){
    	Button NB_Lock_Contral = (Button)findViewById(R.id.button1);		//���翪��
    	Button Show_Baidu_GIS = (Button)findViewById(R.id.button2);			//��ͼ����
    	Button BT_Lock_Contral = (Button)findViewById(R.id.button3);		//��������
    	Button Help_me_Open = (Button)findViewById(R.id.button4);			//��������
    	Button Btn_Lock_Query = (Button)findViewById(R.id.button5);			//�豸��ѯ
    	
    	Show_GPS_Data=(TextView)findViewById(R.id.textView7);
    	
    	//Ϊ�����ܰ�����ӵ����¼�����
    	NB_Lock_Contral.setOnClickListener(new ClickEvent());
    	Show_Baidu_GIS.setOnClickListener(new ClickEvent());
    	BT_Lock_Contral.setOnClickListener(new ClickEvent());
    	Help_me_Open.setOnClickListener(new ClickEvent());	
    	Btn_Lock_Query.setOnClickListener(new ClickEvent());	
    		
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
    }
    
  
    
    //�Զ��嵥���¼���
    class ClickEvent implements View.OnClickListener {    
    	
	       @SuppressLint("NewApi") @Override    
	       public void onClick(View v) {

	    	   Intent in;
	    	   
	    	   switch(v.getId()){
	    	   case R.id.button1:
	    		   //��ѡȡ�����豸�������翪��ǰ���Ƚ���TCP����
	    		   
	    		   if (Lock_Lng[Selected_Lock_index] == null || Lock_Lat[Selected_Lock_index] == null) {
	    			   AlertDialog dialog_1 = new AlertDialog.Builder(context)
			   				.setTitle("��δ�յ�GPS�ź�")			//���öԻ���ı���		
							.setMessage("����ȴ���ȡ��GPS����")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
	    			   dialog_1.show();
	    		   }
	    		   else {
	    			   SQLiteDatabase db=dbHelper.getWritableDatabase();
		    		   String temp_User = UserLogin_Activity.Login_User_ID;
		    		   	    		   
		    		   final double Error_Lng_C = Your_Lng_C - Double.valueOf(Lock_Lng[Selected_Lock_index]);
		    		   final double Error_Lat_C = Your_Lat_C - Double.valueOf(Lock_Lat[Selected_Lock_index]);
	       				    		   		    		
		    		   Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, Lock_ID[Selected_Lock_index]});
		    		   if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin")){
		    			   //�û���Ȩ�򳬼��û����������
		    			   if (Error_Lng_C > Scope_Lnglat || Error_Lat_C > Scope_Lnglat || Error_Lng_C < -1 * Scope_Lnglat || Error_Lat_C < -1 * Scope_Lnglat) {
		    				   AlertDialog dialog_1 = new AlertDialog.Builder(context)
		    				   			.setTitle("C : " + String.valueOf(Error_Lng_C) + "; " + String.valueOf(Error_Lat_C))			//���öԻ���ı���		
		    				   			
		   								//.setTitle("�������̫Զ")			//���öԻ���ı���
		   								.setMessage("�������޷��������ߣ���ɿ���")
		   								//���öԻ���İ�ť
		   								.setPositiveButton("ȷ��", null)
		   								.create();
		    				   dialog_1.show();
		    			   }
		    			   else {
		    				   //������Ҫ��
		    				   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
		    				   Connect_NMS.Make_Socket_Connect();
		    			   }
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
	    	   case R.id.button2:
	    		   //�����ٶȵ�ͼ�����ܱ����豸λ��
	    		   in=new Intent(context, BaidumapActivity.class);	
	    		   in.putExtra("Mode", "Near");
	    		   if (Your_Lng_C == 0.0 || Your_Lat_C == 0.0) {
	    			   Your_Lng_C = 120.65456;		//��ͨ����λ��
	    			   Your_Lat_C = 31.20083;
	    		   }
    			   
	    		   in.putExtra("GPS_Lng", String.valueOf(Your_Lng_C));
	    		   in.putExtra("GPS_Lat", String.valueOf(Your_Lat_C));
	    		   startActivity(in);//�޸�Ϊ�ڵ��ͼ��ʾ�Ĳ�����
	    		   break;
	    	   case R.id.button3:
	    		   //��������
	    		   Open_BLE_Lock();   	       
 
	    		   break;
	    	   case R.id.button4:
	    		   //���Ϳ���������Ϣ
	    		   if (Lock_Lng[Selected_Lock_index] == null || Lock_Lat[Selected_Lock_index] == null) {
	    			   AlertDialog dialog_1 = new AlertDialog.Builder(context)
		   				.setTitle("��δ�յ�GPS�ź�")			//���öԻ���ı���		
						.setMessage("����ȴ���ȡ��GPS����")
						//���öԻ���İ�ť
						.setPositiveButton("ȷ��", null)
						.create();
	    			   dialog_1.show();
	    		   }
	    		   else{
	    			   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6203");
		    		   Connect_NMS.Make_Socket_Connect();
	    		   }
	    		   	    		   
	    		   break;
	    		   
	    	   case R.id.button5:
	    		   //���߲�ѯ
	    		   in=new Intent(context, Locker_Quert_Activity.class);	
	    		   startActivity(in);//��ѯ���豸λ��
	    		   
	    		   break;
	    	   }
	       }
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

    //��ʾ��γ����Ϣ  
   	private void showLocation(final Location location) {  
   		
   		double this_Lat = location.getLatitude();
   		double this_Lng = location.getLongitude();
   		double Show_Lng, Show_Lat;
   		
   		Show_Lng = ((int) (this_Lng * 1000000 + 0.5)) / 1000000.0;
   		Show_Lat = ((int) (this_Lat * 1000000 + 0.5)) / 1000000.0;

   		Show_GPS_Data.setText(String.valueOf(Show_Lng) + " ;  " + String.valueOf(Show_Lat));
   		
   		LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
		
		com.baidu.mapapi.utils.CoordinateConverter converter  = new com.baidu.mapapi.utils.CoordinateConverter();    
       	converter.from(CoordType.GPS);    
       	// sourceLatLng��ת������    	
       	converter.coord(ll);    
       	ll= converter.convert(); 
   		
       	this_Lat = ll.latitude;
    	this_Lng = ll.longitude;
    	Your_Lng_C = this_Lng;
    	Your_Lat_C = this_Lat;
   		
   		if (this_Lng - Last_Lng > Er_Lnglat || Last_Lng - this_Lng > Er_Lnglat || 
				this_Lat - Last_Lat > Er_Lnglat || Last_Lat - this_Lat > Er_Lnglat) {
			//����ƶ�����ˢ������λ��Ϣ
				
	    	Last_Lat = this_Lat;
			Last_Lng = this_Lng;
	    	
	    	Numb_Lock = 0;
	    	list_Name.clear();
	    	
	    	Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6206");
			Connect_NMS.Make_Socket_Connect();

		}     	
	
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
		dbHelper.close();
		controller.close();

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
	
	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i;
						
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
						                	.setTitle("ֱ������")			//���öԻ���ı���
						                	.setMessage(BLE_Name[0])	//��ʾ�������豸��
						                	//���öԻ���İ�ť
						                	.setNegativeButton("ȡ��", null)
						                	.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						                		@Override
						                		public void onClick(DialogInterface dialog, int which) {
						                			Open_BLE_Lock_Start(0);
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
						                			Open_BLE_Lock_Start(which);
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
						                			Open_BLE_Lock_Start(which);
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
						                			Open_BLE_Lock_Start(which);
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
						                			Open_BLE_Lock_Start(which);
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
				AlertDialog dialog;
				
				if (Send_BLE_Tag) {
					Send_BLE_Tag = false;		//�����ն����Ӧ��
					
					if (Recive_Str.subSequence(0, 7).equals("C610200") && ! BLE_Lock_Contrl_Tag) {
						//�յ��豸ID�����뿪������
						Locker_ID = Recive_Str.substring(7, 41);
						
						Send_BLE_Tag = true;	//��������ָ���־��λ
						BLE_Lock_Contrl_Tag = BLE_Communication.Try_Open_BLE_Lock(controller, dbHelper, Recive_Str, false);		
						
						if (! BLE_Lock_Contrl_Tag) {
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							controller.close();			//�ر���������
							
							dialog = new AlertDialog.Builder(context)
	        					.setTitle("��ʾ")			//���öԻ���ı���
	        					.setMessage("��û�д򿪴�����Ȩ��")
	        					//���öԻ���İ�ť
	        					.setPositiveButton("ȷ��", null)
	        					.create();
							dialog.show();
						}
					}
					else if (Recive_Str.subSequence(0, 7).equals("E610200")) {
						//���豸ID��Ӧ��У�����
						controller.close();			//�ر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
						
						dialog = new AlertDialog.Builder(context)
							.setTitle("ֱ����������")			//���öԻ���ı���
							.setMessage("ͨ�Ź��� ��")
							//���öԻ���İ�ť
							.setPositiveButton("ȷ��", null)
							.create();
						dialog.show();
					}
					else if (Recive_Str.substring(0, 7).equals("C610100") || Recive_Str.subSequence(0, 7).equals("E610100")) {
						//�յ�����Ӧ��
						Recive_Str = Recive_Str.substring(7);
						if (Recive_Str.equals("00")) {
							//�����ɹ�
							SQLiteDatabase db=dbHelper.getWritableDatabase();
							DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "ͨ�����������ɹ�", Locker_ID);
							db.close();
							
							controller.close();			//�ر���������
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							
							dialog = new AlertDialog.Builder(context)
	    						.setTitle("��ʾ")			//���öԻ���ı���
	    						.setMessage("ֱ�������ɹ�")
	    						//���öԻ���İ�ť
	    						.setPositiveButton("ȷ��", null)
	    						.create();
							dialog.show();
						}
						else {
							//����ʧ��
							controller.close();			//�ر���������
							BLE_Get_Start_Tag = false;
							BLE_Lock_Contrl_Tag = false;
							
							dialog = new AlertDialog.Builder(context)
	        					.setTitle("��ʾ")			//���öԻ���ı���
	        					.setMessage("ֱ������ʧ��")
	        					//���öԻ���İ�ť
	        					.setPositiveButton("ȷ��", null)
	        					.create();
							dialog.show();
						}
						
					}
					else if (Recive_Str.subSequence(0, 7).equals("C610300")) {
						//TODO �յ���ѯ���豸״̬��Ӧ����Ҫ����״̬�ϴ�����
						
						Recive_Str = Recive_Str.substring(7);
						
					}
				}
				else {
					//����Ļظ�
					if (! Recive_Str.subSequence(0, 7).equals("C610200")) {
						controller.close();			//�ر���������
						BLE_Get_Start_Tag = false;
						BLE_Lock_Contrl_Tag = false;
					}
				}
			}
			else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_LOSS_DEVICE_CHARACT)){
				//δ�ܻ�ȡ������ͨ������ֵ
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
	
	
	private void Open_BLE_Lock_Start(int this_BLE_Numb) {
		//��������ָ���
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
	
	public void Open_BLE_Lock() {
		//��������
		BLE_Get_Start_Tag = false;
		BLE_Lock_Contrl_Tag = false;
		controller.close();		//�ȹر����з���
		
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
	
	Handler mHandler = new Handler(){
		@SuppressLint("HandlerLeak") public void handleMessage(Message msg){
			String s, temp_Str;
			
			switch(msg.what) {
			
			case 0:
				//Ӧ�����
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("6206")) {
					if (s.substring(5, 6).equals("N")) {
						Total_Locks = Integer.valueOf(s.substring(7));
					}
					else {
						//��GIS��ѯ��Ӧ��
						if (Numb_Lock < 100) {
							//ֻ����100����������Ķ���
							temp_Str = s.substring(5, 6);
							Lock_Error[Numb_Lock] = true;
							if (temp_Str.equals("W")) {
								//�и澯
								Lock_Error[Numb_Lock] = false;
							}
							
							Lock_Lng[Numb_Lock] = s.substring(7, 17);
							Lock_Lat[Numb_Lock] = s.substring(18, 28);
							Lock_ID[Numb_Lock] = s.substring(29, 63);	
							Lock_Name[Numb_Lock] = s.substring(64);	
							
							list_Name.add(Lock_Name[Numb_Lock]);
							
							Numb_Lock++;
													
							final ArrayAdapter<String> sAdapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list_Name);
							sAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //����ÿһ��item����ʽ
							Nearby_Locker_List.setAdapter(sAdapter1);
						}
					}
				}
				
				if (temp_Str.equals("6202")) {
					//�ǿ������Ƶ�Ӧ��
					temp_Str = s.substring(5);
					
					if (temp_Str.equals("00")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
	            			.setTitle("��ʾ")			//���öԻ���ı���
	            			.setMessage("�����ɹ� ��")	//��ʾ�������豸��
	            			//���öԻ���İ�ť
	            			.setPositiveButton("ȷ��", null)
	            			.create();
						dialog.show();
						
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "ͨ������NB�����ɹ�", Lock_Name[Selected_Lock_index]);
						db.close();
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
				
				if (temp_Str.equals("6203")) {
					//������������Ӧ��
					temp_Str = s.substring(5);
					
					if (temp_Str.equals("00")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
	            			.setTitle("��ʾ")	
	            			.setMessage("������Ϣ���ͳɹ� ��")
	            			//���öԻ���İ�ť
	            			.setPositiveButton("ȷ��", null)
	            			.create();
						dialog.show();
						
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, "����Զ�̿��������ɹ�", Lock_Name[Selected_Lock_index]);
						db.close();
					}
					else {
						AlertDialog dialog = new AlertDialog.Builder(context)
            				.setTitle("��ʾ")	
            				.setMessage("����ʧ�� ��")
            				//���öԻ���İ�ť
            				.setPositiveButton("ȷ��", null)
            				.create();
						dialog.show();
					}
					
				}

				break;
				
			case 2:
				s=msg.obj.toString();

				if (s.equals("Wait_Send_6206")) {
					Numb_Lock = 0;
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.GIS_Query_6206(Your_Lat_C, Your_Lng_C, 3 * Er_Lnglat);

				}
				else if (s.equals("Wait_Send_6202")) {

					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Open_NB_Lock_6202(UserLogin_Activity.Login_User_ID, Lock_ID[Selected_Lock_index]);  
			    	
				}
				else if (s.equals("Wait_Send_6203")) {

					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Call_Open_Lock_6203(UserLogin_Activity.Login_User_ID, Lock_ID[Selected_Lock_index], Lock_Lng[Selected_Lock_index], Lock_Lat[Selected_Lock_index]);  
			    	
				}
				

				break;
			
			}
			
		}

	};
	
}
