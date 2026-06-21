export interface TripReportRow {
  tripId: number;
  tripDate: string;
  truckNumber: string;
  driverName: string;
  dealerName: string;
  sandTypeName: string;
  tons: number;
  ratePerTon: number;
  totalAmount: number;
  status: string;
  sourceLocation: string;
  destinationLocation: string;
}

export interface PaymentReportRow {
  paymentId: number;
  tripId: number;
  tripDate: string;
  truckNumber: string;
  dealerName: string;
  originalAmount: number;
  interestAmount: number;
  finalAmount: number;
  paidAmount: number;
  balanceDue: number;
  paymentStatus: string;
  dueDate: string;
  overdue: boolean;
}

export interface TripReportFilters {
  from?: string;
  to?: string;
  truckId?: number;
  driverId?: number;
  dealerId?: number;
  status?: string;
}

export interface PaymentReportFilters {
  from?: string;
  to?: string;
  dealerId?: number;
  paymentStatus?: string;
  overdueOnly?: boolean;
}

export type ExportFormat = 'pdf' | 'xlsx';
