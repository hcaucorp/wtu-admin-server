package es.coffeebyt.wtu.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {

    private int status;
    private String message;
    private String error;
    private String timestamp;
    private String path;

}
