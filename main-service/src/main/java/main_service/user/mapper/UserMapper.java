package main_service.user.mapper;

import main_service.user.dto.NewUserRequest;
import main_service.user.dto.UserDto;
import main_service.user.dto.UserShortDto;
import main_service.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(NewUserRequest newUserRequest);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);
}
