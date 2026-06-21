import { DealerSummary, SandTypeSummary } from './trip.model';

export type TripRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface TripRequest {
  id: number;
  dealer: DealerSummary;
  sandType: SandTypeSummary;
  tons: number;
  sourceLocation: string;
  destinationLocation: string;
  requestedDate: string;
  notes?: string;
  status: TripRequestStatus;
  adminNotes?: string;
  approvedTripId?: number | null;
  estimatedAmount: number;
  respondedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateTripRequestPayload {
  sandTypeId: number;
  tons: number;
  sourceLocation: string;
  destinationLocation: string;
  requestedDate: string;
  notes?: string;
}

export interface ApproveTripRequestPayload {
  truckId: number;
  driverId: number;
  tripDate?: string | null;
  adminNotes?: string;
}

export interface RejectTripRequestPayload {
  reason: string;
}
