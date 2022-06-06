package br.com.cursoudemy.productapi.modules.supplier.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

import br.com.cursoudemy.productapi.modules.supplier.dto.SupplierRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "SUPPLIER")
public class Supplier {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer id;
	
	@Column(name= "NAME", nullable = false)
	private String name;
	
	
	public static Supplier of(SupplierRequest request) {
		var supplier = new Supplier();
		BeanUtils.copyProperties(request, supplier);
		return supplier;
	}
	
}
