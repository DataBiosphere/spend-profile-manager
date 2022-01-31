package bio.terra.profile.service.profile.flight.create;

import bio.terra.profile.db.ProfileDao;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.service.profile.exception.DuplicateProfileException;
import bio.terra.profile.service.profile.exception.ProfileNotFoundException;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;
import bio.terra.stairway.StepStatus;

/**
 * Gets a billing profile, and fails if it already exists. This step is designed to run immediately
 * before {@link CreateProfileStep} to ensure idempotency of the create operation.
 */
public record GetProfileStep(ProfileDao profileDao, ApiCreateProfileRequest profileRequest)
    implements Step {
  @Override
  public StepResult doStep(FlightContext context) {
    try {
      profileDao.getBillingProfileById(profileRequest.getId());
      return new StepResult(
          StepStatus.STEP_RESULT_FAILURE_FATAL,
          new DuplicateProfileException(
              String.format(
                  "A billing profile with ID [%s] already exists", profileRequest.getId())));
    } catch (ProfileNotFoundException e) {
      return StepResult.getStepResultSuccess();
    } catch (Exception e) {
      return new StepResult(StepStatus.STEP_RESULT_FAILURE_RETRY, e);
    }
  }

  @Override
  public StepResult undoStep(FlightContext context) {
    // Nothing to undo
    return StepResult.getStepResultSuccess();
  }
}
