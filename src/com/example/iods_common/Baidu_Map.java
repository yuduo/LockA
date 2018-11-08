package com.example.iods_common;

import java.net.URISyntaxException;

import com.example.iods_lock_app.R;




import android.app.Activity;
import android.content.Intent;
import android.net.Uri;


public class Baidu_Map {
	
public Baidu_Map() {
	// TODO 自动生成的构造函数存根
}
	@SuppressWarnings("deprecation")
	public void tunToBaiduMap(String[] result2, Activity mactivity) {
		// TODO 自动生成的方法存根
   		String[] back=result2;	 
   		Double	latitude=Double.parseDouble(back[1]);
		Double	longitude=Double.parseDouble(back[0]);
		Double lastLa=Double.parseDouble(back[3]);
		Double lastLn=Double.parseDouble(back[2]);
			Intent intent = null;
		try {// 如果有安装百度地图 就启动百度地图
			StringBuffer sbs = new StringBuffer();
			
			
			sbs.append("intent://map/direction?origin=latlng:")
					// 我的位置
					.append(latitude)
					.append(",")
					.append(longitude)
					.append("|name:")
					.append(mactivity.getResources().getString(R.string.location))
					// 去的位置
					.append("&destination=latlng:")
					.append(lastLa) 
					.append(",")
					.append(lastLn)
					.append("|name:目标位置")
					.append("&mode=walking?coord_type=wgs84")
					.append("&referer=com.menu|menu#Intent;scheme=bdapp;package=com.ocm.main;end");
				
			//sbs.append("intent://map/direction?origin=latlng:31.200437,120.652794|name:我家&destination=latlng:31.210271,120.663857|name:目的地&coord_type=wgs84&mode=driving&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			
			
			try {
				intent = Intent.getIntent(sbs.toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			mactivity.startActivity(intent);
		} catch (Exception e) {// 没有百度地图则弹出网页端
			StringBuffer sb = new StringBuffer();
			sb.append("http://api.map.baidu.com/direction?origin=latlng:")
					// 我的位置
					.append(latitude)
					.append(",")
					.append(longitude)
					.append("|name:")
					.append(mactivity.getResources().getString(R.string.location))
					// 去的位置
					.append("&destination=latlng:")
					.append(lastLa)
					.append(",")
					.append(lastLn)
					.append("|name:")
					.append("终点")
					// 城市
					.append("&mode=driving?ion=").append("App.area_name")
					.append("&output=html");
			Uri uri = Uri.parse(sb.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			mactivity.startActivity(intent);
		}	
	}
	
	public void Market_BaiduMap(String lastLng, String lastLat, Activity mactivity, String lnglat_Type) {
		//在百度地图上标记施工目的地点, lnglat_Type取值仅有"GPS"和"Baidu"，分别表示两种坐标类型
		Intent intent = null;
		try {// 如果有安装百度地图 就启动百度地图
			StringBuffer sbs = new StringBuffer();
			
			if (lnglat_Type.equals("GPS")) {
				sbs.append("intent://map/marker?location=")
				.append(lastLat) 
				.append(",")
				.append(lastLng)
				.append("&title=施工地点&content=目的地&coord_type=wgs84&")
				.append("&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			
			}
			else {
				sbs.append("intent://map/marker?location=")
				.append(lastLat) 
				.append(",")
				.append(lastLng)
				.append("&title=施工地点&content=目的地&coord_type=bd09ll&")
				.append("&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			
			}
			
			
			try {
				intent = Intent.getIntent(sbs.toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			mactivity.startActivity(intent);
		} catch (Exception e) {// 没有百度地图则弹出网页端
			StringBuffer sb = new StringBuffer();
			/*
			sb.append("http://api.map.baidu.com/direction?origin=latlng:")
					// 我的位置
					.append(latitude)
					.append(",")
					.append(longitude)
					.append("|name:")
					.append(mactivity.getResources().getString(R.string.location))
					// 去的位置
					.append("&destination=latlng:")
					.append(lastLa)
					.append(",")
					.append(lastLn)
					.append("|name:")
					.append("终点")
					// 城市
					.append("&mode=driving?ion=").append("App.area_name")
					.append("&output=html");
			*/
			sb.append("bdapp://map/marker?location=")
				.append(lastLat) 
				.append(",")
				.append(lastLng)
				.append("&title=施工地点&content=目的地&coord_type=wgs84&")
				.append("&src=yourCompanyName|yourAppName");
			Uri uri = Uri.parse(sb.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			mactivity.startActivity(intent);
		}	
	}  
	
	
	
	/*
	
	public void startNavi(View view) {
		int lat = (int) (mLat1 * 1E6);
		int lon = (int) (mLon1 * 1E6);
		GeoPoint pt1 = new GeoPoint(lat, lon);
		lat = (int) (mLat2 * 1E6);
		lon = (int) (mLon2 * 1E6);
		GeoPoint pt2 = new GeoPoint(lat, lon);
		// 构建 导航参数
		NaviPara para = new NaviPara();
		para.startPoint = pt1;
		para.startName = "从这里开始";
		para.endPoint = pt2;
		para.endName = "到这里结束";

		try {

			BaiduMapNavigation.openBaiduMapNavi(para, this);

		} catch (BaiduMapAppNotSupportNaviException e) {
			e.printStackTrace();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
			builder.setTitle("提示");
			builder.setPositiveButton("确认", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					BaiduMapNavigation.GetLatestBaiduMapApp(NaviActivity.this);
				}
			});

			builder.setNegativeButton("取消", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			builder.create().show();
			
		}

	}
	
	*/
	
}
