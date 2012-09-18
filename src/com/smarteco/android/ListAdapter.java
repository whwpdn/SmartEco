package com.smarteco.android;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {

	Context context;
	int layout;
	ArrayList<ListItem> data;
	
	public ListAdapter(){}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position).getDistance();
	}


	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	
	public ListAdapter(Context context, int layout, ArrayList<ListItem> data){
		this.context = context;
		this.layout = layout;
		this.data = data;
	}
	public View getView(int position, View cView, ViewGroup parent) {
		
		final ListItem item = data.get(position);
		if(cView == null){
			cView = View.inflate(context, layout, null);
		}
		TextView toileffection = (TextView)cView.findViewById(R.id.toileffection);
		TextView tdistance = (TextView)cView.findViewById(R.id.tdistance);
		TextView toil = (TextView)cView.findViewById(R.id.toil);
		TextView totaldistance=(TextView)cView.findViewById(R.id.totaldistance);
		TextView date = (TextView)cView.findViewById(R.id.date);
		toileffection.setText(item.getOilString()+"km/L");
		tdistance.setText(Integer.toString(item.getDistance())+" KM");
		toil.setText(Integer.toString(item.getOil())+"L");
		totaldistance.setText(Integer.toString(item.getTotaldistance())+" KM");
		date.setText(item.getDate());
		
		//Toast.makeText(context, item.getName(),Toast.LENGTH_SHORT).show();
	
		return cView;
	}
	
	

}
