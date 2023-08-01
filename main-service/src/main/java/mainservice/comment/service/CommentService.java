package mainservice.comment.service;

import mainservice.comment.dto.CommentDto;
import mainservice.comment.dto.NewCommentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    List<CommentDto> getCommentsByAdmin(Pageable pageable);

    void deleteCommentByAdmin(Long commentId);

    CommentDto createCommentByPrivate(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getCommentsByPrivate(Long userId, Long eventId, Pageable pageable);

    CommentDto updateCommentByPrivate(Long userId, Long commentId, NewCommentDto newCommentDto);

    void deleteCommentByPrivate(Long userId, Long commentId);

    List<CommentDto> getCommentsByPublic(Long eventId, Pageable pageable);

    CommentDto getCommentByPublic(Long commentId);

    void checkCommentInBase(Long commentId);
}
