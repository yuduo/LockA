package com.example.iods_lock_app;

import com.example.iods_bluetooch.BLEService;
import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_bluetooch.BluetoothController;
import com.example.iods_bluetooch.ConstantUtils;
import com.example.iods_bluetooch.EntityDevice;
import com.example.iods_common.DBHelper;
import com.example.iods_common.NMS_Communication;
import com.example.iods_manage.Make_NewLock_Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
//import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;




//�б���ʾ��
public class WorkOrderActivity extends Activity {
	//��������ҳ��
	
	//�����ؼ�����

	private ListView Order_ListView;
	private SimpleAdapter Order_listAdapter;				//�½����豸�б�������
	private List<HashMap<String,String>> OrderList;//�����б�����Դ

	private Spinner spinner1, spinner2;//��ѯspinner
	int selectedposition;//spinner2��ѡ���

	Context context=WorkOrderActivity.this;
	DBHelper dbHelper;
	
	static NMS_Communication Connect_NMS;
	
	//�㲥����
	private ServiceReceiver mReceiver;
	private String action="Flash_Order_List";
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean BLE_Lock_Contrl_Tag = false;
	
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order);
        setTitle("��ͨ���");
        dbHelper = new DBHelper(context);//��ʼ�����ݿ����    
        
        //�㲥��ʼ��
        mReceiver = new ServiceReceiver();
	    //ʵ����������������Ҫ���˵Ĺ㲥
	    IntentFilter mFilter = new IntentFilter();
	    mFilter.addAction(action);
	    registerReceiver(mReceiver, mFilter);
	    
		//�ؼ���ʼ��
		initi();
		
    }
    
    public void initi(){
    	spinner1 = (Spinner)findViewById(R.id.spinner_Select);//�ڶ��������˵����ɵ�һ�������˵���ѡ�������
		spinner2 = (Spinner)findViewById(R.id.spinner2);//��һ�������˵�		
		
		Button DownLoad_Btn;
		
		Order_ListView=(ListView)findViewById(R.id.worklistView);
		
		Button Apply_Insert_Tag_BTN = (Button)findViewById(R.id.Apply_Insert_Tag_btn);
		DownLoad_Btn = (Button)findViewById(R.id.Save_Config);
		
		DownLoad_Btn.setOnClickListener(new ClickEvent());
		Apply_Insert_Tag_BTN.setOnClickListener(new ClickEvent());
		
		Apply_Insert_Tag_BTN.setText("�豸ע���ѯ");
		
		//Apply_Insert_Tag_BTN.setVisibility(View.INVISIBLE);			//����ʾ�˰���
		spinner1.setVisibility(View.INVISIBLE);			//����ʾ�˰���
		spinner2.setVisibility(View.INVISIBLE);			//����ʾ�˰���
		
		Flash_Order_List();		//ˢ�¹����б�
		
		//���ع����б�
		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620D");
		Connect_NMS.Make_Socket_Connect();
			
	}
    

    @SuppressLint("NewApi") 
    private void Flash_Order_List() {
		//ˢ�¹����б�
    	String Str_ID, temp_Str, temp_Str_1 = null, Str_Date;
    	    	
    	SQLiteDatabase db=dbHelper.getWritableDatabase();
		Cursor cursor;  
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");// HH:mm:ss
		//��ȡ��ǰʱ��
		Date date = new Date(System.currentTimeMillis());
		Str_Date = simpleDateFormat.format(date);
		cursor=db.rawQuery("select * from Order_List_Table where userID = ? AND Status != '4' AND (Status != '5' OR back_date = ?) ORDER BY Order_Type", new String[]{UserLogin_Activity.Login_User_ID, Str_Date});    	
	     
		OrderList = new ArrayList<HashMap<String,String >>();
	      
	    while(cursor.moveToNext()){
      
    		HashMap<String,String > map=new HashMap<String,String>();   		
    		
    		Str_ID = cursor.getString(1);		//ȡ����ID
    		map.put("OrderID", Str_ID);
    		
    		temp_Str = Str_ID.substring(0, 6);	//��ȡ��������
    		
    		if (temp_Str.equals("INSERT")) {
    			temp_Str_1 = "ʩ������";
    		}
    		else if (temp_Str.equals("MAINTE")) {
    			temp_Str_1 = "ά������";
    		}
    		else if (temp_Str.equals("MOVETO")) {
    			temp_Str_1 = "�ƻ�����";
    		}
    		else if (temp_Str.equals("REMOVE")) {
    			temp_Str_1 = "�������";
    		}
    		
    		temp_Str = cursor.getString(5);		//ȡ����״̬
    		if (temp_Str.equals("5")) {
    			temp_Str_1 = "�ѻص�";
    		}
    		else if (temp_Str.equals("4")) {
    			//����ʾ���˵�����
    			continue;
    		}
    		
    		map.put("OrderType", temp_Str_1);
    		
    		Str_Date = cursor.getString(4);		//ȡ�깤ʱ��
    		if (Str_Date == null) {
    			Str_Date = "";
    		}
    		if (Str_Date.equals("0")) {
    			Str_Date = "";
    		}
    		map.put("OrderLimite", Str_Date);
    		
    		OrderList.add(map);
    	}
      
		db.close();
		
		String[] from=new String[]{"OrderID", "OrderLimite", "OrderType"};
		int[] to=new int[]{R.id.textView1, R.id.textView3, R.id.textView5};
		Order_listAdapter=new SimpleAdapter(context, OrderList, R.layout.listitem_order,from, to);
	  	Order_ListView.setAdapter(Order_listAdapter);
	  	Order_listAdapter.notifyDataSetChanged();
  	
	  	Order_ListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent();   //����Intent���� 
				
				TextView tv=(TextView)view.findViewById(R.id.textView1);
				String Str_Order = tv.getText().toString();
				
				tv=(TextView)view.findViewById(R.id.textView5);
				String Str_Status = tv.getText().toString();
				
	    		intent.setClass(context, Make_NewLock_Activity.class);	
	    		intent.putExtra("workID", Str_Order);
	    		intent.putExtra("Status", Str_Status);
	        	startActivity(intent);
		
			}
    		
    	});
	}
    
    
    class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().contains("Flash_Order_List")) {
				//��ɻص���ʩ��ҳ�汻�ر�
				Flash_Order_List();		//ˢ�¹����б�

			}
		}	
    }
		

	//����Handler����
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			String s, temp_Str;
			int temp_Int = 0;

			switch(msg.what){
			case 0:
				//Ӧ�����
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				if (temp_Str.equals("620D")) {
					temp_Str = s.substring(5);
					
					if (temp_Str.length() > 0) {
						//�й���ID
						SQLiteDatabase db=dbHelper.getWritableDatabase();
						
						Cursor cursor=db.rawQuery("select * from Order_List_Table  where Order_ID = ?", new String[]{temp_Str});    			    
						if (cursor.moveToNext()){
							temp_Int = 1;
						}
						cursor.close();
						
						if (temp_Int == 0) {
							db.execSQL("INSERT INTO Order_List_Table (Order_ID, Order_Type, userID, dateLimit, Status, flow) " +
									"VALUES (?, ?, ?, '', '0', 0)", new String[]{temp_Str, temp_Str.substring(0, 6), UserLogin_Activity.Login_User_ID});
						}
						db.close();
						
						Flash_Order_List();
					}
				}

				break;
			case 2:
				s=msg.obj.toString();

				if (s.equals("Wait_Send_620D")) {
					
					Connect_NMS.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.DownLoad_OrderList_620D(UserLogin_Activity.Login_User_ID);
					
				}
				break;
			}
		}
	};

	
	
	

	
	@Override
	protected void onResume() {

		super.onResume();
		Flash_Order_List();		//ˢ�¹����б�
	}
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i("ǰ����", "onDestroy()");		
		dbHelper.close();
		
		unregisterReceiver(mReceiver);
	}
	   


	public void onNewIntent(Intent intent) {
		//NFCUntils.NFC_onNewIntent(intent, nfcv, mHandler, true);
		//true��ʾ����ǩ
	}
	
	//�Զ��嵥���¼���
	class ClickEvent implements View.OnClickListener {    

		

		public void onClick(View v) {

	    	   switch(v.getId()){
	    	   case R.id.Save_Config:
	    		   //���ع����б�
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_620D");
				   Connect_NMS.Make_Socket_Connect();
	    		   					
	    		   break;
	    		   
	    	   case R.id.Apply_Insert_Tag_btn:
	    		   //��ѯ�豸BNƽ̨ע�����
	    		   Open_BLE_Lock();  
	    		   					
	    		   break;
	    		
	    	   }
		}
	}
		
	
	public void Open_BLE_Lock() {
		//����������
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
						                	.setTitle("��ѯ�豸")			//���öԻ���ı���
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
						                	.setTitle("��ѡ���ѯ���豸")	//���öԻ���ı���
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
						                	.setTitle("��ѡ���ѯ���豸")	//���öԻ���ı���
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
						                	.setTitle("��ѡ���ѯ���豸")	//���öԻ���ı���
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
						                	.setTitle("��ѡ���ѯ���豸")	//���öԻ���ı���
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
					
					BLE_Communication.Send_Command_6105(controller, 3);		//����д�豸ע���ѯָ��
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
				
				if (Recive_Str.substring(0, 7).equals("C610500")) {
					//�յ��豸ע�������ѯ
					if (Recive_Str.substring(7).equals("04")) {
						Recive_Str = "�豸�ѳɹ������NBƽ̨��ע��";
					}
					else {
						Recive_Str = "�豸��δ��NBƽ̨��ע��ɹ��������¼ӵ糢���ٴ�ע�ᡣ";
					}
					
					dialog = new AlertDialog.Builder(context)
						.setTitle("�豸ע�����")			//���öԻ���ı���
						.setMessage(Recive_Str)
						//���öԻ���İ�ť
						.setPositiveButton("ȷ��", null)
						.create();
					dialog.show();

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
	
	
}
