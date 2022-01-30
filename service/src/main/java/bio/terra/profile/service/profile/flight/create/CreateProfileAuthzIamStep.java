package bio.terra.profile.service.profile.flight.create;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.service.iam.SamService;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;

record CreateProfileAuthzIamStep(
    SamService samService, ApiCreateProfileRequest request, AuthenticatedUserRequest user)
    implements Step {

  @Override
  public StepResult doStep(FlightContext context) throws InterruptedException {
    samService.createProfileResource(user, request.getId());
    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext context) throws InterruptedException {
    samService.deleteProfileResource(user, request.getId());
    return StepResult.getStepResultSuccess();
  }
}
