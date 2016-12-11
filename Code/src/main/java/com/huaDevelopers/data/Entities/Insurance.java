package com.huaDevelopers.data.Entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "Insurance")
public class Insurance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1118564151849532911L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "insurance_id")
	private int id;

	@NotEmpty
	@Column(name = "license_plate", length = 7, unique = true, nullable = false)
	private Vehicle LicensePlate;

	@NotEmpty
	@Column(name = "date_start", nullable = false)
	private Date InsuranceDate;

	@NotEmpty
	@Column(name = "price", nullable = false)
	private float price;

	@Column(name = "discount", nullable = true)
	private float discount;

	@NotEmpty
	@Column(name = "type", nullable = false)
	private String Type;

	@Column(name = "new_driver", nullable = true)
	private Boolean NewDriver;

	public Insurance() {
		// TODO Auto-generated constructor stub
	}

	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	@OneToOne
	@JoinColumn(name="license_plate", nullable=false)
	public Vehicle getLicensePlate() {
		return this.LicensePlate;
	}

	public void setLicensePlate(Vehicle licensePlate) {
		this.LicensePlate = licensePlate;
	}

	public Date getInsuranceDate() {
		return this.InsuranceDate;
	}

	public void setInsuranceDate(Date insuranceDate) {
		this.InsuranceDate = insuranceDate;
	}

	public float getPrice() {
		return this.price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getDiscount() {
		return discount;
	}

	public void setDiscount(float discount) {
		this.discount = discount;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public Boolean getNewDriver() {
		return NewDriver;
	}

	public void setNewDriver(Boolean newDriver) {
		NewDriver = newDriver;
	}

}
