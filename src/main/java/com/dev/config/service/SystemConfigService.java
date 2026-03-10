package com.dev.config.service;

import com.dev.config.model.SystemConfig;

import java.util.List;

public interface SystemConfigService {
    
    String getConfigValue(String configKey);
    
    Integer getConfigValueAsInt(String configKey);
    
    Long getConfigValueAsLong(String configKey);
    
    SystemConfig getConfig(String configKey);
    
    List<SystemConfig> getAllConfigs();
    
    SystemConfig updateConfig(String configKey, String configValue);
    
    void seedDefaultConfigs();
}
