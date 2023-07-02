package mainservice.user.mapper;

import mainservice.user.dto.NewUserRequest;
import mainservice.user.dto.UserDto;
import mainservice.user.dto.UserShortDto;
import mainservice.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(NewUserRequest newUserRequest);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);
}
