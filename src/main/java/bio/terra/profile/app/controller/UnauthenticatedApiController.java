package bio.terra.profile.app.controller;

import bio.terra.controller.UnauthenticatedApi;
import bio.terra.model.ApiSystemStatus;
import bio.terra.model.ApiSystemVersion;
import bio.terra.profile.app.configuration.VersionConfiguration;
import bio.terra.profile.app.service.status.ProfileStatusService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Api(tags = {"Unauthenticated"})
public class UnauthenticatedApiController implements UnauthenticatedApi {

  private final ProfileStatusService statusService;
  private final ApiSystemVersion currentVersion;

  @Autowired
  public UnauthenticatedApiController(
      ProfileStatusService statusService, VersionConfiguration versionConfiguration) {
    this.statusService = statusService;

    this.currentVersion =
        new ApiSystemVersion()
            .gitTag(versionConfiguration.getGitTag())
            .gitHash(versionConfiguration.getGitHash())
            .github(
                "https://github.com/DataBiosphere/spend-profile-manager/commit/"
                    + versionConfiguration.getGitHash())
            .build(versionConfiguration.getBuild());
  }

  @Override
  public ResponseEntity<ApiSystemStatus> serviceStatus() {
    ApiSystemStatus systemStatus = statusService.getStatus();
    HttpStatus httpStatus = systemStatus.isOk() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
    return new ResponseEntity<>(systemStatus, httpStatus);
  }

  @Override
  public ResponseEntity<ApiSystemVersion> serviceVersion() {
    return new ResponseEntity<>(currentVersion, HttpStatus.OK);
  }
}
