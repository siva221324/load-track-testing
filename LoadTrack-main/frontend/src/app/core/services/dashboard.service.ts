import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminDashboard } from '../models/admin-dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/dashboard`;

  adminSummary(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.baseUrl}/admin`);
  }
}
