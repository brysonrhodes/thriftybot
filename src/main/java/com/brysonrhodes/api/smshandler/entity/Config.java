package com.brysonrhodes.api.smshandler.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "configs")
public class Config {

    @Id
    @Column(name = "config_name")
    public String configName;

    @Column(name = "config_value")
    public String configValue;
}
