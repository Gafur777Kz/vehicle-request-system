package kz.narxoz.vrs.handler;

import kz.narxoz.vrs.config.Application;
import kz.narxoz.vrs.config.VehicleRequestService;
import kz.narxoz.vrs.domain.DuplicateVehicleRequest;
import kz.narxoz.vrs.domain.VehicleRequest;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestNotFound;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static kz.narxoz.vrs.domain.VehicleRequestStatus.DRAFT;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// только веб-слой: без PostgreSQL, сервис замокан
@WebMvcTest(VehicleRequestController.class)
@ContextConfiguration(classes = Application.class)
class VehicleRequestControllerTest {

    private static final String URL = "/api/v1/vehicle-requests";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VehicleRequestService service;

    @Test
    void validBodyReturns201WithLocation() throws Exception {
        VehicleRequestId id = VehicleRequestId.newId();
        when(service.create("VR-1042", "Поездка в аэропорт"))
                .thenReturn(new VehicleRequest(id, "VR-1042", DRAFT, "Поездка в аэропорт"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"VR-1042\",\"purpose\":\"Поездка в аэропорт\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(URL + "/" + id.value())))
                .andExpect(jsonPath("$.id").value(id.value()))
                .andExpect(jsonPath("$.businessKey").value("VR-1042"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void blankPurposeReturns400AndServiceIsNotCalled() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"VR-1042\",\"purpose\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(containsString("purpose")));

        verify(service, never()).create(anyString(), anyString());
    }

    @Test
    void purposeLongerThan120Returns400() throws Exception {
        String purpose = "a".repeat(121);

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"VR-1042\",\"purpose\":\"" + purpose + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("purpose size must be between 0 and 120"));
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(service.create(anyString(), anyString())).thenThrow(new DuplicateVehicleRequest("VR-1042"));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"VR-1042\",\"purpose\":\"Ещё раз\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));
    }

    @Test
    void missingRequestReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new VehicleRequestNotFound(id.toString()));

        mvc.perform(get(URL + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void businessKeyInPathReturns400() throws Exception {
        mvc.perform(get(URL + "/VR-1042"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("id must be a UUID"));

        verify(service, never()).get(any());
    }

    @Test
    void correlationIdComesBack() throws Exception {
        mvc.perform(get(URL + "/VR-1042").header("X-Correlation-Id", "test-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId").value("test-123"));
    }
}
