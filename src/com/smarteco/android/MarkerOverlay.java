package com.smarteco.android;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import java.util.Locale;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
//import android.location.Address;
//import android.location.Geocoder;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

class MarkerOverlay extends ItemizedOverlay<OverlayItem>
{
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private Drawable marker = null;
	Context _Context;
	public MarkerOverlay(Context context, Drawable marker) 
	{
		super(marker);
		this.marker = marker;
		_Context = context;		
		populate();
	}
	
	public void addItem(GeoPoint point, String title, String snippet)
	{
		items.add( new OverlayItem(point, title, snippet));
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return items.get(i);
	}

	@Override
	public int size() {
		return items.size();
	}
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, shadow);
		boundCenterBottom(marker);	
	}
	
	public boolean onTap(int index)
	{

		return true;
	}
	
}
