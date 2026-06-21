import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../../environments/environment';
import { DecodedToken, LoginRequest, LoginResponse, UserRole } from '../models/auth.model';

const TOKEN_KEY = 'loadtrack_token';
const USER_KEY = 'loadtrack_user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly _user = signal<LoginResponse | null>(this.readStoredUser());
  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null && !this.isExpired());
  readonly role = computed<UserRole | null>(() => this._user()?.role ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, req).pipe(
      tap(res => this.persist(res))
    );
  }

  signup(username: string, password: string): Observable<{ userId: number; username: string; role: string }> {
    return this.http.post<{ userId: number; username: string; role: string }>(
      `${environment.apiUrl}/auth/signup`,
      { username, password }
    );
  }

  forgotPassword(username: string): Observable<{ message: string; temporaryPassword: string }> {
    return this.http.post<{ message: string; temporaryPassword: string }>(
      `${environment.apiUrl}/auth/forgot-password`,
      { username }
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._user.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private persist(res: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res));
    this._user.set(res);
  }

  private readStoredUser(): LoginResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw) as LoginResponse; } catch { return null; }
  }

  private isExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;
    try {
      const decoded = jwtDecode<DecodedToken>(token);
      return decoded.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
