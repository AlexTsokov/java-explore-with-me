package mainservice.comment.controller;

import lombok.RequiredArgsConstructor;
import mainservice.comment.dto.CommentDto;
import mainservice.comment.dto.NewCommentDto;
import mainservice.comment.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;

import javax.validation.Valid;
import java.util.List;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
@Validated
public class CommentPrivateController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createCommentByPrivate(@PathVariable Long userId,
                                             @RequestParam Long eventId,
                                             @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.createCommentByPrivate(userId, eventId, newCommentDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByPrivate(
            @PathVariable Long userId,
            @RequestParam(required = false) Long eventId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        return commentService.getCommentsByPrivate(userId, eventId, PageRequest.of(from / size, size));
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateCommentByPrivate(@PathVariable Long userId,
                                             @PathVariable Long commentId,
                                             @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.updateCommentByPrivate(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByPrivate(@PathVariable Long userId,
                                       @PathVariable Long commentId) {
        commentService.deleteCommentByPrivate(userId, commentId);
    }

}
