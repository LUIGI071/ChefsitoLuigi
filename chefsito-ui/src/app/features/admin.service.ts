// src/app/features/admin.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AdminUser {
  id: number;
  email: string;
  fullName: string;
  roles: string[];
  enabled: boolean;
}

export interface SystemStats {
  totalUsers: number;
  totalRecipes: number;
  totalIngredients: number;
}

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private http = inject(HttpClient);

  // Base URL del backend de administración
  private readonly API_URL = `${environment.apiBaseUrl}/admin`;

  /**
   * Obtener todos los usuarios (zona admin).
   * GET /api/admin/users
   */
  getAllUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.API_URL}/users`);
  }

  /**
   * Alias para mantener compatibilidad con admin.ts
   */
  getUsers(): Observable<AdminUser[]> {
    return this.getAllUsers();
  }

  /**
   * Actualizar roles de un usuario.
   * Solo toca la lista de roles.
   */
  updateUserRoles(userId: number, roles: string[]): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.API_URL}/users/${userId}/roles`, {
      roles,
    });
  }

  /**
   * Eliminar usuario (desde panel admin).
   */
  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/users/${userId}`);
  }

  /**
   * Estadísticas generales del sistema para el dashboard admin.
   * GET /api/admin/stats
   */
  getSystemStats(): Observable<SystemStats> {
    return this.http.get<SystemStats>(`${this.API_URL}/stats`);
  }

  /**
   * Alias para compatibilidad con admin.ts
   */
  getStats(): Observable<SystemStats> {
    return this.getSystemStats();
  }
}
