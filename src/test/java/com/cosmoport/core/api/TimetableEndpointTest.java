package com.cosmoport.core.api;

import com.cosmoport.core.dto.EventDtoWithColor;
import com.cosmoport.core.persistence.PersistenceTest;
import com.cosmoport.core.persistence.SettingsPersistenceService;
import com.cosmoport.core.persistence.TimeTableReadOnlyService;
import com.cosmoport.core.persistence.TimetablePersistenceService;
import com.cosmoport.core.service.SuggestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimetableEndpointTest extends PersistenceTest {
    private ObjectMapper mapper;
    private Dispatcher dispatcher;

    @BeforeEach
    public void before() {
        super.before();

        final SettingsPersistenceService settings =
                new SettingsPersistenceService(getLogger(), getDataSourceProvider());
        TimetablePersistenceService service = new TimetablePersistenceService(
                getLogger(), getDataSourceProvider(), settings);
        TimeTableReadOnlyService roService = new TimeTableReadOnlyService(
                getLogger(), getDataSourceProvider());
        final SuggestionService suggestionService = new SuggestionService(service);
        EventBus eventBus = new EventBus();
        mapper = new ObjectMapper();

        dispatcher = MockDispatcherFactory.createDispatcher();
        dispatcher.getRegistry().addSingletonResource(new TimetableEndpoint(service, roService, settings, suggestionService, eventBus));
    }

    @Test
    @DisplayName("Should be able to fetch events with the API call")
    void get() throws Exception {
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(MockHttpRequest.get("/timetable"), response);

        final String data = response.getContentAsString();

        List<EventDtoWithColor> result = mapper.readValue(data, new TypeReference<>() {
        });
        assertEquals(10, result.size());
    }

    @Test
    @DisplayName("Should be able to delete an event with the API call")
    void delete() throws Exception {
        dispatcher.invoke(MockHttpRequest.delete("/timetable/1"), new MockHttpResponse());

        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(MockHttpRequest.get("/timetable"), response);

        final String data = response.getContentAsString();
        List<EventDtoWithColor> result = mapper.readValue(data, new TypeReference<>() {
        });
        assertEquals(9, result.size());
    }
}
