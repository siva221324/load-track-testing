import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Dealer, DealerRequest } from '../models/dealer.model';
import { Page } from '../models/page.model';
import { CreateLoginRequest, LoginInfo } from '../models/login-info.model';

export interface DealerListParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
}

@Injectable({ providedIn: 'root' })
export class DealerService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/dealers`;

  list(params: DealerListParams = {}): Observable<Page<Dealer>> {
    let p = new HttpParams();
    if (params.page !== undefined) p = p.set('page', params.page);
    if (params.size !== undefined) p = p.set('size', params.size);
    if (params.sort) p = p.set('sort', params.sort);
    if (params.search) p = p.set('search', params.search);
    return this.http.get<Page<Dealer>>(this.baseUrl, { params: p });
  }

  get(id: number): Observable<Dealer> {
    return this.http.get<Dealer>(`${this.baseUrl}/${id}`);
  }

  create(req: DealerRequest): Observable<Dealer> {
    return this.http.post<Dealer>(this.baseUrl, req);
  }

  update(id: number, req: DealerRequest): Observable<Dealer> {
    return this.http.put<Dealer>(`${this.baseUrl}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getLogin(dealerId: number): Observable<LoginInfo | null> {
    return this.http.get<LoginInfo>(`${this.baseUrl}/${dealerId}/login`);
  }

  createLogin(dealerId: number, req: CreateLoginRequest): Observable<LoginInfo> {
    return this.http.post<LoginInfo>(`${this.baseUrl}/${dealerId}/login`, req);
  }
}
