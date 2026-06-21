export type TripStatus = 'PENDING' | 'STARTED' | 'COMPLETED';

export interface DriverSummary {
  id: number;
  name: string;
  phone: string;
}

export interface DealerSummary {
  id: number;
  name: string;
  phone: string;
}

export interface SandTypeSummary {
  id: number;
  name: string;
  pricePerTon: number;
}

export interface TruckSummary {
  id: number;
  truckNumber: string;
  model: string;
}

export interface Trip {
  id: number;
  truck: TruckSummary;
  driver: DriverSummary;
  dealer: DealerSummary;
  sandType: SandTypeSummary;
  tons: number;
  sourceLocation: string;
  destinationLocation: string;
  tripDate: string;       // ISO date "YYYY-MM-DD"
  ratePerTon: number;
  totalAmount: number;
  status: TripStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface TripRequest {
  truckId: number;
  driverId: number;
  dealerId: number;
  sandTypeId: number;
  tons: number;
  sourceLocation: string;
  destinationLocation: string;
  tripDate: string;
}
