package main_service.category.service;

import main_service.category.dto.CategoryDto;
import main_service.category.dto.NewCategoryDto;
import main_service.category.model.Category;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);
    List<CategoryDto> getAllCategoryDto(Pageable pageable);
    CategoryDto getCategoryDtoById(Long catId);
    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);
    void deleteCategoryById(Long catId);
    Category getCategoryById(Long catId);
}
