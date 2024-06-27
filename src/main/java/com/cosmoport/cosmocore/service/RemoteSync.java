package com.cosmoport.cosmocore.service;

import com.cosmoport.cosmocore.Constants;
import com.cosmoport.cosmocore.repository.SettingsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Service
public class RemoteSync {
    private static final Logger logger = LoggerFactory.getLogger(RemoteSync.class);
    private final SettingsRepository settingsRepository;

    public RemoteSync(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void process(Types type, EventIdDto event) {
        if (settingsRepository.getValueOrThrow(Constants.SYNC_SERVER_ON).equals("on")) {
            final RemoteSyncObj object = new RemoteSyncObj(type, event);
            // Convert to JSON
            ObjectMapper mapper = new ObjectMapper();
            OutputStreamWriter out = null;
            try {
                String value = mapper.writeValueAsString(object);

                // Request
                URL url = URI.create(settingsRepository.getValueOrThrow(Constants.SYNC_SERVER_ADDRESS)).toURL();
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("POST");
                out = new OutputStreamWriter(httpCon.getOutputStream());
                out.write(value);
                logger.info("[sync] [out] {}, {}", httpCon.getResponseCode(), httpCon.getResponseMessage());
                out.close();
            } catch (Exception e) {
                logger.error("[sync] [out] {}", e.getMessage());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        logger.error("[sync] [out] {}", e.getMessage());
                    }
                }
            }
        } else {
            logger.info("[sync] [out] disabled");
        }
    }

    public record RemoteSyncObj(Types type, EventIdDto event) {
    }

    public record EventIdDto(int id) {
    }
}
