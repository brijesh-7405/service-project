/**
 * 
 */
package com.workruit.us.application.models;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
@Entity
@Table(name = "supplemental_pay")
public class SupplementalPay implements Serializable {

	private static final long serialVersionUID = 507185152055439918L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "supplemental_pay_id")
	private Integer supplementalPayId;

	@Column(name = "supplemental_pay_name")
	private String supplementalPayName;

}
