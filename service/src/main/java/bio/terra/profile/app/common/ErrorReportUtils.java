package bio.terra.profile.app.common;

import bio.terra.common.exception.ErrorReportException;
import bio.terra.profile.generated.model.ApiErrorReport;
import org.springframework.http.HttpStatus;

/** A common utility for building an ApiErrorReport from an exception. */
public class ErrorReportUtils {

  public static ApiErrorReport buildApiErrorReport(Exception exception) {
    if (exception instanceof ErrorReportException) {
      ErrorReportException errorReport = (ErrorReportException) exception;
      return new ApiErrorReport()
          .message(errorReport.getMessage())
          .statusCode(errorReport.getStatusCode().value())
          .causes(errorReport.getCauses());
    } else {
      return new ApiErrorReport()
          .message(exception.getMessage())
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .causes(null);
    }
  }
}
