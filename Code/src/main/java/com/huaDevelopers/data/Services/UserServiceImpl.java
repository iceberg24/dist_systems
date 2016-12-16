package com.huaDevelopers.data.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.huaDevelopers.dao.UserDAO;
import com.huaDevelopers.data.Entities.User;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserDAO usrDAO;
	
//	@Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public void setUsrDAO(UserDAO usrDAO) {
		this.usrDAO = usrDAO;
	}
	
	@Override
	@Transactional
	public void addUser(User usr) {
//		usr.setPassword(bCryptPasswordEncoder.encode(usr.getPassword()));
		this.usrDAO.addUser(usr);
	}

	@Override
	@Transactional
	public void updateUser(User usr) {
		this.usrDAO.updateUser(usr);
	}

	@Override
	@Transactional
	public User getUserByUsername(String username) {
		return this.usrDAO.getUserByUsername(username);
	}

	@Override
	@Transactional
	public User getUserById(int id) {
		return this.usrDAO.getUserById(id);
	}

	@Override
	@Transactional
	public List<User> listAllUser() {
		return this.usrDAO.listAllUser();
	}

	@Override
	@Transactional
	public void removeUser(int userId) {
		this.usrDAO.removeUser(userId);
	}

}
