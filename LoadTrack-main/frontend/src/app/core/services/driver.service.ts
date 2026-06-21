import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Driver, DriverRequest } from '../models/driver.model';
import { Page } from '../models/page.model';
import { CreateLoginRequest, LoginInfo } from '../models/login-info.model';

export interface DriverListParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
}

@Injectable({ providedIn: 'root' })
export class DriverService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/drivers`;

  list(params: DriverListParams = {}): Observable<Page<Driver>> {
    let p = new HttpParams();
    if (params.page !== undefined) p = p.set('page', params.page);
    if (params.size !== undefined) p = p.set('size', params.size);
    if (params.sort) p = p.set('sort', params.sort);
    if (params.search) p = p.set('search', params.search);
    return this.http.get<Page<Driver>>(this.baseUrl, { params: p });
  }

  get(id: number): Observable<Driver> {
    return this.http.get<Driver>(`${this.baseUrl}/${id}`);
  }

  create(req: DriverRequest): Observable<Driver> {
    return this.http.post<Driver>(this.baseUrl, req);
  }

  update(id: number, req: DriverRequest): Observable<Driver> {
    return this.http.put<Driver>(`${this.baseUrl}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getLogin(driverId: number): Observable<LoginInfo | null> {
    return this.http.get<LoginInfo>(`${this.baseUrl}/${driverId}/login`);
  }

  createLogin(driverId: number, req: CreateLoginRequest): Observable<LoginInfo> {
    return this.http.post<LoginInfo>(`${this.baseUrl}/${driverId}/login`, req);
  }
}
