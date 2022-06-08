package br.com.cursoudemy.productapi.modules.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCheckStockRequest {
	List<ProductQuantityDTO> products;

}
