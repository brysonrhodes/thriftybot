package com.brysonrhodes.api.smshandler.repository;

import com.brysonrhodes.api.smshandler.entity.UserGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends CrudRepository<UserGroup, String> {
}
