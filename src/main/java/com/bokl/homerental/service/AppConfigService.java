package com.bokl.homerental.service;

import com.bokl.homerental.entity.AppConfig;
import com.bokl.homerental.repository.AppConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AppConfigService {

    private final AppConfigRepository repo;

    public AppConfigService(AppConfigRepository repo) {
        this.repo = repo;
    }

    public String get(String key, String defaultValue) {
        return repo.findByKey(key)
                .map(AppConfig::getValue)
                .orElse(defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return repo.findByKey(key)
                .map(c -> {
                    try { return Integer.parseInt(c.getValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        return repo.findByKey(key)
                .map(c -> {
                    try { return new BigDecimal(c.getValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    @Transactional
    public void set(String key, String value) {
        repo.findByKey(key).ifPresentOrElse(
                config -> config.setValue(value),
                () -> {
                    AppConfig config = new AppConfig();
                    config.setKey(key);
                    config.setValue(value);
                    repo.save(config);
                }
        );
    }
}
