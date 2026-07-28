package com.fpt.swp.sealhackathonbe;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandlerLogging {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String log = LocalDateTime.now() + " - " + e.getMessage() + "\n" + sw.toString() + "\n\n";
            Files.writeString(Paths.get("C:/temp/backend_error.log"), log, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.writeString(Paths.get("backend_error.log"), log, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ex) {}
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
