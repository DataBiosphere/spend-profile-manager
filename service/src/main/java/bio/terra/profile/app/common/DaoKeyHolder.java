package bio.terra.profile.app.common;

import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DaoKeyHolder extends GeneratedKeyHolder {

  public UUID getId() {
    return getField("id", UUID.class).orElse(null);
  }

  public Instant getCreatedDate() {
    return getField("created_date", Timestamp.class)
            .map(Timestamp::toInstant)
            .orElse(null);
  }

  public String getString(String fieldName) {
    return getField(fieldName, String.class).orElse(null);
  }

  public <T> Optional<T> getField(String fieldName, Class<T> type) {
    Map<String, Object> keys = getKeys();
    if (keys != null) {
      Object fieldObject = keys.get(fieldName);
      if (type.isInstance(fieldObject)) {
        return Optional.of(type.cast(fieldObject));
      }
    }
    return Optional.empty();
  }
}
