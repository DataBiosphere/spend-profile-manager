package bio.terra.profile.db;

import bio.terra.common.iam.AuthenticatedUserRequest;
import bio.terra.profile.service.profile.exception.ProfileInUseException;
import bio.terra.profile.service.profile.exception.ProfileNotFoundException;
import bio.terra.profile.service.profile.model.BillingProfile;
import bio.terra.profile.service.profile.model.CloudPlatform;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProfileDao {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  // SQL select string constants
  private static final String SQL_SELECT_LIST =
      "id, display_name, biller, billing_account_id, description, cloud_platform, "
          + "tenant_id, subscription_id, resource_group_name, application_deployment_name, created_date, created_by";

  private static final String SQL_GET =
      "SELECT " + SQL_SELECT_LIST + " FROM billing_profile WHERE id = :id";

  private static final String SQL_LIST =
      "SELECT "
          + SQL_SELECT_LIST
          + " FROM billing_profile"
          + " WHERE id in (:idlist)"
          + " OFFSET :offset LIMIT :limit";

  private static final String SQL_TOTAL =
      "SELECT count(id) AS total" + " FROM billing_profile" + " WHERE id in (:idlist)";

  private static final String SQL_LIST_ALL = "SELECT " + SQL_SELECT_LIST + " FROM billing_profile";

  @Autowired
  public ProfileDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
  public BillingProfile createBillingProfile(
      BillingProfile profile, AuthenticatedUserRequest user) {
    String sql =
        "INSERT INTO billing_profile"
            + " (id, display_name, biller, billing_account_id, description, cloud_platform, "
            + "     tenant_id, subscription_id, resource_group_name, application_deployment_name, created_by) VALUES "
            + " (:id, :display_name, :biller, :billing_account_id, :description, :cloud_platform, "
            + "     :tenant_id, :subscription_id, :resource_group_name, :application_deployment_name, :created_by)";

    String billingAccountId = profile.billingAccountId().orElse(null);
    String tenantId = profile.tenantId().map(UUID::toString).orElse(null);
    String subscriptionId = profile.subscriptionId().map(UUID::toString).orElse(null);
    String resourceGroupName = profile.resourceGroupName().orElse(null);
    String applicationDeploymentName = profile.applicationDeploymentName().orElse(null);

    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("id", profile.id())
            .addValue("display_name", profile.displayName())
            .addValue("biller", profile.biller())
            .addValue("billing_account_id", billingAccountId)
            .addValue("description", profile.description())
            .addValue("cloud_platform", profile.cloudPlatform().toSql())
            .addValue("tenant_id", tenantId)
            .addValue("subscription_id", subscriptionId)
            .addValue("resource_group_name", resourceGroupName)
            .addValue("application_deployment_name", applicationDeploymentName)
            .addValue("created_by", user.getEmail());

    var keyHolder = new DaoKeyHolder();
    jdbcTemplate.update(sql, params, keyHolder);

    return new BillingProfile(
        profile.id(),
        profile.displayName(),
        profile.description(),
        profile.biller(),
        profile.cloudPlatform(),
        profile.billingAccountId(),
        profile.tenantId(),
        profile.subscriptionId(),
        profile.resourceGroupName(),
        profile.applicationDeploymentName(),
        keyHolder.getCreatedDate(),
        keyHolder.getString("created_by"));
  }

  //  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
  //  public ApiProfileModel updateBillingProfileById(ApiUpdateProfileRequest profileRequest) {
  //    String sql =
  //        "UPDATE billing_profile "
  //            + "SET billing_account_id = :billing_account_id, description = :description "
  //            + "WHERE id = :id";
  //    MapSqlParameterSource params =
  //        new MapSqlParameterSource()
  //            .addValue("id", profileRequest.getId())
  //            .addValue("billing_account_id", profileRequest.getBillingAccountId())
  //            .addValue("description", profileRequest.getDescription());
  //    DaoKeyHolder keyHolder = new DaoKeyHolder();
  //    int updated = jdbcTemplate.update(sql, params, keyHolder);
  //
  //    // Assume if the following two conditions are true, then the profile could not be found
  //    // 1. the db command successfully completed
  //    // 2. no rows were updated
  //    if (updated != 1) {
  //      throw new ProfileNotFoundException("Billing Profile was not updated.");
  //    }
  //
  //    return new ApiProfileModel()
  //        .id(keyHolder.getId())
  //        .displayName(keyHolder.getString("name"))
  //        .biller(keyHolder.getString("biller"))
  //        .billingAccountId(keyHolder.getString("billing_account_id"))
  //        .description(keyHolder.getString("description"))
  //        .cloudPlatform(ApiCloudPlatform.valueOf(keyHolder.getString("cloud_platform")))
  //        .tenantId(keyHolder.getField("tenant_id", UUID.class).orElse(null))
  //        .subscriptionId(keyHolder.getField("subscription_id", UUID.class).orElse(null))
  //        .resourceGroupName(keyHolder.getString("resource_group_name"))
  //        .applicationDeploymentName(keyHolder.getString("application_deployment_name"))
  //        .createdBy(keyHolder.getString("created_by"))
  //        .createdDate(keyHolder.getCreatedDate().toString());
  //  }

  //  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  //  public ApiProfileModelList listBillingProfiles(
  //      int offset, int limit, Collection<UUID> accessibleProfileId) {
  //
  //    MapSqlParameterSource params =
  //        new MapSqlParameterSource()
  //            .addValue("offset", offset)
  //            .addValue("limit", limit)
  //            .addValue("idlist", accessibleProfileId);
  //
  //    List<ApiProfileModel> profiles =
  //        jdbcTemplate.query(SQL_LIST, params, new BillingProfileMapper());
  //    Integer total = jdbcTemplate.queryForObject(SQL_TOTAL, params, Integer.class);
  //    if (total == null) {
  //      throw new CorruptMetadataException("Impossible null value from count");
  //    }
  //
  //    return new ApiProfileModelList().items(profiles).total(total);
  //  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public BillingProfile getBillingProfileById(UUID id) {
    try {
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
      return jdbcTemplate.queryForObject(SQL_GET, params, new BillingProfileMapper());
    } catch (EmptyResultDataAccessException ex) {
      throw new ProfileNotFoundException("Profile not found for id: " + id.toString());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
  public boolean deleteBillingProfileById(UUID id) {
    try {
      int rowsAffected =
          jdbcTemplate.update(
              "DELETE FROM billing_profile WHERE id = :id",
              new MapSqlParameterSource().addValue("id", id));
      return rowsAffected > 0;
    } catch (DataIntegrityViolationException ex) {
      // Just in case some concurrent thing slips through the usage check step,
      // handle a case of some active references.
      throw new ProfileInUseException("Profile is in use and cannot be deleted", ex);
    }
  }

  /**
   * This method is made for use by upgrade, where we need to find all of the old billing profiles
   * without regard to visibility.
   *
   * @return list of billing profile models
   */
  //  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  //  public List<ApiProfileModel> getOldBillingProfiles() {
  //    return jdbcTemplate.query(SQL_LIST_ALL, new BillingProfileMapper());
  //  }

  private static class BillingProfileMapper implements RowMapper<BillingProfile> {
    public BillingProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new BillingProfile(
          rs.getObject("id", UUID.class),
          rs.getString("display_name"),
          rs.getString("description"),
          rs.getString("biller"),
          CloudPlatform.fromSql(rs.getString("cloud_platform")),
          Optional.ofNullable(rs.getString("billing_account_id")),
          Optional.ofNullable(rs.getObject("tenant_id", UUID.class)),
          Optional.ofNullable(rs.getObject("subscription_id", UUID.class)),
          Optional.ofNullable(rs.getString("resource_group_name")),
          Optional.ofNullable(rs.getString("application_deployment_name")),
          rs.getTimestamp("created_date").toInstant(),
          rs.getString("created_by"));
    }
  }
}
