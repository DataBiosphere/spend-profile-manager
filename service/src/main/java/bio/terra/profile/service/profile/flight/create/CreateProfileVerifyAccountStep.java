package bio.terra.profile.service.profile.flight.create;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.service.crl.CrlService;
import bio.terra.profile.service.profile.exception.InaccessibleBillingAccountException;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;
import bio.terra.stairway.StepStatus;
import com.google.cloud.billing.v1.BillingAccountName;
import com.google.iam.v1.TestIamPermissionsRequest;
import com.google.iam.v1.TestIamPermissionsResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record CreateProfileVerifyAccountStep(
    CrlService crlService, ApiCreateProfileRequest request, AuthenticatedUserRequest user)
    implements Step {
  private static final Logger logger =
      LoggerFactory.getLogger(CreateProfileVerifyAccountStep.class);

  @Override
  public StepResult doStep(FlightContext context) throws InterruptedException {
    var billingCow = crlService.getCloudBillingClientCow();

    var permissionsToTest = List.of("billing.resourceAssociations.create");
    var testPermissionsRequest =
        TestIamPermissionsRequest.newBuilder()
            .setResource(BillingAccountName.of(request.getBillingAccountId()).toString())
            .addAllPermissions(permissionsToTest)
            .build();

    final TestIamPermissionsResponse testPermissionsResponse;
    try {
      testPermissionsResponse = billingCow.testIamPermissions(testPermissionsRequest);
    } catch (Exception e) {
      return new StepResult(StepStatus.STEP_RESULT_FAILURE_RETRY, e);
    }

    var actualPermissions = testPermissionsResponse.getPermissionsList();
    if (actualPermissions == null || !actualPermissions.equals(permissionsToTest)) {
      var message =
          String.format(
              "The user '%s' needs access to the billing account '%s' to perform the requested operation",
              user.getEmail(), request.getId());
      logger.info(message);
      return new StepResult(
          StepStatus.STEP_RESULT_FAILURE_FATAL, new InaccessibleBillingAccountException(message));
    }

    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext context) throws InterruptedException {
    // Verify account has no side effects to clean up
    return StepResult.getStepResultSuccess();
  }
}
