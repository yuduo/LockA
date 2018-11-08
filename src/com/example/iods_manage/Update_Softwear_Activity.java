package com.example.iods_manage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iods_bluetooch.BLEService;
import com.example.iods_bluetooch.BLE_Communication;
import com.example.iods_bluetooch.BluetoothController;
import com.example.iods_bluetooch.ConstantUtils;
import com.example.iods_bluetooch.EntityDevice;
import com.example.iods_common.DataAlgorithm;
import com.example.iods_common.NMS_Communication;
import com.example.iods_lock_app.R;
import com.example.iods_lock_app.Show_Start_Logo;
import com.example.iods_lock_app.UserLogin_Activity;
import com.example.iods_manage.Group_Manage_Activity.ClickEvent;



public class Update_Softwear_Activity  extends Activity{
	//�豸�������
	
	Button BTN_Link_BT, BTN_To_Sleep, BTN_Lock_BT, BTN_Lock_NB;
	
	TextView Show_Lean_Status, Show_Water_Status, Show_Other_Status, Show_Low_Status;
	TextView Show_Open_A_Status, Show_Open_B_Status, Show_Lock_A_Status, Show_Lock_B_Status;
	TextView Show_New_Error, Show_Status_Error, Show_Help;
	
	private Intent intentService;
	private MsgReceiver receiver;
	BluetoothController controller=BluetoothController.getInstance();
	boolean BLE_Get_Start_Tag = false;
	boolean Send_BLE_Tag = false;
	
	int BLE_List_Total, this_BLE;
	String[] BLE_Name = new String[5];
	String[] BLE_Address = new String[5];
	
	String Locker_ID;
	
	String Softwear_Ver, Softwear_CRC;
	int int_Softwear_Len, int_This_Frame, int_This_byteLen, int_This_Cell;
	int index_Update;
	
	byte[] Softwear_byteData=new byte[30000];
	byte[] Softwear_Frame=new byte[1200];
	byte[] Softwear_Cell=new byte[20];
	
	TextView Show_informat;
	
	Button Project_Checkup_Btn, User_Manage_Btn;
	
	static NMS_Communication Connect_NMS;
	
	private Context context = Update_Softwear_Activity.this;//����Context����
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_group_manage);
	    setTitle("��ͨ���");
	     
	    
		initi();
	}

	private void initi() {
		Button Remote_Lock_Contral_Btn;
	    
	    User_Manage_Btn = (Button)findViewById(R.id.button2);
	    Project_Checkup_Btn = (Button)findViewById(R.id.button3);
	    Remote_Lock_Contral_Btn = (Button)findViewById(R.id.button1);
	    
	    User_Manage_Btn.setText("�����������");
	    Project_Checkup_Btn.setText("��ʼ�豸�������");
	    
	    User_Manage_Btn.setOnClickListener(new ClickEvent());
	    Project_Checkup_Btn.setOnClickListener(new ClickEvent());
	    //Project_Checkup_Btn.setEnabled(false);
	    //User_Manage_Btn.setEnabled(false);
				
		TextView Titale_View=(TextView)findViewById(R.id.Search_text_1);
		Show_informat=(TextView)findViewById(R.id.textView2);
		Titale_View.setText("�豸�������");
		
		Show_informat.setText("���������������");
		Remote_Lock_Contral_Btn.setVisibility(View.INVISIBLE);
		
		//�����������
		Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6224");
		Connect_NMS.Make_Socket_Connect();
	}
	
	class ClickEvent implements View.OnClickListener {    
    	
	       @SuppressLint("NewApi") @Override    
	       public void onClick(View v) {

	    	   switch(v.getId()){

	    	   case R.id.button3:
	    		   //�����豸�������	    		   
	    		   Open_BLE_Lock();
	    		   
	    		   break;
	   
	    	   case R.id.button2:
	    		   //�����������
	    		   Connect_NMS = new NMS_Communication(mHandler, "Wait_Send_6224");
	    		   Connect_NMS.Make_Socket_Connect();
						    		   
	    		   break;
	    	   }
	    	   
	       }
	}
	
	public void Open_BLE_Lock() {
		//��������
		BLE_Get_Start_Tag = false;

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
	
	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i;
			String temp_Str_1, temp_Str_2;
						
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
						                	.setTitle("�������")			//���öԻ���ı���
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
						                	.setTitle("��ѡ��������豸")	//���öԻ���ı���
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
						                	.setTitle("��ѡ��������豸")	//���öԻ���ı���
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
						                	.setTitle("��ѡ��������豸")	//���öԻ���ı���
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
						                	.setTitle("��ѡ��������豸")	//���öԻ���ı���
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
					//�ȶ�ȡ�豸�����ǰ�汾
					BLE_Communication.Send_Command_6104(controller);
					
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
					//TODO
					
					Recive_Str = Recive_Str.toUpperCase();
					
					if (Recive_Str.substring(0, 7).equals("C610400")) {
						if (Recive_Str.length() > 39) {
							Recive_Str = Recive_Str.substring(0, 39);
						}
						temp_Str_1 = Recive_Str.substring(23);
						temp_Str_2 = Softwear_Ver.substring(0, 3);
						String temp_Str_3 = Softwear_Ver.substring(3);
						
						Show_informat.setText("�豸����汾�� " + temp_Str_1);
						
						if (temp_Str_1.substring(0, 3).equals(temp_Str_2)) {
							//����汾����
							if(temp_Str_1.substring(3).compareTo(temp_Str_3) < 0) {
								//�����������
								index_Update = 1;
								Send_BLE_Tag = true;	//��������ָ���־��λ
								BLE_Communication.Send_Command_6124_1(controller, int_Softwear_Len);		//���Ͷ��豸IDָ��
							}
							else {
								dialog = new AlertDialog.Builder(context)
									.setTitle("��ʾ")			//���öԻ���ı���
									.setMessage("��������汾���ߣ���������")
									//���öԻ���İ�ť
									.setPositiveButton("ȷ��", null)
									.create();
								dialog.show();
							}
						}
						else {
							dialog = new AlertDialog.Builder(context)
    							.setTitle("��ʾ")			//���öԻ���ı���
    							.setMessage("����汾�����ݣ���������")
    							//���öԻ���İ�ť
    							.setPositiveButton("ȷ��", null)
    							.create();
							dialog.show();
						}
					}
					else if (Recive_Str.substring(0, 5).equals("C6124")) {
						//����ָ���Ӧ��

						if (index_Update == 4) {
							//�������������ѯ����
							if (Recive_Str.substring(0,9).equals("C61240004")) {
								//�����ɹ�
								dialog = new AlertDialog.Builder(context)
    								.setTitle("�ɹ�")			//���öԻ���ı���
    								.setMessage("�豸��������ɹ� ��")
    								//���öԻ���İ�ť
    								.setPositiveButton("ȷ��", null)
    								.create();
								dialog.show();
							}
							else {
								//����ʧ��
								Show_Dialog_Error();
							}
						}
						else if (index_Update == 3) {
							//��ʱ�������������ѯָ��
							if (Recive_Str.substring(0,9).equals("C61240003")) {
								Send_BLE_Tag = true;	//��������ָ���־��λ
								index_Update = 4;
							}
							else {
								//����ʧ��
								Show_Dialog_Error();
							}
						}
						else if (index_Update == 2) {
							//�����������ָ��
							if (Recive_Str.equals("C61240001")) {
								//�����ᷴ����һ���ź�
								index_Update = 2;
								int_This_Frame = 1;		//��һ֡
								int_This_Cell = 1;		//����ų�ʼ��
								//��֯��һ֡�����������
								Make_And_Send_Soft_Frame(0);
							}
							else if (Recive_Str.equals("C61240002")) {
								//��֡������ɵ�Ӧ��
								i = int_Softwear_Len % 1000;
								if (i > 0) {
									i = (int) (int_Softwear_Len/1000) + 1;
								}
								else {
									i = (int) (int_Softwear_Len/1000);
								}
								
								if (int_This_Frame < i) {
									//��֯����֡�����������
									int_This_Frame ++;
									int_This_Cell = 1;		//����ų�ʼ��
									
									Make_And_Send_Soft_Frame(0);
								}
								else {
									//�������
									Send_BLE_Tag = true;	//��������ָ���־��λ
									BLE_Communication.Send_Command_6124_3(controller);
									index_Update = 3;
								}
							}
							else if(Recive_Str.substring(5,7).equals("6C")) {
								//�Ƕ��а���Ӧ��
								temp_Str_1 = Recive_Str.substring(7);
								i = Integer.parseInt(temp_Str_1, 16);
								Make_And_Send_Soft_Frame(i);
							}
							else {
								//����ʧ��
								Show_Dialog_Error();
							}
						}
						else if (index_Update == 1) {
							//��ʼ���������������
							if (Recive_Str.substring(7).equals("01")) {
								index_Update = 2;
								int_This_Frame = 1;		//��һ֡
								int_This_Cell = 1;		//����ų�ʼ��
								//��֯��һ֡�����������
								Make_And_Send_Soft_Frame(0);
							}
							else {
								//����ʧ��
								Show_Dialog_Error();
							}
						}
					}
				}
				
				
				
				
				//TODO
				
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

		private void Make_And_Send_Soft_Frame(int Last_Cell) {
			// TODO ���������������֡�������ͳ���int_This_FrameΪ֡��ţ�int_This_CellΪ�����, int_This_byteLen���浱ǰ֡��(��ͷβ��7E)
			//Last_CellΪ��һ���а����
			int i, j, k;
			
			if (Last_Cell + 1 != int_This_Cell) {
				return;
			}
			
			i = int_Softwear_Len/1000;
			if (int_Softwear_Len%1000 > 0) {
				i = i+1;
			}
			
			Show_informat.setText("�豸����������̣� " + String.valueOf(int_This_Frame) + "/" + String.valueOf(i) + "  " + String.valueOf(Last_Cell));
			
			if (int_This_Cell == 1) {
				//�״ν��룬����֡����
				Softwear_Frame[0] = 0x7E;
				Softwear_Frame[1] = 0x00;
				Softwear_Frame[2] = 0x10;
				Softwear_Frame[3] = (byte)(int_This_Frame & 0xFF);;
				Softwear_Frame[4] = (byte)(int_This_Frame >> 8);		//С�˸�ʽ��֡���
				
				i = int_Softwear_Len % 1000;
				if (i > 0) {
					i = (int) (int_Softwear_Len/1000) + 1;
				}
				else {
					i = (int) (int_Softwear_Len/1000);
				}
				
				Softwear_Frame[5] = (byte)(i & 0xFF);
				Softwear_Frame[6] = (byte)(i >> 8);		//С�˸�ʽ��֡����
				
				//������
				Softwear_Frame[7] = 0x24;
				Softwear_Frame[8] = 0x61;
				Softwear_Frame[9] = (byte) 0xFF;		//״̬��
				
				Softwear_Frame[10] = 2;		//����������
				
				Softwear_Frame[11] = 1;		//��������
				
				Softwear_Frame[12] = Softwear_Frame[3];
				Softwear_Frame[13] = Softwear_Frame[4];		//���������
				
				for (i = 0; i<1000; i++) {
					if (i + 1000 *(int_This_Frame - 1) <= int_Softwear_Len) {
						Softwear_Frame[14 + i] = Softwear_byteData[i + 1000*(int_This_Frame - 1)];
					}
					else {
						//���ȫ������������ݵĳ�֡����
						break;
					}
				}
				int_This_byteLen = i + 14 + 2;		//δ����7D��7Eת����֡��
				
				//CRCУ��
				i = NMS_Communication.Cala_CRC(Softwear_Frame, i + 13);
				Softwear_Frame[int_This_byteLen - 2] = (byte) (i & 0xFF);				//��8λ
				Softwear_Frame[int_This_byteLen - 1] = (byte) (i >> 8);					//��8λ
				
				//������
				Softwear_Frame[int_This_byteLen] = 0x7E;
												
				//�ǽ�������0x7E ��ת�⴦��
				//��7Dת�� 7D 5D
				i = 1;
				j = 0;
				while (j == 0) {
					if (Softwear_Frame[i] == 0x7D ) {
						//������������ 7D���򽫺�������ȫ������һλ�����ݳ��� + 1������ 7E ת��Ϊ 7D, 5D
						for (k = int_This_byteLen; k >= i; k--) {
							Softwear_Frame[k + 1] = Softwear_Frame[k];
						}
						int_This_byteLen++;
						Softwear_Frame[i] = 0x7D;
						Softwear_Frame[i + 1] = 0x5D;
						i ++;
					}
					
					i ++;
					if (i >= int_This_byteLen)		j = 1;
				}
				
				//��7Eת�� 7D 5E
				i = 1;
				j = 0;
				while (j == 0) {
					if (Softwear_Frame[i] == 0x7E ) {
						//������������ 7E���򽫺�������ȫ������һλ�����ݳ��� + 1������ 7E ת��Ϊ 7D, 5E
						for (k = int_This_byteLen; k >= i; k--) {
							Softwear_Frame[k + 1] = Softwear_Frame[k];
						}
						int_This_byteLen++;
						Softwear_Frame[i] = 0x7D;
						Softwear_Frame[i + 1] = 0x5E;
						i ++;
					}
					
					i ++;
					if (i >= int_This_byteLen)		j = 1;
				}
				int_This_byteLen ++;	//���֡��������
				
				//�е�һ������������
				for (i = 0; i<20; i++) {
					Softwear_Cell[i] = Softwear_Frame[i];
				}				
				int_This_Cell ++;
				
				Send_BLE_Tag = true;	//��������ָ���־��λ
				
				controller.write(Softwear_Cell);		//�������ݰ�
			}
			else {
				//������ֻ�����а�����
				j = int_This_byteLen - 2;
				if (j % 18 > 0) {
					i = j/18 + 1;
				}
				else {
					i = j/18;
				}				//��õ�ǰ֡���а�����
				
				if (int_This_byteLen - (20 + 18*(int_This_Cell - 2)) > 18) {
					//����20
					Softwear_Cell[0] = (byte) i;
					Softwear_Cell[1] = (byte) int_This_Cell;
					
					for (i = 0; i < 18; i++) {
						Softwear_Cell[i + 2] = Softwear_Frame[18 * (int_This_Cell-1) + 2 + i];
					}
					int_This_Cell ++;
					
					Send_BLE_Tag = true;	//��������ָ���־��λ
					controller.write(Softwear_Cell);		//�������ݰ�
				}
				else {
					//ʣ�����
					j = int_This_byteLen - (20 + 18*(int_This_Cell - 2)) + 2;
					byte[] Last_Softwear_Cell = new byte[j];
					
					Last_Softwear_Cell[0] = (byte) i;
					Last_Softwear_Cell[1] = (byte) int_This_Cell;
					
					for (i = 2; i<j; i++) {
						Last_Softwear_Cell[i] = Softwear_Frame[18 * (int_This_Cell-1) + i];
					}
					int_This_Cell ++;
					
					Send_BLE_Tag = true;	//��������ָ���־��λ
					controller.write(Last_Softwear_Cell);		//�������ݰ�
				}
			}
		}

		private void Show_Dialog_Error() {
			AlertDialog dialog = new AlertDialog.Builder(context)
				.setTitle("��ʾ")			//���öԻ���ı���
				.setMessage("�豸�������ʧ�ܣ�������������")
				//���öԻ���İ�ť
				.setPositiveButton("ȷ��", null)
				.create();
			dialog.show();
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
		controller.close();

	}
		
	
	Handler mHandler = new Handler(){
  		public void handleMessage(Message msg){
  			String s, temp_Str;
  			int i, j;

  			switch(msg.what){
  			case 0:
  				//Ӧ�����
  				s=msg.obj.toString();
  				temp_Str = s.substring(0, 6);
  				
  				if (temp_Str.equals("6224 F")) {
					//�����ص������ļ�����
					temp_Str = s.substring(7, 10);
					
					if (temp_Str.equals("Ver")) {
						//�汾������
						Softwear_Ver = s.substring(10, 42);		//����汾��
						
						byte[] temp_byte = new byte[16];
						temp_byte = DataAlgorithm.hexStringToBytes(Softwear_Ver);
						Softwear_Ver = new String(temp_byte);
						
						Softwear_CRC = s.substring(43, 47);		//���CRCУ��ֵ
						
						int_Softwear_Len = Integer.valueOf(s.substring(48));	//�������
						
						int_This_Frame = 0;
						int_This_byteLen = 0;
						Softwear_byteData[0] = 0;	//��λ�ò������ݣ��Ա��ڵ���CRC�������
					}
					else {
						//��������
						temp_Str = s.substring(7);

						i = temp_Str.indexOf(" ");
						
						s = temp_Str.substring(0, i);	//��ǰ֡������֡��
						Show_informat.setText("����������ؽ��̣� " + s);
						
						temp_Str = temp_Str.substring(i + 1);	//�������
						byte[] Softwear_Frame_1 = DataAlgorithm.hexStringToBytes(temp_Str);
						int_This_Frame = temp_Str.length()/2;
						
						for (i = 0; i < int_This_Frame; i++) {
							Softwear_byteData[int_This_byteLen + i] = Softwear_Frame_1[i];
						}
						int_This_byteLen = int_This_byteLen + int_This_Frame;
						
						i = s.indexOf("/");
						//��ǰ֡��
						int_This_Frame = Integer.valueOf(s.substring(0, i));
						
						i = Integer.valueOf(s.substring(i + 1));		//��֡��
						
						if (i == int_This_Frame && int_This_byteLen == int_Softwear_Len) {
							//���һ֡
							if (int_This_byteLen == int_Softwear_Len) {
								//֡����ȷ�������CRC
								i = NMS_Communication.Cala_CRC(Softwear_byteData, 0, int_This_byteLen - 1);
								
								temp_Str = Softwear_CRC.substring(0, 2);
								s =  Softwear_CRC.substring(2);
								
								if ((i & 0xFF) ==  Integer.parseInt(temp_Str, 16) && (i >> 8) == Integer.parseInt(s, 16)) {
									//CRCУ����ȷ
									Show_informat.setText("���������أ����Խ����������");
									Project_Checkup_Btn.setEnabled(true);
									User_Manage_Btn.setEnabled(true);
									
									AlertDialog dialog = new AlertDialog.Builder(context)
	  									.setTitle("��ʾ")			//���öԻ���ı���
	  									.setMessage("���������أ����������������")	//��ʾ�������豸��
	  									//���öԻ���İ�ť
	  									.setPositiveButton("ȷ��", null)
	  									.create();
									dialog.show();

								}
								else {
									//CRCУ����ȷ
									AlertDialog dialog = new AlertDialog.Builder(context)
		  								.setTitle("��ʾ")			//���öԻ���ı���
		  								.setMessage("������س�������������")	//��ʾ�������豸��
		  								//���öԻ���İ�ť
		  								.setPositiveButton("ȷ��", null)
		  								.create();
									dialog.show();
								}
							}
							else {
								//֡������
								AlertDialog dialog = new AlertDialog.Builder(context)
		  							.setTitle("��ʾ")			//���öԻ���ı���
		  							.setMessage("������س�������������")	//��ʾ�������豸��
		  							//���öԻ���İ�ť
		  							.setPositiveButton("ȷ��", null)
		  							.create();
								dialog.show();
							}
						}
					}						
				}
  				else {
  					//���س���
  					AlertDialog dialog = new AlertDialog.Builder(context)
  						.setTitle("��ʾ")			//���öԻ���ı���
  						.setMessage("������س�������������")	//��ʾ�������豸��
  						//���öԻ���İ�ť
  						.setPositiveButton("ȷ��", null)
  						.create();
  					dialog.show();
  				}
  				
  				
  				break;
  				
  			case 2:
  				s=msg.obj.toString();

  				if (s.equals("Wait_Send_6224")) {
  					
  					Connect_NMS.Wait_Recive_TCP_Reply();	
  					NMS_Communication.Download_Softwear_6224();	//�����������
  					
  				}
  				break;
  			}
  		}
  	};

}
