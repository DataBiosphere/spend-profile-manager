package bio.terra.profile.service.profile.flight.create;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.service.crl.CrlService;
import bio.terra.profile.service.profile.exception.InaccessibleApplicationDeploymentException;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;

public class CreateProfileVerifyDeployedApplicationStep implements Step {
  private final CrlService crlService;
  private final ApiCreateProfileRequest request;
  private final AuthenticatedUserRequest user;

  public CreateProfileVerifyDeployedApplicationStep(
          CrlService crlService,
      ApiCreateProfileRequest request,
      AuthenticatedUserRequest user) {
    this.crlService = crlService;
    this.request = request;
    this.user = user;
  }

  @Override
  public StepResult doStep(FlightContext context) throws InterruptedException {
    // TODO: implement Azure logic
    if (false) {
      throw new InaccessibleApplicationDeploymentException(
              String.format(
                      "The user '%s' needs access to teh deployed application '%s' to perform the requested operation",
                      user.getEmail(),
                      request.getApplicationDeploymentName()
              ));
    }
    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext context) throws InterruptedException {
    // Verify account has no side effects to clean up
    return StepResult.getStepResultSuccess();
  }
}
