package com.smarteco.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
  
	Button _drivingMap;
	Button _effiency;
	Intent intent;

	View.OnClickListener bHandler = new View.OnClickListener(){
		public void onClick(View v){
			switch(v.getId()){
			// 주행 액티비티 호출
			case R.id.drivingmap:
				intent = new Intent(MainActivity.this, DrivingMap.class);
				startActivity(intent);	
				break;
			// 연비 계산 액티비티 호출
			case R.id.effiency:
				intent = new Intent(MainActivity.this, EfficiencyActivity.class);
				intent.putExtra("delDB", false);
				startActivity(intent);	
				break;
			}
		}
	};
	public boolean onCreateOptionsMenu(Menu menu) {

		    boolean result = super.onCreateOptionsMenu(menu);
		    MenuInflater menuInflator = new MenuInflater(this);

		    menuInflator.inflate(R.menu.menu, menu);
		    return result;

		}

	public void CallIntent(Object Activity){
		Intent intent = new Intent(MainActivity.this, Activity.class);
		startActivity(intent);		
		
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		           switch (item.getItemId()) {
		           case R.id.delDB:
		        	   intent = new Intent(MainActivity.this, EfficiencyActivity.class);
		        	   intent.putExtra("delDB", true);
		        	   startActivity(intent);
		               return true;
		           case R.id.introduce:
		        	   intent = new Intent(MainActivity.this, TutorialActivity.class);
		        	   startActivity(intent);
		               return true;
		           }
		           return (super.onOptionsItemSelected(item));
		   }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.drivingmap).setOnClickListener(bHandler);
        findViewById(R.id.effiency).setOnClickListener(bHandler);
    }
}