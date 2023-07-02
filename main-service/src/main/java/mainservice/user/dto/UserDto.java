package mainservice.user.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserDto {
    private Long id;
    @Size(min = 6, max = 254)
    private String email;
    @Size(min = 2, max = 250)
    private String name;
}
