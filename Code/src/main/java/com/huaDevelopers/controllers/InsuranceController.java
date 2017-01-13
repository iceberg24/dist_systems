package com.huaDevelopers.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.huaDevelopers.data.Entities.Customer;
import com.huaDevelopers.data.Entities.Insurance;
import com.huaDevelopers.data.Entities.User;
import com.huaDevelopers.data.Entities.Vehicle;
import com.huaDevelopers.data.Entities.External.ExternalVehicle;
import com.huaDevelopers.data.Services.Transformers;
import com.huaDevelopers.data.Services.Interfaces.CustomerService;
import com.huaDevelopers.data.Services.Interfaces.DepartmentService;
import com.huaDevelopers.data.Services.Interfaces.ExternalService;
import com.huaDevelopers.data.Services.Interfaces.InsuranceService;
import com.huaDevelopers.data.Services.Interfaces.RoleService;
import com.huaDevelopers.data.Services.Interfaces.UserService;
import com.huaDevelopers.data.Services.Interfaces.VehicleService;

@Controller
@RequestMapping("/cms/insurance")
@SessionAttributes({ "insurance", "customer" })
public class InsuranceController {

	@Autowired
	private InsuranceService insuranceService;

	@Autowired
	private VehicleService vService;

	@Autowired
	private ExternalService externalService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private DepartmentService deptService;

	// returns the search form to find vehicle and customer from the national
	// database
	@RequestMapping(value = "/findVehicle", method = RequestMethod.GET)
	public String addCustomer(Model model) {
		model.addAttribute("vehicle", new Vehicle());
		return "vehicle_add";
	}

	// makes the query to the external database for vehicle customer and history
	// and place it in the internal one
	@RequestMapping(value = "/findVehicle", method = RequestMethod.POST)
	public String saveCustomer(Model model, @Valid @ModelAttribute("vehicle") Vehicle vehicle, Errors errors) {
		// check Transformers class for thorough documentation
		Transformers megatron = new Transformers();
		if (errors.hasErrors()) {
			System.out.println(errors);
			return "vehicle_add";
		} else if (ExternalVehicle.isEqual(this.externalService.getVehicle(vehicle.getLicensePlate()), null)) {
			errors.rejectValue("licensePlate", "vehicle.licensePlate", "***This license plate does not exist***");
			return "vehicle_add";
		}

		vehicle = megatron.externalVToMyV.apply(this.externalService.getVehicle(vehicle.getLicensePlate()));
		Vehicle checkvehicle = this.vService.getVehicleByLP(vehicle.getLicensePlate());
		int flag = 0; // check if vehicle exists and has an insurance
		if (!Vehicle.isEqual(checkvehicle, null)) {
			flag = 1;
			Insurance check = checkvehicle.getInsurance();
			if (!Insurance.isEqual(check, null)) {
				flag = 2;
			}
		}
		if (flag == 2) { // if there is insurance employee cannot create another
							// for this vehicle
			errors.rejectValue("licensePlate", "vehicle.licensePlate",
					"***Attention***This license plate is insuranced***");
			return "vehicle_add";
		}
		if (flag == 1) { // if there is vehicle but no insurance (by error),
							// vehicle is removed
			this.vService.removeVehicle(checkvehicle.getId());
		}
		Customer cust = this.customerService.getCustomerByID(vehicle.getCustomerPersonID().getPersonalId());
		if (!Customer.isEqual(cust, null)) {
			vehicle = this.vService.insertVehicle(vehicle, cust);
		} else
			vehicle = this.externalService.searchNationalDB(vehicle.getLicensePlate());
		Long id = vehicle.getId();
		model.addAttribute("id", id);
		model.addAttribute("vehicle", vehicle);
		return "redirect:/cms/insurance/{id}/create";

	}

	// provide the form for adding a new insurance
	@RequestMapping(value = "/{id:\\d+}/create", method = RequestMethod.GET)
	public String addInsurance(@PathVariable("id") Long id, Model model, @ModelAttribute("vehicle") Vehicle vehicle) {
		vehicle = this.vService.getVehicleByPID(id);
		Customer cust = this.customerService.getCustomerByID(vehicle.getCustomerPersonID().getPersonalId());
		model.addAttribute("customer", cust);
		id = vehicle.getId();
		model.addAttribute("id", id);
		model.addAttribute("vehicle", vehicle);
		Insurance insurance = new Insurance();
		insurance.setNewDriver(this.insuranceService.newDriver(cust));
		model.addAttribute("insurance", insurance);
		return "insur_add";
	}

	// send the insurance form for review (either edit, cancel, or save)
	@RequestMapping(value = "/{id:\\d+}/review", method = RequestMethod.POST)
	public String reviewInsurance(@PathVariable("id") Long id, Model model,
			@Valid @ModelAttribute("insurance") Insurance insur, Errors errors) {
		Vehicle vehicle = this.vService.getVehicleByPID(id);
		Customer cust = vehicle.getCustomerPersonID();
		if (errors.hasErrors()) {
			System.out.println(errors);
			model.addAttribute("customer", cust);
			model.addAttribute("vehicle", vehicle);
			return "insur_add";
		}
		int duration = insur.getDuration();
		String type = insur.getType();
		boolean flag = insur.getNewDriver();
		insur.setInsuranceDate(LocalDate.now());
		insur.setPrice(this.insuranceService.countInsurCost(vehicle, cust, type, duration, flag));
		insur.setDiscount(this.insuranceService.countInsurDiscount(cust, duration));
		insur.setNewDriver(flag);
		insur.setLicensePlate(vehicle);
		insur.setExpired(false);
		insur.setExpirationDate(this.insuranceService.expirationDateFunc(insur));
		insur.setPaid(true);
		model.addAttribute("customer", cust);
		model.addAttribute("vehicle", vehicle);
		model.addAttribute("insurance", insur);
		return "insur_review";

	}

	// returns success page message in case of saving the new insurance
	@RequestMapping(value = "/{id:\\d+}/adduser", method = RequestMethod.GET)
	public String saveInsurance(@PathVariable("id") Long id, Model model, @ModelAttribute("insurance") Insurance insur,
			SessionStatus status) {

		if (!Insurance.isEqual(this.insuranceService.getInsuranceByID(insur.getId()), null))
			this.insuranceService.updateInsurance(insur);
		else
			this.insuranceService.addInsurance(insur);
		model.addAttribute("insurance", insur);
		model.addAttribute("user", new User());
		return "insur_user_add";
	}

	@RequestMapping(value = "/{id:\\d+}/adduser", method = RequestMethod.POST)
	public String saveInsuranceAndUser(@PathVariable("id") Long id, Model model,
			@ModelAttribute("insurance") Insurance insur, @ModelAttribute("customer") Customer cust,
			@Validated(User.ValidationStepTwo.class) @ModelAttribute("user") User user, Errors errors,
			SessionStatus status) {
		/* Email validation */
		if (!user.getEmailAdress().isEmpty()) {
			if (this.userService.getUserByEmail(user.getEmailAdress()) != null) {
				errors.rejectValue("emailAdress", "user.emailAdress", "This email is already in use.");
				return "insur_user_add";
			}
		}
		/* username validation */
		if (!user.getUserName().isEmpty()) {
			if (this.userService.getUserByUsername(user.getUserName()) != null) {
				errors.rejectValue("userName", "user.userName", "This username is already in use.");
				return "insur_user_add";
			}
		}
		user.setAssignedRole(this.roleService.getRoleByID(6));
		if (user.getWorkingDept() == null)
			user.setWorkingDept(this.deptService.getDeptByID(6));
		user.setFirstName(cust.getFirstName());
		user.setLastName(cust.getLastName());
		/* Validation for the rest of the fields */
		if (errors.hasErrors()) {
			System.out.println(errors.getAllErrors().toString());
			System.out.println(cust.getFirstName() + cust.getLastName());
			System.out.println(user.getEmailAdress() + user.getFirstName() + user.getLastName() + user.getPassword()
					+ user.getTelephone() + user.getUserName() + user.getUserId() + user.getAssignedRole()
					+ user.getWorkingDept());
			return "insur_user_add";
		} else {
			/* set existing role and department to the user */
			this.userService.addUser(user);
			cust = this.customerService.getCustomerByID(cust.getPersonalId());
			cust.setUserEmail(user.getEmailAdress());
			this.customerService.updateCustomer(cust);

		}
		status.setComplete();
		return "insur_success";
	}

	// view table with all insurances
	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public String viewAllInsurance(Model model) {
		Vehicle veh = new Vehicle();
		List<Insurance> viewAllList = this.insuranceService.listAllInsurances();
		System.out.println("before loop");
		for (Insurance insur : viewAllList) {
			System.out.println("before check");
			if (!insur.getExpired().equals(this.insuranceService.checkExpiration(insur))) {
				System.out.println("in check");
				insur.setExpired(this.insuranceService.checkExpiration(insur));
				this.insuranceService.updateInsurance(insur);
			}
		}
		model.addAttribute("search", veh);
		model.addAttribute("insurances", viewAllList);
		return "insur_all";
	}

	// view table with only one insurance - the one that provided in the search
	// bar
	@RequestMapping(value = "/view", method = RequestMethod.POST)
	public String viewSearchedInsurance(Model model, @Valid @ModelAttribute("search") Vehicle veh, Errors errors) {
		Vehicle searched = vService.getVehicleByLP(veh.getLicensePlate());
		if (searched == null) {
			model.addAttribute("msg", "The insurance you searched does not exist");
			model.addAttribute("insurances", this.insuranceService.listAllInsurances());
			return "insur_all";
		}
		List<Insurance> search_results = new ArrayList<Insurance>();
		search_results.add(this.insuranceService.getInsuranceByID(searched.getId()));
		model.addAttribute("insurances", search_results);
		return "insur_all";
	}

	// edit insurance's type, or duration
	@RequestMapping(value = "/{id:\\d+}/edit", method = RequestMethod.GET)
	public String editInsurance(@PathVariable("id") Long id, Model model, @ModelAttribute("vehicle") Vehicle vehicle) {
		vehicle = this.vService.getVehicleByPID(id);
		Customer cust = this.customerService.getCustomerByID(vehicle.getCustomerPersonID().getPersonalId());
		model.addAttribute("customer", cust);
		boolean flag = this.insuranceService.newDriver(cust);
		id = vehicle.getId();
		model.addAttribute("id", id);
		model.addAttribute("vehicle", vehicle);
		Insurance insurance = new Insurance();
		insurance.setNewDriver(flag);
		model.addAttribute("insurance", insurance);
		return "insur_edit";
	}

	// delete insurance as well as the vehicle and the customer, provided that
	// he hasn't any other vehicle insured, from database
	@RequestMapping(value = "/{id:\\d+}/delete", method = RequestMethod.GET)
	public String deleteInsurance(@PathVariable("id") Long id) {
		Customer cust = this.vService.getVehicleByPID(id).getCustomerPersonID();
		this.vService.removeVehicle(id);
		List<Vehicle> vList = this.vService.listAllVehiclesPerCustomer(cust.getPersonalId());
		if (vList.isEmpty()) {
			User user = this.userService.getUserByEmail(cust.getUserEmail());
			if (user != null) {
				this.userService.removeUser(user.getUserId());
			}
			this.customerService.removeCustomer(cust.getId());
		}
		return "redirect:/cms/insurance/view";
	}

	@RequestMapping(value = "/{id:\\d+}/expand", method = RequestMethod.PUT)
	public String expandContract(@PathVariable("id") Long id, Model model, @ModelAttribute("vehicle") Vehicle vehicle) {
		String resultJSP = editInsurance(id, model, vehicle);
		return resultJSP;
	}

	/*
	 * global for the scope of InsuranceController model attributes passed only
	 * once in the ORM due to sessionAttributes tag and once the insurance has
	 * been saved session is going to be flushed
	 */
	@ModelAttribute("insurance")
	public Insurance getInsurance() {
		return new Insurance();
	}

	@ModelAttribute("typeOptions")
	public List<String> getTypes() {
		return new LinkedList<>(Arrays.asList(new String[] { "Basic", "Intermediate", "Premium" }));
	}

	@ModelAttribute("durationOptions")
	public List<Integer> getYears() {
		return new LinkedList<>(Arrays.asList(new Integer[] { 1, 2 }));
	}
}
