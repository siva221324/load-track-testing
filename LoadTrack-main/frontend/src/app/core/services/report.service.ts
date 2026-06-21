import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ExportFormat,
  PaymentReportFilters,
  PaymentReportRow,
  TripReportFilters,
  TripReportRow
} from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/reports`;

  trips(filters: TripReportFilters): Observable<TripReportRow[]> {
    return this.http.get<TripReportRow[]>(`${this.baseUrl}/trips`,
      { params: this.toParams(filters) });
  }

  exportTrips(format: ExportFormat, filters: TripReportFilters): Observable<Blob> {
    let params = this.toParams(filters);
    params = params.set('format', format);
    return this.http.get(`${this.baseUrl}/trips/export`, { params, responseType: 'blob' });
  }

  payments(filters: PaymentReportFilters): Observable<PaymentReportRow[]> {
    return this.http.get<PaymentReportRow[]>(`${this.baseUrl}/payments`,
      { params: this.toParams(filters) });
  }

  exportPayments(format: ExportFormat, filters: PaymentReportFilters): Observable<Blob> {
    let params = this.toParams(filters);
    params = params.set('format', format);
    return this.http.get(`${this.baseUrl}/payments/export`, { params, responseType: 'blob' });
  }

  triggerDownload(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

  private toParams<T extends object>(obj: T): HttpParams {
    let p = new HttpParams();
    for (const [key, value] of Object.entries(obj as Record<string, unknown>)) {
      if (value !== undefined && value !== null && value !== '') {
        p = p.set(key, String(value));
      }
    }
    return p;
  }
}
