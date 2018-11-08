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
	//施工锁位置的地图显示

	String[] Lock_Name = new String[500];	//锁名
	String[] Lock_ID = new String[500];	//锁ID
	boolean[] Lock_Error = new boolean[500];//锁告警状态
	int Numb_Lock = 0;			//锁数量
	int Selected_Lock_index = 0;
	
	private Double Your_Lng_C = 0.0, Your_Lat_C = 0.0;
	Double Er_Lnglat = 5 * UserLogin_Activity.Lock_Search_Scope * UserLogin_Activity.error_Limit;  //地图搜锁范围较文字搜锁范围大5倍
	
	//static Socket_Recive Lock_GIS_Accept;
	//static Make_TCP_Connect Connect_TCP;
	
	DBHelper dbHelper ;//数据库服务对象
    
	private Context context = Baidu_NewLock_Activity.this;//定义Context对象
	
	private Baidu_NewLock_Activity mactivity;

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Marker mMarker  = null;		//当前位置marker标记节点
    private Marker[] Marker_Lock = new Marker[100];		//锁位置标记节点
    private InfoWindow mInfoWindow = null;
    
    private LocationClient mLocationClient = null;
    
    private LocationManager locationManager = null;  
    private String provider;
    
    String workID;		//工单ID
    String Show_Type;	//工作方式: Show为显示方式,用于显示设备位置和导航； Position为定位方式，在新建和移机施工是，定位设备位置
 
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
        
        dbHelper = new DBHelper(context);	//创建DBHlper对象实例

        //获取百度定位客户端
        mLocationClient = new LocationClient(getApplicationContext()); 
 
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);

        
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        

        // 显示出当前位置的小图标
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient.start();
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
        provider = LocationManager.GPS_PROVIDER;
        //使用getLastKnownLocation就可以获取到记录当前位置信息的Location对象了  
        //并且用showLocation()显示当前设备的位置信息  
        //requestLocationUpdates用于设置位置监听器  
        //此处监听器的时间间隔为1秒，距离间隔是51米  
        //也就是说每隔1秒或者每移动1米，locationListener中会更新一下位置信息  
        Location location = locationManager.getLastKnownLocation(provider);  
        if (location != null) {  

        	showLocation(location);  
        }  
        String provider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(provider, 1000, 1,  locationListener);  

        //当前坐标图标
		if (Show_Type.equals("Position")) {
			//新建施工和移机施工的当前位置图标即为施工点图标
			bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_make);
		}
		else {
			//其它施工的当前位置图标为圆点图标
			bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
		}
                
        //当前坐标创建
        LatLng ll = new LatLng(Your_Lat_C, Your_Lng_C);
        MarkerOptions oo_This = new MarkerOptions().position(ll).icon(bd_This).zIndex(20).draggable(true);
        
        Make_NewLock_Market();		//标记已完成配置的设备点标记
       
        oo_This.animateType(MarkerAnimateType.drop);			//下落动作
        mMarker = (Marker) (mBaiduMap.addOverlay(oo_This));
        
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
        mBaiduMap.setMapStatus(u);
        
        //百度标记点点击事件
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

                	button.setText("锁安装位置定位");
                	
                	button.setOnClickListener(new OnClickListener() {
               	 		
                		public void onClick(View v) {         
                			
                			final EditText et = new EditText(context);

                	        // 创建有设备名输入的对话框对象
                	        new AlertDialog.Builder(context)
                	        // 设置标题
                	        	.setTitle("锁安装在当前位置 ？")			//设置对话框的标题
								.setMessage("请输入锁设备名")
                	                // 添加输入的文本框
                	            .setView(et)
                	                // 添加确定按钮
                	            .setNegativeButton("取消", null)
                	            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
                	                // 产生并显示
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
        							.setTitle("对下述锁具如何操作 ？")			//设置对话框的标题
        							.setMessage(Lock_Name[Selected_Lock_index])
        							//设置对话框的按钮
        							.setPositiveButton("维护", new DialogInterface.OnClickListener() {
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
        							.setNegativeButton("导航", new DialogInterface.OnClickListener() {
        								@Override
        								public void onClick(DialogInterface dialog, int i) {
        									Toast.makeText(context, "请等待，正在连接百度地图", Toast.LENGTH_SHORT).show();
               					 			//百度导航
               					 			new Baidu_Map().Market_BaiduMap(String.valueOf(marker.getPosition().longitude), String.valueOf(marker.getPosition().longitude), mactivity, "Baidu");		
        								}
        							})
        							.create();
        						dialog.show();
        					}
        					else {
        						AlertDialog dialog = new AlertDialog.Builder(context)
                					.setTitle("对下述锁具如何操作 ？")			//设置对话框的标题
                					.setMessage(Lock_Name[Selected_Lock_index])
                					//设置对话框的按钮
                					.setPositiveButton("开锁", new DialogInterface.OnClickListener() {
                						@Override
                						public void onClick(DialogInterface dialog, int i) {
                							if (Error_Lng_C > 0.0002 || Error_Lat_C > 0.0002 || Error_Lng_C < -0.0002 || Error_Lat_C < -0.0002) {
                								AlertDialog dialog_1 = new AlertDialog.Builder(context)
         			   								.setTitle("距离太远")			//设置对话框的标题
         			   								
         			   								.setMessage("您恐怕无法激活锁具，完成开锁")
         			   								//设置对话框的按钮
         			   								.setPositiveButton("确定", null)
         			   								.create();
                								dialog_1.show();
                							}
                							else {
                								//间距符合要求
                								Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6202");
                								Connect_NMS.Make_Socket_Connect();

                							}
                						}
                					})
                					.setNegativeButton("导航", new DialogInterface.OnClickListener() {
               					 		@Override
               					 		public void onClick(DialogInterface dialog, int i) {
               					 			Toast.makeText(context, "请等待，正在连接百度地图", Toast.LENGTH_SHORT).show();
               					 			//百度导航
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
		//对锁位置进行地图标记
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
    			//未完工设备取老数据
				temp_Str = cursor.getString(3);
		    	temp_Lng = Double.valueOf(cursor.getString(4));
	    		temp_Lat = Double.valueOf(cursor.getString(5));
	    		
	    		ll = new LatLng(temp_Lat, temp_Lng);
				oo = new MarkerOptions().position(ll).icon(bd_Make).zIndex(9).draggable(true);
			}
			else {
				//已完工设备取新数据
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
    			temp_Str = "未命名";
    		}
    		else if (temp_Str.length() == 0) {
    			temp_Str = "未命名";
    		}
    		Lock_Name[Numb_Lock] = temp_Str;
    		Lock_ID[Numb_Lock] = cursor.getString(2);
					           
			Marker_Lock[Numb_Lock] = (Marker) mBaiduMap.addOverlay(oo);
			
			//标注点设置额外的信息 
			Bundle bundle = new Bundle();  
			bundle.putInt("deviceSN", Numb_Lock);  
			Marker_Lock[Numb_Lock].setExtraInfo(bundle);  

			Numb_Lock++;
    	}

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
			showLocation(location);  
		}  
	};  

  
    //显示经纬度信息  
	private void showLocation(final Location location) {  
		Double this_Lng, this_Lat;
		
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
		if (mMarker != null) {
			mMarker.setPosition(ll);
		}

		this_Lat = ll.latitude;
    	this_Lng = ll.longitude;
    	Your_Lng_C = this_Lng;
    	Your_Lat_C = this_Lat;
						
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
				//应答分析
				s=msg.obj.toString();

				
				

				break;
				
			case 2:
				s=msg.obj.toString();

				
				

				break;
			
			}
			
		}

	};

}
