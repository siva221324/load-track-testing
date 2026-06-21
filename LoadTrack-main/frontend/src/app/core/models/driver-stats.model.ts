export interface DriverStats {
  driverId: number;
  driverName: string;
  totalTrips: number;
  completedTrips: number;
  pendingTrips: number;
  inProgressTrips: number;
  salaryPerTrip: number;
  totalEarnings: number;
  thisMonthTrips: number;
  thisMonthCompleted: number;
  thisMonthEarnings: number;
}
