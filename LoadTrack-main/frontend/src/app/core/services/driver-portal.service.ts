import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DriverStats } from '../models/driver-stats.model';
import { Trip } from '../models/trip.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class DriverPortalService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/me/driver`;

  myStats(): Observable<DriverStats> {
    return this.http.get<DriverStats>(`${this.baseUrl}/stats`);
  }

  myTrips(status?: string, page = 0, size = 20): Observable<Page<Trip>> {
    let p = new HttpParams().set('page', page).set('size', size);
    if (status) p = p.set('status', status);
    return this.http.get<Page<Trip>>(`${this.baseUrl}/trips`, { params: p });
  }
}
