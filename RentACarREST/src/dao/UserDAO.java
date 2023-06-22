package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import models.Customer;
import models.CustomerType;
import models.Gender;
import models.Renting;
import models.Role;
import models.ShoppingCart;
import models.User;

public class UserDAO {
	private HashMap<Integer, Customer> users = new HashMap<>();
	private String path = null;
	
	public UserDAO() {
	}
	@SuppressWarnings("deprecation")
	public UserDAO(String contextPath) {
		Calendar cal = Calendar.getInstance();
		path = contextPath;
		loadCustomers(path);
	}
	
	public void loadCustomers(String contextPath) {
		BufferedReader in = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		try {
			File file = new File(contextPath + "/users.txt");
			System.out.println(file.getCanonicalPath());
			in = new BufferedReader(new FileReader(file));
			String line, id = "", username = "", password = "", firstName = "", lastName = "", gender = "", role = "", dateOfBirth="", collectedPoints = "";
			StringTokenizer st;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.equals("") || line.indexOf('#') == 0)
					continue;
				st = new StringTokenizer(line, ";");
				while (st.hasMoreTokens()) {
					id = st.nextToken().trim();
					username = st.nextToken().trim();
					password = st.nextToken().trim();
					firstName = st.nextToken().trim();
					lastName = st.nextToken().trim();
					gender = st.nextToken().trim();
					role = st.nextToken().trim();
					dateOfBirth = st.nextToken().trim();
					collectedPoints = st.nextToken().trim();
				}
				users.put(Integer.parseInt(id), new Customer(Integer.parseInt(id), username, password, firstName, lastName, Gender.valueOf(gender) , Role.valueOf(role), formatter.parse(dateOfBirth), Integer.parseInt(collectedPoints)));
			}
		} catch (Exception e) {
			System.out.println("greska");
			e.printStackTrace();
		} finally {
			if ( in != null ) {
				try {
					in.close();
				}
				catch (Exception e) { }
			}
		}
	}
	
	public Collection<Customer> getAll(){
		return users.values();
	}
	
	public Customer getById(Integer id){
		return users.get(id);
	}
	
	public String saveCustomer(Customer c){
		if(c.getUsername() == null || c.getFirstName() == null || c.getPassword() == null
				|| c.getFirstName() == null || c.getLastName() == null || c.getGender() == null || c.getDateOfBirth() == null) {
			return "Can not register this user, not all fields have been filled correctly";
		}
		if(c.getUsername().isBlank() || c.getFirstName().isBlank() || c.getPassword().isBlank()
				|| c.getFirstName().isBlank() || c.getLastName().isBlank()) {
			return "Can not register this user, not all fields have been filled correctly";
		}
		if(!isUsernameUnique(c.getUsername())) {
			return "This username already exists, please choose a different one";
		}
		Integer maxId = 0;
		for (Integer id : users.keySet()) {
			if (id > maxId) {
				maxId = id;
			}
		}
		maxId++;
		c.setId(maxId);
		c.setCollectedPoints(0);
		users.put(c.getId(), c);
		SaveToFile();
		return "ok";
	}
	public Boolean isUsernameUnique(String username) {
		for(User u : users.values()) {
			if(u.getUsername().equals(username)) {
				return false;
			}
		}
		return true;
	}
	public String editCustomer(Customer c){
		if(c.getFirstName().isBlank() || c.getLastName().isBlank()) {
			return "First name and last name can't contain only white spaces";
		}
		if(c.getFirstName().isEmpty() || c.getLastName().isEmpty()) {
			return "First name and last name can't be empty";
		}
		Customer oldCustomer = users.get(c.getId());
		oldCustomer.setUsername(c.getUsername());
		oldCustomer.setPassword(c.getPassword());
		oldCustomer.setFirstName(c.getFirstName());
		oldCustomer.setLastName(c.getLastName());
		oldCustomer.setGender(c.getGender());
		oldCustomer.setDateOfBirth(c.getDateOfBirth());
		SaveToFile();
		return "ok";
	}
	public boolean changePassword(Integer id, String oldPassword, String newPassword) {
		Customer c = getById(id);
		if(c.getPassword().equals(oldPassword)) {
			c.setPassword(newPassword);
			SaveToFile();
			return true;
		}
		else {
			return false;
		}
	}
	public User find(String username, String password) {
		for(Customer c : users.values()){
			if (c.getUsername().equals(username)) {
				if(c.getPassword().equals(password)) {
					return c;
				}
			}
		}
		return null;
	}
	private void SaveToFile() {
		BufferedWriter bw = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		try {
			File fout = new File(path + "/users.txt");
			FileOutputStream fos = new FileOutputStream(fout);
			bw = new BufferedWriter(new OutputStreamWriter(fos));
			for(Customer c : users.values()) {
				String date = formatter.format(c.getDateOfBirth());
				String lineToWrite = 
				c.getId()+";"+c.getUsername()+";"+c.getPassword()+";"+c.getFirstName()+";"+c.getLastName()+";"+c.getGender()+";"+c.getRole()+";"+date+";"+c.getCollectedPoints();
				bw.write(lineToWrite);
				bw.newLine();
			}
			bw.close();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(bw!=null) {
				try {
					bw.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
	}
}