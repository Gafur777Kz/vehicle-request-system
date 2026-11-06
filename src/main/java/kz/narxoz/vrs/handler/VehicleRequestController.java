package kz.narxoz.vrs.handler;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import kz.narxoz.vrs.config.VehicleRequestService;
import kz.narxoz.vrs.domain.VehicleRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

// HTTP-адаптер: принимает JSON, зовёт сервис, выбирает статус
@RestController
@RequestMapping("/api/v1/vehicle-requests")
public class VehicleRequestController {

    private final VehicleRequestService service;

    public VehicleRequestController(VehicleRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "заявка создана",
            content = @Content(schema = @Schema(implementation = VehicleRequestResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDATION",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "DUPLICATE — номер уже есть",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<VehicleRequestResponse> create(@Valid @RequestBody CreateVehicleRequest body) {
        VehicleRequest created = service.create(body.businessKey(), body.purpose());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id().value()).toUri();
        return ResponseEntity.created(location).body(VehicleRequestResponse.from(created));
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "заявка найдена",
            content = @Content(schema = @Schema(implementation = VehicleRequestResponse.class)))
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public VehicleRequestResponse get(@PathVariable UUID id) {
        return VehicleRequestResponse.from(service.get(id));
    }
}
