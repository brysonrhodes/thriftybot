package com.brysonrhodes.api.smshandler.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_groups")
public class UserGroup {

    @Id
    public int id;

    @NonNull
    @Column(name = "phone")
    public String phone;

    @NonNull
    @Column(name = "group_code")
    public String groupCode;
}
