package bio.terra.profile.service.iam;

import bio.terra.common.exception.ForbiddenException;
import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.common.sam.SamRetry;
import bio.terra.common.sam.exception.SamExceptionFactory;
import bio.terra.profile.app.configuration.SamConfiguration;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.contrib.spring.aop.Traced;
import okhttp3.OkHttpClient;
import org.broadinstitute.dsde.workbench.client.sam.ApiClient;
import org.broadinstitute.dsde.workbench.client.sam.ApiException;
import org.broadinstitute.dsde.workbench.client.sam.api.ResourcesApi;
import org.broadinstitute.dsde.workbench.client.sam.api.UsersApi;
import org.broadinstitute.dsde.workbench.client.sam.model.UserStatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SamService {
  private static final Logger logger = LoggerFactory.getLogger(SamService.class);
  private final SamConfiguration samConfig;
  private final OkHttpClient commonHttpClient;

  @Autowired
  public SamService(SamConfiguration samConfig) {
    this.samConfig = samConfig;
    this.commonHttpClient = new ApiClient().getHttpClient();
  }

  /**
   * Wrapper around isAuthorized which throws an appropriate exception if a user does not have
   * access to a resource.
   */
  @Traced
  public void verifyAuthorization(
      AuthenticatedUserRequest userRequest,
      SamResourceType resourceType,
      String resourceId,
      SamAction action)
      throws InterruptedException {
    final boolean isAuthorized = isAuthorized(userRequest, resourceType, resourceId, action);
    final String userEmail = userRequest.getEmail();
    if (!isAuthorized)
      throw new ForbiddenException(
          String.format(
              "User %s is not authorized to %s resource %s of type %s",
              userEmail, action, resourceId, resourceType));
    else {
      logger.info(
          "User {} is authorized to {} resource {} of type {}",
          userEmail,
          action,
          resourceId,
          resourceType);
    }
  }

  /**
   * Checks if a user authorized to do an action on a resource.
   *
   * @param userRequest authenticated user
   * @param resourceType resource type
   * @param resourceId resource in question
   * @param action action to check
   * @return true if authorized; false otherwise
   * @throws InterruptedException
   */
  public boolean isAuthorized(
      AuthenticatedUserRequest userRequest,
      SamResourceType resourceType,
      String resourceId,
      SamAction action)
      throws InterruptedException {
    String accessToken = userRequest.getToken();
    ResourcesApi resourceApi = samResourcesApi(accessToken);
    try {
      return SamRetry.retry(
          () ->
              resourceApi.resourcePermissionV2(
                  resourceType.getSamResourceName(), resourceId, action.getSamActionName()));
    } catch (ApiException apiException) {
      throw SamExceptionFactory.create("Error checking resource permission in Sam", apiException);
    }
  }

  /**
   * Checks if a user has any action on a resource.
   *
   * <p>If user has any action on a resource than we allow that user to list the resource, rather
   * than have a specific action for listing. That is the Sam convention.
   *
   * @param userRequest authenticated user
   * @param resourceType resource type
   * @param resourceId resource in question
   * @return true if the user has any actions on that resource; false otherwise.
   */
  public boolean hasActions(
      AuthenticatedUserRequest userRequest, SamResourceType resourceType, String resourceId)
      throws InterruptedException {
    String accessToken = userRequest.getToken();
    ResourcesApi resourceApi = samResourcesApi(accessToken);
    try {
      return SamRetry.retry(
          () ->
              resourceApi.resourceActions(resourceType.getSamResourceName(), resourceId).size()
                  > 0);
    } catch (ApiException apiException) {
      throw SamExceptionFactory.create("Error checking resource permission in Sam", apiException);
    }
  }

  /**
   * Fetch the user status (email and subjectId) from Sam.
   *
   * @param userToken user token
   * @return {@link UserStatusInfo}
   */
  public UserStatusInfo getUserStatusInfo(String userToken) throws InterruptedException {
    UsersApi usersApi = samUsersApi(userToken);
    try {
      return SamRetry.retry(() -> usersApi.getUserStatusInfo());
    } catch (ApiException apiException) {
      throw SamExceptionFactory.create("Error getting user email from Sam", apiException);
    }
  }

  @VisibleForTesting
  UsersApi samUsersApi(String accessToken) {
    return new UsersApi(getApiClient(accessToken));
  }

  @VisibleForTesting
  ResourcesApi samResourcesApi(String accessToken) {
    return new ResourcesApi(getApiClient(accessToken));
  }

  private ApiClient getApiClient(String accessToken) {
    // OkHttpClient objects manage their own thread pools, so it's much more performant to share one
    // across requests.
    ApiClient apiClient =
        new ApiClient().setHttpClient(commonHttpClient).setBasePath(samConfig.getBasePath());
    apiClient.setAccessToken(accessToken);
    return apiClient;
  }
}
