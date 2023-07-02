package main_service.compilation.service;

import org.springframework.data.domain.Pageable;
import main_service.compilation.dto.CompilationDto;
import main_service.compilation.dto.NewCompilationDto;
import main_service.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilationById(Long compId);

    List<CompilationDto> getAllCompilationDto(Boolean pinned, Pageable pageable);

    CompilationDto getCompilationDtoById(Long compId);

}
