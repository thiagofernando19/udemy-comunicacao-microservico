package br.com.cursoudemy.productapi.modules.category.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.cursoudemy.productapi.config.exception.SuccessResponse;
import br.com.cursoudemy.productapi.config.exception.ValidationException;
import br.com.cursoudemy.productapi.modules.category.dto.CategoryRequest;
import br.com.cursoudemy.productapi.modules.category.dto.CategoryResponse;
import br.com.cursoudemy.productapi.modules.category.model.Category;
import br.com.cursoudemy.productapi.modules.category.repository.CategoryRepository;
import br.com.cursoudemy.productapi.modules.product.service.ProductService;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository  categoryRepository;
	
	@Autowired
	private ProductService  productService;
	
	
	public List<CategoryResponse> findAll(){
		return categoryRepository
				.findAll()
				.stream()
				.map(CategoryResponse :: of)
				.collect(Collectors.toList());
	}
	
	public CategoryResponse findByIdResponse(Integer id) {
		return CategoryResponse.of(findById(id));
	}
	
	public List<CategoryResponse> findByDescription(String description){
		if(description.isEmpty()) {
			throw new ValidationException("The category description must be informed.");
		}
		return categoryRepository
				.findByDescriptionIgnoreCaseContaining(description)
				.stream()
				.map(CategoryResponse :: of)
				.collect(Collectors.toList());
	}
	
	public Category findById(Integer id) {
		validateInformedId(id);
		return categoryRepository
				.findById(id)
				.orElseThrow(() -> new ValidationException("There's no category for the given ID."));
	}
	
	public CategoryResponse save(CategoryRequest request) {
		validateCategoryNameInformed(request);
		var category  = categoryRepository.save(Category.of(request));
		return CategoryResponse.of(category);
	}
	
	public CategoryResponse update(CategoryRequest request, Integer id) {
		validateCategoryNameInformed(request);
		validateInformedId(id);
		var category  = Category.of(request);
		category.setId(id);
		categoryRepository.save(category);
		return CategoryResponse.of(category);
	}
	
	private void validateCategoryNameInformed(CategoryRequest request) {
		if(request.getDescription().isEmpty()) {
			throw new ValidationException("The category description was not informed.");
		}
	}
	
	public SuccessResponse delete(Integer id) {
		validateInformedId(id);
		
		if(productService.existsByCategoryId(id)) {
			throw new ValidationException("You cannot delete this category it's already defined by a product.");
		}
		
		categoryRepository.deleteById(id);
		return SuccessResponse.create("The category was deleted.");
		
	}
	
	private void validateInformedId(Integer id) {
		if(id == null) {
			throw new ValidationException("The category ID must be informed.");
		}
	}
	
	
	
}
