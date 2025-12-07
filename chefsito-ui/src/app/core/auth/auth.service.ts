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
        this.storeSession(res);
      })
    );
  }

  /**
   * Registro de nuevo usuario.
   * Coincide con lo que espera tu test (email, password, fullName)
   * y guarda la sesión igual que login.
   */
  register(
    email: string,
    password: string,
    fullName: string
  ): Observable<LoginResponse> {
    const body = {
      email: email.trim(),
      password: password,
      fullName: fullName.trim(),
    };

    return this.http.post<LoginResponse>(`${this.API_URL}/register`, body).pipe(
      tap((res) => {
        this.storeSession(res);
      })
    );
  }

  /**
   * Guardar la sesión en sessionStorage.
   * (token + info básica del usuario)
   */
  private storeSession(res: LoginResponse): void {
    sessionStorage.setItem('token', res.token);
    sessionStorage.setItem('email', res.email);
    sessionStorage.setItem('userId', String(res.id));
    sessionStorage.setItem('fullName', res.fullName ?? '');
    sessionStorage.setItem('roles', JSON.stringify(res.roles ?? []));
  }

  /**
   * Eliminar completamente la sesión en cliente.
   */
  logout(): void {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('email');
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('fullName');
    sessionStorage.removeItem('roles');
  }

  /**
   * Obtener el token actual (o null si no hay).
   */
  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  /**
   * Indica si hay sesión activa en el cliente.
   */
  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  /**
   * Obtener el usuario actual (a partir de sessionStorage).
   */
  getCurrentUser(): CurrentUser | null {
    const id = sessionStorage.getItem('userId');
    const email = sessionStorage.getItem('email');
    const fullName = sessionStorage.getItem('fullName');
    const rolesRaw = sessionStorage.getItem('roles');

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

  /**
   * Obtener solo el ID del usuario actual (o null).
   */
  getUserId(): number | null {
    const id = sessionStorage.getItem('userId');
    return id ? Number(id) : null;
  }
}
