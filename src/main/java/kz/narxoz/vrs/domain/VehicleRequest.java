package kz.narxoz.vrs.domain;

// заявка, как её видит домен: без JPA и без JSON
public record VehicleRequest(VehicleRequestId id, String businessKey, VehicleRequestStatus status, String purpose) {
}
