package utilities;

/** Provider-owned data for an isolated dealer/admin trip-request workflow. */
public record TripRequestTestData(
        String truckNumber,
        String truckModel,
        String truckCapacity,
        String driverName,
        String driverPhone,
        String driverLicense,
        String driverAddress,
        String driverSalary,
        String dealerName,
        String dealerPhone,
        String dealerAddress,
        String sandTypeName,
        String sandTypePrice,
        String tons,
        String requestedDate,
        String sourceLocation,
        String destinationLocation,
        String dealerNotes,
        String approvalDate,
        String approvalNotes,
        String rejectionReason) {
}
