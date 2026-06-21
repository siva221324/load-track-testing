import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SandType, SandTypeRequest } from '../models/sand-type.model';

@Injectable({ providedIn: 'root' })
export class SandTypeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/sand-types`;

  list(): Observable<SandType[]> {
    return this.http.get<SandType[]>(this.baseUrl);
  }

  create(req: SandTypeRequest): Observable<SandType> {
    return this.http.post<SandType>(this.baseUrl, req);
  }

  update(id: number, req: SandTypeRequest): Observable<SandType> {
    return this.http.put<SandType>(`${this.baseUrl}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
