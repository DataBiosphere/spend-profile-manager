package bio.terra.spendprofilemanager.app.controller;

import bio.terra.controller.UnauthenticatedApi;
import bio.terra.model.SystemStatus;
import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Api(tags = {"Unauthenticated"})
public class UnauthenticatedApiController implements UnauthenticatedApi {
  @Override
  public ResponseEntity<SystemStatus> serviceStatus() {
    return new ResponseEntity<>(new SystemStatus().ok(true), HttpStatus.OK);
  }
}
