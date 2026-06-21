import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Settings, SettingsRequest } from '../models/settings.model';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/settings`;

  get(): Observable<Settings> {
    return this.http.get<Settings>(this.baseUrl);
  }

  update(req: SettingsRequest): Observable<Settings> {
    return this.http.put<Settings>(this.baseUrl, req);
  }
}
