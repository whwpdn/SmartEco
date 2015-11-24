package com.smarteco.android;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.ViewFlipper;

public class TutorialActivity extends Activity {
	ViewFlipper flipper1;
	ViewFlipper flipper2;
	TabHost mTabHost;
/** Called when the activity is first created. */
@Override

public void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_tutorial);

	
	flipper1 = (ViewFlipper)findViewById(R.id.ViewFlipper1);
	flipper2 = (ViewFlipper)findViewById(R.id.ViewFlipper2);
	//Resources res = getResources(); 
	
	Button prevbt = (Button)findViewById(R.id.prev);
	Button nextbt = (Button)findViewById(R.id.next);
	
	ImageView imgd1 = (ImageView)findViewById(R.id.imgdriving1);
	ImageView imgd2 = (ImageView)findViewById(R.id.imgdriving2);
	ImageView imgd3 = (ImageView)findViewById(R.id.imgdriving3);
	
	
	imgd1.setImageResource(R.drawable.imgdriving1);
	imgd2.setImageResource(R.drawable.imgdriving2);
	imgd3.setImageResource(R.drawable.imgdriving3);
	ImageView imge1 = (ImageView)findViewById(R.id.imgeffi1);
	ImageView imge2 = (ImageView)findViewById(R.id.imgeffi2);
	ImageView imge3 = (ImageView)findViewById(R.id.imgeffi3);
	ImageView imge4 = (ImageView)findViewById(R.id.imgeffi4);
	ImageView imge5 = (ImageView)findViewById(R.id.imgeffi5);
	
	imge1.setImageResource(R.drawable.imgeffi1);
	imge2.setImageResource(R.drawable.imgeffi2);
	imge3.setImageResource(R.drawable.imgeffi3);
	imge4.setImageResource(R.drawable.imgeffi4);
	imge5.setImageResource(R.drawable.imgeffi5);
	
	mTabHost = (TabHost)findViewById(R.id.tabHost);

	mTabHost.setup();
	
	mTabHost.addTab(mTabHost.newTabSpec("driving").setIndicator("driving").setContent(R.id.ViewFlipper1));
	mTabHost.addTab(mTabHost.newTabSpec("efficiency").setIndicator("efficiency").setContent(R.id.ViewFlipper2));
	
	prevbt.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	switch(mTabHost.getCurrentTab()){
        	case 0:
        		flipper1.showPrevious();
        		return;
        	case 1:
        		flipper2.showPrevious();
        		return;
        	}
        	
        }
     });

     nextbt.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	switch(mTabHost.getCurrentTab()){
        	case 0:
        		flipper1.showNext();
        		return;
        	case 1:
        		flipper2.showNext();
        		return;
        	}
        }
     });
	mTabHost.setCurrentTab(0);

}

 

}
