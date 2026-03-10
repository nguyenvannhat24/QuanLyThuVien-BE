package com.dev.config.service;

import com.dev.config.model.SystemConfig;
import com.dev.config.repository.SystemConfigRepository;
import com.dev.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public String getConfigValue(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configKey));
    }

    @Override
    public Integer getConfigValueAsInt(String configKey) {
        String value = getConfigValue(configKey);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Config " + configKey + " is not a valid integer: " + value);
        }
    }

    @Override
    public Long getConfigValueAsLong(String configKey) {
        String value = getConfigValue(configKey);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Config " + configKey + " is not a valid long: " + value);
        }
    }

    @Override
    @Cacheable(value = "systemConfig", key = "#configKey")
    public SystemConfig getConfig(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configKey));
    }

    @Override
    @Cacheable(value = "systemConfigs")
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"systemConfigs", "systemConfig"}, allEntries = true)
    public SystemConfig updateConfig(String configKey, String configValue) {
        SystemConfig config = getConfig(configKey);
        config.setConfigValue(configValue);
        return systemConfigRepository.save(config);
    }

    @PostConstruct
    @Transactional
    public void seedDefaultConfigs() {
        log.info("Seeding default system configurations...");
        
        createConfigIfNotExists("default_borrow_days", "14", "Số ngày mượn sách mặc định");
        createConfigIfNotExists("max_renew_count", "2", "Số lần gia hạn tối đa cho mỗi lần mượn");
        createConfigIfNotExists("fine_per_day", "5000", "Phạt trễ hạn mỗi ngày (VND)");
        createConfigIfNotExists("reservation_hold_days", "3", "Số ngày giữ sách cho người đặt trước");
        createConfigIfNotExists("max_borrow_per_reader", "5", "Số sách tối đa một Reader có thể mượn cùng lúc");
        
        log.info("System configurations seeded successfully");
    }

    private void createConfigIfNotExists(String key, String value, String description) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            SystemConfig config = SystemConfig.builder()
                    .configKey(key)
                    .configValue(value)
                    .description(description)
                    .build();
            systemConfigRepository.save(config);
            log.info("Created config: {} = {}", key, value);
        }
    }
}
