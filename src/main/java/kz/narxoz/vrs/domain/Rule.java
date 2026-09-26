package kz.narxoz.vrs.domain;

public interface Rule {

    void check(VehicleRequestStatus from, VehicleRequestStatus to);
}
