package services;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import dao.UserDAO;
import dao.VehicleDAO;
import dao.ManagerDAO;
import dao.PurchaseDAO;
import dao.RentACarDAO;
import dto.RentACarDTO;
import models.Customer;
import models.Manager;
import models.Purchase;
import models.RentACar;
import models.Role;
import models.User;
import models.Vehicle;

@Path("/rentacar")
public class RentACarService {
	@Context
	ServletContext ctx;
	public RentACarService() {
		
	}
	@PostConstruct
	public void init() {
		if (ctx.getAttribute("rentACarDAO") == null ) {
	    	String contextPath = ctx.getRealPath("");
			ctx.setAttribute("rentACarDAO", new RentACarDAO(contextPath));
		}
		if (ctx.getAttribute("vehicleDAO") == null ) {
	    	String contextPath = ctx.getRealPath("");
			ctx.setAttribute("vehicleDAO", new VehicleDAO(contextPath));
		}
		if(ctx.getAttribute("userDAO") == null) {
			String contextPath = ctx.getRealPath("");
			ctx.setAttribute("userDAO", new UserDAO(contextPath));
		}
		if(ctx.getAttribute("purchaseDAO") == null) {
			String contextPath = ctx.getRealPath("");
			ctx.setAttribute("purchaseDAO", new PurchaseDAO(contextPath));
		}
		linkManagers();
		linkCars();
		linkPurchasesRentACar();
		linkPurchasesVehicles();
	}
	private void linkPurchasesVehicles() {
		VehicleDAO vdao = (VehicleDAO) ctx.getAttribute("vehicleDAO");
		PurchaseDAO pdao = (PurchaseDAO) ctx.getAttribute("purchaseDAO");
		for(Purchase p : pdao.getAll()) {
			p.getVehicles().clear();
			for(Integer vId : p.getVehicleIds()){
				Vehicle v = vdao.getById(vId);
				p.getVehicles().add(v);
			}
		}
	}
	private void linkPurchasesRentACar() {
		RentACarDAO rdao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		PurchaseDAO pdao = (PurchaseDAO) ctx.getAttribute("purchaseDAO");
		for(RentACar rentACar : rdao.getAll()) {
			rentACar.getRentings().clear();
			for(Purchase p : pdao.getByRentingsId(rentACar.getId())){
				rentACar.getRentings().add(p);
				p.setRentACar(rentACar);
			}
		}
	}
	public void linkManagers() {
		UserDAO udao = (UserDAO) ctx.getAttribute("userDAO");
		RentACarDAO rdao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		for(Manager m : udao.getManagers()) {
			RentACar rent = rdao.getById(m.getRentACarId());
			m.setRentACar(rent);
		}
	}
	public void linkCars() {
		VehicleDAO vdao = (VehicleDAO) ctx.getAttribute("vehicleDAO");
		RentACarDAO rdao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		for(RentACar r : rdao.getAll()) {
			r.getVehicles().clear();
		}
		for(Vehicle v : vdao.getAll()) {
			RentACar rent = rdao.getById(v.getRentACarId());
			v.setRentACar(rent);
			rent.getVehicles().add(v);
		}
	}
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<RentACar> getAll(){
		RentACarDAO dao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		return dao.getAll();
	}
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RentACar getById(@PathParam("id") int id){
		RentACarDAO dao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		return dao.getById(id);
	}
	@GET
	@Path("/manager/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public RentACar getByManagerId(@PathParam("id") int id){
		UserDAO dao = (UserDAO) ctx.getAttribute("userDAO");
		Manager m = (Manager)dao.getById(id);
		return m.getRentACar();
	}
	@GET
	@Path("/vehicles")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Vehicle> getAvailableVehicles(){
		VehicleDAO dao = (VehicleDAO) ctx.getAttribute("vehicleDAO");
		return dao.getAvailable();
	}
	@GET
	@Path("/vehicles/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Vehicle> getVehiclesFromRentACar(@PathParam("id") int id){
		UserDAO uDao = (UserDAO) ctx.getAttribute("userDAO");
		Manager m = (Manager)uDao.getById(id);
		RentACarDAO rDao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		return rDao.getFromRentACar(m.getRentACarId());
	}
	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public void save(@PathParam("id") int id, RentACarDTO rentACarDTO){
		RentACarDAO dao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		int rentacarId = dao.save(rentACarDTO, id);
		UserDAO uDao = (UserDAO) ctx.getAttribute("userDAO");
		uDao.updateManager(id, rentacarId);
	}
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public void save(RentACarDTO rentACarDTO) {
		RentACarDAO dao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		dao.createRentACar(rentACarDTO);
	}
	@POST
	@Path("/newmanager")
	@Produces(MediaType.APPLICATION_JSON)
	public void saveManager(Manager manager){
		RentACarDAO dao = (RentACarDAO) ctx.getAttribute("rentACarDAO");
		int rentacarId = dao.getNewId();
		UserDAO uDao = (UserDAO) ctx.getAttribute("userDAO");
		uDao.saveManager(manager, rentacarId);
	}
	
	@POST
	@Path("/vehicle")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createVehicle(Vehicle vehicle, @Context HttpServletRequest request) {
		Manager manager = (Manager) request.getSession().getAttribute("currentUser");
		VehicleDAO vDao = (VehicleDAO) ctx.getAttribute("vehicleDAO");
		vehicle.setRentACarId(manager.getRentACarId());
		vehicle.setRentACar(manager.getRentACar());
		vDao.save(vehicle);
	}
}
