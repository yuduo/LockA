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
 * 百度地图，节点显示图
 * 注意，如果GPS还没获取到经纬度，跳转的时候会报错
 */
public class BaidumapActivity extends Activity {
	//以地图方式显示周边锁设备
	
	int index_Int = 0;

	String[] Lock_Lng = new String[500];	//锁经度
	String[] Lock_Lat = new String[500];	//锁纬度
	String[] Lock_ID = new String[500];		//锁ID
	String[] Lock_Name = new String[500];	//锁名
	boolean[] Lock_Error = new boolean[500];//锁告警状态
	int Numb_Lock = 0, Total_Locks;			//锁数量
	int Selected_Lock_index = 0;
	
	private Double Last_Lng = 0.0, Last_Lat = 0.0;
	private Double Your_Lng_C = 0.0, Your_Lat_C = 0.0;
	Double Er_Lnglat = 5 * UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //地图搜锁范围较文字搜锁范围大5倍
	
	//static Socket_Recive Lock_GIS_Accept;
	//static Make_TCP_Connect Connect_TCP;
	static NMS_Communication Connect_NMS = null;
	
	String Map_Mode;	//Near为周边模式，用于施工人员开锁； Remote为远程模式，用于管理员的远程开锁
	
	DBHelper dbHelper ;//数据库服务对象
    
	private Context context = BaidumapActivity.this;//定义Context对象

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Marker mMarker = null;		//当前位置marker标记节点
    private Marker[] Marker_Lock = new Marker[100];		//锁位置标记节点
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
        Map_Mode=in.getStringExtra("Mode");		//Remote为远程协助开锁， Near为自行控制NB开锁
        Your_Lng_C = Double.valueOf(in.getStringExtra("GPS_Lng"));
        Your_Lat_C = Double.valueOf(in.getStringExtra("GPS_Lat"));
        
        dbHelper = new DBHelper(context);	//创建DBHlper对象实例

        //获取百度定位客户端
        mLocationClient = new LocationClient(getApplicationContext());  
 
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);

        
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        
        if (Map_Mode.equals("Near")) {
        	// 显示出当前位置的小图标
            mBaiduMap.setMyLocationEnabled(true);

            mLocationClient.start();//当前坐标创建
            bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo); 
            LatLng ll = new LatLng(Your_Lat_C, Your_Lng_C);
            MarkerOptions oo_This = new MarkerOptions().position(ll).icon(bd_This).zIndex(20).draggable(true);
           
            oo_This.animateType(MarkerAnimateType.drop);			//下落动作
            mMarker = (Marker) (mBaiduMap.addOverlay(oo_This));
            
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
            mBaiduMap.setMapStatus(u);
                        
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
            provider = LocationManager.GPS_PROVIDER;
            //使用getLastKnownLocation就可以获取到记录当前位置信息的Location对象了  
            //并且用showLocation()显示当前设备的位置信息  
            //requestLocationUpdates用于设置位置监听器  
            //此处监听器的时间间隔为1秒，距离间隔是1米  
            //也就是说每隔1秒或者每移动1米，locationListener中会更新一下位置信息  
            Location location = locationManager.getLastKnownLocation(provider);  
            if (location != null) {  

            	showLocation(location);  
            }  
            String provider = LocationManager.GPS_PROVIDER;
            locationManager.requestLocationUpdates(provider, 1000, 1,  locationListener);  
                       
            
        }
        else {
        	//百度地图显示中心移动事件setCenter
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
					//状态变动完成，则调用地图中心点附近的锁具数据
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
        
        
        //百度标记点点击事件
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
            	int i;
                Button button = new Button(getApplicationContext());
                
                OnInfoWindowClickListener listener = null;
                LatLng ll = marker.getPosition();
                if (marker == mMarker){
                	button.setBackgroundResource(R.drawable.popup_2);
                	button.setText("你的位置");
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
                				.setTitle("打开如下所选锁具 ？")			//设置对话框的标题
                				.setMessage(Lock_Name[Selected_Lock_index])
                				//设置对话框的按钮
                				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                					 @Override
                					 public void onClick(DialogInterface dialog, int i) {
                						 
                						 SQLiteDatabase db=dbHelper.getWritableDatabase();
                						 int temp_Int = 0;
                						 
                			    		   String temp_User = UserLogin_Activity.Login_User_ID;
                			    		   		    		
                			    		   Cursor cursor=db.rawQuery("SELECT * FROM User_Locker_Table WHERE UserID = ? AND Locker_ID = ?",new String[]{temp_User, Lock_ID[Selected_Lock_index]});
                			    		   if(cursor.moveToNext() || UserLogin_Activity.Login_User_Type.equals("Admin")){
                			    			   //用户有权或超级用户，则检查距离
                			    			   if (Error_Lng_C > 0.0002 || Error_Lat_C > 0.0002 || Error_Lng_C < -0.0002 || Error_Lat_C < -0.0002) {
                			    				   AlertDialog dialog_1 = new AlertDialog.Builder(context)
               			   								.setTitle("C : " + String.valueOf(Error_Lng_C) + "; " + String.valueOf(Error_Lat_C))			//设置对话框的标题
               			   								
               			   								.setMessage("您恐怕无法激活锁具，完成开锁")
               			   								//设置对话框的按钮
               			   								.setPositiveButton("确定", null)
               			   								.create();
                			    				   dialog_1.show();
                			    			   }
                			    			   else {
                			    				   //间距符合要求
                			    				   temp_Int = 1;
                			    			   }
                			    			   
                			    			   if (temp_Int == 1 || Map_Mode.equals("Remote")) {
                			    				   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
                			    				   Connect_NMS.Make_Socket_Connect();
                			    			   }
                			    		   }
                			    		   else {
                				    			
                			    			   AlertDialog dialog_1 = new AlertDialog.Builder(context)
                			   						.setTitle("提示")			//设置对话框的标题
                			   						.setMessage("您没有打开此锁的权限")
                			   						//设置对话框的按钮
                			   						.setPositiveButton("确定", null)
                			   						.create();
                			    			   dialog_1.show();
                				    			
                			    		   }
                			    		   cursor.close();
                			    		   db.close();
                					 }
                					 })
		                		.setNegativeButton("取消", null)
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
    
       
    //locationListener中其他3个方法新手不太用得到，笔者在此也不多说了，有兴趣的可以自己去了解一下  
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
			// 更新当前设备的位置信息  

			if (Map_Mode.equals("Near")) {
				showLocation(location);  
			}
			
		}  
	};  

  
    //显示经纬度信息  
	private void showLocation(final Location location) {  
		Double this_Lng, this_Lat;
		int i;
		
		LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
		
		this_Lat = ll.latitude;
    	this_Lng = ll.longitude;

		com.baidu.mapapi.utils.CoordinateConverter converter  = new com.baidu.mapapi.utils.CoordinateConverter();    
    	converter.from(CoordType.GPS);    
    	// sourceLatLng待转换坐标    	
    	converter.coord(ll);    
    	ll= converter.convert(); 
    	
    	MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
    	mBaiduMap.animateMapStatus(u);

    	//总是移动到地图中心
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
			//大幅移动，则刷新锁定位信息

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
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     * 暂时用toast来显示
     */
    
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();             
            
        }
    }
    
    @Override
    protected void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()

        super.onPause();
    }

    @Override
    protected void onResume() {
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()

        super.onResume();
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
        dbHelper.close();
                
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
    	// 退出时销毁定位  
    	mLocationClient.stop();  
    	// 关闭定位图层  
    	mBaiduMap.setMyLocationEnabled(false);  
    	mMapView.onDestroy();  
    	mMapView = null;  
    	        
    	// 回收 bitmap 资源
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
				//应答分析
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("6206")) {
					//是GIS查询的应答
					if (s.substring(5, 6).equals("N")) {
						Total_Locks = Integer.valueOf(s.substring(7));
					}
					else {
						if (Numb_Lock < 500) {
							//只处理500把锁，多余的丢弃
							BitmapDescriptor bd_Error = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
							BitmapDescriptor bd_Nomal = BitmapDescriptorFactory.fromResource(R.drawable.icon_rewrite);
							
							temp_Str = s.substring(5, 6);
							
							Lock_Lng[Numb_Lock] = s.substring(7, 17);
							Lock_Lat[Numb_Lock] = s.substring(18, 28);
							Lock_ID[Numb_Lock] = s.substring(29, 63);	
							Lock_Name[Numb_Lock] = s.substring(64);	
							
							ll = new LatLng(Double.parseDouble(Lock_Lat[Numb_Lock]), Double.parseDouble(Lock_Lng[Numb_Lock]));
							if (temp_Str.equals("W")) {
								//有告警
								oo = new MarkerOptions().position(ll).icon(bd_Error).zIndex(9).draggable(true);
			        		}
			        		else {
			        			oo = new MarkerOptions().position(ll).icon(bd_Nomal).zIndex(9).draggable(true);
			        		}
									           
							//oo.animateType(MarkerAnimateType.drop);//下落动作
							Marker_Lock[Numb_Lock] = (Marker) mBaiduMap.addOverlay(oo);
							
							//标注点设置额外的信息 
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
					//是开锁控制的应答
					temp_Str = s.substring(6);
					
					if (temp_Str.equals("00")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
	            			.setTitle("提示")			//设置对话框的标题
	            			.setMessage("开锁成功 ！")	//显示锁蓝牙设备名
	            			//设置对话框的按钮
	            			.setPositiveButton("确定", null)
	            			.create();
						dialog.show();
						
				        if (Map_Mode.equals("Near")) {
				        	temp_Str = "通过蓝牙NB开锁成功";
				        }
				        else {
				        	temp_Str = "远程协助开锁成功";
				        }
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						DBHelper.Save_Log_inDB(db, UserLogin_Activity.Login_User_ID, temp_Str, Lock_Name[Selected_Lock_index]);
						db.close();
					}
					else if (temp_Str.equals("01")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
            				.setTitle("提示")			//设置对话框的标题
            				.setMessage("正在开锁")	//显示锁蓝牙设备名
            				//设置对话框的按钮
            				.setPositiveButton("确定", null)
            				.create();
						dialog.show();
					}
					else if (temp_Str.equals("06")) {
						AlertDialog dialog = new AlertDialog.Builder(context)
        					.setTitle("提示")			//设置对话框的标题
        					.setMessage("通信故障")	//显示锁蓝牙设备名
        					//设置对话框的按钮
        					.setPositiveButton("确定", null)
        					.create();
						dialog.show();
					}
					else {
						AlertDialog dialog = new AlertDialog.Builder(context)
        					.setTitle("提示")			//设置对话框的标题
        					.setMessage("开锁失败 ！")	//显示锁蓝牙设备名
        					//设置对话框的按钮
        					.setPositiveButton("确定", null)
        					.create();
						dialog.show();
					}
					
				}
				
				if (temp_Str.equals("6203")) {
					//是求助开锁的应答
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
