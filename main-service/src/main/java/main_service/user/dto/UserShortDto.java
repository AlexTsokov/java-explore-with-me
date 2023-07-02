package main_service.user.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserShortDto {
    private Long id;

    private String name;
}
