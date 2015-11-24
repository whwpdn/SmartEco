package com.smarteco.android;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
//import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

class LocationOverlay extends MyLocationOverlay
{
	static final String TAG = "MyOverlay";
	
	Location _MyCurrentLocation;
	
	Path _Path;
	Paint _Paint;
	static ArrayList<MyMovingposition> _MyMovingposition;
	Context _Context;	
	MapView _MapView;
	float _nowDistance;
	float _TotalDistance;
	float _TempDistance=0;
	float _acclvl=1;
	int _mylocstate=0;
	float _maxLongitude;
	float _maxLatitude;
	float _minLongitude;
	float _minLatitude;
	private static int _Latspan;
	private static int _Longspan;
	int _Latcenter;
	int _Longcenter;
	int _index =0;
	float _checkDistance;
	Location _tempLocation;
	DrivingMap _drivingMap;
	
	public LocationOverlay(Context context, MapView mapView) 
	{
		super(context, mapView);
		_Path = new Path();
		_Path.reset();
		_Paint = new Paint();
		_Paint.setAntiAlias(true);
		_Paint.setDither(true);
		_Paint.setColor(Color.GREEN);
		_Paint.setStyle(Paint.Style.STROKE);
		_Paint.setStrokeJoin(Paint.Join.ROUND);
		_Paint.setStrokeCap(Paint.Cap.ROUND);
		_Paint.setStrokeWidth(10);
		_Paint.setStyle(Paint.Style.FILL);
    	_MyMovingposition = new ArrayList<MyMovingposition>();
    	_Context = context;
    	_MapView = mapView;		
	}
	
	public void onLocationChanged(Location location)
	{
		super.onLocationChanged(location);		
		_MyCurrentLocation = location;
	
		if(_mylocstate != 0  && _MyCurrentLocation != null){
			if(_index>0){
				MyMovingposition before = _MyMovingposition.get(_index-1);
				_checkDistance = before._CurrentLocation.distanceTo(_MyCurrentLocation);
				Log.d("test", "_index"+_index);
				Log.d("test", "거리값"+before._CurrentLocation);
				Log.d("test", "거리값"+_MyCurrentLocation);
				Log.d("test", "거리값"+_checkDistance);
			
				if(_checkDistance < 30){
					Log.d("test", "check저장"+_checkDistance);
					addLocation(_MyCurrentLocation,_checkDistance);
					Log.d("test", "30미터이하 좌표저장");
				}
				else{
					if(_tempLocation.distanceTo(_MyCurrentLocation)<30)
					{
						Log.d("test", "temp저장"+_tempLocation.distanceTo(_MyCurrentLocation));
						addLocation(_MyCurrentLocation,_tempLocation.distanceTo(_MyCurrentLocation));
					}
				}
				Log.d("test", "현재좌표 임시저장"+_tempLocation);
			}
			else{
				Log.d("test", "첫인덱스");
				addLocation(_MyCurrentLocation,0);
			}
			_tempLocation = _MyCurrentLocation;
			zoomlevel(_MyCurrentLocation);
		}
		else 
			return ;
	}	
	public void addLocation(Location _MyLocation, float _distance){
		_MyMovingposition.add(new MyMovingposition(_MyLocation, _acclvl));
		_index++;
	}

	
	public void zoomlevel(Location currentLocation){
		_maxLatitude = (float) Math.max(currentLocation.getLatitude()*1E6, _maxLatitude);
		_maxLongitude = (float) Math.max(currentLocation.getLongitude()*1E6, _maxLongitude);
		if(_minLatitude == 0){
			_minLatitude = (float) (currentLocation.getLatitude()*1E6);
		}
		if(_minLongitude == 0){
			_minLongitude = (float) (currentLocation.getLongitude()*1E6);
		}
		_minLatitude = (float) Math.min(currentLocation.getLatitude()*1E6, _minLatitude);
		_minLongitude = (float) Math.min(currentLocation.getLongitude()*1E6, _minLongitude);
		_Latcenter =(int)(_maxLatitude+_minLatitude)/2;
		_Longcenter =(int)(_maxLongitude+_minLongitude)/2;
		setLatspan((int) (_maxLatitude-_minLatitude));
		setLongspan((int) (_maxLongitude-_minLongitude));
		
		
	}
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,long when)
	{
		if( _MyMovingposition != null)
		{
			_Path.reset();
			_movingLocationDraw();
			_Paint.setStyle(Paint.Style.STROKE);
			canvas.drawPath(_Path, _Paint);
			_Paint.setStyle(Paint.Style.FILL);
		}		
		return super.draw(canvas, mapView, shadow,when);
	}
	
	public void changeacc (float i) {
		// TODO Auto-generated method stub
		if(i < 1){
			_acclvl = 1;
		}
		else
			_acclvl =i;
	} 

	public void _movingLocationDraw()
	{	
		Point _beforePoint = new Point();
		Point _currentPoint = new Point();
		Point _tempPoint = new Point();
		Path _path = new Path();
		_path.reset();		
		int check=0;
		_Paint.setColor(Color.GREEN);
		for(int i = 2 ; i < _MyMovingposition.size(); i++){
			MyMovingposition current = _MyMovingposition.get(i);
			MyMovingposition before = _MyMovingposition.get(i-1);
			_MapView.getProjection().toPixels(new GeoPoint((int)(before._CurrentLocation.getLatitude()*1E6), (int)(before._CurrentLocation.getLongitude()*1E6)), _beforePoint);
			_MapView.getProjection().toPixels(new GeoPoint((int)(current._CurrentLocation.getLatitude()*1E6), (int)(current._CurrentLocation.getLongitude()*1E6)), _currentPoint);
			_nowDistance = current._CurrentLocation.distanceTo(before._CurrentLocation);
			if(_nowDistance > 30){
				if(check==1){
					_beforePoint = _tempPoint;
					check=0;
				}
				else {
					_tempPoint = _beforePoint;
					check++;
				}
				continue;
			}
			else {
//				Log.d("test","now거리"+_nowDistance);
				_path.moveTo(_beforePoint.x, _beforePoint.y);	
				_path.lineTo(_currentPoint.x, _currentPoint.y);
				_TempDistance += _nowDistance;
			}
//			Log.d("test","temp거리"+_TempDistance);
			
		}
		_TotalDistance = _TempDistance;
//		Log.d("test","total거리"+_TotalDistance);
		_nowDistance =0;
		_TempDistance=0;
//		Log.d("test","temp거리"+_TempDistance);
		_Path.addPath(_path);	
		
	}

	public void locstate(int locstate)
	{
		switch(locstate){
		case 1: // 주행시작
			_mylocstate = 1;
			break;
		case 2: // 주행종료
			_mylocstate = 0;
			break;
		}
	}

	public static int getLatspan() {
		return _Latspan;
	}

	public static void setLatspan(int latspan) {
		_Latspan = latspan;
	}

	static int getLongspan() {
		return _Longspan;
	}

	public static void setLongspan(int longspan) {
		_Longspan = longspan;
	}
}



class MyMovingposition{
	public float _acclvl;
	public Location _CurrentLocation;
	public MyMovingposition(){
	
	}
	public MyMovingposition(Location _MyCurrentLocation, float _Myacclvl) {
		_CurrentLocation =_MyCurrentLocation;
		_acclvl = _Myacclvl;
		// TODO Auto-generated constructor stub
	}

	public float _getacclvl() {
		return this._acclvl;
		// TODO Auto-generated method stub
		
	}
	public Location _getCurrentLocation() {
		return this._CurrentLocation;
		// TODO Auto-generated method stub
		
	}
}