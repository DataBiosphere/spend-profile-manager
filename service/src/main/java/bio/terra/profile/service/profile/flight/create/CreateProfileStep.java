package bio.terra.profile.service.profile.flight.create;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.db.ProfileDao;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.generated.model.ApiProfileModel;
import bio.terra.profile.service.job.JobMapKeys;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.FlightMap;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;
import bio.terra.stairway.exception.RetryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

record CreateProfileStep(
    ProfileDao profileDao, ApiCreateProfileRequest profileRequest, AuthenticatedUserRequest user)
    implements Step {
  private static final Logger logger = LoggerFactory.getLogger(CreateProfileStep.class);

  @Override
  public StepResult doStep(FlightContext flightContext)
      throws RetryException, InterruptedException {
    // TODO: handle step re-running?
    ApiProfileModel profile = profileDao.createBillingProfile(profileRequest, user.getEmail());
    logger.info("Profile created with id {}", profileRequest.getId());

    FlightMap workingMap = flightContext.getWorkingMap();
    workingMap.put(JobMapKeys.RESPONSE.getKeyName(), profile);
    workingMap.put(JobMapKeys.STATUS_CODE.getKeyName(), HttpStatus.CREATED);
    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext flightContext) throws InterruptedException {
    profileDao.deleteBillingProfileById(profileRequest.getId());
    return StepResult.getStepResultSuccess();
  }
}
