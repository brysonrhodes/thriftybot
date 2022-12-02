package com.brysonrhodes.api.smshandler.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gcp.data.firestore.Document;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document( collectionName = "configs")
public class Config {
    @DocumentId
    public String id;
    public String book;
    public String dinner;
    public Map<String, String> usernames;
}
