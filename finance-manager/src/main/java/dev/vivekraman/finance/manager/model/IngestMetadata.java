package dev.vivekraman.finance.manager.model;

import java.util.List;
import java.util.Map;

import dev.vivekraman.finance.manager.entity.IngestLog;
import dev.vivekraman.finance.manager.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestMetadata {
  private User user;
  private Map<String, IngestLog> ingestParams;
  private List<Map<String, String>> newEntries;
  private List<Map<String, String>> oldUpdatedEntries;
  private IngestSplitwiseResponseDTO response;

  public static IngestMetadata create(User user, Map<String, IngestLog> ingestParams) {
    IngestMetadata metadata = new IngestMetadata();
    metadata.setUser(user);
    metadata.setIngestParams(ingestParams);
    metadata.setResponse(new IngestSplitwiseResponseDTO());
    return metadata;
  }
}
