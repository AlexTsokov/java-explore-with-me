package mainservice.compilation.service;

import org.springframework.data.domain.Pageable;
import mainservice.compilation.dto.CompilationDto;
import mainservice.compilation.dto.NewCompilationDto;
import mainservice.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilationById(Long compId);

    List<CompilationDto> getAllCompilationDto(Boolean pinned, Pageable pageable);

    CompilationDto getCompilationDtoById(Long compId);

}
