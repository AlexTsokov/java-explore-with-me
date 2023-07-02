package mainservice.user.service;

import org.springframework.data.domain.Pageable;
import mainservice.user.dto.NewUserRequest;
import mainservice.user.dto.UserDto;
import mainservice.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    User getUserById(Long id);

    List<UserDto> getUsersDto(List<Long> ids, Pageable pageable);

    void deleteUserById(Long id);

    void checkUserInBase(Long id);
}
