package mainservice.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mainservice.category.dto.CategoryDto;
import mainservice.category.dto.NewCategoryDto;
import mainservice.category.mapper.CategoryMapper;
import mainservice.category.model.Category;
import mainservice.category.repository.CategoryRepository;
import mainservice.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("MainService: newCategoryDto={}", newCategoryDto);
        return mapper.toCategoryDto(categoryRepository.save(mapper.newCategoryDtoToCategory(newCategoryDto)));
    }

    @Override
    public List<CategoryDto> getAllCategoryDto(Pageable pageable) {
        log.info("MainService: pageable={}", pageable);
        return categoryRepository.findAll(pageable)
                .stream()
                .map(mapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryDtoById(Long catId) {
        log.info("MainService: catId={}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена"));
        return mapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("MainService: catId={}, categoryDto={}", catId, categoryDto);
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с ID " + catId + " не найдена");
        }
        categoryDto.setId(catId);
        return mapper.toCategoryDto(categoryRepository.save(mapper.categoryDtoToCategory(categoryDto)));
    }

    @Override
    public void deleteCategoryById(Long catId) {
        log.info("MainService: catId={}", catId);
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с ID " + catId + " не найдена");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public Category getCategoryById(Long catId) {
        log.info("MainService: catId={}", catId);
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID " + catId + " не найдена"));
    }

}
