package com.example.iods_manage;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.example.iods_common.DBHelper;
import com.example.iods_lock_app.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;





//�б���ʾ��
public class Browse_Log_Activity extends Activity {
	//��־��ѯҳ��
	
	//�����ؼ�����
	private ListView workListView;
	private SimpleAdapter listAdapter;//�����б�������
	private List<HashMap<String,String>> workList;//�����б�����Դ

	private String selectedType;//spinner1ѡ����

	Context context=Browse_Log_Activity.this;
	DBHelper dbHelper;
	
	private EditText Bef_Day_1, Bef_Day_2;
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_log);
        setTitle("��־��ѯ");
        
        dbHelper = new DBHelper(context);//��ʼ�����ݿ����    
        	 	   
		//�ؼ���ʼ��
		initi();
		
    }
    public void initi(){
    	
    	Bef_Day_1 = (EditText)findViewById(R.id.bef_Day_Max);
    	Bef_Day_2 = (EditText)findViewById(R.id.bef_Day_Min);	
    	    	
    	
		Button Browse_Again_BTN = (Button)findViewById(R.id.button_Browse);

		Browse_Again_BTN.setOnClickListener(new ClickEvent());
		
	}
    
   
   //��־�б�ˢ��
    
	  	private void updatalist(String str_Start, String str_End) {
			// TODO �Զ����ɵķ������
	  		String Str_user, Str_datetime, Str_Operation, Str_Obj;
	//  		Cursor cursor1 =dbHelper.queryTb(DBHelper.Table_WorkList);//��ѯ���ݱ� 
	  		SQLiteDatabase db=dbHelper.getWritableDatabase();	
	  		Cursor cursor1;
	  		if(selectedType.equals("0")){
//	  			cursor1=dbHelper.queryTb(DBHelper.Table_WorkList);//��ѯ���ݱ� 
	  			 
	  			cursor1=db.rawQuery("select * from Table_Log_Record order by User_Account DESC, Date_Time DESC", new String[]{});    	
	  		}else{	
		  		cursor1=db.rawQuery("select * from Table_Log_Record where Date_Time > ? and Date_Time < ? order by User_Account DESC, Date_Time DESC", new String[]{str_Start, str_End});    	
	  		}//�б���ʾ����
		    	workList=new ArrayList<HashMap<String,String >>();
			    	while(cursor1.moveToNext()) {
			    		Str_user = cursor1.getString(1);
			    		Str_datetime = cursor1.getString(2);
			    		Str_Operation = cursor1.getString(3);
			    		Str_Obj = cursor1.getString(4);
			    					    		
			    		if (Str_user == null) {
			    			Str_user = "";
			    		}
			    		if (Str_datetime == null) {
			    			Str_datetime  = "";
			    		}
			    		if (Str_Operation == null) {
			    			Str_Operation  = "";
			    		}
			    		if (Str_Obj == null) {
			    			Str_Obj  = "";
			    		}
			    		
				    	HashMap<String,String > map=new HashMap<String,String>();
				    	map.put("_id",cursor1.getString(0));
				    	
				    	map.put("WorkID", Str_datetime);
				    	map.put("DateLine", Str_Operation);
		    			map.put("UserID", Str_Obj);
		    			map.put("WorkStatus",Str_user);
				    	
				    	workList.add(map);
			    	}
			    	
			    	String[] from=new String[]{"_id","WorkID","DateLine","UserID","WorkStatus"};
			    	int[] to=new int[]{R.id.order,R.id.workID,R.id.finshTime,R.id.userID,R.id.orderStatus};
			    	workListView=(ListView)findViewById(R.id.worklistView);
			    	listAdapter=new SimpleAdapter(context,workList,R.layout.log_list_item,from, to);
			    	workListView.setAdapter(listAdapter);
			    	listAdapter.notifyDataSetChanged();
			    				    	
			    	cursor1.close();
			    	db.close();
			    		    			
		}
		
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.work_deal, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	
	@Override
	protected void onResume() {
		// TODO �Զ����ɵķ������
		super.onResume();
		selectedType="0";
		updatalist("", "");

	}
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
	
		dbHelper.close();
	}
	   

	
	//�Զ��嵥���¼���
		class ClickEvent implements View.OnClickListener {    

			long Browse_Start, Browse_End;
			String this_Time, Start_time, End_time;
			Date date1, date2;
			double dayCount;

			public void onClick(View v) {

		    	   switch(v.getId()){
		    	   case R.id.button_Browse:
		    		   Browse_Start = Long.valueOf(Bef_Day_1.getText().toString());
		    		   Browse_End = Long.valueOf(Bef_Day_2.getText().toString());
		    		   
		    		   SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    		   this_Time = sDateFormat.format(new java.util.Date());
		    		   
		    		   Date now=new Date();
		    		   long This_DaysMili = now.getTime();
		    		   long twoDaysAgoMili=This_DaysMili-24*1000*60*60 * Browse_End;
		    		   Date twodaysago=new Date((long) twoDaysAgoMili);
		    		   End_time = sDateFormat.format(twodaysago);

		    		   twoDaysAgoMili=This_DaysMili-24*1000*60*60 * Browse_Start;
		    		   twodaysago=new Date((long) twoDaysAgoMili);
		    		   Start_time = sDateFormat.format(twodaysago);
		    		   
		    		   selectedType="1";
		    		   updatalist(Start_time, End_time);
		    		   
		    		   break;
		    		
		    	   }
			}
		}
	
		
	
	
}
