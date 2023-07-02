package mainservice.category.mapper;

import org.mapstruct.Mapper;
import mainservice.category.dto.CategoryDto;
import mainservice.category.dto.NewCategoryDto;
import mainservice.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category newCategoryDtoToCategory(NewCategoryDto newCategoryDto);
    Category categoryDtoToCategory(CategoryDto categoryDto);
    CategoryDto toCategoryDto(Category category);
}
