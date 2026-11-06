package kz.narxoz.vrs.handler;

import kz.narxoz.vrs.domain.VehicleRequest;

// что видит клиент. Entity наружу не отдаём
public record VehicleRequestResponse(String id, String businessKey, String status, String purpose) {

    static VehicleRequestResponse from(VehicleRequest request) {
        return new VehicleRequestResponse(request.id().value(), request.businessKey(),
                request.status().name(), request.purpose());
    }
}
