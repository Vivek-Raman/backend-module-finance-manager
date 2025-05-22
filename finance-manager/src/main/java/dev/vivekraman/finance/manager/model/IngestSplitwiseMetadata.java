package dev.vivekraman.finance.manager.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import dev.vivekraman.finance.manager.entity.IngestParameter;
import dev.vivekraman.finance.manager.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestSplitwiseMetadata {
  private User user;
  private IngestParameter parameters;
  private OffsetDateTime newestDateInEntries;
  private List<Map<String, String>> newEntries;
  private Double newBalance;
  private IngestResponseDTO response;

  public static IngestSplitwiseMetadata create(User user, IngestParameter ingestParameters) {
    IngestSplitwiseMetadata metadata = new IngestSplitwiseMetadata();
    metadata.setUser(user);
    metadata.setParameters(ingestParameters);
    metadata.setResponse(new IngestResponseDTO());
    return metadata;
  }
}
