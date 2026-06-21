import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface AccountInfo {
  userId: number;
  username: string;
  role: string;
  linkedToType?: 'driver' | 'dealer';
  linkedToId?: number;
  linkedToName?: string;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/account`;

  me(): Observable<AccountInfo> {
    return this.http.get<AccountInfo>(`${this.baseUrl}/me`);
  }

  changePassword(req: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/change-password`, req);
  }
}
