package bio.terra.profile.service.profile.flight.create;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;

public class CreateProfileAuthzIamStep implements Step {
  //  private final IamService iamService;
  private final ApiCreateProfileRequest request;
  private final AuthenticatedUserRequest user;

  public CreateProfileAuthzIamStep(
      //          IamService iamService,
      ApiCreateProfileRequest request, AuthenticatedUserRequest user) {
    //    this.iamService = iamService;
    this.request = request;
    this.user = user;
  }

  @Override
  public StepResult doStep(FlightContext context) throws InterruptedException {
    //    iamService.createProfileResource(user, request.getId().toString());
    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext context) throws InterruptedException {
    //    iamService.deleteProfileResource(user, profileId.toString());
    return StepResult.getStepResultSuccess();
  }
}
