import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Truck, TruckRequest } from '../models/truck.model';
import { Page } from '../models/page.model';

export interface TruckListParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: string;
  search?: string;
}

@Injectable({ providedIn: 'root' })
export class TruckService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/trucks`;

  list(params: TruckListParams = {}): Observable<Page<Truck>> {
    let httpParams = new HttpParams();
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page);
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size);
    if (params.sort) httpParams = httpParams.set('sort', params.sort);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.search) httpParams = httpParams.set('search', params.search);
    return this.http.get<Page<Truck>>(this.baseUrl, { params: httpParams });
  }

  get(id: number): Observable<Truck> {
    return this.http.get<Truck>(`${this.baseUrl}/${id}`);
  }

  create(req: TruckRequest): Observable<Truck> {
    return this.http.post<Truck>(this.baseUrl, req);
  }

  update(id: number, req: TruckRequest): Observable<Truck> {
    return this.http.put<Truck>(`${this.baseUrl}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
