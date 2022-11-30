package com.brysonrhodes.api.smshandler.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gcp.data.firestore.Document;

import javax.annotation.Nullable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document( collectionName = "groups")
public class Group {
    @DocumentId
    public String name;
    @Nullable
    public List<String> users;
}
