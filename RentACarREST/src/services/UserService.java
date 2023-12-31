package services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dao.BlockedUserDAO;
import dao.CancelingDAO;
import dao.CommentDAO;
import dao.CustomerTypeDAO;
import dao.PurchaseDAO;
import dao.RentACarDAO;
import dao.UserDAO;
import dao.VehicleDAO;
import dto.UserDTO;
import models.BlockedUser;
import models.Canceling;
import models.Comment;
import models.Customer;
import models.Manager;
import models.Purchase;
import models.PurchaseStatus;
import models.RentACar;
import models.Role;
import models.ShoppingCart;
import models.SubPurchase;
import models.User;
import models.Vehicle;

@Path("/users")
public class UserService {
	@Context
	ServletContext context;
	public UserService() {
		
	}
	@PostConstruct
	public void init() {
		if(context.getAttribute("userDAO") == null) {
			String contextPath = context.getRealPath("");
			context.setAttribute("userDAO", new UserDAO(contextPath));
		}
		if(context.getAttribute("customerTypeDAO") == null) {
			String contextPath = context.getRealPath("");
			context.setAttribute("customerTypeDAO", new CustomerTypeDAO(contextPath));
		}
		if(context.getAttribute("purchaseDAO") == null) {
			String contextPath = context.getRealPath("");
			context.setAttribute("purchaseDAO", new PurchaseDAO(contextPath));
		}
		if(context.getAttribute("commentDAO") == null) {
			String contextPath = context.getRealPath("");
			context.setAttribute("commentDAO", new CommentDAO(contextPath));
		}
		if (context.getAttribute("rentACarDAO") == null ) {
	    	String contextPath = context.getRealPath("");
	    	context.setAttribute("rentACarDAO", new RentACarDAO(contextPath));
		}
		if (context.getAttribute("blockedUserDAO") == null ) {
	    	String contextPath = context.getRealPath("");
	    	context.setAttribute("blockedUserDAO", new BlockedUserDAO(contextPath));
		}
		if (context.getAttribute("cancelingDAO") == null ) {
	    	String contextPath = context.getRealPath("");
	    	context.setAttribute("cancelingDAO", new CancelingDAO(contextPath));
		}
	}
	@GET
	@Path("/cart")
	@Produces(MediaType.APPLICATION_JSON)
	public ShoppingCart getCart(@Context HttpServletRequest request){
		User u = (User) request.getSession().getAttribute("currentUser");
		Customer c = (Customer)u;
		return c.getShoppingCart();
	}
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<User> getAll(){
		UserDAO dao = (UserDAO) context.getAttribute("userDAO");
		return dao.getAll();
	}
	@GET
	@Path("/userstoshow/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<UserDTO> getUsers(@PathParam("id") int rentId){
		if(rentId == -1) {
			UserDAO dao = (UserDAO) context.getAttribute("userDAO");
			ArrayList<UserDTO> users = dao.getAllUsersDTO();
			for(UserDTO u : users) {
				BlockedUserDAO bdao = (BlockedUserDAO) context.getAttribute("blockedUserDAO");
				for(Integer id : bdao.getAll()) {
					if(u.getId() == id) {
						u.setBlocked(true);
					}
				}
			}
			return users;
		}
		else {
			PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
			ArrayList<Purchase> purchases= pdao.getByRentId(rentId);
			ArrayList<UserDTO> users = new ArrayList<UserDTO>();
			boolean add = true;
			for(Purchase p : purchases) {
				add = true;
				for(UserDTO u : users) {
					if(p.getCustomerId() == u.getId()) {
						add = false;
						break;
					}
				}
				if(add) {
					UserDTO u = new UserDTO(p.getCustomer().getId(), p.getCustomer().getUsername(), p.getCustomer().getFirstName(), 
					p.getCustomer().getLastName(), p.getCustomer().getGender(), p.getCustomer().getRole(), p.getCustomer().getDateOfBirth());
					u.setCollectedPoints(p.getCustomer().getCollectedPoints());
					u.setCustomerType(p.getCustomer().getCustomerType().getName());
					users.add(u);
				}
			}
			return users;
		}
		
	}
	@GET
	@Path("/suspects")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Customer> getSuspects(){
		UserDAO dao = (UserDAO) context.getAttribute("userDAO");
		ArrayList<Customer> allcustomers = dao.getAllCustomers();
		ArrayList<Customer> customers = new ArrayList<>();
		for(Customer c : allcustomers) {
			BlockedUserDAO bdao = (BlockedUserDAO) context.getAttribute("blockedUserDAO");
			if(bdao.isNotBlocked(c.getId())) {
				customers.add(c);
			}
		}
		ArrayList<Customer> result = new ArrayList<>();
		CancelingDAO cdao = (CancelingDAO) context.getAttribute("cancelingDAO");
		for(Customer c : customers) {
			if(cdao.isSuspecious(c.getId())) {
				result.add(c);
			}
		}
		return result;
	}
	@POST
	@Path("/block/{id}")
	public void block(@PathParam("id") Integer id){
		BlockedUserDAO dao = (BlockedUserDAO) context.getAttribute("blockedUserDAO");
		dao.save(id);
	}
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getById(@PathParam("id") Integer id){
		UserDAO dao = (UserDAO) context.getAttribute("userDAO");
		return dao.getById(id);
	}
	@POST
	@Path("/addToCart/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addToCart(Purchase purchase, @PathParam("id") Integer id, @Context HttpServletRequest request){
		User u = (User) request.getSession().getAttribute("currentUser");
		VehicleDAO vDAO = (VehicleDAO) context.getAttribute("vehicleDAO");
		Vehicle v = vDAO.getById(id);
		if(v != null) {
			Customer c = (Customer)u;
			c.getShoppingCart().addVehicle(v);
			for(Purchase p : c.getShoppingCart().getPrepairedPurchases()) {
				if(p.getStartDateTime().equals(purchase.getStartDateTime()) && p.getEndDateTime().equals(purchase.getEndDateTime())) {
					p.getVehicleIds().add(v.getId());
					p.getVehicles().add(v);
					p.addPrice(v.getPrice()*p.getDuration()*(1.0-c.getCustomerType().getDiscount()/100.0));
					if(!p.getRentACars().contains(v.getRentACar())) {
						p.getRentACars().add(v.getRentACar());
						p.getSubPurchases().add(new SubPurchase(p.getId(), v.getRentACarId(), p.getDuration(),
							p.getStartDateTime(), p.getStatus(), false));
					}
					c.getShoppingCart().addPrice(v.getPrice()*p.getDuration()*(1.0-c.getCustomerType().getDiscount()/100.0));
					return Response.status(200).build();
				}
			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
			LocalDateTime dateTime = LocalDateTime.parse(purchase.getStartDateTime(), formatter);
			purchase.setStart(dateTime);
			dateTime = LocalDateTime.parse(purchase.getEndDateTime(), formatter);
			purchase.setEnd(dateTime);
			Duration d = Duration.between(purchase.getStart(), purchase.getEnd());
			purchase.setDuration((int) (d.getSeconds()/3600));
			purchase.setPrice(v.getPrice()*purchase.getDuration()*(1.0-c.getCustomerType().getDiscount()/100.0));
			purchase.setStatus(PurchaseStatus.pending);
			purchase.setCustomerId(c.getId());
			purchase.setCustomer(c);
			purchase.getRentACars().add(v.getRentACar());
			purchase.getVehicleIds().add(v.getId());
			purchase.getVehicles().add(v);
			purchase.getSubPurchases().add(new SubPurchase(purchase.getId(), v.getRentACarId(), purchase.getDuration(),
					purchase.getStartDateTime(), purchase.getStatus(), false));
			c.getShoppingCart().getPrepairedPurchases().add(purchase);
			c.getShoppingCart().addPrice(v.getPrice()*purchase.getDuration()*(1.0-c.getCustomerType().getDiscount()/100.0));
			return Response.status(200).build();
		}
		return Response.status(400).entity("error").build();
	}
	@POST
	@Path("/rent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response rent(@Context HttpServletRequest request){
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		UserDAO udao = (UserDAO) context.getAttribute("userDAO");
		User u = (User) request.getSession().getAttribute("currentUser");
		Customer c = (Customer)u;
		for(Purchase p : c.getShoppingCart().getPrepairedPurchases()) {
			pdao.save(p);
		}
		c.setCollectedPoints(c.getCollectedPoints() + c.getShoppingCart().getPrice()/1000.0*133.0);
		udao.SaveAll();
		c.setShoppingCart(new ShoppingCart());
		c.getShoppingCart().setUser(c);
		return Response.status(200).build();
	}
	@PUT
	@Path("/cancel/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response cancelPurchase(@PathParam("id") String purchaseId, @Context HttpServletRequest request){
		User u = (User) request.getSession().getAttribute("currentUser");
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		UserDAO udao = (UserDAO) context.getAttribute("userDAO");
		CancelingDAO cancelingdao = (CancelingDAO) context.getAttribute("cancelingDAO");
		Customer c = (Customer)u;
		for(Purchase p : c.getRentings()) {
			if(p.getId().equals(purchaseId)) {
				if((c.getCollectedPoints() - (double)p.getPrice()/1000.0*133.0*4.0) <= 0.0) {
					c.setCollectedPoints(0.0);
				}else {
					c.setCollectedPoints(c.getCollectedPoints() - (double)p.getPrice()/1000.0*133.0*4.0);
				}
				udao.SaveAll();
				p.setStatus(PurchaseStatus.canceled);
				pdao.updatePurchases();
				cancelingdao.save(new Canceling(c.getId(), new Date()));
				return Response.status(200).build();
			}
		}
		return Response.status(400).entity("error").build();
	}
	@POST
	@Path("/removeFromCart/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeFromCart(Purchase purchase, @PathParam("id") Integer id, @Context HttpServletRequest request){
		User u = (User) request.getSession().getAttribute("currentUser");
		VehicleDAO vDAO = (VehicleDAO) context.getAttribute("vehicleDAO");
		Vehicle v = vDAO.getById(id);
		if(v != null) {
			Customer c = (Customer)u;
			c.getShoppingCart().removeVehicle(v);
			Purchase PforDeleting = null;
			for(Purchase p : c.getShoppingCart().getPrepairedPurchases()){
				if(p.getVehicles().contains(v) && p.getStartDateTime().equals(purchase.getStartDateTime()) && p.getEndDateTime().equals(purchase.getEndDateTime())) {
					p.getVehicles().remove(v);
					p.removeVehicleId(v.getId());
					p.removePrice(v.getPrice()*p.getDuration());
					c.getShoppingCart().removePrice(v.getPrice()*p.getDuration());
					p.getRentACars().clear();
					for(Vehicle vehicle : p.getVehicles()) {
						p.getRentACars().add(vehicle.getRentACar());
					}
					
					SubPurchase SPforDeleting = null;
					for(SubPurchase s : p.getSubPurchases()) {
						boolean exists = false;
						for(RentACar r : p.getRentACars()) {
							if(s.getRentACarId() == r.getId()) {
								exists = true;
							}
						}
						if(!exists) {
							SPforDeleting = s;
							break;
						}
					}
					if(SPforDeleting != null) {
						p.getSubPurchases().remove(SPforDeleting);
					}
					if(p.getVehicles().isEmpty()) {
						PforDeleting = p;
					}
					break;
				}
			}
			if(PforDeleting != null) {
				c.getShoppingCart().getPrepairedPurchases().remove(PforDeleting);
			}
		}
		if(v == null) {
			return Response.status(400).entity("error").build();
		}else {
			return Response.status(200).build();
		}
	}
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(User c, @Context HttpServletRequest request){
		UserDAO dao = (UserDAO) context.getAttribute("userDAO");
		String message = dao.saveCustomer(c);
		if(!message.equals("ok")) {
			return Response.status(400).entity(message).build();
		}else {
			return Response.status(200).build();
		}
	}
	@PUT
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response edit(User c){
		UserDAO dao = (UserDAO) context.getAttribute("userDAO");
		String message = dao.editCustomer(c);
		if(!message.equals("ok")) {
			return Response.status(400).entity(message).build();
		}else {
			return Response.status(200).build();
		}
	}
	@PUT
	@Path("/changepassword/{id}/{oldPassword}/{newPassword}")
	public boolean changePassword(@PathParam("id") Integer id, @PathParam("oldPassword") String oldPassword, @PathParam("newPassword") String newPassword){
		UserDAO dao = (UserDAO) context.getAttribute("userDAO");
		return dao.changePassword(id, oldPassword, newPassword);
	}
	
	@GET
	@Path("/freeManagers")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Manager> getFreeManagers(){
		UserDAO dao = (UserDAO) context.getAttribute("userDAO");
		return dao.getFreeManagers();
	}
	@GET
	@Path("/customersRentings")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Purchase> getCustomersRentings(@Context HttpServletRequest request){
		User u = (User) request.getSession().getAttribute("currentUser");
		Customer c = (Customer)u;
		return c.getRentings();
	}
	@GET
	@Path("/managersRentings")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Purchase> getManagersRentings(@Context HttpServletRequest request){
		User u = (User) request.getSession().getAttribute("currentUser");
		Manager m = (Manager)u;
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		ArrayList<Purchase> result = new ArrayList<Purchase>();
		for(Purchase p : pdao.getAll()) {
			if(p.getRentACars().contains(m.getRentACar())) {
				for(Vehicle v : p.getVehicles()) {
					if(v.getRentACarId() == m.getRentACarId()) {
						v.setFromCurrentRentACar(true);
					}
					else {
						v.setFromCurrentRentACar(false);
					}
				}
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getRentACarId() == m.getRentACarId()) {
						sp.setFromCurrentRentACar(true);
					}
					else {
						sp.setFromCurrentRentACar(false);
					}
				}
				result.add(p);
			}
		}
		return result;
	}
	@PUT
	@Path("/grade/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response grade(@PathParam("id") String purchaseId, SubPurchase subPurchase, @Context HttpServletRequest request){
		User u = (User) request.getSession().getAttribute("currentUser");
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		CommentDAO cdao = (CommentDAO) context.getAttribute("commentDAO");
		RentACarDAO rDao = (RentACarDAO) context.getAttribute("rentACarDAO");
		Customer c = (Customer)u;
		for(Purchase p : c.getRentings()) {
			if(p.getId().equals(purchaseId)) {
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getRentACarId() == subPurchase.getRentACarId()) {
						Comment comment = subPurchase.getComment();
						comment.setRentACar(sp.getRentACar());
						comment.setRentACarId(sp.getRentACarId());
						comment.setCustomer(c);
						comment.setCustomerId(c.getId());
						comment.setApproved(false);
						cdao.save(comment);
						sp.setGraded(true);
						pdao.updatePurchases();
					}
				}
				return Response.status(200).build();
			}
		}
		return Response.status(400).entity("error").build();
	}
	@PUT
	@Path("/accept/{id}/{rentACarId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response acceptPurchase(@PathParam("id") String purchaseId, @PathParam("rentACarId") Integer rentACarId){
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		for(Purchase p : pdao.getAll()) {
			if(p.getId().equals(purchaseId)) {
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getRentACarId() == rentACarId) {
						sp.setStatus(PurchaseStatus.accepted);
					}
				}
				boolean isAccepted = true;
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getStatus() != PurchaseStatus.accepted) {
						isAccepted = false;
						break;
					}
				}
				if(isAccepted) {
					p.setStatus(PurchaseStatus.accepted);
				}
				pdao.updatePurchases();
				return Response.status(200).build();
			}
		}
		return Response.status(400).entity("error").build();
	}
	@PUT
	@Path("/decline/{id}/{decliningReason}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response declinePurchase(@PathParam("id") String purchaseId, @PathParam("decliningReason") String decliningReason){
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		for(Purchase p : pdao.getAll()) {
			if(p.getId().equals(purchaseId)) {
				p.setDecliningReason(decliningReason);
				p.setStatus(PurchaseStatus.declined);
				pdao.updatePurchases();
				return Response.status(200).build();
			}
		}
		return Response.status(400).entity("error").build();
	}
	@PUT
	@Path("/take/{id}/{rentACarId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response takeVehicles(@PathParam("id") String purchaseId, @PathParam("rentACarId") Integer rentACarId){
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		VehicleDAO vDAO = (VehicleDAO) context.getAttribute("vehicleDAO");
		for(Purchase p : pdao.getAll()) {
			if(p.getId().equals(purchaseId)) {
				for(Vehicle v : p.getVehicles()) {
					if(v.getRentACarId() == rentACarId) {
						v.setAvailable(false);
						vDAO.updateVehicles();
					}
				}
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getRentACarId() == rentACarId) {
						sp.setStatus(PurchaseStatus.taken);
					}
				}
				boolean isTaken = true;
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getStatus() != PurchaseStatus.taken) {
						isTaken = false;
						break;
					}
				}
				if(isTaken) {
					p.setStatus(PurchaseStatus.taken);
				}
				pdao.updatePurchases();
				return Response.status(200).build();
			}
		}
		return Response.status(400).entity("error").build();
	}
	@PUT
	@Path("/return/{id}/{rentACarId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response returnVehicles(@PathParam("id") String purchaseId, @PathParam("rentACarId") Integer rentACarId){
		PurchaseDAO pdao = (PurchaseDAO) context.getAttribute("purchaseDAO");
		VehicleDAO vDAO = (VehicleDAO) context.getAttribute("vehicleDAO");
		for(Purchase p : pdao.getAll()) {
			if(p.getId().equals(purchaseId)) {
				for(Vehicle v : p.getVehicles()) {
					if(v.getRentACarId() == rentACarId) {
						v.setAvailable(true);
						vDAO.updateVehicles();
					}
				}
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getRentACarId() == rentACarId) {
						sp.setStatus(PurchaseStatus.returned);
					}
				}
				boolean isReturned = true;
				for(SubPurchase sp : p.getSubPurchases()) {
					if(sp.getStatus() != PurchaseStatus.returned) {
						isReturned = false;
						break;
					}
				}
				if(isReturned) {
					p.setStatus(PurchaseStatus.returned);
				}
				pdao.updatePurchases();
				return Response.status(200).build();
			}
		}
		return Response.status(400).entity("error").build();
	}
}
