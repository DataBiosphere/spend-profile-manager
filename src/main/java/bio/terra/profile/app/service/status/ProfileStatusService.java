package bio.terra.profile.app.service.status;

import bio.terra.model.ApiSystemStatus;
import bio.terra.model.ApiSystemStatusSystems;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

// TODO: do in background
@Component
public class ProfileStatusService {
  private static final Logger logger = LoggerFactory.getLogger(ProfileStatusService.class);
  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  public ProfileStatusService(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public ApiSystemStatus getStatus() {
    ApiSystemStatus systemStatus = new ApiSystemStatus();

    // Used by Unit test: StatusTest
    //    if (configurationService.testInsertFault(ConfigEnum.LIVENESS_FAULT)) {
    //      logger.info("LIVENESS_FAULT insertion - failing status response");
    //      statusModel.setOk(false);
    //      return statusModel;
    //    }

    systemStatus.putSystemsItem("Postgres", postgresStatus());

    // if all systems are ok, then isOk = true
    systemStatus.setOk(
        systemStatus.getSystems().values().stream().allMatch(ApiSystemStatusSystems::isOk));

    return systemStatus;
  }

  private ApiSystemStatusSystems postgresStatus() throws DataAccessException {
    // Used by Unit test: StatusTest
    //    if (configurationService.testInsertFault(ConfigEnum.CRITICAL_SYSTEM_FAULT)) {
    //      logger.info(
    //              "CRITICAL_SYSTEM_FAULT inserted for test - setting postgres system status to
    // failing");
    //      return new RepositoryStatusModelSystems()
    //              .ok(false)
    //              .critical(true)
    //              .message("CRITICAL_SYSTEM_FAULT inserted for test");
    //    }
    try {
      logger.debug("Checking database connection valid");

      return new ApiSystemStatusSystems()
          .ok(jdbcTemplate.getJdbcTemplate().execute((Connection conn) -> conn.isValid(5000)));
    } catch (Exception ex) {
      String errorMsg = "Database status check failed";
      logger.error(errorMsg, ex);
      return new ApiSystemStatusSystems()
          .ok(false)
          .addMessagesItem(errorMsg + ": " + ex.getMessage());
    }
  }
}
