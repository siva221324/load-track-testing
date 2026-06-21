package utilities;

/** Complete provider-owned data set for an isolated trip-management scenario. */
public record TripTestData(
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
        String tripDate,
        String sourceLocation,
        String destinationLocation,
        String editedTons,
        String editedTripDate,
        String editedSourceLocation,
        String editedDestinationLocation) {
}
