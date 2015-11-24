package com.smarteco.android;

import java.io.ByteArrayOutputStream;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class DrivingMap extends MapActivity implements SensorEventListener{
	MapView _mapView;
	Context _Context;
	LocationOverlay _MyOverlay;
	MarkerOverlay _markerOverlay;
//	MarkerOverlay _startMarkerOverlay;
//	MarkerOverlay _destMarkerOverlay;
	MapController _mapController;
	Drawable marker = null;
	LocationManager _LocationManager;
	LocationProvider _LocationProvider;
//	ArrayList<MyMovingposition> _Locationinfo;
	SensorManager _sm;
	Sensor _accSensor; // ���ӵ�
	private static final boolean DEVELOPER_MODE = true; 
	private long _lastTime; 
	float _accx;
	float _accy;
	float _accz;
	float _accScala;
	float _beforeaccScala=0;
	float _nowacc;
	float _TotalDistancef;
//	Location _movingLcation;	
//	Canvas _canvas;
	private Facebook _Facebook = new Facebook(C.FACEBOOK_APP_ID);	
	int acclvlCount =0;
//	boolean _destroyFlag =false;
	boolean _startDrive = true;
	Button _facebookPosting;
	Button _drivingButton;
//	TextView _acc;
	TextView _distance;
	TextView _driveStatus;
	String _TotalDistance;
	
	View.OnClickListener bHandler = new View.OnClickListener(){
		public void onClick(View v){
			switch(v.getId()){	
			case R.id.Drive:
				if(_startDrive){		
					_startDrive = false;
					nowlocation();	
					Log.v("test","�������1");
					 confirmAlert();
					 _driveStatus.setText("���������Դϴ�\n���������ϼ���");
					 Log.v("test","���� ����2");
					 _drivingButton.setText("��������");
					break;
				}
				else{
					_mapController.zoomToSpan(LocationOverlay.getLatspan(), LocationOverlay.getLongspan());
					GeoPoint Centerpoint = new GeoPoint(_MyOverlay._Latcenter, _MyOverlay._Longcenter);
					_mapController.setCenter(Centerpoint);
					accMarker();	
					String _TotalDistance = String.format("%.2f", (_MyOverlay._TotalDistance/1000));
					Log.d("test", "testthis  :  "+_TotalDistance);
					_driveStatus.setText("������ ����Ǿ����ϴ�. \n ���ӵ��� Ƚ����"+acclvlCount+"�� \n �̵��Ÿ��� "+_TotalDistance +"km �Դϴ�");
//					_distance.setText("Total : " + _MyOverlay._TotalDistance);
					_drivingButton.setVisibility(View.INVISIBLE);
					_facebookPosting.setVisibility(View.VISIBLE);
					_startDrive = true;
					_MyOverlay.locstate(2);	
					break;			
				}
			case R.id.Facebook:
				 try {
					 screenshot(_mapView);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		
		private void accMarker() {
			// TODO Auto-generated method stub
			for(int i =2 ;  i <LocationOverlay._MyMovingposition.size(); i++){
				MyMovingposition Locinfo =LocationOverlay._MyMovingposition.get(i);
//				MyMovingposition bfLocinfo =LocationOverlay._MyMovingposition.get(i-1);
				float acclvl = Locinfo._getacclvl();
				Location movingLocation = Locinfo._getCurrentLocation();
//				Location bfmovingLocation = bfLocinfo._getCurrentLocation();
//				_TotalDistancef = movingLocation.distanceTo(bfmovingLocation);
				double geoLat = movingLocation.getLatitude()*1E6;
				double geoLong = movingLocation.getLongitude()*1E6;
				if(acclvl > 1)
				{
					GeoPoint checkpoint = new GeoPoint( (int)geoLat , (int)geoLong);
					_markerOverlay.addItem(checkpoint,"","");
					acclvlCount++;
				}
				_TotalDistancef += _TotalDistancef;
			} 
		}
	};

	 private void confirmAlert(){
	    	new AlertDialog.Builder(this)
	    	.setTitle("Ȯ��")
			.setMessage("����� �ڵ����� ��������������. ��Ȯ�� ������ �ȵ˴ϴ�. ")
			.setPositiveButton("Ȯ��",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.dismiss();
					_MyOverlay.locstate(1);
				}
			})
	    	.show();
	    }
	 
	 public void onActivityResult(int requestCode, int resultCode, Intent data)
	  {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(resultCode == RESULT_OK)
	    {
	    	if(requestCode == 32665){
	    		_Facebook.authorizeCallback(requestCode,resultCode, data);
	    	}
	    }
	    else{
	    	if(requestCode == 32665)
	    	{
	    		_Facebook.authorizeCallback(requestCode, resultCode, data);
	    	}
	    }
	  }
	  
	  public void login()
	  {
		  _Facebook.authorize2(this, new String[] {"publish_stream, user_photos, email"}, new AuthorizeListener());
	  }

	  ///////////////////////////////
	  private void feed(Bitmap screenshot)
	  {
	   try{
		   byte[] imgData=null;
		   imgData = bitmapToByteArray(screenshot);
		   Bundle params = new Bundle();
		   String _TotalDistance = String.format("%.2f", (_MyOverlay._TotalDistance/1000));
		   params.putString("message","������Ÿ��� "+_TotalDistance+"km �̸�, �ް��� �ް��� Ƚ���� "+acclvlCount+"�� �Դϴ�");
		   params.putString("picture", "test.jpg");
		   params.putString("link","");
		   params.putString("description", "");
		   params.putByteArray("test.jpg",imgData);	   
		   _Facebook.request("me/photos",params,"POST");
		   Toast.makeText(_Context, "Facebook ������ �Ϸ�!", Toast.LENGTH_SHORT).show();
	   	}
	   catch(Exception e){
		   e.printStackTrace();
		   Toast.makeText(_Context, "Facebook ������ �����Ͽ����ϴ�", Toast.LENGTH_SHORT).show();
	   }
	  }
	  public byte[] bitmapToByteArray(Bitmap bitmap){
		  ByteArrayOutputStream stream = new ByteArrayOutputStream();
		  bitmap.compress(CompressFormat.JPEG,100,stream);
		  byte[] byteArray = stream.toByteArray();
		return byteArray;
	  }
	  public void screenshot(MapView view)throws Exception{
		     view.setDrawingCacheEnabled(true);
		     Bitmap bitmap = view.getDrawingCache();
		    feed(bitmap);

		     view.setDrawingCacheEnabled(false);
		    }
	  public class AuthorizeListener implements DialogListener
	  {
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			if(C.D)Log.v(C.LOG_TAG,"::: onComplete :::");
		}
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub	
		}
		public void onError(DialogError e) {
			// TODO Auto-generated method stub	
		}
		public void onCancel() {
			// TODO Auto-generated method stub	
		}
	  }

    public void onCreate(Bundle savedInstanceState) {
    	if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()

                    .detectDiskReads()

                    .detectDiskWrites()

                    .detectNetwork()

                    .penaltyLog()

                    .build());

        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //  ȭ�� ��� �����ְ��ϱ�
        setContentView(R.layout.activity_drivingmap);
        _Context = this;
        _mapView = (MapView)findViewById(R.id.mapview);
        _distance = (TextView)findViewById(R.id.distanceInfo);
        _driveStatus = (TextView)findViewById(R.id.driveStatus);
        _drivingButton = (Button) findViewById(R.id.Drive);
        _facebookPosting = (Button) findViewById(R.id.Facebook);     
        
        findViewById(R.id.Drive).setOnClickListener(bHandler);
        findViewById(R.id.Facebook).setOnClickListener(bHandler);   
        _sm = (SensorManager)getSystemService(SENSOR_SERVICE); 
        _accSensor = _sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // ���ӵ�
        login();   
        _MyOverlay = new LocationOverlay(_Context, _mapView);
        _mapView.getOverlays().add(_MyOverlay);
        _mapController = _mapView.getController();
        _mapController.setZoom(_mapView.getMaxZoomLevel()-5);
        Drawable marker = getResources().getDrawable(R.drawable.acclvl);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		_markerOverlay = new MarkerOverlay(_Context, marker);
		_mapView.getOverlays().add(_markerOverlay);
		_LocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		if(!_LocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertCheckGPS();
		}	     
		
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);	//��Ȯ��
        criteria.setPowerRequirement(Criteria.POWER_LOW);	//��������
        criteria.setAltitudeRequired(false);	//�ع�
        criteria.setBearingRequired(false);	//��ħ�� ����
        criteria.setSpeedRequired(false);	//�ӵ�
        criteria.setCostAllowed(false);		//���
        
        _LocationProvider = _LocationManager.getProvider(_LocationManager.getBestProvider(criteria, true));
        
    }
 // GPS ����ȭ������ �̵�
    private void moveConfigGPS() {
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }
    
    private void alertCheckGPS() {
		// TODO Auto-generated method stub
    	 AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("GPS�� ����Ͽ� ��ġ���������� �����Ͻðڽ��ϱ�?\n(Google ��ġ ����� ��뿡 üũ)")
                 .setCancelable(false)
                 .setPositiveButton("��",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 moveConfigGPS();
                             }
                     })
                 .setNegativeButton("�ƴϿ�",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                             }
                     });
         AlertDialog alert = builder.create();
         alert.show();
		
	}

	///////////////////////////////////////////
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { 

            long currentTime = System.currentTimeMillis(); 
            long gabOfTime = (currentTime - _lastTime); 

            if (gabOfTime > 1000) { 
            	_lastTime = currentTime;   
            	_accx = event.values[0]; 
                _accy = event.values[1]; 
                _accz = event.values[2];   
 
                _accScala = (float) Math.sqrt(Math.pow(_accx, 2) + Math.pow(_accy, 2) + Math.pow(_accz, 2));
                _nowacc = Math.abs(_accScala - _beforeaccScala);
                
                synchronized (this) {
        			switch (event.sensor.getType()) {
        			case Sensor.TYPE_ACCELEROMETER:
        				_MyOverlay.changeacc(_nowacc);
        				break;		
         			}	                          
        		}
                _beforeaccScala = _accScala; 
                } 
        } 
	}
	private void startLocationService(){
		LocationManager locmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location location = locmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	public void nowlocation(){
		GeoPoint Startpoint = new GeoPoint((int)(_MyOverlay._MyCurrentLocation.getLatitude()*1E6) , (int)(_MyOverlay._MyCurrentLocation.getLongitude()*1E6));
		_mapController.animateTo(Startpoint);
	}

	public void onBackPressed() { 
		// TODO Auto-generated method stub
		Builder _backbutton = new AlertDialog.Builder(this);
		_backbutton.setMessage("�������� �����˴ϴ�. ���� �����Ͻðڽ��ϱ�?");
		_backbutton.setPositiveButton("��", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}); 
		_backbutton.setNegativeButton("�ƴϿ�", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		_backbutton.show();
 	} 
	
	public void onResume()
    {
    	super.onResume();
    	_MyOverlay.enableMyLocation();
    	_sm.registerListener(this, _accSensor, SensorManager.SENSOR_DELAY_NORMAL); // ���ӵ�
    }
    public void onPause()
    {
    	super.onPause();
    	_MyOverlay.disableMyLocation();
    	_sm.unregisterListener(this);
    }
    public void onDestroy()
    {
    	_mapView.getOverlays().remove(_MyOverlay);
		super.onDestroy();	
    }
	protected boolean isRouteDisplayed() 
	{
		return false;	//���� ��Ƽ��Ƽ�� ���� ������ ǥ���ϰ� �ִٸ� �ݵ�� true�� �����ؾ���. �׷��� ������ false ����
	}
	class GPSListener implements LocationListener{
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			
		}
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub	
		}
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub	
		}
		
	}
}
