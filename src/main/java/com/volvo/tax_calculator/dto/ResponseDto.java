package com.volvo.tax_calculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseDto<T> {
    private HttpStatus status;
    private String message;
    private T data;

    public static <T> ResponseDto<T> success(T data, String message) {
        return new ResponseDto<>(HttpStatus.OK, message, data);
    }

    public static <T> ResponseDto<T> error(String message) {
        return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }
}
