package com.rohan.orderflow.common;

import com.rohan.orderflow.product.DuplicateSkuException;
import com.rohan.orderflow.auth.DuplicateEmailException;
import com.rohan.orderflow.product.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.rohan.orderflow.inventory.DuplicateInventoryException;
import com.rohan.orderflow.inventory.InventoryNotFoundException;
import com.rohan.orderflow.inventory.InsufficientInventoryException;
import com.rohan.orderflow.order.OrderNotFoundException;
import com.rohan.orderflow.auth.InvalidCredentialsException;
import com.rohan.orderflow.order.InvalidOrderStateException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(
            ProductNotFoundException exception) {

        Map<String, String> response = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(InsufficientInventoryException.class)
public ResponseEntity<Map<String, String>> handleInsufficientInventory(
        InsufficientInventoryException exception
) {

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
}

    @ExceptionHandler(InventoryNotFoundException.class)
public ResponseEntity<Map<String, String>> handleInventoryNotFound(
        InventoryNotFoundException exception
) {

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
}

        @ExceptionHandler(DuplicateInventoryException.class)
public ResponseEntity<Map<String, String>> handleDuplicateInventory(
        DuplicateInventoryException exception
) {

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
}

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateSku(
            DuplicateSkuException exception) {

        Map<String, String> response = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response = Map.of(
                "errors", errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    @ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<Map<String, String>> handleOrderNotFound(
        OrderNotFoundException exception
) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
}
@ExceptionHandler(InvalidOrderStateException.class)
public ResponseEntity<Map<String, String>> handleInvalidOrderState(
        InvalidOrderStateException exception
) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
}
@ExceptionHandler(DuplicateEmailException.class)
public ResponseEntity<Map<String, String>> handleDuplicateEmail(
        DuplicateEmailException exception
) {

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
}
@ExceptionHandler(InvalidCredentialsException.class)
public ResponseEntity<Map<String, String>> handleInvalidCredentials(
        InvalidCredentialsException exception
) {

    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(Map.of(
                    "error", exception.getMessage()
            ));
}
}