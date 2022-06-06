package br.com.cursoudemy.productapi.modules.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.cursoudemy.productapi.modules.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	List<Category> findByDescriptionIgnoreCaseContaining(String description);
}
