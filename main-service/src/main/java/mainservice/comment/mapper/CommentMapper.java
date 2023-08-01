package mainservice.comment.mapper;

import mainservice.comment.dto.CommentDto;
import mainservice.comment.model.Comment;
import mainservice.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    @Mapping(target = "eventId", expression = "java(comment.getEvent().getId())")
    CommentDto toCommentDto(Comment comment);
}
