package com.brysonrhodes.api.smshandler.repository;

import com.brysonrhodes.api.smshandler.entity.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends CrudRepository<Group, String> {
}
