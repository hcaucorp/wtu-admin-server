package es.coffeebyt.wtu.api;

import lombok.Value;

import org.springframework.http.HttpStatus;

import java.util.List;

@Value
public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> errors;

}
