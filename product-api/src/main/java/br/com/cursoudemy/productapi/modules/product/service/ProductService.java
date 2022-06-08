package br.com.cursoudemy.productapi.modules.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.cursoudemy.productapi.config.exception.SuccessResponse;
import br.com.cursoudemy.productapi.config.exception.ValidationException;
import br.com.cursoudemy.productapi.modules.category.service.CategoryService;
import br.com.cursoudemy.productapi.modules.product.dto.ProductCheckStockRequest;
import br.com.cursoudemy.productapi.modules.product.dto.ProductQuantityDTO;
import br.com.cursoudemy.productapi.modules.product.dto.ProductRequest;
import br.com.cursoudemy.productapi.modules.product.dto.ProductResponse;
import br.com.cursoudemy.productapi.modules.product.dto.ProductSalesResponse;
import br.com.cursoudemy.productapi.modules.product.dto.ProductStockDTO;
import br.com.cursoudemy.productapi.modules.product.model.Product;
import br.com.cursoudemy.productapi.modules.product.repository.ProductRepository;
import br.com.cursoudemy.productapi.modules.sales.client.SalesClient;
import br.com.cursoudemy.productapi.modules.sales.dto.SalesConfirmationDTO;
import br.com.cursoudemy.productapi.modules.sales.enums.SalesStatus;
import br.com.cursoudemy.productapi.modules.sales.rabbitmq.SalesConfirmationSender;
import br.com.cursoudemy.productapi.modules.supplier.service.SupplierService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {
	private static final Integer ZERO = 0; 
	@Autowired
	private ProductRepository  productRepository;
	
	@Autowired
	private SupplierService supplierService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private SalesConfirmationSender salesConfirmationSender;
	
	@Autowired
	private SalesClient salesClient;

	public List<ProductResponse> findAll(){
		return productRepository
				.findAll()
				.stream()
				.map(ProductResponse :: of)
				.collect(Collectors.toList());
	}
	
	
	
	public List<ProductResponse> findByName(String name){
		if(name == null || name.isEmpty()) {
			throw new ValidationException("The product name must be informed.");
		}
		return productRepository
				.findByNameIgnoreCaseContaining(name)
				.stream()
				.map(ProductResponse :: of)
				.collect(Collectors.toList());
	}
	
	public List<ProductResponse> findBySupplierId(Integer supplierId){
		if(supplierId == null ) {
			throw new ValidationException("The product supplier ID name must be informed.");
		}
		return productRepository
				.findBySupplierId(supplierId)
				.stream()
				.map(ProductResponse :: of)
				.collect(Collectors.toList());
	}
	public List<ProductResponse> findByCategoryId(Integer categoryId){
		if(categoryId == null ) {
			throw new ValidationException("The product category ID name must be informed.");
		}
		return productRepository
				.findByCategoryId(categoryId)
				.stream()
				.map(ProductResponse :: of)
				.collect(Collectors.toList());
	}
	public ProductResponse findByIdResponse(Integer id) {
		return ProductResponse.of(findById(id));
	}
	
	public Product findById(Integer id) {
		validateInformedId(id);
		return productRepository
				.findById(id)
				.orElseThrow(() -> new ValidationException("There's no product for the given ID."));
	}	
	public ProductResponse save(ProductRequest request) {
		validateProductDataInformed(request);
		validateCategoryAndSupplierIdInformed(request);
		var category = categoryService.findById(request.getCategoryId());
		var supplier = supplierService.findById(request.getSupplierId());
		var product  = productRepository.save(Product.of(request, supplier, category));
		return ProductResponse.of(product);
	}
	
	public ProductResponse update(ProductRequest request, Integer id) {
		validateProductDataInformed(request);
		validateInformedId(id);
		validateCategoryAndSupplierIdInformed(request);
		var category = categoryService.findById(request.getCategoryId());
		var supplier = supplierService.findById(request.getSupplierId());
		var product  = Product.of(request, supplier, category);
		product.setId(id);		
		productRepository.save(product);
		return ProductResponse.of(product);
	}
	
	
	private void validateProductDataInformed(ProductRequest request) {
		if(request.getName().isEmpty()) {
			throw new ValidationException("The product's name was not informed.");
		}
		
		if(request.getQuantityAvailable() != null && request.getQuantityAvailable() <= ZERO) {
			throw new ValidationException("The category quantity was not informed.");
		}
	}
	
	private void validateCategoryAndSupplierIdInformed(ProductRequest request) {
		if(request.getCategoryId() <= ZERO) {
			throw new ValidationException("The category ID was not informed.");
		}
		
		if(request.getSupplierId() <= ZERO) {
			throw new ValidationException("The supplier ID was not informed.");
		}
	}
	
	
	public Boolean existsByCategoryId(Integer categoryId) {
		return productRepository.existsByCategoryId(categoryId);
	}
	
	public Boolean existsBySupplierId(Integer supplierId) {
		return productRepository.existsBySupplierId(supplierId);
	}
	
	public SuccessResponse delete(Integer id) {
		validateInformedId(id);
		
		productRepository.deleteById(id);
		return SuccessResponse.create("The product was deleted.");
		
	}
	
	private void validateInformedId(Integer id) {
		if(id == null) {
			throw new ValidationException("The product ID must be informed.");
		}
	}
	
	public void  updateProductStock(ProductStockDTO product) {
		try {
			validateStockUpdateData(product);
			updateStock(product);
			
		} catch (Exception e) {
			log.error("Error while trying to update stock for message with error: {}", e.getCause(),e);
			var rejectMessage = new SalesConfirmationDTO(product.getSalesId(), SalesStatus.REJECT);
			salesConfirmationSender.sendSalesConfirmationMessage(rejectMessage);
			
		}
	}
	
	@Transactional
	private void validateStockUpdateData(ProductStockDTO product) {
		if(product == null 
				|| product.getSalesId() == null
				|| product.getSalesId().equals("")) {
			throw new ValidationException("The product data and the sales ID must be informed.");
		}
		if( product.getProducts() == null
				|| product.getProducts().isEmpty()) {
			throw new ValidationException("The sale's products must be informed.");
		}

		product
			.getProducts()
			.forEach(salesProduct ->{
				if(salesProduct.getQuantity() == null
						|| salesProduct.getProductId() == null) {
					throw new ValidationException("The productID and the quantity must be informed.");
				}
			});
	}
	
	private void updateStock(ProductStockDTO product) {
		var productForUpdate = new ArrayList<Product>();
		product
		.getProducts()
		.forEach(salesProduct ->{
			var existingProduct = findById(salesProduct.getProductId());
			validateQuantityInStock(salesProduct, existingProduct);
			existingProduct.updateStock(salesProduct.getQuantity());
			productForUpdate.add(existingProduct);
		});
		
		if(!productForUpdate.isEmpty()) {
			productRepository.saveAll(productForUpdate);
			var approvedMessage = new SalesConfirmationDTO(product.getSalesId(),SalesStatus.APPROVED);
			salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
		}
	}
	
	
	private void validateQuantityInStock(ProductQuantityDTO salesProduct, Product existingProduct) {
		if(salesProduct.getQuantity() > existingProduct.getQuantityAvailable()) {
			throw new ValidationException(
					String.format("The product %s is out of stock.", existingProduct.getId()));
		}
	}
	
	public ProductSalesResponse findProductSales(Integer id) {
		var product = findById(id);
		try {
			var sales = salesClient
					.findSalesByProductId(product.getId())
					.orElseThrow(()-> new ValidationException("The sales was not found by this product."));
			
			return ProductSalesResponse.of(product, sales.getSalesIds());
		} catch (Exception e) {
			throw new ValidationException("There was an error trying to get the product's sales.");
		}
	}
	
	public SuccessResponse checkProductsStock(ProductCheckStockRequest request) {
		if(request == null || request.getProducts() == null) {
			throw new ValidationException("The request data and products must not be informed.");
		}
		
		request
			.getProducts()
			.forEach(this :: validateStock );
		return SuccessResponse.create("The stock s ok!");
	}
	
	private void validateStock(ProductQuantityDTO productQuantity) {
		if(productQuantity.getProductId() == null || productQuantity.getQuantity() == null) {
			throw new ValidationException("Product ID and quantity must br informed.");
		}
		var product = findById(productQuantity.getProductId());
		if(productQuantity.getQuantity() > product.getQuantityAvailable()) {
			throw new ValidationException(String.format("The product %s is out of stock.",product.getId()));
		}
	}
}
