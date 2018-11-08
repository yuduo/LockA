package com.example.iods_manage;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;

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
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.model.LatLng;
import com.example.iods_common.DBHelper;
import com.example.iods_lock_app.R;


public class Map_Checkup_Activity extends Activity {
	//工单审查的地图显示页面
	
	String[] Lock_Lng = new String[500];	//锁经度
	String[] Lock_Lat = new String[500];	//锁纬度
	String[] Lock_Name = new String[500];	//锁名
	int Numb_Lock = 0, Total_Locks;			//锁数量
	int Selected_Lock_index = 0;
	
	//private BitmapDescriptor bd_Nomal = BitmapDescriptorFactory.fromResource(R.drawable.icon_rewrite);
	//private BitmapDescriptor bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
    
    DBHelper dbHelper ;//数据库服务对象
    
	private Context context = Map_Checkup_Activity.this;//定义Context对象

	private Double Your_Lng_C = 0.0, Your_Lat_C = 0.0;
	
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
    
    String workID;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SDKInitializer.initialize(getApplicationContext());
        
        setContentView(R.layout.activity_overlay);
        
        Intent in=getIntent();
        workID=in.getStringExtra("workID");
        
        dbHelper = new DBHelper(context);	//创建DBHlper对象实例

        //获取百度定位客户端
        mLocationClient = new LocationClient(getApplicationContext());  
 
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);

        
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        
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
                }else{
                	button.setBackgroundResource(R.drawable.popup_1);
                	i = (int) marker.getExtraInfo().get("deviceSN");  
                	
                	button.setText(Lock_Name[i]);
        			                  
                    mInfoWindow = new InfoWindow(button, ll, -47);                    
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
                return true;
            }
            
        });      

        initi();
    }
    
    private void initi() {
    	int Index = 0;;
    	String temp_Str;
    	
    	LatLng ll = null;
		MarkerOptions oo;
		
		BitmapDescriptor bd_Nomal = BitmapDescriptorFactory.fromResource(R.drawable.icon_rewrite);
		BitmapDescriptor bd_This = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
	        	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor; 
		
    	cursor=db.rawQuery("select * from Check_Data_Table  where Order_ID = ?", new String[]{workID});    			    

	    while(cursor.moveToNext()){
      		
    		temp_Str = cursor.getString(2);
    		Lock_Lng[Index] = cursor.getString(3);
			Lock_Lat[Index] = cursor.getString(4);  		
    		
    		if (temp_Str == null) {
    			temp_Str = "未命名";
    		}
    		else if (temp_Str.length() == 0) {
    			temp_Str = "未命名";
    		}
    		Lock_Name[Index] = temp_Str;
    		
    		ll = new LatLng(Double.parseDouble(Lock_Lat[Index]), Double.parseDouble(Lock_Lng[Index]));
    		oo = new MarkerOptions().position(ll).icon(bd_Nomal).zIndex(9).draggable(true);
					           
			oo.animateType(MarkerAnimateType.drop);//下落动作
			Marker_Lock[Index] = (Marker) mBaiduMap.addOverlay(oo);
			
			//标注点设置额外的信息 
			Bundle bundle = new Bundle();  
			bundle.putInt("deviceSN", Index);  
			Marker_Lock[Index].setExtraInfo(bundle);  
			
			Index++;

    	}
	    
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
        mBaiduMap.setMapStatus(u);
        
	    bd_Nomal.recycle();
        bd_This.recycle();
		
	}

	protected void onResume() {
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
    	// 退出时销毁定位  
    	mLocationClient.stop();  
    	// 关闭定位图层  
    	mBaiduMap.setMyLocationEnabled(false);  
    	mMapView.onDestroy();  
    	mMapView = null;  
    	
        super.onDestroy();
        dbHelper.close();

        
    }
}
