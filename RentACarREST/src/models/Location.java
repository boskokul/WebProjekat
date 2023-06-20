package models;

public class Location {
	private int id;
	private double latitude; //g. sirina
	private double longitude; //g. duzina
	private String address;
	
	public Location() {
	}

	public Location(int id, double latitude, double longitude, String address) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
}
