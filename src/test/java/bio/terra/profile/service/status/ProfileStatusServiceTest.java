package bio.terra.profile.service.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.model.ApiSystemStatus;
import bio.terra.model.ApiSystemStatusSystems;
import bio.terra.profile.common.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileStatusServiceTest extends BaseUnitTest {

  @Autowired private ProfileStatusService statusService;

  // TODO add more cases when we have something to mock out

  @Test
  void testStatusWithWorkingEndpoints() {
    statusService.checkStatus();
    assertEquals(
        new ApiSystemStatus()
            .ok(true)
            .putSystemsItem("CloudSQL", new ApiSystemStatusSystems().ok(true)),
        statusService.getCurrentStatus());
  }
}
