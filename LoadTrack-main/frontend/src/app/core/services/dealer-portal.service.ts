import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DealerStats } from '../models/dealer-stats.model';
import { Payment } from '../models/payment.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class DealerPortalService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/me/dealer`;

  myStats(): Observable<DealerStats> {
    return this.http.get<DealerStats>(`${this.baseUrl}/stats`);
  }

  myPayments(status?: string, overdueOnly?: boolean, page = 0, size = 20): Observable<Page<Payment>> {
    let p = new HttpParams().set('page', page).set('size', size);
    if (status) p = p.set('status', status);
    if (overdueOnly) p = p.set('overdueOnly', true);
    return this.http.get<Page<Payment>>(`${this.baseUrl}/payments`, { params: p });
  }
}
