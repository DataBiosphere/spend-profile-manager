package bio.terra.profile.app.controller;

import bio.terra.common.exception.AbstractGlobalExceptionHandler;
import bio.terra.profile.generated.model.ApiErrorReport;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler<ApiErrorReport> {
    @Override
    public ApiErrorReport generateErrorReport(Throwable ex, HttpStatus statusCode, List<String> causes) {
        return new ApiErrorReport().message(ex.getMessage()).statusCode(statusCode.value()).causes(causes);
    }
}

