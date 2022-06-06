package br.com.cursoudemy.productapi.modules.supplier.dto;

import org.springframework.beans.BeanUtils;

import br.com.cursoudemy.productapi.modules.supplier.model.Supplier;
import lombok.Data;

@Data
public class SupplierResponse {
	private Integer id;
	private String name;
	
	public static SupplierResponse of(Supplier supplier) {
		var response = new SupplierResponse();
		BeanUtils.copyProperties(supplier, response);
		return response;
	}
}
