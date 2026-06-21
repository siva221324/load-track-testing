import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MarkPaidRequest, Payment, PaymentStatus } from '../models/payment.model';
import { Page } from '../models/page.model';

export interface PaymentListParams {
  page?: number;
  size?: number;
  sort?: string;
  dealerId?: number;
  status?: PaymentStatus | '';
  overdueOnly?: boolean;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/payments`;

  list(params: PaymentListParams = {}): Observable<Page<Payment>> {
    let p = new HttpParams();
    if (params.page !== undefined) p = p.set('page', params.page);
    if (params.size !== undefined) p = p.set('size', params.size);
    if (params.sort) p = p.set('sort', params.sort);
    if (params.dealerId) p = p.set('dealerId', params.dealerId);
    if (params.status) p = p.set('status', params.status);
    if (params.overdueOnly) p = p.set('overdueOnly', true);
    return this.http.get<Page<Payment>>(this.baseUrl, { params: p });
  }

  get(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.baseUrl}/${id}`);
  }

  markPaid(id: number, req: MarkPaidRequest): Observable<Payment> {
    return this.http.post<Payment>(`${this.baseUrl}/${id}/pay`, req);
  }
}
