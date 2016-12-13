package com.huaDevelopers.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.huaDevelopers.data.Entities.Department;
import com.huaDevelopers.data.Entities.Role;
import com.huaDevelopers.data.Entities.User;
import com.huaDevelopers.data.Services.DepartmentService;
import com.huaDevelopers.data.Services.RoleService;
import com.huaDevelopers.data.Services.UserService;

@Controller
@RequestMapping("/admin/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private DepartmentService deptService;

	
	/*@Qualifier(value = "UserService")
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Qualifier(value = "roleService")
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	@Qualifier(value = "deptService")
	public void setDeptService(DepartmentService deptService) {
		this.deptService = deptService;
	}*/

	@RequestMapping(value = "/{UserName}")
	public String findUser(Model model, @PathVariable("UserName") String UserName) {
		model.addAttribute("user", this.userService.getUserByUsername(UserName));
		return "user";
	}

	@RequestMapping(value = "/find", method = RequestMethod.GET)
	public String find(Model model) {
		model.addAttribute("users", this.userService.listAllUser());
		return "users";
	}

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addUser(Model model) {
		List<Role> roles = roleService.listAllRoles();
		List<Department> departments = deptService.getAllDepts();
		model.addAttribute("roles", roles);
		model.addAttribute("departments", departments);
		model.addAttribute("user", new User());
		return "user_add";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String saveUser(@Valid @ModelAttribute("user") User user, Errors errors) {
		if (errors.hasErrors()) {
			return "user_add";
		}
		if(user.getUserid() == 0){
			//new person, add it
			user.setAssignedRole(this.roleService.getRoleByID(user.getAssignedRole().getRoleId()));
			user.setWorkingDept(this.deptService.getDeptByID(user.getWorkingDept().getId()));
			this.userService.addUser(user);
		}else{
			//existing person, call update
			this.userService.updateUser(user);
		}

		return "redirect:/users";
	}

	public void DeleteUser() {
		// TODO - implement UserController.DeleteUser
		throw new UnsupportedOperationException();
	}

	@RequestMapping(value = "/{UserName}/edit")
	public void UpdateUserInfo() {
		// TODO - implement UserController.UpdateUserInfo
		throw new UnsupportedOperationException();
	}

}
