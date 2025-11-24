// src/app/core/auth/auth.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginResponse {
  email: string;
  token: string;
  id: number;
  message: string;
  roles: string[];
  fullName: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  // usamos ruta relativa, el proxy lo manda a http://localhost:8080
  private readonly API_URL = '/api/auth';

  login(email: string, password: string): Observable<LoginResponse> {
    const body = { email, password };

    return this.http.post<LoginResponse>(`${this.API_URL}/login`, body).pipe(
      tap((res) => {
        // 🔐 Guardamos todo lo que nos interesa del usuario
        localStorage.setItem('token', res.token);
        localStorage.setItem('email', res.email);
        localStorage.setItem('roles', JSON.stringify(res.roles || []));
        localStorage.setItem('userId', String(res.id));
        localStorage.setItem('fullName', res.fullName || '');
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('roles');
    localStorage.removeItem('userId');
    localStorage.removeItem('fullName');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUserId(): number | null {
    const raw = localStorage.getItem('userId');
    if (!raw) return null;
    const n = Number(raw);
    return Number.isNaN(n) ? null : n;
  }

  getFullName(): string | null {
    return localStorage.getItem('fullName');
  }

  getRoles(): string[] {
    const raw = localStorage.getItem('roles');
    if (!raw) return [];
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
