package models;

import java.util.ArrayList;

public class ShoppingCart {
	private Integer id;
	private ArrayList<Vehicle> vehicles;
	private User user;
	private double price;
	public ShoppingCart() {
		vehicles = new ArrayList<>();
		price = 0;
	}
	public ShoppingCart(Integer id, ArrayList<Vehicle> vehicles, User user, double price) {
		super();
		this.id = id;
		this.vehicles = vehicles;
		this.user = user;
		this.price = price;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public ArrayList<Vehicle> getVehicles() {
		return vehicles;
	}
	public void setVehicles(ArrayList<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public void addVehicle(Vehicle v) {
		this.vehicles.add(v);
	}
	public void removeVehicle(Vehicle v) {
		this.vehicles.remove(v);
	}
	public void addPrice(int p) {
		this.price += p;
	}
	public void removePrice(int p) {
		this.price -= p;
	}
}
