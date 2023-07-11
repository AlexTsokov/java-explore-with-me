package mainservice.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mainservice.comment.dto.CommentDto;
import mainservice.comment.dto.NewCommentDto;
import mainservice.comment.mapper.CommentMapper;
import mainservice.comment.model.Comment;
import mainservice.comment.repository.CommentRepository;
import mainservice.event.enums.EventState;
import mainservice.event.model.Event;
import mainservice.event.repository.EventRepository;
import mainservice.exception.NotFoundException;
import mainservice.user.model.User;
import mainservice.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper mapper;

    @Override
    public List<CommentDto> getCommentsByAdmin(Pageable pageable) {
        return toCommentsDto(commentRepository.findAll(pageable).toList());
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("MainService: commentId={}", commentId);
        checkCommentInBase(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto createCommentByPrivate(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("MainService: userId={}, eventId={}, NewCommentDto={}",
                eventId, userId, newCommentDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataIntegrityViolationException("Невозможно добавить комментарий к событию, статус которого не PUBLISHED");
        }
        Comment comment = Comment.builder()
                .text(newCommentDto.getText())
                .author(user)
                .event(event)
                .created(LocalDateTime.now())
                .build();
        return mapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getCommentsByPrivate(Long userId, Long eventId, Pageable pageable) {
        log.info("MainService: userId={}, eventId={}, pageable={}",
                userId, eventId, pageable);
        checkUserInBase(userId);
        List<Comment> comments;
        if (eventId == null) {
            comments = commentRepository.findAllByAuthorId(userId);
        } else {
            checkEventInBase(eventId);
            comments = commentRepository.findAllByAuthorIdAndEventId(userId, eventId);
        }
        return toCommentsDto(comments);
    }

    @Override
    public CommentDto updateCommentByPrivate(Long userId, Long commentId, NewCommentDto newCommentDto) {
        log.info("MainService: userId={}, commentId={}, newCommentDto={}",
                commentId, userId, newCommentDto);
        checkUserInBase(userId);
        Comment commentFromRepository = getCommentById(commentId);
        checkIfUserIsCommentAuthor(userId, commentFromRepository.getAuthor().getId());
        commentFromRepository.setText(newCommentDto.getText());
        commentFromRepository.setEdited(LocalDateTime.now());
        return mapper.toCommentDto(commentRepository.save(commentFromRepository));
    }

    @Override
    public void deleteCommentByPrivate(Long userId, Long commentId) {
        log.info("MainService: userId={}, commentId={}", userId, commentId);
        checkUserInBase(userId);
        checkIfUserIsCommentAuthor(userId, getCommentById(commentId).getAuthor().getId());
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsByPublic(Long eventId, Pageable pageable) {
        log.info("MainService: eventId={}, pageable={}", eventId, pageable);
        checkEventInBase(eventId);
        return toCommentsDto(commentRepository.findAllByEventId(eventId, pageable));
    }

    @Override
    public CommentDto getCommentByPublic(Long commentId) {
        log.info("MainService: commentId={}", commentId);
        return mapper.toCommentDto(getCommentById(commentId));
    }

    @Override
    public void checkCommentInBase(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с ID " + commentId + " не найден");
        }
    }

    private List<CommentDto> toCommentsDto(List<Comment> comments) {
        return comments.stream()
                .map(mapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + commentId + " не найден"));
    }

    private void checkIfUserIsCommentAuthor(Long userId, Long authorId) {
        if (!Objects.equals(userId, authorId)) {
            throw new DataIntegrityViolationException("Пользователь с ID " + userId + " не является автором комментария");
        }
    }

    public void checkUserInBase(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    public void checkEventInBase(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с ID " + eventId + " не существует");
        }
    }
}
