export type PaymentStatus = 'PENDING' | 'PARTIAL' | 'PAID';

export interface TripSummary {
  id: number;
  tripDate: string;
  truckNumber: string;
  driverName: string;
  dealerName: string;
  sandTypeName: string;
  totalAmount: number;
}

export interface Payment {
  id: number;
  trip: TripSummary;
  originalAmount: number;
  interestAmount: number;
  finalAmount: number;
  paidAmount: number;
  balanceDue: number;
  paymentStatus: PaymentStatus;
  paymentDate?: string | null;
  dueDate: string;
  overdue: boolean;
  daysOverdue: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface MarkPaidRequest {
  paidAmount: number;
}
