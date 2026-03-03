package com.dev.auth.dto;

import com.dev.auth.model.Role;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    private Role role;
}