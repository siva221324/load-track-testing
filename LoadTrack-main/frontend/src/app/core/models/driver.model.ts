import { LoginInfo } from './login-info.model';

export interface TruckSummary {
  id: number;
  truckNumber: string;
  model: string;
}

export interface Driver {
  id: number;
  name: string;
  phone: string;
  licenseNumber: string;
  address?: string;
  salaryPerTrip: number;
  assignedTruck?: TruckSummary | null;
  loginInfo?: LoginInfo | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface DriverRequest {
  name: string;
  phone: string;
  licenseNumber: string;
  address?: string;
  salaryPerTrip: number;
  assignedTruckId?: number | null;
}
