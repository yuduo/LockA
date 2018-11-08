package com.example.iods_manage;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;




/**
 * �ٶȵ�ͼ���ڵ���ʾͼ
 * ע�⣬���GPS��û��ȡ����γ�ȣ���ת��ʱ��ᱨ��
 */
public class BaidumapActivity extends Activity {
	//�Ե�ͼ��ʽ��ʾ�ܱ����豸
	
	int index_Int = 0;

	String[] Lock_Lng = new String[500];	//������
	String[] Lock_Lat = new String[500];	//��γ��
	String[] Lock_ID = new String[500];		//��ID
	String[] Lock_Name = new String[500];	//����
	boolean[] Lock_Error = new boolean[500];//���澯״̬
	int Numb_Lock = 0, Total_Locks;			//������
	int Selected_Lock_index = 0;
	
	private Double Last_Lng = 0.0, Last_Lat = 0.0;
	private Double Your_Lng_C = 0.0, Your_Lat_C = 0.0;
	Double Er_Lnglat = 5 * UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //��ͼ������Χ������������Χ��5��
	
	//static Socket_Recive Lock_GIS_Accept;
	//static Make_TCP_Connect Connect_TCP;
	static NMS_Communication Connect_NMS = null;
	
	String Map_Mode;	//NearΪ�ܱ�ģʽ������ʩ����Ա������ RemoteΪԶ��ģʽ�����ڹ���Ա��Զ�̿���
	
	DBHelper dbHelper ;//���ݿ�������
    
	private Context context = BaidumapActivity.this;//����Context����

    /**
     * MapView �ǵ�ͼ���ؼ�
     */
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Marker mMarker = null;		//��ǰλ��marker��ǽڵ�
    private Marker[] Marker_Lock = new Marker[100];		//��λ�ñ�ǽڵ�
    private InfoWindow mInfoWindow = null;
    
    private LocationClient mLocationClient = null;
    private LocationManager locationManager = null;  
    private String provider;
 
    BitmapDescriptor bd_This;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SDKInitializer.initialize(getApplicationContext());
        
        setContentView(R.layout.activity_overlay);
        
        Intent in=getIntent();
        Map_Mode=in.getStringExtra("Mode");		//RemoteΪԶ��Э�������� NearΪ���п���NB����
        Your_Lng_C = Double.valueOf(in.getStringExtra("GPS_Lng"));
        Your_Lat_C = Double.valueOf(in.getStringExtra("GPS_Lat"));
        
        dbHelper = new DBHelper(context);	//����DBHlper����ʵ��

        //��ȡ�ٶȶ�λ�ͻ���
        mLocationClient = new LocationClient(getApplicationContext());  
 
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);

        
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        
        if (Map_Mode.equals("Near")) {
        	// ��ʾ����ǰλ�õ�Сͼ��
            mBaiduMap.setMyLocationEnabled(true);

            mLocationClient.start();//��ǰ���괴��
            bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo); 
            LatLng ll = new LatLng(Your_Lat_C, Your_Lng_C);
            MarkerOptions oo_This = new MarkerOptions().position(ll).icon(bd_This).zIndex(20).draggable(true);
           
            oo_This.animateType(MarkerAnimateType.drop);			//���䶯��
            mMarker = (Marker) (mBaiduMap.addOverlay(oo_This));
            
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
            mBaiduMap.setMapStatus(u);
                        
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
            provider = LocationManager.GPS_PROVIDER;
            //ʹ��getLastKnownLocation�Ϳ��Ի�ȡ����¼��ǰλ����Ϣ��Location������  
            //������showLocation()��ʾ��ǰ�豸��λ����Ϣ  
            //requestLocationUpdates��������λ�ü�����  
            //�˴���������ʱ����Ϊ1�룬��������1��  
            //Ҳ����˵ÿ��1�����ÿ�ƶ�1�ף�locationListener�л����һ��λ����Ϣ  
            Location location = locationManager.getLastKnownLocation(provider);  
            if (location != null) {  

            	showLocation(location);  
            }  
            String provider = LocationManager.GPS_PROVIDER;
            locationManager.requestLocationUpdates(provider, 1000, 1,  locationListener);  
                       
            
        }
        else {
        	//�ٶȵ�ͼ��ʾ�����ƶ��¼�setCenter
        	LatLng ll = new LatLng(Your_Lat_C, Your_Lng_C);
        	MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
            mBaiduMap.setMapStatus(u);
            
            Numb_Lock = 0;
			 
			Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6206");
			Connect_NMS.Make_Socket_Connect();
        	
        	mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
				
				@Override
				public void onMapStatusChangeStart(MapStatus arg0, int arg1) {
					
				}
				
				@Override
				public void onMapStatusChangeStart(MapStatus arg0) {
					
				}
				
				@Override
				public void onMapStatusChangeFinish(MapStatus status) {
					//״̬�䶯��ɣ�����õ�ͼ���ĵ㸽������������
					 LatLng GPS_latLng = status.target;
					 int i;

					 Your_Lat_C = GPS_latLng.latitude;
					 Your_Lng_C = GPS_latLng.longitude;
					 
					 for (i = 0; i<Numb_Lock; i++) {
						 Marker_Lock[i].remove();
					 }
					 Numb_Lock = 0;
					 
					 Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6206");
					 Connect_NMS.Make_Socket_Connect();
				}
				
				@Override
				public void onMapStatusChange(MapStatus arg0) {
					// TODO Auto-generated method stub
					
				}
			});
        }
        
        
        //�ٶȱ�ǵ����¼�
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
            	int i;
                Button button = new Button(getApplicationContext());
                
                OnInfoWindowClickListener listener = null;
                LatLng ll = marker.getPosition();
                if (marker == mMarker){
                	button.setBackgroundResource(R.drawable.popup_2);
                	button.setText("���λ��");
                	mInfoWindow = new InfoWindow(button, ll, -47);
                	mBaiduMap.showInfoWindow(mInfoWindow);               	
                }
                else{
                	//
                	button.setBackgroundResource(R.drawable.popup_1);
                	i = (int) marker.getExtraInfo().get("deviceSN");  
                	
                	button.setText(Lock_Name[i]);

        			Selected_Lock_index = i;
 	    		   	        			
 	    		   	final double Error_Lng_C = Your_Lng_C - Double.valueOf(Lock_Lng[Selected_Lock_index]);
	    		   	final double Error_Lat_C = Your_Lat_C - Double.valueOf(Lock_Lat[Selected_Lock_index]);

               	 	button.setOnClickListener(new OnClickListener() {
               	 		
                		public void onClick(View v) {         
                			
                			AlertDialog dialog = new AlertDialog.Builder(context)
                				.setTitle("��������ѡ���� ��")			//���öԻ���ı���
                				.setMessage(Lock_Name[Selected_Lock_index])
                				//���öԻ���İ�ť
                				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                					 @Override
                					 public void onClick(DialogInterface dialog, int i) {
                						 
                						 SQLiteDatabase db=dbHelper.getWritableDatabase();
                						 int temp_Int = 0;
                						 
                			    		   String temp_User = UserLogin_Activity.Login_User_ID;
                			    		   		    		
                			    		   Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, Lock_ID[Selected_Lock_index]});
                			    		   if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin")){
                			    			   //�û���Ȩ�򳬼��û����������
                			    			   if (Error_Lng_C > 0.0002 || Error_Lat_C > 0.0002 || Error_Lng_C < -0.0002 || Error_Lat_C < -0.0002) {
                			    				   AlertDialog dialog_1 = new AlertDialog.Builder(context)
               			   								.setTitle("C : " + String.valueOf(Error_Lng_C) + "; " + String.valueOf(Error_Lat_C))			//���öԻ���ı���
               			   								
               			   								.setMessage("�������޷��������ߣ���ɿ���")
               			   								//���öԻ���İ�ť
               			   								.setPositiveButton("ȷ��", null)
               			   								.create();
                			    				   dialog_1.show();
                			    			   }
                			    			   else {
                			    				   //������Ҫ��
                			    				   temp_Int = 1;
                			    			   }
                			    			   
                			    			   if (temp_Int == 1 || Map_Mode.equals("Remote")) {
                			    				   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
                			    				   Connect_NMS.Make_Socket_Connect();
                			    			   }
                			    		   }
                			    		   else {
                				    			
                			    			   AlertDialog dialog_1 = new AlertDialog.Builder(context)
                			   						.setTitle("��ʾ")			//���öԻ���ı���
                			   						.setMessage("��û�д򿪴�����Ȩ��")
                			   						//���öԻ���İ�ť
                			   						.setPositiveButton("ȷ��", null)
                			   						.create();
                			    			   dialog_1.show();
                				    			
                			    		   }
                			    		   cursor.close();
                			    		   db.close();
                					 }
                					 })
		                		.setNegativeButton("ȡ��", null)
		                		.create();
                			dialog.show();

                        }
                    });                      
                    mInfoWindow = new InfoWindow(button, ll, -47);                    
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
                return true;
            }
            
        });

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

			if (Map_Mode.equals("Near")) {
				showLocation(location);  
			}
			
		}  
	};  

  
    //��ʾ��γ����Ϣ  
	private void showLocation(final Location location) {  
		Double this_Lng, this_Lat;
		int i;
		
		LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
		
		this_Lat = ll.latitude;
    	this_Lng = ll.longitude;

		com.baidu.mapapi.utils.CoordinateConverter converter  = new com.baidu.mapapi.utils.CoordinateConverter();    
    	converter.from(CoordType.GPS);    
    	// sourceLatLng��ת������    	
    	converter.coord(ll);    
    	ll= converter.convert(); 
    	
    	MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
    	mBaiduMap.animateMapStatus(u);

    	//�����ƶ�����ͼ����
		if (Map_Mode.equals("Near")) {
			if (mMarker != null) {
				mMarker.setPosition(ll);
			}
		}
	
		this_Lat = ll.latitude;
    	this_Lng = ll.longitude;
		   
    	Your_Lng_C = this_Lng;
    	Your_Lat_C = this_Lat;
		
		if (this_Lng - Last_Lng > Er_Lnglat || Last_Lng - this_Lng > Er_Lnglat || 
				this_Lat - Last_Lat > Er_Lnglat || Last_Lat - this_Lat > Er_Lnglat) {
			//����ƶ�����ˢ������λ��Ϣ

	    	Last_Lat = this_Lat;
			Last_Lng = this_Lng;
			
			for (i = 0; i<Numb_Lock; i++) {
				Marker_Lock[i].remove();
			}
			
			Numb_Lock = 0;

			Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6206");
			Connect_NMS.Make_Socket_Connect();

					
		}
		
	}


    /**
     * ����㲥�����࣬���� SDK key ��֤�Լ������쳣�㲥
     * ��ʱ��toast����ʾ
     */
    
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();             
            
        }
    }
    
    @Override
    protected void onPause() {
        // MapView������������Activityͬ������activity����ʱ�����MapView.onPause()

        super.onPause();
    }

    @Override
    protected void onResume() {
        // MapView������������Activityͬ������activity�ָ�ʱ�����MapView.onResume()

        super.onResume();
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
        dbHelper.close();
                
        // MapView������������Activityͬ������activity����ʱ�����MapView.destroy()
    	// �˳�ʱ���ٶ�λ  
    	mLocationClient.stop();  
    	// �رն�λͼ��  
    	mBaiduMap.setMyLocationEnabled(false);  
    	mMapView.onDestroy();  
    	mMapView = null;  
    	        
    	// ���� bitmap ��Դ
        bd_This.recycle();
        
    }
    
    Handler mHandler = new Handler(){
		@SuppressLint
		("HandlerLeak") public void handleMessage(Message msg){
			String s, temp_Str;
			LatLng ll;
			MarkerOptions oo;
			
			switch(msg.what) {
			
			case 0:
				//Ӧ�����
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("6206")) {
					//��GIS��ѯ��Ӧ��
					if (s.substring(5, 6).equals("N")) {
						Total_Locks = Integer.valueOf(s.substring(7));
					}
					else {
						if (Numb_Lock < 500) {
							//ֻ����500����������Ķ���
							BitmapDescriptor bd_Error = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
							BitmapDescriptor bd_Nomal = BitmapDescriptorFactory.fromResource(R.drawable.icon_rewrite);
							
							temp_Str = s.substring(5, 6);
							
							Lock_Lng[Numb_Lock] = s.substring(7, 17);
							Lock_Lat[Numb_Lock] = s.substring(18, 28);
							Lock_ID[Numb_Lock] = s.substring(29, 63);	
							Lock_Name[Numb_Lock] = s.substring(64);	
							
							ll = new LatLng(Double.parseDouble(Lock_Lat[Numb_Lock]), Double.parseDouble(Lock_Lng[Numb_Lock]));
							if (temp_Str.equals("W")) {
								//�и澯
								oo = new MarkerOptions().position(ll).icon(bd_Error).zIndex(9).draggable(true);
			        		}
			        		else {
			        			oo = new MarkerOptions().position(ll).icon(bd_Nomal).zIndex(9).draggable(true);
			        		}
									           
							//oo.animateType(MarkerAnimateType.drop);//���䶯��
							Marker_Lock[Numb_Lock] = (Marker) mBaiduMap.addOverlay(oo);
							
							//��ע�����ö������Ϣ 
							Bundle bundle = new Bundle();  
							bundle.putInt("deviceSN", Numb_Lock);  
							Marker_Lock[Numb_Lock].setExtraInfo(bundle);  

							Numb_Lock++;
							
							bd_Error.recycle();
							bd_Nomal.recycle();
						}
					}
				}
				
				if (temp_Str.equals("6202")) {
					//�ǿ������Ƶ�Ӧ��
					temp_Str = s.substring(6);
					
					if (temp_Str.equals("00")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
	            			.setTitle("��ʾ")			//���öԻ���ı���
	            			.setMessage("�����ɹ� ��")	//��ʾ�������豸��
	            			//���öԻ���İ�ť
	            			.setPositiveButton("ȷ��", null)
	            			.create();
						dialog.show();
						
				        if (Map_Mode.equals("Near")) {
				        	temp_Str = "ͨ������NB�����ɹ�";
				        }
				        else {
				        	temp_Str = "Զ��Э�������ɹ�";
				        }
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, temp_Str, Lock_Name[Selected_Lock_index]);
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
					temp_Str = s.substring(5, 6);
					
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
				

				break;
			
			}
			
		}

	};

    


}
