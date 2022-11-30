package com.brysonrhodes.api.smshandler.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class FirestoreService {

    @Autowired
    Firestore firestore;

    public Object getObject(String collection, String id, Object classType) throws ExecutionException, InterruptedException {
        log.info("Getting id {} from collection {}", id, collection);
        ApiFuture<DocumentSnapshot> documentFuture = firestore.document(String.format("%s/%s", collection, id)).get();
        return documentFuture.get().toObject(classType.getClass());
    }

    public void addObject(String collection, Object data, String id) throws ExecutionException, InterruptedException {
        log.info("Writing data {} to collection {}", data, collection);
        WriteResult writeResult = firestore.document(String.format("%s/%s", collection, id)).set(data).get();
    }

    public void removeObject(String collection, String id) throws ExecutionException, InterruptedException {
        log.info("Removing {} from {}", id, collection);
        WriteResult writeResult = firestore.document(String.format("%s/%s", collection, id)).delete().get();
    }
}
