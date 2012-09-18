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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

class LocationOverlay extends MyLocationOverlay
{
	static final String TAG = "MyOverlay";
	Location mMyCurrentLocation;
	Path mPath;
	Paint mPaint;
	ArrayList<MyMovingposition> _MyMovingposition;
	Context mContext;	
	MapView mMapView;
	float mMyDistance;
	float TotalDistance=0;
	float Lvl3Distance;
	float _acclvl=1;
	int _drawstate=1;
	int _mylocstate=1;
	
	
	public LocationOverlay(Context context, MapView mapView) 
	{
		super(context, mapView);
		mPath = new Path();
		mPath.reset();
		mPaint = new Paint();
    	mPaint.setAntiAlias(true);
    	mPaint.setDither(true);
    	mPaint.setColor(Color.GREEN);
    	mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeJoin(Paint.Join.ROUND);
    	mPaint.setStrokeCap(Paint.Cap.ROUND);
    	mPaint.setStrokeWidth(10);
    	mPaint.setStyle(Paint.Style.FILL);
    	_MyMovingposition = new ArrayList<MyMovingposition>();
    	mContext = context;
    	mMapView = mapView;		
	}
	
	public void onLocationChanged(Location location)
	{
		super.onLocationChanged(location);		
		mMyCurrentLocation = location;
	
		if(_mylocstate != 0  && mMyCurrentLocation != null)
		{		
			Log.v("test", "좌표수신");
			_MyMovingposition.add(new MyMovingposition(mMyCurrentLocation, _acclvl));

			Log.v("test", "좌표저장"+_acclvl);
		}
		else 
			return ;
	}
	
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,long when)
	{
		int _cycle;		
		if(_drawstate == 1){
			if( _MyMovingposition != null)
			{
				for(_cycle = 1; _cycle<4; _cycle++){
					mPath.reset();

					updatePath(_cycle);
					mPaint.setStyle(Paint.Style.STROKE);
					canvas.drawPath(mPath, mPaint);
					mPaint.setStyle(Paint.Style.FILL);
				}
				_cycle = 1;
				TotalDistance =0;
			}		
		}
//		_drawstate = 0;
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

	public void updatePath(int _cycle)
	{	
		Point beforePoint = new Point();
		Point currentPoint = new Point();
		Point tempPoint = new Point();
		int check=0;
		Path p = new Path();
		p.reset();		

		switch(_cycle){		
		case 1:
			mPaint.setColor(Color.GREEN);
			for(int i = 1 ; i < _MyMovingposition.size(); i++){
				MyMovingposition current = _MyMovingposition.get(i);
				MyMovingposition before = _MyMovingposition.get(i-1);
				mMapView.getProjection().toPixels(new GeoPoint((int)(before._CurrentLocation.getLatitude()*1E6), (int)(before._CurrentLocation.getLongitude()*1E6)), beforePoint);
				mMapView.getProjection().toPixels(new GeoPoint((int)(current._CurrentLocation.getLatitude()*1E6), (int)(current._CurrentLocation.getLongitude()*1E6)), currentPoint);
				mMyDistance = current._CurrentLocation.distanceTo(before._CurrentLocation);
//				Log.v("test", "그리기거리"+mMyDistance);
				if(mMyDistance > 30){
//					Log.v("test", "30미터이상");
					if(check==1){
						beforePoint = tempPoint;
						check=0;
					}
					else {
						tempPoint = beforePoint;
						check++;
					}
					continue;
				}
				else {
//					Log.v("test", "30미터이하 그리기");
					p.moveTo(beforePoint.x, beforePoint.y);	
					Log.v("test", "과속"+current._acclvl);
					p.lineTo(currentPoint.x, currentPoint.y);
					TotalDistance = mMyDistance+TotalDistance;			
//					Log.d("test11", "total"+TotalDistance);
					
				}
				mPath.addPath(p);	
			}
			break;
			
		case 2:
			
			mPaint.setColor(Color.RED);
			Log.v("test", "과속그리기테스트");
			for(int i = 1 ; i < _MyMovingposition.size(); i++){
				MyMovingposition current = _MyMovingposition.get(i);
				MyMovingposition before = _MyMovingposition.get(i-1);
				mMapView.getProjection().toPixels(new GeoPoint((int)(current._CurrentLocation.getLatitude()*1E6), (int)(current._CurrentLocation.getLongitude()*1E6)), currentPoint);
				mMyDistance = current._CurrentLocation.distanceTo(before._CurrentLocation);
				Log.v("test", "과속"+current._acclvl);
					if(current._acclvl != 1){
						p.moveTo(currentPoint.x, currentPoint.y);
						p.addCircle(currentPoint.x, currentPoint.y, current._acclvl*10,Path.Direction.CW);
					}
				mPath.addPath(p);
			}
			break;
		}
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
	
	public void drawstate(int drawstate){
		switch(drawstate){
		case 1: // 그리기시작
			_drawstate = 1;
			break;
		case 2: // 그리기종료
			_drawstate = 0;
			break;
		}
	}
}

class MyMovingposition{
	public float _acclvl;
	public Location _CurrentLocation;
	
	public MyMovingposition(Location _MyCurrentLocation, float _Myacclvl) {
		_CurrentLocation =_MyCurrentLocation;
		_acclvl = _Myacclvl;
		// TODO Auto-generated constructor stub
	}
}