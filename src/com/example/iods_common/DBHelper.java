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

//数据库服务类
public class DBHelper extends SQLiteOpenHelper{
	public static String db_name = "cable";	//数据库名称
	public static int  db_ver =1;//数据库版本	
	public static SQLiteDatabase db;//数据库对象
	
	static NMS_Communication Connect_NMS_Log;
	static String User_Log, Log_recode, Log_Obj;

	
	//工单列表
	
	public static String Table_Order_List = "Order_List_Table";
	
	/*
	 * 工单列表
	 * 1 _id：:工单序号
	 * Order_ID：工单ID
	 * Order_Type：工单类型
	 * userID:接单用户
	 * dateLimit：施工时限
	 * Status:工单状态 ：0或null未下载数据；1已下载未施工；2已部分施工；3已完成待回单；4已退单；5已完工回单
	 * flow:工单进程：
	 * back_date: 回单日期
	 * 
	 * 
	 */
	public static String Create_Order_List_Table = "Create Table if not exists Order_List_Table (_id integer primary key autoincrement," +
			" Order_ID text , Order_Type text , userID text, dateLimit text , Status text, flow integer, back_date text, UNIQUE(Order_ID))";

	
	public static String Table_Order_Data = "Order_Data_Table";
	
	/*
	 * 工单数据表
	 * 1 _id：:记录序号
	 * Order_ID：工单ID
	 * Old_Locker_ID：		锁具ID
	 * Old_Locker_Name:	老锁名
	 * Old_Locker_Lng：	老经纬度
	 * Old_Locker_lat
	 * New_Locker_Name:	新锁名
	 * New_Locker_Lng：	新经纬度
	 * New_Locker_lat
	 * New_Locker_ID
	 * Status:			完工工单状态：0未完成，1完成
	 * New_Lock_imsi

	 * 
	 */
	public static String Create_Order_Data_Table = "Create Table if not exists Order_Data_Table (_id integer primary key autoincrement , " +
			"Order_ID text, Old_Locker_ID text, Old_Locker_Name text, Old_Locker_Lng text, Old_Locker_lat text, New_Locker_Name text, New_Locker_Lng text, New_Locker_lat text, New_Locker_ID  text, Status  text, New_Lock_imsi text)"; 
	
	
	public static String Table_Check_List = "Check_List_Table";
	
	/*
	 * 审查工单列表
	 * 1 _id：:工单序号
	 * Order_ID：工单ID
	 * userID:管理用户
	 * WorkerID:施工用户
	 * Lock_Numb：数量
	 * Oder_Numb: 工单要求数量
	 * Status:工单状态 ：0或null未下载数据；1已下载未审查；4完成审查；
	 * 
	 */
	public static String Create_Check_List_Table = "Create Table if not exists Check_List_Table (_id integer primary key autoincrement," +
			" Order_ID text , userID text , WorkerID text , Lock_Numb text , Oder_Numb text , Status text, UNIQUE(Order_ID))";

	
	public static String Table_Check_Data = "Check_Data_Table";
	
	/*
	 * 审查工单数据表
	 * 1 _id：:记录序号
	 * Order_ID：工单ID
	 * Locker_Name:	老锁名
	 * Locker_Lng：	老经纬度
	 * Locker_lat
	 * Status:			完工工单状态：0未完成，1完成

	 * 
	 */
	public static String Create_Check_Data_Table = "Create Table if not exists Check_Data_Table (_id integer primary key autoincrement , " +
			"Order_ID text, Locker_Name text, Locker_Lng text, Locker_lat text, Status,  text)"; 
		


	public static String Table_Log_Record = "Log_Record_Table";//日志记录表
	/*
	 * 日志记录表
	 * _id  主键
	 * User_Account 记录账号(string)
	 * Date_Time 记录时间(string)
	 * Operation_Record 操作描述(string)
	 * Operation_Obj 操作目标；
	 */

	public static String Create_Log_Record = "Create Table " + 
			"if not exists Table_Log_Record(_id integer primary key autoincrement ,User_Account text,  Date_Time text , Operation_Record text  , Operation_Obj)";
	
	
	public static String Basic_Config_Table = "Basic_Config_Table";
	
	/*
	 * 基本配置表，仅第一个记录保存配置和当前用户ID，以便在离线情况下执行操作
	 * 1 _id：:记录序号
	 * NMSAdrres：网管服务地址
	 * NMSPort：网管服务端口号
	 * Search_Scope: 搜锁范围 1~10.
	 * Breakgroup: 背景图号. 0为空
	 * 
	 */
	public static String Create_Basic_Config_Table = "Create Table if not exists Basic_Config_Table (_id integer primary key autoincrement , " +
			"NMSAdrres text, NMSPort text, Search_Scope text, Breakgroup text)"; 
			
	public static String User_Record_Table = "User_Record_Table";
	
	/*
	 * 用户记录表，仅第一个记录保存当前用户ID，以便在离线情况下执行操作
	 * 1 _id：:记录序号
	 * UserID：登录用户ID
	 * UserType：用户类型，  用户类型中，3为终端管理用户，4为终端施工用户，网管回复0表示非法用户
	 * UserPassWd:用户密码
	 * Flash_Date: 用户锁数据刷新时间
	 * Last_Tag：上次登录标记

	 * 
	 */
	public static String Create_User_Record_Table = "Create Table if not exists User_Record_Table (_id integer primary key autoincrement , " +
			"UserID text , UserPassWd text , UserType text , Flash_Date text, Last_Tag text)"; 
			

	public static String User_Locker_List_Table = "User_Locker_Table";
	
	/*
	 * 用户所辖锁具列表
	 * 1 _id：:记录序号
	 * UserID：用户ID
	 * Locker_ID：所辖锁具ID

	 * 
	 */
	public static String Create_User_Locker_Table = "Create Table if not exists User_Locker_Table (_id integer primary key autoincrement , " +
			"UserID text , Locker_ID text)"; 
			
	/**
	 * 创建类实例调用
	 * @param context：Context对象
	 */
	
	
	
	public DBHelper(Context context) {
		super(context, db_name, null, db_ver);
		// TODO 自动生成的构造函数存根
	}

	//创建数据表
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO 自动生成的方法存根
		//执行创建表
//		db.execSQL(create_order_list_table);
//		db.execSQL(create_fiber_data_table);
//		db.execSQL(create_fiber_back_table);
		this.db = db;
		db.execSQL(Create_Order_List_Table);
		//db.execSQL(Create_Polling_Record);
	}
	
	/**
	 * 判断数据库中，某表是否存在
	 * @param table：Sting，数据表名
	 * @return boolean：true，表存在；false，表不存在
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
                        if(count>0){//表存在
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
	 * 向数据表中插入数据
	 * @param values ：ContentValues对象，包括插入数据的键-值对形式
	 * @param table：String，要插入数据的数据表名称
	 */
	public void insertTb(ContentValues values, String table){
		SQLiteDatabase db = getWritableDatabase();
		db.insert(table, null, values);
	}
	
	/**
	 * 查询数据表所有数据
	 * @param table：要查询数据的数据表名称
	 * @return Cursor：返回数据游标
	 */
	public Cursor queryTb(String table){
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(table, null, null, null, null, null, null);
		return c;		
	}
	
	/**
	 * 清空数据表
	 * @param table：String，要插入数据的数据表名称
	 */
	public void clearTb(String table){
		SQLiteDatabase db = getWritableDatabase();
//		db.delete(table, "1", null);//清空表数据
		String sql = "delete from " + table + ";";//清空表数据
		db.execSQL(sql);
		sql = "update sqlite_sequence set seq=0 where name='"+table+"'";//重新设置自增长
		db.execSQL(sql);
	}
	
	/**
	 * 创建数据表
	 * @param tableSql：String，创建数据表的SQL语句
	 */
	public void createTb(String tableSql){//参数：建表sql语句
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(tableSql);//执行建表语句
	}
	
	/**
	 * 删除数据表
	 * @param table：String，要删除数据的数据表名称
	 */
	public void deleteTb(String table){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + table);//执行删除表语句
	}
	
	/**
	 * 关闭数据库
	 */
	public void close(){
		if(db != null){
		}
	}
	
	/**
	 * 删除数据库
	 * @param context：Context对象实例
	 * @return
	 */
	public boolean deleteDB(Context context){
		return context.deleteDatabase(db_name); 
	}
	
	/**
	 * 当打开数据库时传入的版本号与当前的版本号不同时会调用该方法
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//自动生成的方法存根		
		//		 db.execSQL("DROP TABLE IF EXISTS Warn_Info_Table");
		//         onCreate(db);
	}
	
	public static void Save_Log_inDB(SQLiteDatabase SQL_db, String User_Acct, String Operat_recode, String Operat_Obj) {
		// 保存日志
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
				//应答分析
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
