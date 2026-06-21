export interface MonthlyEarningPoint {
  label: string;
  amount: number;
}

export interface AdminDashboard {
  totalTrucks: number;
  availableTrucks: number;
  onTripTrucks: number;
  totalDrivers: number;
  totalDealers: number;
  totalTrips: number;
  pendingTrips: number;
  activeTrips: number;
  completedTrips: number;
  pendingPayments: number;
  overduePayments: number;
  thisMonthEarnings: number;
  totalBilled: number;
  totalCollected: number;
  outstandingBalance: number;
  monthlyEarnings: MonthlyEarningPoint[];
}
