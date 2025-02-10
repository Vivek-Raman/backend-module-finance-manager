package dev.vivekraman.finance.manager.model;

import java.time.LocalDate;
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
public class IngestMetadata {
  private User user;
  private IngestParameter parameters;
  private LocalDate newestDateInEntries;
  private List<Map<String, String>> newEntries;
  private Double newBalance;
  private IngestSplitwiseResponseDTO response;

  public static IngestMetadata create(User user, IngestParameter ingestParameters) {
    IngestMetadata metadata = new IngestMetadata();
    metadata.setUser(user);
    metadata.setParameters(ingestParameters);
    metadata.setResponse(new IngestSplitwiseResponseDTO());
    return metadata;
  }
}
