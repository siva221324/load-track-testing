import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReceiptService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/receipts`;

  downloadForPayment(paymentId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/payment/${paymentId}/download`, { responseType: 'blob' });
  }

  triggerBrowserDownload(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
