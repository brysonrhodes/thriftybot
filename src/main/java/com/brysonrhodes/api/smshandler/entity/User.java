package com.brysonrhodes.api.smshandler.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gcp.data.firestore.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document( collectionName = "users")
public class User {
    @DocumentId
    public String phone;
    public String firstName;
    public String lastName;
}
