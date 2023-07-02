package main_service.category.mapper;

import org.mapstruct.Mapper;
import main_service.category.dto.CategoryDto;
import main_service.category.dto.NewCategoryDto;
import main_service.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category newCategoryDtoToCategory(NewCategoryDto newCategoryDto);
    Category categoryDtoToCategory(CategoryDto categoryDto);
    CategoryDto toCategoryDto(Category category);
}
