package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.Constants;
import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.model.SettingsEntity;
import com.cosmoport.cosmocore.repository.SettingsRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthEndpoint {
    private final SettingsRepository settingsRepository;
    public AuthEndpoint(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @PostMapping("/check")
    public ResultDto check(@RequestBody PasswordDto password) {
        final SettingsEntity passwordEntity = settingsRepository.findByParam(Constants.PASSWORD).orElseThrow();
        return new ResultDto(passwordEntity.getValue().equals(password.pwd()));
    }

    @PostMapping("/set")
    public ResultDto set(@RequestBody PasswordDto password) {
        settingsRepository.findByParam(Constants.PASSWORD).ifPresentOrElse(settingsEntity -> {
            settingsEntity.setValue(password.pwd());
            settingsRepository.save(settingsEntity);
        }, () -> {throw new IllegalStateException("Password not found");});
        return new ResultDto(true);
    }


    public record PasswordDto (String pwd) {
    }
}
