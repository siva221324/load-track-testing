import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import {
  ApproveTripRequestPayload,
  CreateTripRequestPayload,
  RejectTripRequestPayload,
  TripRequest
} from '../models/trip-request.model';

@Injectable({ providedIn: 'root' })
export class TripRequestService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}`;

  // ===== Dealer =====
  createByDealer(payload: CreateTripRequestPayload): Observable<TripRequest> {
    return this.http.post<TripRequest>(`${this.baseUrl}/me/dealer/trip-requests`, payload);
  }

  listForDealer(page = 0, size = 50): Observable<Page<TripRequest>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<TripRequest>>(`${this.baseUrl}/me/dealer/trip-requests`, { params });
  }

  cancelByDealer(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/me/dealer/trip-requests/${id}/cancel`, {});
  }

  // ===== Admin =====
  listForAdmin(status?: string, page = 0, size = 50): Observable<Page<TripRequest>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<Page<TripRequest>>(`${this.baseUrl}/trip-requests`, { params });
  }

  pendingCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/trip-requests/pending-count`);
  }

  approve(id: number, payload: ApproveTripRequestPayload): Observable<TripRequest> {
    return this.http.post<TripRequest>(`${this.baseUrl}/trip-requests/${id}/approve`, payload);
  }

  reject(id: number, payload: RejectTripRequestPayload): Observable<TripRequest> {
    return this.http.post<TripRequest>(`${this.baseUrl}/trip-requests/${id}/reject`, payload);
  }
}
