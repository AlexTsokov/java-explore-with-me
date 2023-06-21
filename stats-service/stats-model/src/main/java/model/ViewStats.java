package model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}
