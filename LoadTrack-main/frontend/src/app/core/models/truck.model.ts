export type TruckStatus = 'AVAILABLE' | 'ON_TRIP' | 'MAINTENANCE';

export interface Truck {
  id: number;
  truckNumber: string;
  model: string;
  capacityTons: number;
  insuranceNumber?: string;
  rcNumber?: string;
  status: TruckStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface TruckRequest {
  truckNumber: string;
  model: string;
  capacityTons: number;
  insuranceNumber?: string;
  rcNumber?: string;
  status?: TruckStatus;
}
