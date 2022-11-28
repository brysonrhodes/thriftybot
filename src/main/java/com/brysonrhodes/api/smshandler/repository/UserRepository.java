package com.brysonrhodes.api.smshandler.repository;

import com.brysonrhodes.api.smshandler.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    User findByPhone(String phone);
}
