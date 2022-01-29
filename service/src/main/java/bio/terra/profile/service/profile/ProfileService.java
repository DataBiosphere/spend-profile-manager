package bio.terra.profile.service.profile;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.db.ProfileDao;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.generated.model.ApiProfileModel;
import bio.terra.profile.service.iam.SamAction;
import bio.terra.profile.service.iam.SamResourceType;
import bio.terra.profile.service.iam.SamRethrow;
import bio.terra.profile.service.iam.SamService;
import bio.terra.profile.service.job.JobMapKeys;
import bio.terra.profile.service.job.JobService;
import bio.terra.profile.service.profile.exception.ProfileNotFoundException;
import bio.terra.profile.service.profile.flight.ProfileMapKeys;
import bio.terra.profile.service.profile.flight.create.CreateProfileFlight;
import bio.terra.profile.service.profile.flight.delete.DeleteProfileFlight;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
  private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

  private final ProfileDao profileDao;
  private final SamService samService;
  private final JobService jobService;

  @Autowired
  public ProfileService(ProfileDao profileDao, SamService samService, JobService jobService) {
    this.profileDao = profileDao;
    this.samService = samService;
    this.jobService = jobService;
  }

  /**
   * Create a new billing profile.
   *
   * @param billingProfileRequest request to create a billing profile
   * @param user authenticated user
   * @return jobId of the submitted Stairway job
   */
  public String createProfile(
      ApiCreateProfileRequest billingProfileRequest, AuthenticatedUserRequest user) {
    String description =
        String.format("Create billing profile '%s'", billingProfileRequest.getDisplayName());
    return jobService
        .newJob()
        .description(description)
        .flightClass(CreateProfileFlight.class)
        .request(billingProfileRequest)
        .userRequest(user)
        .submit();
  }

  //  /**
  //   * Update billing profile. We make the following checks:
  //   *
  //   * <ul>
  //   *   <li>The service must have proper permissions on the google billing account
  //   *   <li>The caller must have billing.resourceAssociation.create permission on the google
  // billing
  //   *       account
  //   *   <li>The google billing account must be enabled
  //   * </ul>
  //   *
  //   * @param billingProfileRequest request with changes to billing profile
  //   * @param user the user attempting to update the billing profile
  //   * @return jobId of the submitted stairway job
  //   */
  //  public String updateProfile(
  //          ApiUpdateProfileRequest billingProfileRequest, AuthenticatedUserRequest user) {
  //    iamService.verifyAuthorization(
  //        user,
  //        IamResourceType.SPEND_PROFILE,
  //        billingProfileRequest.getId().toString(),
  //        IamAction.UPDATE_BILLING_ACCOUNT);
  //
  //    String description =
  //        String.format("Update billing for profile id '%s'", billingProfileRequest.getId());
  //    return jobService
  //
  // .newJob().description(description).flightClass(ProfileUpdateFlight.class).request(billingProfileRequest).userRequest(user)
  //        .submit();
  //  }
  //
  /**
   * Delete billing profile. Blocks until the deletion is complete.
   *
   * @param id unique ID of the billing profile to delete
   * @param user authenticated user
   */
  public void deleteProfile(UUID id, AuthenticatedUserRequest user) {
    SamRethrow.onInterrupted(
        () ->
            samService.verifyAuthorization(
                user, SamResourceType.PROFILE, id.toString(), SamAction.DELETE),
        "verifyAuthorization");
    var billingProfile = profileDao.getBillingProfileById(id);
    var platform = billingProfile.getCloudPlatform();
    var description = String.format("Delete billing profile id '%s'", id);
    var deleteJob =
        jobService
            .newJob()
            .description(description)
            .flightClass(DeleteProfileFlight.class)
            .userRequest(user)
            .addParameter(ProfileMapKeys.PROFILE_ID, id)
            .addParameter(JobMapKeys.CLOUD_PLATFORM.getKeyName(), platform.name());
    deleteJob.submitAndWait(null);
  }

  //  /**
  //   * Enumerate the profiles that are visible to the requesting user
  //   *
  //   * @param offset start of the range of profiles to return for this request
  //   * @param limit maximum number of profiles to return in this request
  //   * @param user user on whose behalf we are making this request
  //   * @return enumeration profile containing the list and total
  //   */
  //  public EnumerateBillingProfileModel enumerateProfiles(
  //      Integer offset, Integer limit, AuthenticatedUserRequest user) {
  //    Set<UUID> resources =
  //        iamService.listAuthorizedResources(user, IamResourceType.SPEND_PROFILE).keySet();
  //    if (resources.isEmpty()) {
  //      return new EnumerateBillingProfileModel().total(0).items(List.of());
  //    }
  //    return profileDao.enumerateBillingProfiles(offset, limit, resources);
  //  }
  //
  /**
   * Retrieves a billing profile by ID.
   *
   * @param id unique ID of the billing profile to retrieve
   * @param user authenticated user
   * @return billing profile model
   * @throws ProfileNotFoundException when the profile is not found
   */
  public ApiProfileModel getProfile(UUID id, AuthenticatedUserRequest user) {
    var hasActions =
        SamRethrow.onInterrupted(
            () -> samService.hasActions(user, SamResourceType.PROFILE, id.toString()),
            "hasActions");
    if (!hasActions) {
      throw new UnauthorizedException("unauthorized");
    }
    return profileDao.getBillingProfileById(id);
  }

  //  /**
  //   * Lookup a billing profile by the profile id with no auth check. Used for internal
  // references.
  //   *
  //   * @param id the unique idea of this billing profile
  //   * @return On success, the billing profile model
  //   * @throws ProfileNotFoundException when the profile is not found
  //   */
  //  public BillingProfileModel getProfileByIdNoCheck(UUID id) {
  //    return profileDao.getBillingProfileById(id);
  //  }
  //
  //  // The idea is to use this call from create snapshot and create asset to validate that the
  //  // billing account is usable by the calling user
  //
  //  /**
  //   * Called by services to verify that a profile exists, that the user has the link permission
  // on
  //   * the profile, that the underlying billing account is usable, and that there is a path of
  //   * delegation to the user. The path of delegation is formed by one of the owners of the
  // billing
  //   * profile having "create link" permission on the billing account.
  //   *
  //   * @param profileId the profile id to attempt to authorize
  //   * @param user the user attempting associate some object with the profile
  //   * @return the profile model associated with the profile id
  //   */
  //  public BillingProfileModel authorizeLinking(UUID profileId, AuthenticatedUserRequest user) {
  //    logger.info("Verify authorization for link id={} user={}", profileId, user.getEmail());
  //    iamService.verifyAuthorization(
  //        user, IamResourceType.SPEND_PROFILE, profileId.toString(), IamAction.LINK);
  //    return profileDao.getBillingProfileById(profileId);
  //  }
  //
  //  public PolicyModel addProfilePolicyMember(
  //      UUID profileId,
  //      String policyName,
  //      PolicyMemberRequest policyMember,
  //      AuthenticatedUserRequest user) {
  //    return iamService.addPolicyMember(
  //        user, IamResourceType.SPEND_PROFILE, profileId, policyName, policyMember.getEmail());
  //  }
  //
  //  public PolicyModel deleteProfilePolicyMember(
  //      UUID profileId, String policyName, String memberEmail, AuthenticatedUserRequest user) {
  //    logger.info(
  //        "id={} policy={} email={} authuser={}",
  //        profileId,
  //        policyName,
  //        memberEmail,
  //        user.getEmail());
  //    // member email can't be null since it is part of the URL
  //    if (!ValidationUtils.isValidEmail(memberEmail)) {
  //      throw new ValidationException("InvalidMemberEmail");
  //    }
  //
  //    return iamService.deletePolicyMember(
  //        user, IamResourceType.SPEND_PROFILE, profileId, policyName, memberEmail);
  //  }
  //
  //  public List<PolicyModel> retrieveProfilePolicies(UUID profileId, AuthenticatedUserRequest
  // user) {
  //    return iamService.retrievePolicies(user, IamResourceType.SPEND_PROFILE, profileId);
  //  }
  //
  //  // -- methods invoked from billing profile flights --
  //
  //  public BillingProfileModel createProfileMetadata(
  //      BillingProfileRequestModel profileRequest, AuthenticatedUserRequest user) {
  //    return profileDao.createBillingProfile(profileRequest, user.getEmail());
  //  }
  //
  //  public BillingProfileModel updateProfileMetadata(BillingProfileUpdateModel profileRequest) {
  //    return profileDao.updateBillingProfileById(profileRequest);
  //  }
  //
  //  public boolean deleteProfileMetadata(UUID profileId) {
  //    // TODO: refuse to delete if there are dependent projects
  //    return profileDao.deleteBillingProfileById(profileId);
  //  }
  //
  //  public void createProfileIamResource(
  //      BillingProfileRequestModel request, AuthenticatedUserRequest user) {
  //    iamService.createProfileResource(user, request.getId().toString());
  //  }
  //
  //  public void deleteProfileIamResource(UUID profileId, AuthenticatedUserRequest user) {
  //    iamService.deleteProfileResource(user, profileId.toString());
  //  }
  //

}
