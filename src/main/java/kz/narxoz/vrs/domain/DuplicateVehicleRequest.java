package kz.narxoz.vrs.domain;

// unchecked, чтобы Spring откатил транзакцию
public class DuplicateVehicleRequest extends RuntimeException {

    public DuplicateVehicleRequest(String number) {
        super("duplicate vehicle request: " + number);
    }
}
