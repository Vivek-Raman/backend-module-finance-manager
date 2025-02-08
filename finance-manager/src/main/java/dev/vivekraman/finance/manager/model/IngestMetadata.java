package dev.vivekraman.finance.manager.model;

import java.util.List;
import java.util.Map;

import dev.vivekraman.finance.manager.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestMetadata {
  private User user;
  private Map<String, String> ingestParams;
  private List<Map<String, String>> entries;

  public static IngestMetadata create(User user, Map<String, String> ingestParams) {
    IngestMetadata metadata = new IngestMetadata();
    metadata.setUser(user);
    metadata.setIngestParams(ingestParams);
    return metadata;
  }
}
