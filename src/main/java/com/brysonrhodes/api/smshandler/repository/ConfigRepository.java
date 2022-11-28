package com.brysonrhodes.api.smshandler.repository;

import com.brysonrhodes.api.smshandler.entity.Config;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends CrudRepository<Config, String> {
    Config findByConfigName(String configName);
}
