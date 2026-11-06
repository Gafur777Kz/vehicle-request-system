package kz.narxoz.vrs.domain;

// unchecked, обработчик ошибок превращает в 404
public class VehicleRequestNotFound extends RuntimeException {

    public VehicleRequestNotFound(String id) {
        super("vehicle request not found: " + id);
    }
}
