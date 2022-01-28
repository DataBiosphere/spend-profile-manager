package bio.terra.profile.service.profile.flight.delete;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;

import java.util.UUID;

public class DeleteProfileAuthzIamStep implements Step {
  //  private final IamService iamService;
  private final UUID profileId;
  private final AuthenticatedUserRequest user;

  public DeleteProfileAuthzIamStep(
//          IamService iamService,
          UUID profileId,
          AuthenticatedUserRequest user) {
//    this.iamService = iamService;
    this.profileId = profileId;
    this.user = user;
  }

  @Override
  public StepResult doStep(FlightContext context) throws InterruptedException {
//    iamService.deleteProfileResource(user, profileId.toString());
    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext context) throws InterruptedException {
    return StepResult.getStepResultSuccess();
  }
}
