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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.example.iods_common.Baidu_Map;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.UserLogin_Activity;


public class Baidu_NewLock_Activity extends Activity {
	//ʩ����λ�õĵ�ͼ��ʾ

	String[] Lock_Name = new String[500];	//����
	String[] Lock_ID = new String[500];	//��ID
	boolean[] Lock_Error = new boolean[500];//���澯״̬
	int Numb_Lock = 0;			//������
	int Selected_Lock_index = 0;
	
	private Double Your_Lng_C = 0.0, Your_Lat_C = 0.0;
	Double Er_Lnglat = 5 * UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //��ͼ������Χ������������Χ��5��
	
	//static Socket_Recive Lock_GIS_Accept;
	//static Make_TCP_Connect Connect_TCP;
	
	DBHelper dbHelper ;//���ݿ�������
    
	private Context context = Baidu_NewLock_Activity.this;//����Context����
	
	private Baidu_NewLock_Activity mactivity;

    /**
     * MapView �ǵ�ͼ���ؼ�
     */
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Marker mMarker  = null;		//��ǰλ��marker��ǽڵ�
    private Marker[] Marker_Lock = new Marker[100];		//��λ�ñ�ǽڵ�
    private InfoWindow mInfoWindow = null;
    
    private LocationClient mLocationClient = null;
    
    private LocationManager locationManager = null;  
    private String provider;
    
    String workID;		//����ID
    String Show_Type;	//������ʽ: ShowΪ��ʾ��ʽ,������ʾ�豸λ�ú͵����� PositionΪ��λ��ʽ�����½����ƻ�ʩ���ǣ���λ�豸λ��
 
    static NMS_Communication Connect_NMS;
    
    BitmapDescriptor bd_Nomal = null;
	BitmapDescriptor bd_Make = null;
	BitmapDescriptor bd_This = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SDKInitializer.initialize(getApplicationContext());
        
        setContentView(R.layout.activity_overlay);
        
        Intent in=getIntent();
        workID=in.getStringExtra("workID");
        Show_Type=in.getStringExtra("Type");
        
        mactivity = this;
        
        dbHelper = new DBHelper(context);	//����DBHlper����ʵ��

        //��ȡ�ٶȶ�λ�ͻ���
        mLocationClient = new LocationClient(getApplicationContext()); 
 
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);

        
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        

        // ��ʾ����ǰλ�õ�Сͼ��
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient.start();
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
        provider = LocationManager.GPS_PROVIDER;
        //ʹ��getLastKnownLocation�Ϳ��Ի�ȡ����¼��ǰλ����Ϣ��Location������  
        //������showLocation()��ʾ��ǰ�豸��λ����Ϣ  
        //requestLocationUpdates��������λ�ü�����  
        //�˴���������ʱ����Ϊ1�룬��������51��  
        //Ҳ����˵ÿ��1�����ÿ�ƶ�1�ף�locationListener�л����һ��λ����Ϣ  
        Location location = locationManager.getLastKnownLocation(provider);  
        if (location != null) {  

        	showLocation(location);  
        }  
        String provider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(provider, 1000, 1,  locationListener);  

        //��ǰ����ͼ��
		if (Show_Type.equals("Position")) {
			//�½�ʩ�����ƻ�ʩ���ĵ�ǰλ��ͼ�꼴Ϊʩ����ͼ��
			bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_make);
		}
		else {
			//����ʩ���ĵ�ǰλ��ͼ��ΪԲ��ͼ��
			bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
		}
                
        //��ǰ���괴��
        LatLng ll = new LatLng(Your_Lat_C, Your_Lng_C);
        MarkerOptions oo_This = new MarkerOptions().position(ll).icon(bd_This).zIndex(20).draggable(true);
        
        Make_NewLock_Market();		//�����������õ��豸����
       
        oo_This.animateType(MarkerAnimateType.drop);			//���䶯��
        mMarker = (Marker) (mBaiduMap.addOverlay(oo_This));
        
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
        mBaiduMap.setMapStatus(u);
        
        //�ٶȱ�ǵ����¼�
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
            	int i;
                Button button = new Button(getApplicationContext());
                button.setBackgroundResource(R.drawable.popup_1);

                LatLng ll = marker.getPosition();
                
                i = 0;
        		if (Show_Type.equals("Position")) {
        			i = 1;
        		}
        		
                if (marker == mMarker && i == 1){

                	button.setText("����װλ�ö�λ");
                	
                	button.setOnClickListener(new OnClickListener() {
               	 		
                		public void onClick(View v) {         
                			
                			final EditText et = new EditText(context);

                	        // �������豸������ĶԻ������
                	        new AlertDialog.Builder(context)
                	        // ���ñ���
                	        	.setTitle("����װ�ڵ�ǰλ�� ��")			//���öԻ���ı���
								.setMessage("���������豸��")
                	                // ���������ı���
                	            .setView(et)
                	                // ���ȷ����ť
                	            .setNegativeButton("ȡ��", null)
                	            .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						             public void onClick(DialogInterface dialog, int which) {
						                			String in = et.getText().toString();
						                			Uri data = Uri.parse("Get Lock Baidu GPS");
						                			Intent result = new Intent("A", data);
						                			result.putExtra("Lng", Your_Lng_C);
						                			result.putExtra("Lat", Your_Lat_C);
						                			result.putExtra("Name", in);
						                			setResult(RESULT_OK, result);
						                			finish();
	
						             }
                	            })
                	                // ��������ʾ
                	            .create()
                	        .show();

                        }
                    }); 
                	mInfoWindow = new InfoWindow(button, ll, -47);
                	mBaiduMap.showInfoWindow(mInfoWindow);               	
                }else{

                	i = (int) marker.getExtraInfo().get("deviceSN");  
                	
                	button.setText(Lock_Name[i]);
                	
                	Selected_Lock_index = i;
	        			
 	    		   	final double Error_Lng_C = Your_Lng_C - marker.getPosition().longitude;
	    		   	final double Error_Lat_C = Your_Lat_C - marker.getPosition().latitude;
	    		   	final String str_Lock_ID = Lock_ID[i];

               	 	button.setOnClickListener(new OnClickListener() {
               	 		//TODO
                		public void onClick(View v) {         
                			String temp_Str = workID.substring(0, 6);
        					if (temp_Str.equals("MAINTE")) {
        						AlertDialog dialog = new AlertDialog.Builder(context)
        							.setTitle("������������β��� ��")			//���öԻ���ı���
        							.setMessage(Lock_Name[Selected_Lock_index])
        							//���öԻ���İ�ť
        							.setPositiveButton("ά��", new DialogInterface.OnClickListener() {
        								@Override
        								public void onClick(DialogInterface dialog, int i) {
				                			Uri data = Uri.parse("Get Lock Baidu GPS");
				                			Intent result = new Intent("A", data);
				                			result.putExtra("Lng", Your_Lat_C);
				                			result.putExtra("Lat", Your_Lat_C);
				                			result.putExtra("Name", str_Lock_ID);
				                			setResult(RESULT_OK, result);
				                			finish();
				                			dialog.dismiss();
        								}
        							})
        							.setNegativeButton("����", new DialogInterface.OnClickListener() {
        								@Override
        								public void onClick(DialogInterface dialog, int i) {
        									Toast.makeText(context, "��ȴ����������Ӱٶȵ�ͼ", Toast.LENGTH_SHORT).show();
               					 			//�ٶȵ���
               					 			new Baidu_Map().Market_BaiduMap(String.valueOf(marker.getPosition().longitude), String.valueOf(marker.getPosition().longitude), mactivity, "Baidu");		
        								}
        							})
        							.create();
        						dialog.show();
        					}
        					else {
        						AlertDialog dialog = new AlertDialog.Builder(context)
                					.setTitle("������������β��� ��")			//���öԻ���ı���
                					.setMessage(Lock_Name[Selected_Lock_index])
                					//���öԻ���İ�ť
                					.setPositiveButton("����", new DialogInterface.OnClickListener() {
                						@Override
                						public void onClick(DialogInterface dialog, int i) {
                							if (Error_Lng_C > 0.0002 || Error_Lat_C > 0.0002 || Error_Lng_C < -0.0002 || Error_Lat_C < -0.0002) {
                								AlertDialog dialog_1 = new AlertDialog.Builder(context)
         			   								.setTitle("����̫Զ")			//���öԻ���ı���
         			   								
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
                					})
                					.setNegativeButton("����", new DialogInterface.OnClickListener() {
               					 		@Override
               					 		public void onClick(DialogInterface dialog, int i) {
               					 			Toast.makeText(context, "��ȴ����������Ӱٶȵ�ͼ", Toast.LENGTH_SHORT).show();
               					 			//�ٶȵ���
               					 			new Baidu_Map().Market_BaiduMap(String.valueOf(marker.getPosition().longitude), String.valueOf(marker.getPosition().longitude), mactivity, "Baidu");		
                   						
               					 		}
                					})
                					.create();
        						dialog.show();
        					}
                        }
                    });  
                  
                    mInfoWindow = new InfoWindow(button, ll, -47);                    
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
                return true;
            }
            
        });

    }
    
       
    private void Make_NewLock_Market() {
		//����λ�ý��е�ͼ���
		String temp_Str, Lock_Status;
		double temp_Lng, temp_Lat;
		LatLng ll;
		MarkerOptions oo;
		bd_Nomal = BitmapDescriptorFactory.fromResource(R.drawable.icon_rewrite);
		bd_Make = BitmapDescriptorFactory.fromResource(R.drawable.icon_work);
		
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ?", new String[]{workID});    	

		Numb_Lock = 0;
	    while(cursor.moveToNext()){
	    	temp_Str = workID.substring(0, 6);
	    	
			Lock_Status = cursor.getString(10);
			
			if (Lock_Status.equals("0")) {
    			//δ�깤�豸ȡ������
				temp_Str = cursor.getString(3);
		    	temp_Lng = Double.valueOf(cursor.getString(4));
	    		temp_Lat = Double.valueOf(cursor.getString(5));
	    		
	    		ll = new LatLng(temp_Lat, temp_Lng);
				oo = new MarkerOptions().position(ll).icon(bd_Make).zIndex(9).draggable(true);
			}
			else {
				//���깤�豸ȡ������
	    		temp_Str = cursor.getString(7);
	    		Lock_Status = cursor.getString(8);
	    		if (temp_Str == null || Lock_Status == null) {
	    			temp_Lng = Double.valueOf(cursor.getString(4));
		    		temp_Lat = Double.valueOf(cursor.getString(5));
	    		}
	    		else {
	    			temp_Lng = Double.valueOf(cursor.getString(7));
		    		temp_Lat = Double.valueOf(cursor.getString(8));
	    		}
	    		
	    		temp_Str = cursor.getString(6);
				if (temp_Str == null) {
					temp_Str = cursor.getString(3);
				}
	    		ll = new LatLng(temp_Lat, temp_Lng);
				oo = new MarkerOptions().position(ll).icon(bd_Nomal).zIndex(9).draggable(true);
			}
    		    	
    		if (temp_Str == null) {
    			temp_Str = "δ����";
    		}
    		else if (temp_Str.length() == 0) {
    			temp_Str = "δ����";
    		}
    		Lock_Name[Numb_Lock] = temp_Str;
    		Lock_ID[Numb_Lock] = cursor.getString(2);
					           
			Marker_Lock[Numb_Lock] = (Marker) mBaiduMap.addOverlay(oo);
			
			//��ע�����ö������Ϣ 
			Bundle bundle = new Bundle();  
			bundle.putInt("deviceSN", Numb_Lock);  
			Marker_Lock[Numb_Lock].setExtraInfo(bundle);  

			Numb_Lock++;
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
		Double this_Lng, this_Lat;
		
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
		if (mMarker != null) {
			mMarker.setPosition(ll);
		}

		this_Lat = ll.latitude;
    	this_Lng = ll.longitude;
    	Your_Lng_C = this_Lng;
    	Your_Lat_C = this_Lat;
						
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
        
    	/*
        bd_Make.recycle();
        bd_Nomal.recycle();
        bd_This.recycle();
        */

    }
    
    Handler mHandler = new Handler(){
		@SuppressLint
		("HandlerLeak") public void handleMessage(Message msg){
			String s;
			
			switch(msg.what) {
			
			case 0:
				//Ӧ�����
				s=msg.obj.toString();

				
				

				break;
				
			case 2:
				s=msg.obj.toString();

				
				

				break;
			
			}
			
		}

	};

}
