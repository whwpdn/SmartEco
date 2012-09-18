package com.smarteco.android;

import java.io.Serializable;
import java.text.DecimalFormat;

public class ListItem implements Serializable {
	private double oilEffection;
	private int distance;
	private int oil;
	private String oilString;
	private String date;
	private int totaldistance;

	public String getOilString() {
		return oilString;
	}

	public double getOilEffection() {
		return oilEffection;
	}

	public void setOilEffection(double oilEffection) {
		this.oilEffection = oilEffection;
	}

	public void setOilString(String oilString) {
		this.oilString = oilString;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getOil() {
		return oil;
	}

	public void setOil(int oil) {
		this.oil = oil;
	}

	public ListItem(String date, int distance, int oil, int totaldistance) {
		this.setDate(date);
		this.setTotaldistance(totaldistance);
		this.distance = distance;
		this.oilEffection = (double) distance / (double) oil;
		this.oil = oil;
		String pattern = "###.#";
		DecimalFormat dformat = new DecimalFormat(pattern);
		this.oilString = dformat.format(this.oilEffection);

	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getTotaldistance() {
		return totaldistance;
	}

	public void setTotaldistance(int totaldistance) {
		this.totaldistance = totaldistance;
	}

}
