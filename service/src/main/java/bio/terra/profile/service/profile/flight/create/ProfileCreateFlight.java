package bio.terra.profile.service.profile.flight.create;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.db.ProfileDao;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.service.crl.CrlService;
import bio.terra.profile.service.job.JobMapKeys;
import bio.terra.stairway.Flight;
import bio.terra.stairway.FlightMap;
import org.springframework.context.ApplicationContext;

public class ProfileCreateFlight extends Flight {

  public ProfileCreateFlight(FlightMap inputParameters, Object applicationContext) {
    super(inputParameters, applicationContext);

    ApplicationContext appContext = (ApplicationContext) applicationContext;
    ProfileDao profileDao = appContext.getBean(ProfileDao.class);
    CrlService crlService = appContext.getBean(CrlService.class);

    ApiCreateProfileRequest request =
        inputParameters.get(JobMapKeys.REQUEST.getKeyName(), ApiCreateProfileRequest.class);
    AuthenticatedUserRequest user =
        inputParameters.get(JobMapKeys.AUTH_USER_INFO.getKeyName(), AuthenticatedUserRequest.class);

    addStep(new CreateProfileStep(profileDao, request, user));
    switch (request.getCloudPlatform()) {
      case GCP:
        addStep(new CreateProfileVerifyAccountStep(crlService, request, user));
        break;
      case AZURE:
        addStep(new CreateProfileVerifyDeployedApplicationStep(crlService, request, user));
        break;
    }
    addStep(new CreateProfileAuthzIamStep(request, user));
  }
}
