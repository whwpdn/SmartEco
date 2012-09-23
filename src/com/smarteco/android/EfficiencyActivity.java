package com.smarteco.android;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class EfficiencyActivity extends Activity {
    /** Called when the activity is first created. */
	// 위젯 관련
	Button btnCalc; 
	EditText editDistance, editOil;
	TextView tvavgefficiency, tvtotaldistance, tvtotaloil;
	ListView list;
	DatePicker datepick;
	// 아이템
	ArrayList<ListItem> data = null;
    ListAdapter adapter = null;
    // 임시 값..
    private int _index;
    
    //DB관련
    SQLiteDatabase m_db;
    String strSQL;
    Cursor m_cursor;
    boolean dbFlag = false;

    
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effiency);        

        list = (ListView)findViewById(R.id.listView1);
        data = new ArrayList<ListItem>();
      
        findViewById(R.id.bt_cal).setOnClickListener(bHandler);
   
        createTable();// db table 생성
        getData();//db data 불러오기
        adapter = new ListAdapter(this, R.layout.item, data);	// 어댑터에  불러온 data 를 listview에 삽입준비..
        list.setAdapter(adapter);	// list view 보여주기.. adapter에 있는 data를 listview 에 출력 
       
        Intent intent = getIntent();
        dbFlag = intent.getBooleanExtra("delDB",true);
        if(dbFlag){
        	confirmAlert();
        }
        // listview의 item을 오래 터치시 발생 event 처리.
        list.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
            	//다이얼로그를 띄워준다.
            	alert(index);
                return true;
            }
        });
       }
    ///   methods ~~~~ /////////////
       
	void saveData(){
		ListItem temp_item=null;
		if(data.size()!=0){
			temp_item = data.get(data.size()-1);
		}
	  //context 에 현재 EfficiencyActivity 를 할당..
		Context mContext = com.smarteco.android.EfficiencyActivity.this;
		// 다이얼로그 창을 띄우기 위한 객체
		AlertDialog.Builder builder;
		AlertDialog dialog;
		// 
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout=inflater.inflate(R.layout.customdialog,(ViewGroup)findViewById(R.id.customdialog_layout));
		
		builder = new AlertDialog.Builder(mContext);
		
		editDistance = (EditText)layout.findViewById(R.id.ed_distance);
		editOil = (EditText)layout.findViewById(R.id.ed_oil);
		datepick = (DatePicker)layout.findViewById(R.id.datePicker);
		if(data.size()!=0){
			editDistance.setHint("이전 기록 : "+Integer.toString(temp_item.getTotaldistance())+" Km");
			editOil.setHint("이전 기록 : "+Integer.toString(temp_item.getOil())+" L");
		}
		builder.setView(layout);
		
		builder.setTitle("주유 정보를 입력하세요");
		
		// 다이얼로그 창 띄우기..
		/*
		 * 입력 버튼을 만들고 
		 * editText가 값이 없으면 예외처리...
		 * 값이 있으면 DB에 값 저장
		 */
		builder.setPositiveButton("입력",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				String str1 = editDistance.getText().toString();
				String str2 = editOil.getText().toString();
				int distance=0;
				if((str1.length() == 0 || str2.length() == 0)){
					err_alert();
				}
				else{
					// date 정보를 문자열로 저장 . 년 월 일
					// getMonth() 는  0~11 월, + 1 해줘야 한다.
					String strDate =datepick.getYear()+"-"+(datepick.getMonth()+1)+"-"+datepick.getDayOfMonth();
					
					// DB 를 연다..
					createTable();
					m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
					 
					m_cursor.moveToLast();
					if(m_cursor.getPosition()==-1){
						distance = 0;
					}
					else{
						distance = Integer.parseInt(editDistance.getText().toString())-m_cursor.getInt(4);	
					}

					insert(strDate,distance,Integer.parseInt(editOil.getText().toString()),Integer.parseInt(editDistance.getText().toString()));
					getLastData();
					list.setAdapter(adapter);
				}
			}
		});
		// 다이얼로그 취소버튼 세팅
		builder.setNegativeButton("취소",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int which){
				dialog.dismiss();
			}
		});
		// 다이얼로그 만들고.. show
		dialog=builder.create();
		dialog.show();
	}
	
	View.OnClickListener bHandler = new View.OnClickListener(){
		public void onClick(View v){
			switch(v.getId()){
			// 입력 버튼
			case R.id.bt_cal:
				saveData();
				break;
			}
		}
	};
    
	
	
	
    public void alert(final int index){
    	 
    	new AlertDialog.Builder(this)
    	.setItems(C.items,new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int item){
     		switch(item){
     		case 0:// 수정
     			fix_data(index);
     			break;
     		case 1:// 삭제
     			deleteData(index);
     			break;
     			}
    		}
    	})
    	.show();
    }
    public void err_alert(){
   	 
    	new AlertDialog.Builder(this)
    	.setTitle("입력 오류")
		.setMessage("숫자만 입력하세요")
    	.show();
    }
    
    public void confirmAlert(){
    	new AlertDialog.Builder(this)
    	.setTitle("확인")
		.setMessage("데이터베이스를 초기화 하시겠습니까 ?(복구 할 수 없음)")
		.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				createTable();
				dropTable();
				dialog.dismiss();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				// TODO Auto-generated method stub
				
			}
		})
    	.show();
    }

    // DB table 생성
    public void createTable(){
    	
    	m_db = openOrCreateDatabase("efficiency.db",Context.MODE_PRIVATE,null);
       	if(!m_db.isOpen()){
    		Log.v("SQLite","openOrCreate Fail");
    		return;
    	}
    	try{
    		strSQL = 
    				"CREATE TABLE IF NOT EXISTS efficiency"+
    				"("+
					"	_id	INTEGER	"		+
    				"	, _date TEXT"		+
    				"	, _distance INTEGER"	+
    				"	, _oil INTEGER"		+
    				"	, t_distance INTEGER"+
    				");";
 
    		m_db.execSQL(strSQL);
    		
    		Log.i("SQLite","Create Table ... OK");
    	}
    	catch(SQLException e){
    	}
    	finally{
    		//m_db.close();
    	
    	}
    	
    }
    
   //DB 삽입
   public void insert(String strDate,int distance,int edOil,int edtDistance){
	   m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
	   m_cursor.moveToLast();
	   Log.d("test",Integer.toString(m_cursor.getPosition()));
	   strSQL=
			"INSERT INTO efficiency ( _id, _date, _distance, _oil, t_distance)"+
			" VALUES ( '"+	(m_cursor.getPosition()+1)	+	"','"	+
							strDate		+	"','"	+
							distance	+	"','"	+
							edOil		+	"','"	+
							edtDistance	+	"'"		+
			");";
	   m_db.execSQL(strSQL);  
	   m_cursor.close();
	   Log.i("SQLite","Insert data ... OK");
	  
   }
   // DB로 부터 데이터 가져오기.
   public void getData(){
	   
	   m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
	   
	   if(m_cursor !=null){
		
		   if(m_cursor.moveToFirst()){
			   do{
					   data.add(new ListItem(m_cursor.getString(1),		// date
							   m_cursor.getInt(2),						// distance
							   m_cursor.getInt(3),						// oil
							   m_cursor.getInt(4)						// total distance
							   ));
					   
					   Log.v("TESTd",Integer.toString(m_cursor.getInt(0)));
			   }while(m_cursor.moveToNext());
		   }
	   }
	   m_cursor.close();
	   m_db.close();
   }
	public void getLastData(){
		   m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
		   if(m_cursor !=null){
			   if(m_cursor.moveToLast()){
				   do{
						   data.add(new ListItem(m_cursor.getString(1),		// date
								   m_cursor.getInt(2),						// distance
								   m_cursor.getInt(3),						// oil
								   m_cursor.getInt(4)));					// total distance
				   }while(m_cursor.moveToNext());
			   }
		   }
		   m_cursor.close();
		   m_db.close();
	}
	
	
	public void deleteData(int index){
		int _index = index;
		createTable();
		int fix_distance=0;
		m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
		m_cursor.moveToPosition(index);
			
		if(m_cursor.isLast()){
			
		}
		else if(m_cursor.isFirst()){
			fix_distance =0;
			m_cursor.moveToPosition(index+1);
			strSQL="UPDATE efficiency SET _distance="+fix_distance+" WHERE _id="+(index+1)+";";
			m_db.execSQL(strSQL);
			data.set(index+1, new ListItem(m_cursor.getString(1),fix_distance,m_cursor.getInt(3),m_cursor.getInt(4)));
		}
		else{
			index = index+1;
			m_cursor.moveToPosition(index);
			fix_distance = m_cursor.getInt(4);
			index = index-2;
			m_cursor.moveToPosition(index);
			fix_distance = fix_distance-m_cursor.getInt(4);
			m_cursor.moveToPosition(index+2);
			strSQL="UPDATE efficiency SET _distance="+fix_distance+" WHERE _id="+(index+2)+";";
			m_db.execSQL(strSQL);
			Log.v("dfef",Integer.toString(fix_distance));
			data.set(index+2, new ListItem(m_cursor.getString(1),fix_distance,m_cursor.getInt(3),m_cursor.getInt(4)));
			index++;
			
		}
		
		try{
			strSQL="DELETE FROM efficiency WHERE _id="+index+";";
			 m_db.execSQL(strSQL);
			 data.remove(index);
		}
		catch(SQLException e){
			Log.v("error", e.toString());
		}
		
		m_cursor.moveToPosition(index);
		//strSQL="UPDATE efficiency SET _distance="+fix_distance+" WHERE _id="+(index+1)+";";
		//m_db.execSQL(strSQL);
		
		 do{
			 if(m_cursor.isLast()){
				 Log.v("warring","empty");
			 }
			 else{
				 index++;
				 strSQL="UPDATE efficiency SET _id="+(index-1)+" WHERE _id="+index+";";
				 m_db.execSQL(strSQL);
			 }
		 }while(m_cursor.moveToNext());
		 list.invalidate();
		 adapter.notifyDataSetChanged();
		  m_db.close();
		  m_cursor.close();
		 Toast.makeText(this,"Data가 삭제 되었습니다.",Toast.LENGTH_SHORT).show();   
	}
	
	public void fix_data(int index){
			_index = index;
			//context 에 현재 EfficiencyActivity 를 할당..
			ListItem temp_item = data.get(index);
			Context mContext = com.smarteco.android.EfficiencyActivity.this;
			// 다이얼로그 창을 띄우기 위한 객체
			AlertDialog.Builder builder;
			AlertDialog dialog;
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout=inflater.inflate(R.layout.fixcustomdialog,(ViewGroup)findViewById(R.id.customdialog_layout));
			
			builder = new AlertDialog.Builder(mContext);
			
			editDistance = (EditText)layout.findViewById(R.id.ed_distance);
			editOil = (EditText)layout.findViewById(R.id.ed_oil);			
			builder.setView(layout);
			
			builder.setTitle("주유 정보를 입력하세요");
			
			editDistance.setText(Integer.toString(temp_item.getTotaldistance()));
			editOil.setText(Integer.toString(temp_item.getOil()));
			
			// 다이얼로그 창 띄우기..
			/*
			 * 입력 버튼을 만들고 
			 * editText가 값이 없으면 예외처리...
			 * 값이 있으면 DB에 값 저장
			 */
			builder.setPositiveButton("수정",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					
					String str1 = editDistance.getText().toString();
					String str2 = editOil.getText().toString();
					int distance=0;
					if((str1.length() == 0 || str2.length() == 0)){
						err_alert();
					}
					else{
						// DB 를 연다..
						createTable();
						m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
						m_cursor.moveToPosition(_index);
						if(m_cursor.isFirst()){
							distance = 0;
						}
						else{
							m_cursor.moveToPosition(_index-1);
							distance = Integer.parseInt(editDistance.getText().toString())-m_cursor.getInt(4);	
						}
						strSQL="UPDATE efficiency SET " +
								"_oil="+Integer.parseInt(editOil.getText().toString())+
								", _distance="+distance+
								", t_distance="+Integer.parseInt(editDistance.getText().toString())+
								" WHERE _id="+_index+";";
						m_db.execSQL(strSQL);
						m_cursor.close();

						m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
						m_cursor.moveToPosition(_index);
						data.set(_index, new ListItem(m_cursor.getString(1), m_cursor.getInt(2), m_cursor.getInt(3), m_cursor.getInt(4)));
						m_cursor.moveToPosition(_index);
						if(!m_cursor.isLast()){
							m_cursor.moveToPosition(_index+1);
							
							distance=m_cursor.getInt(4)-Integer.parseInt(editDistance.getText().toString());
							strSQL="UPDATE efficiency SET _distance="+distance+
									" WHERE _id="+(_index+1)+";";
							m_db.execSQL(strSQL);
							m_cursor.close();
							m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
							m_cursor.moveToPosition(_index+1);
							data.set(_index+1, new ListItem(m_cursor.getString(1), m_cursor.getInt(2), m_cursor.getInt(3), m_cursor.getInt(4)));							
						}
						 list.invalidate();
						 adapter.notifyDataSetChanged();
						m_cursor.close();
						m_db.close();
						
					}
				}
			});
			// 다이얼로그 취소버튼 세팅
			builder.setNegativeButton("취소",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog,int which){
					dialog.dismiss();
				}
			});
			// 다이얼로그 만들고.. show
			dialog=builder.create();
			dialog.show();
			//m_db.close();	
	}
	public boolean onCreateOptionsMenu(Menu menu) {

	    boolean result = super.onCreateOptionsMenu(menu);
	    MenuInflater menuInflator = new MenuInflater(this);

	    menuInflator.inflate(R.menu.effmenu, menu);
	    return result;

	}
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.avgeffi:
        	String oilString = null;
        	int _totalDistance =0;
        	int _totalOil =0;
        	
        	if(data.size()==0){
        	        	}
        	else{
        	     	 
        	for(int i=1;i<data.size();i++){
     		  _totalOil += data.get(i).getOil();
        	}
	        	_totalDistance = data.get(data.size()-1).getTotaldistance()-data.get(0).getTotaldistance();
	        	double oilEffection = (double) _totalDistance / (double) _totalOil;
	    		String pattern = "###.#";
	    		DecimalFormat dformat = new DecimalFormat(pattern);
	    		oilString = dformat.format(oilEffection);
        	}       	
        	
        	Context mContext = com.smarteco.android.EfficiencyActivity.this;
			// 다이얼로그 창을 띄우기 위한 객체
			AlertDialog.Builder builder;
			AlertDialog dialog;
					
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout=inflater.inflate(R.layout.avgcustomdialog,(ViewGroup)findViewById(R.id.avgcustomdialog_layout));
			builder = new AlertDialog.Builder(mContext);
			tvavgefficiency = (TextView)layout.findViewById(R.id._avgeffi);
			tvtotaloil = (TextView)layout.findViewById(R.id._totaloil);
			tvtotaldistance = (TextView)layout.findViewById(R.id._totaldistance);
			builder.setView(layout);
			builder.setTitle("평균 연비");
			
			tvavgefficiency.setText(oilString+" Km/L");
			tvtotaloil.setText(Integer.toString(_totalOil)+" L");
			tvtotaldistance.setText(Integer.toString(_totalDistance)+" Km");
			
			// 다이얼로그 창 띄우기..
			/*
			 * 입력 버튼을 만들고 
			 * editText가 값이 없으면 예외처리...
			 * 값이 있으면 DB에 값 저장
			 */
			builder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.dismiss();
					}
				});
			// 다이얼로그 만들고.. show
			dialog=builder.create();
			dialog.show();
            return true;
        }
        return (super.onOptionsItemSelected(item));
	}
	
	/// 설정에서 DB 초기화..
	public void dropTable(){
		createTable();
		strSQL="drop table efficiency";
		try{
		 m_db.execSQL(strSQL);
		 data.clear();
		 
		 list.invalidate();
		 adapter.notifyDataSetChanged(); 
		 m_db.close();
		}
		catch(SQLException e){
			Log.v("error", e.toString());
		}
	}
}