package bio.terra.profile.service.profile.flight.create;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.service.crl.CrlService;
import bio.terra.profile.service.profile.exception.InaccessibleBillingAccountException;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;

public class CreateProfileVerifyAccountStep implements Step {
  private final CrlService crlService;
  private final ApiCreateProfileRequest request;
  private final AuthenticatedUserRequest user;

  public CreateProfileVerifyAccountStep(
          CrlService crlService,
      ApiCreateProfileRequest request,
      AuthenticatedUserRequest user) {
    this.crlService = crlService;
    this.request = request;
    this.user = user;
  }

  @Override
  public StepResult doStep(FlightContext context) throws InterruptedException {
    var billingCow = crlService.getCloudBillingClientCow();

    // TODO: add billingCow.canAccess(user, billingAccountId) to CRL
    if (false) {
      throw new InaccessibleBillingAccountException(
              String.format("The user '%s' needs access to the billing account '%s' to perform the requested operation",
                      user.getEmail(),
                      request.getId()));
    }
    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext context) throws InterruptedException {
    // Verify account has no side effects to clean up
    return StepResult.getStepResultSuccess();
  }
}
