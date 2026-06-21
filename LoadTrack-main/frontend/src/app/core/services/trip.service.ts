import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Trip, TripRequest, TripStatus } from '../models/trip.model';
import { Page } from '../models/page.model';

export interface TripListParams {
  page?: number;
  size?: number;
  sort?: string;
  truckId?: number;
  driverId?: number;
  dealerId?: number;
  status?: TripStatus | '';
  from?: string;
  to?: string;
}

@Injectable({ providedIn: 'root' })
export class TripService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/trips`;

  list(params: TripListParams = {}): Observable<Page<Trip>> {
    let p = new HttpParams();
    if (params.page !== undefined) p = p.set('page', params.page);
    if (params.size !== undefined) p = p.set('size', params.size);
    if (params.sort) p = p.set('sort', params.sort);
    if (params.truckId)  p = p.set('truckId', params.truckId);
    if (params.driverId) p = p.set('driverId', params.driverId);
    if (params.dealerId) p = p.set('dealerId', params.dealerId);
    if (params.status)   p = p.set('status', params.status);
    if (params.from)     p = p.set('from', params.from);
    if (params.to)       p = p.set('to', params.to);
    return this.http.get<Page<Trip>>(this.baseUrl, { params: p });
  }

  get(id: number): Observable<Trip> {
    return this.http.get<Trip>(`${this.baseUrl}/${id}`);
  }

  create(req: TripRequest): Observable<Trip> {
    return this.http.post<Trip>(this.baseUrl, req);
  }

  update(id: number, req: TripRequest): Observable<Trip> {
    return this.http.put<Trip>(`${this.baseUrl}/${id}`, req);
  }

  changeStatus(id: number, status: TripStatus): Observable<Trip> {
    return this.http.put<Trip>(`${this.baseUrl}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
