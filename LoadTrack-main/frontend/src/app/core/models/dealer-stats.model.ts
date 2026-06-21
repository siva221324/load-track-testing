export interface DealerStats {
  dealerId: number;
  dealerName: string;
  totalTrips: number;
  totalPayments: number;
  pendingPayments: number;
  overduePayments: number;
  totalBilled: number;
  totalPaid: number;
  totalBalance: number;
  thisMonthBilled: number;
  thisMonthPaid: number;
}
