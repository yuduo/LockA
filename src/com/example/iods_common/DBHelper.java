package com.example.iods_common;

import java.text.SimpleDateFormat;

import com.example.iods_lock_app.UserLogin_Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;

//���ݿ������
public class DBHelper extends SQLiteOpenHelper{
	public static String db_name = "cable";	//���ݿ�����
	public static int  db_ver =1;//���ݿ�汾	
	public static SQLiteDatabase db;//���ݿ����
	
	static NMS_Communication Connect_NMS_Log;
	static String User_Log, Log_recode, Log_Obj;

	
	//�����б�
	
	public static String Table_Order_List = "Order_List_Table";
	
	/*
	 * �����б�
	 * 1 _id��:�������
	 * Order_ID������ID
	 * Order_Type����������
	 * userID:�ӵ��û�
	 * dateLimit��ʩ��ʱ��
	 * Status:����״̬ ��0��nullδ�������ݣ�1������δʩ����2�Ѳ���ʩ����3����ɴ��ص���4���˵���5���깤�ص�
	 * flow:�������̣�
	 * back_date: �ص�����
	 * 
	 * 
	 */
	public static String Create_Order_List_Table = "Create Table if not exists Order_List_Table (_id integer primary key autoincrement," +
			" Order_ID text , Order_Type text , userID text, dateLimit text , Status text, flow integer, back_date text, UNIQUE(Order_ID))";

	
	public static String Table_Order_Data = "Order_Data_Table";
	
	/*
	 * �������ݱ�
	 * 1 _id��:��¼���
	 * Order_ID������ID
	 * Old_Locker_ID��		����ID
	 * Old_Locker_Name:	������
	 * Old_Locker_Lng��	�Ͼ�γ��
	 * Old_Locker_lat
	 * New_Locker_Name:	������
	 * New_Locker_Lng��	�¾�γ��
	 * New_Locker_lat
	 * New_Locker_ID
	 * Status:			�깤����״̬��0δ��ɣ�1���
	 * New_Lock_imsi

	 * 
	 */
	public static String Create_Order_Data_Table = "Create Table if not exists Order_Data_Table (_id integer primary key autoincrement , " +
			"Order_ID text, Old_Locker_ID text, Old_Locker_Name text, Old_Locker_Lng text, Old_Locker_lat text, New_Locker_Name text, New_Locker_Lng text, New_Locker_lat text, New_Locker_ID  text, Status  text, New_Lock_imsi text)"; 
	
	
	public static String Table_Check_List = "Check_List_Table";
	
	/*
	 * ��鹤���б�
	 * 1 _id��:�������
	 * Order_ID������ID
	 * userID:�����û�
	 * WorkerID:ʩ���û�
	 * Lock_Numb������
	 * Oder_Numb: ����Ҫ������
	 * Status:����״̬ ��0��nullδ�������ݣ�1������δ��飻4�����飻
	 * 
	 */
	public static String Create_Check_List_Table = "Create Table if not exists Check_List_Table (_id integer primary key autoincrement," +
			" Order_ID text , userID text , WorkerID text , Lock_Numb text , Oder_Numb text , Status text, UNIQUE(Order_ID))";

	
	public static String Table_Check_Data = "Check_Data_Table";
	
	/*
	 * ��鹤�����ݱ�
	 * 1 _id��:��¼���
	 * Order_ID������ID
	 * Locker_Name:	������
	 * Locker_Lng��	�Ͼ�γ��
	 * Locker_lat
	 * Status:			�깤����״̬��0δ��ɣ�1���

	 * 
	 */
	public static String Create_Check_Data_Table = "Create Table if not exists Check_Data_Table (_id integer primary key autoincrement , " +
			"Order_ID text, Locker_Name text, Locker_Lng text, Locker_lat text, Status,  text)"; 
		


	public static String Table_Log_Record = "Log_Record_Table";//��־��¼��
	/*
	 * ��־��¼��
	 * _id  ����
	 * User_Account ��¼�˺�(string)
	 * Date_Time ��¼ʱ��(string)
	 * Operation_Record ��������(string)
	 * Operation_Obj ����Ŀ�ꣻ
	 */

	public static String Create_Log_Record = "Create Table " + 
			"if not exists Table_Log_Record(_id integer primary key autoincrement ,User_Account text,  Date_Time text , Operation_Record text  , Operation_Obj)";
	
	
	public static String Basic_Config_Table = "Basic_Config_Table";
	
	/*
	 * �������ñ�����һ����¼�������ú͵�ǰ�û�ID���Ա������������ִ�в���
	 * 1 _id��:��¼���
	 * NMSAdrres�����ܷ����ַ
	 * NMSPort�����ܷ���˿ں�
	 * Search_Scope: ������Χ 1~10.
	 * Breakgroup: ����ͼ��. 0Ϊ��
	 * 
	 */
	public static String Create_Basic_Config_Table = "Create Table if not exists Basic_Config_Table (_id integer primary key autoincrement , " +
			"NMSAdrres text, NMSPort text, Search_Scope text, Breakgroup text)"; 
			
	public static String User_Record_Table = "User_Record_Table";
	
	/*
	 * �û���¼������һ����¼���浱ǰ�û�ID���Ա������������ִ�в���
	 * 1 _id��:��¼���
	 * UserID����¼�û�ID
	 * UserType���û����ͣ�  �û������У�3Ϊ�ն˹����û���4Ϊ�ն�ʩ���û������ܻظ�0��ʾ�Ƿ��û�
	 * UserPassWd:�û�����
	 * Flash_Date: �û�������ˢ��ʱ��
	 * Last_Tag���ϴε�¼���

	 * 
	 */
	public static String Create_User_Record_Table = "Create Table if not exists User_Record_Table (_id integer primary key autoincrement , " +
			"UserID text , UserPassWd text , UserType text , Flash_Date text, Last_Tag text)"; 
			

	public static String User_Locker_List_Table = "User_Locker_Table";
	
	/*
	 * �û���Ͻ�����б�
	 * 1 _id��:��¼���
	 * UserID���û�ID
	 * Locker_ID����Ͻ����ID

	 * 
	 */
	public static String Create_User_Locker_Table = "Create Table if not exists User_Locker_Table (_id integer primary key autoincrement , " +
			"UserID text , Locker_ID text)"; 
			
	/**
	 * ������ʵ������
	 * @param context��Context����
	 */
	
	
	
	public DBHelper(Context context) {
		super(context, db_name, null, db_ver);
		// TODO �Զ����ɵĹ��캯�����
	}

	//�������ݱ�
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO �Զ����ɵķ������
		//ִ�д�����
//		db.execSQL(create_order_list_table);
//		db.execSQL(create_fiber_data_table);
//		db.execSQL(create_fiber_back_table);
		this.db = db;
		db.execSQL(Create_Order_List_Table);
		//db.execSQL(Create_Polling_Record);
	}
	
	/**
	 * �ж����ݿ��У�ĳ���Ƿ����
	 * @param table��Sting�����ݱ���
	 * @return boolean��true������ڣ�false��������
	 */
    public boolean isExist(String table){
        boolean result = false;
        if(table == null){
                return false;
        }
        Cursor cursor = null;
        SQLiteDatabase db = getWritableDatabase();
        try {
                String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"+table.trim()+"' ";
                cursor = db.rawQuery(sql, null);
                if(cursor.moveToNext()){
                        int count = cursor.getInt(0);
                        if(count>0){//�����
                        	result = true;
                        }
                }
                
        } catch (Exception e) {
        	// TODO: handle exception
        }finally {
        	if(null != cursor && !cursor.isClosed()){
        		cursor.close() ;
        	}
        }                
        return result;
}
	
	/**
	 * �����ݱ��в�������
	 * @param values ��ContentValues���󣬰����������ݵļ�-ֵ����ʽ
	 * @param table��String��Ҫ�������ݵ����ݱ�����
	 */
	public void insertTb(ContentValues values, String table){
		SQLiteDatabase db = getWritableDatabase();
		db.insert(table, null, values);
	}
	
	/**
	 * ��ѯ���ݱ���������
	 * @param table��Ҫ��ѯ���ݵ����ݱ�����
	 * @return Cursor�����������α�
	 */
	public Cursor queryTb(String table){
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(table, null, null, null, null, null, null);
		return c;		
	}
	
	/**
	 * ������ݱ�
	 * @param table��String��Ҫ�������ݵ����ݱ�����
	 */
	public void clearTb(String table){
		SQLiteDatabase db = getWritableDatabase();
//		db.delete(table, "1", null);//��ձ�����
		String sql = "delete from " + table + ";";//��ձ�����
		db.execSQL(sql);
		sql = "update sqlite_sequence set seq=0 where name='"+table+"'";//��������������
		db.execSQL(sql);
	}
	
	/**
	 * �������ݱ�
	 * @param tableSql��String���������ݱ��SQL���
	 */
	public void createTb(String tableSql){//����������sql���
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(tableSql);//ִ�н������
	}
	
	/**
	 * ɾ�����ݱ�
	 * @param table��String��Ҫɾ�����ݵ����ݱ�����
	 */
	public void deleteTb(String table){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + table);//ִ��ɾ�������
	}
	
	/**
	 * �ر����ݿ�
	 */
	public void close(){
		if(db != null){
		}
	}
	
	/**
	 * ɾ�����ݿ�
	 * @param context��Context����ʵ��
	 * @return
	 */
	public boolean deleteDB(Context context){
		return context.deleteDatabase(db_name); 
	}
	
	/**
	 * �������ݿ�ʱ����İ汾���뵱ǰ�İ汾�Ų�ͬʱ����ø÷���
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//�Զ����ɵķ������		
		//		 db.execSQL("DROP TABLE IF EXISTS Warn_Info_Table");
		//         onCreate(db);
	}
	
	public static void Save_Log_inDB(SQLiteDatabase SQL_db, String User_Acct, String Operat_recode, String Operat_Obj) {
		// ������־
		String this_Time, Str_sql;
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this_Time = sDateFormat.format(new java.util.Date());
		
		User_Log = User_Acct;
		Log_recode = Operat_recode;
		Log_Obj = Operat_Obj;

		Str_sql = "INSERT INTO Table_Log_Record (User_Account, Date_Time, Operation_Record, Operation_Obj) VALUES (?, ?, ?, ?)";
		SQL_db.execSQL(Str_sql, new String[]{User_Acct, this_Time, Operat_recode, Operat_Obj});			
		
		Connect_NMS_Log = new NMS_Communication(mHandler, "Wait_Send_6210");
		Connect_NMS_Log.Make_Socket_Connect();
	}
	
	
	static Handler mHandler = new Handler(){
		@SuppressLint("HandlerLeak") public void handleMessage(Message msg){
			String s, temp_Str;
			
			switch(msg.what) {
			
			case 0:
				//Ӧ�����
				s=msg.obj.toString();
				temp_Str = s.substring(0, 4);
				
				

				break;
				
			case 2:
				s=msg.obj.toString();

				if (s.equals("Wait_Send_6210")) {
					Connect_NMS_Log.Wait_Recive_TCP_Reply();	
			    	NMS_Communication.Update_Log_Data_6210(User_Log, Log_recode, Log_Obj);  

				}				

				break;
			
			}
			
		}

	};
   
   
}
