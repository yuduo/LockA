package com.example.iods_common;

import java.net.URISyntaxException;

import com.example.iods_lock_app.R;




import android.app.Activity;
import android.content.Intent;
import android.net.Uri;


public class Baidu_Map {
	
public Baidu_Map() {
	// TODO �Զ����ɵĹ��캯�����
}
	@SuppressWarnings("deprecation")
	public void tunToBaiduMap(String[] result2, Activity mactivity) {
		// TODO �Զ����ɵķ������
   		String[] back=result2;	 
   		Double	latitude=Double.parseDouble(back[1]);
		Double	longitude=Double.parseDouble(back[0]);
		Double lastLa=Double.parseDouble(back[3]);
		Double lastLn=Double.parseDouble(back[2]);
			Intent intent = null;
		try {// ����а�װ�ٶȵ�ͼ �������ٶȵ�ͼ
			StringBuffer sbs = new StringBuffer();
			
			
			sbs.append("intent://map/direction?origin=latlng:")
					// �ҵ�λ��
					.append(latitude)
					.append(",")
					.append(longitude)
					.append("|name:")
					.append(mactivity.getResources().getString(R.string.location))
					// ȥ��λ��
					.append("&destination=latlng:")
					.append(lastLa) 
					.append(",")
					.append(lastLn)
					.append("|name:Ŀ��λ��")
					.append("&mode=walking?coord_type=wgs84")
					.append("&referer=com.menu|menu#Intent;scheme=bdapp;package=com.ocm.main;end");
				
			//sbs.append("intent://map/direction?origin=latlng:31.200437,120.652794|name:�Ҽ�&destination=latlng:31.210271,120.663857|name:Ŀ�ĵ�&coord_type=wgs84&mode=driving&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			
			
			try {
				intent = Intent.getIntent(sbs.toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			mactivity.startActivity(intent);
		} catch (Exception e) {// û�аٶȵ�ͼ�򵯳���ҳ��
			StringBuffer sb = new StringBuffer();
			sb.append("http://api.map.baidu.com/direction?origin=latlng:")
					// �ҵ�λ��
					.append(latitude)
					.append(",")
					.append(longitude)
					.append("|name:")
					.append(mactivity.getResources().getString(R.string.location))
					// ȥ��λ��
					.append("&destination=latlng:")
					.append(lastLa)
					.append(",")
					.append(lastLn)
					.append("|name:")
					.append("�յ�")
					// ����
					.append("&mode=driving?ion=").append("App.area_name")
					.append("&output=html");
			Uri uri = Uri.parse(sb.toString());
			intent = new Intent(Intent.ACTION_VIEW, uri);
			mactivity.startActivity(intent);
		}	
	}
	
	public void Market_BaiduMap(String lastLng, String lastLat, Activity mactivity, String lnglat_Type) {
		//�ڰٶȵ�ͼ�ϱ��ʩ��Ŀ�ĵص�, lnglat_Typeȡֵ����"GPS"��"Baidu"���ֱ��ʾ������������
		Intent intent = null;
		try {// ����а�װ�ٶȵ�ͼ �������ٶȵ�ͼ
			StringBuffer sbs = new StringBuffer();
			
			if (lnglat_Type.equals("GPS")) {
				sbs.append("intent://map/marker?location=")
				.append(lastLat) 
				.append(",")
				.append(lastLng)
				.append("&title=ʩ���ص�&content=Ŀ�ĵ�&coord_type=wgs84&")
				.append("&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			
			}
			else {
				sbs.append("intent://map/marker?location=")
				.append(lastLat) 
				.append(",")
				.append(lastLng)
				.append("&title=ʩ���ص�&content=Ŀ�ĵ�&coord_type=bd09ll&")
				.append("&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
			
			}
			
			
			try {
				intent = Intent.getIntent(sbs.toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			mactivity.startActivity(intent);
		} catch (Exception e) {// û�аٶȵ�ͼ�򵯳���ҳ��
			StringBuffer sb = new StringBuffer();
			/*
			sb.append("http://api.map.baidu.com/direction?origin=latlng:")
					// �ҵ�λ��
					.append(latitude)
					.append(",")
					.append(longitude)
					.append("|name:")
					.append(mactivity.getResources().getString(R.string.location))
					// ȥ��λ��
					.append("&destination=latlng:")
					.append(lastLa)
					.append(",")
					.append(lastLn)
					.append("|name:")
					.append("�յ�")
					// ����
					.append("&mode=driving?ion=").append("App.area_name")
					.append("&output=html");
			*/
			sb.append("bdapp://map/marker?location=")
				.append(lastLat) 
				.append(",")
				.append(lastLng)
				.append("&title=ʩ���ص�&content=Ŀ�ĵ�&coord_type=wgs84&")
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
		// ���� ��������
		NaviPara para = new NaviPara();
		para.startPoint = pt1;
		para.startName = "�����￪ʼ";
		para.endPoint = pt2;
		para.endName = "���������";

		try {

			BaiduMapNavigation.openBaiduMapNavi(para, this);

		} catch (BaiduMapAppNotSupportNaviException e) {
			e.printStackTrace();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("����δ��װ�ٶȵ�ͼapp��app�汾���ͣ����ȷ�ϰ�װ��");
			builder.setTitle("��ʾ");
			builder.setPositiveButton("ȷ��", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					BaiduMapNavigation.GetLatestBaiduMapApp(NaviActivity.this);
				}
			});

			builder.setNegativeButton("ȡ��", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			builder.create().show();
			
		}

	}
	
	*/
	
}
