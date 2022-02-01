package bio.terra.profile.app.controller;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.common.iam.AuthenticatedUserRequestFactory;
import bio.terra.profile.generated.controller.ProfileApi;
import bio.terra.profile.generated.model.ApiCreateProfileRequest;
import bio.terra.profile.generated.model.ApiCreateProfileResult;
import bio.terra.profile.generated.model.ApiJobReport;
import bio.terra.profile.generated.model.ApiProfileModel;
import bio.terra.profile.service.job.JobService;
import bio.terra.profile.service.profile.ProfileService;
import bio.terra.profile.service.profile.model.BillingProfile;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ProfileApiController implements ProfileApi {
  private final HttpServletRequest request;
  private final ProfileService profileService;
  private final AuthenticatedUserRequestFactory authenticatedUserRequestFactory;
  private final JobService jobService;

  @Autowired
  public ProfileApiController(
      HttpServletRequest request,
      ProfileService profileService,
      JobService jobService,
      AuthenticatedUserRequestFactory authenticatedUserRequestFactory) {
    this.request = request;
    this.profileService = profileService;
    this.jobService = jobService;
    this.authenticatedUserRequestFactory = authenticatedUserRequestFactory;
  }

  @Override
  public ResponseEntity<ApiCreateProfileResult> createProfile(
      @RequestBody ApiCreateProfileRequest body) {
    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
    BillingProfile profile = BillingProfile.fromApiCreateProfileRequest(body);
    String jobId = profileService.createProfile(profile, user);
    final ApiCreateProfileResult result = fetchCreateProfileResult(jobId, user);
    return new ResponseEntity<>(result, getAsyncResponseCode(result.getJobReport()));
  }

  @Override
  public ResponseEntity<ApiCreateProfileResult> getCreateProfileResult(
      @PathVariable("jobId") String jobId) {
    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
    final ApiCreateProfileResult response = fetchCreateProfileResult(jobId, user);
    return new ResponseEntity<>(response, getAsyncResponseCode(response.getJobReport()));
  }

  @Override
  public ResponseEntity<ApiProfileModel> getProfile(@PathVariable("profileId") UUID profileId) {
    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
    BillingProfile profile = profileService.getProfile(profileId, user);
    return new ResponseEntity<>(profile.toApiProfileModel(), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteProfile(@PathVariable("profileId") UUID id) {
    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
    profileService.deleteProfile(id, user);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  //  @Override
  //  public ResponseEntity<JobModel> updateProfile(
  //      @Valid @RequestBody BillingProfileUpdateModel billingProfileRequest) {
  //    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
  //    String jobId = profileService.updateProfile(billingProfileRequest, user);
  //    return jobToResponse(jobService.retrieveJob(jobId, user));
  //  }
  //

  //  @Override
  //  public ResponseEntity<EnumerateBillingProfileModel> enumerateProfiles(
  //      @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Integer
  // offset,
  //      @Valid @RequestParam(value = "limit", required = false, defaultValue = "10") Integer
  // limit) {
  //    ControllerUtils.validateEnumerateParams(offset, limit);
  //    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
  //    EnumerateBillingProfileModel ebpm = profileService.enumerateProfiles(offset, limit, user);
  //    return new ResponseEntity<>(ebpm, HttpStatus.OK);
  //  }
  //
  //  @Override
  //  public ResponseEntity<BillingProfileModel> retrieveProfile(UUID id) {
  //    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
  //    BillingProfileModel profileModel = profileService.getProfileById(id, user);
  //    return new ResponseEntity<>(profileModel, HttpStatus.OK);
  //  }
  //
  //  @Override
  //  public ResponseEntity<PolicyResponse> addProfilePolicyMember(
  //      @PathVariable("id") UUID id,
  //      @PathVariable("policyName") String policyName,
  //      @Valid @RequestBody PolicyMemberRequest policyMember) {
  //    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
  //    PolicyModel policy = profileService.addProfilePolicyMember(id, policyName, policyMember,
  // user);
  //    PolicyResponse response = new PolicyResponse().policies(Collections.singletonList(policy));
  //    return new ResponseEntity<>(response, HttpStatus.OK);
  //  }
  //
  //  @Override
  //  public ResponseEntity<PolicyResponse> deleteProfilePolicyMember(
  //      @PathVariable("id") UUID id,
  //      @PathVariable("policyName") String policyName,
  //      @PathVariable("memberEmail") String memberEmail) {
  //    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
  //    PolicyModel policy =
  //        profileService.deleteProfilePolicyMember(id, policyName, memberEmail, user);
  //    PolicyResponse response = new PolicyResponse().policies(Collections.singletonList(policy));
  //    return new ResponseEntity<>(response, HttpStatus.OK);
  //  }
  //
  //  @Override
  //  public ResponseEntity<PolicyResponse> retrieveProfilePolicies(@PathVariable("id") UUID id) {
  //    AuthenticatedUserRequest user = authenticatedUserRequestFactory.from(request);
  //    List<PolicyModel> policies = profileService.retrieveProfilePolicies(id, user);
  //    PolicyResponse response = new PolicyResponse().policies(policies);
  //    return new ResponseEntity<>(response, HttpStatus.OK);
  //  }

  private ApiCreateProfileResult fetchCreateProfileResult(
      String jobId, AuthenticatedUserRequest userRequest) {
    final JobService.AsyncJobResult<BillingProfile> jobResult =
        jobService.retrieveAsyncJobResult(jobId, BillingProfile.class, userRequest);
    return new ApiCreateProfileResult()
        .jobReport(jobResult.getJobReport())
        .errorReport(jobResult.getApiErrorReport())
        .profileDescription(
            Optional.ofNullable(jobResult.getResult())
                .map(BillingProfile::toApiProfileModel)
                .orElse(null));
  }

  private static HttpStatus getAsyncResponseCode(ApiJobReport jobReport) {
    return jobReport.getStatus() == ApiJobReport.StatusEnum.RUNNING
        ? HttpStatus.ACCEPTED
        : HttpStatus.OK;
  }
}
