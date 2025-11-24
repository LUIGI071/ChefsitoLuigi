// src/app/core/auth/auth.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginResponse {
  email: string;
  token: string;
  id: number;
  message: string;
  roles: string[];
  fullName: string;
}

export interface CurrentUser {
  id: number;
  email: string;
  fullName: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  // Base del backend para auth: /api/auth
  private readonly API_URL = `${environment.apiBaseUrl}/auth`;

  /**
   * Login con email o usuario.
   * El backend espera el campo "email".
   */
  login(usernameOrEmail: string, password: string): Observable<LoginResponse> {
    const body = {
      email: usernameOrEmail.trim(),
      password: password,
    };

    return this.http.post<LoginResponse>(`${this.API_URL}/login`, body).pipe(
      tap((res) => {
        // Guardar datos del usuario autenticado
        localStorage.setItem('token', res.token);
        localStorage.setItem('email', res.email);
        localStorage.setItem('userId', String(res.id));
        localStorage.setItem('fullName', res.fullName ?? '');
        localStorage.setItem('roles', JSON.stringify(res.roles ?? []));
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('userId');
    localStorage.removeItem('fullName');
    localStorage.removeItem('roles');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): CurrentUser | null {
    const id = localStorage.getItem('userId');
    const email = localStorage.getItem('email');
    const fullName = localStorage.getItem('fullName');
    const rolesRaw = localStorage.getItem('roles');

    if (!id || !email) {
      return null;
    }

    let roles: string[] = [];
    try {
      roles = rolesRaw ? JSON.parse(rolesRaw) : [];
    } catch {
      roles = [];
    }

    return {
      id: Number(id),
      email,
      fullName: fullName ?? '',
      roles,
    };
  }

  getUserId(): number | null {
    const id = localStorage.getItem('userId');
    return id ? Number(id) : null;
  }
}
