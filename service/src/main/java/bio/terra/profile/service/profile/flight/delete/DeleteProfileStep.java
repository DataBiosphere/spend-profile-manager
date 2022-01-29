package bio.terra.profile.service.profile.flight.delete;

import bio.terra.profile.db.ProfileDao;
import bio.terra.stairway.FlightContext;
import bio.terra.stairway.Step;
import bio.terra.stairway.StepResult;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteProfileStep implements Step {
  private static final Logger logger = LoggerFactory.getLogger(DeleteProfileStep.class);

  private final ProfileDao profileDao;
  private final UUID profileId;

  public DeleteProfileStep(ProfileDao profileDao, UUID profileId) {
    this.profileDao = profileDao;
    this.profileId = profileId;
  }

  @Override
  public StepResult doStep(FlightContext context) {
    profileDao.deleteBillingProfileById(profileId);
    return StepResult.getStepResultSuccess();
  }

  @Override
  public StepResult undoStep(FlightContext context) {
    return StepResult.getStepResultSuccess();
  }
}
