import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  RouterOutlet,
  RouterLink,
  RouterLinkActive,
  Router,
} from '@angular/router';
import { AuthService, CurrentUser } from '../../auth/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="layout-container">
      <nav class="navbar">
        <!-- Marca / logo -->
        <div class="nav-brand">
          <span class="logo">👨‍🍳</span>
          <span class="brand-name">Chefsito</span>
        </div>

        <!-- Pestañas centrales -->
        <div class="nav-links">
          <a routerLink="/despensa" routerLinkActive="active">Despensa</a>
          <a routerLink="/recetas" routerLinkActive="active">Recetas</a>
          <a routerLink="/perfil" routerLinkActive="active">Mi perfil</a>
        </div>

        <!-- Zona usuario / admin -->
        <div class="nav-profile" *ngIf="currentUser as user">
          <!-- Botón panel admin SOLO si tiene ROLE_ADMIN -->
          <button
            *ngIf="isAdmin(user)"
            type="button"
            class="btn-small btn-admin"
            routerLink="/admin"
            routerLinkActive="active"
          >
            Administrador
          </button>

          <!-- Botón cerrar sesión -->
          <button type="button" class="btn-small" (click)="logout()">
            Cerrar sesión
          </button>
        </div>

      </nav>

      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [
    `
      .layout-container {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        background: #020617;
        color: #e5e7eb;
      }

      .navbar {
        height: 64px;
        padding: 0 1.5rem;
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1px solid #1f2937;
        background: rgba(15, 23, 42, 0.9);
        backdrop-filter: blur(10px);
      }

      .nav-brand {
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }

      .logo {
        font-size: 1.5rem;
      }

      .brand-name {
        font-weight: 700;
        letter-spacing: 0.04em;
        font-size: 1.05rem;
        background: linear-gradient(90deg, #f97316, #ec4899);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      .nav-links {
        display: flex;
        gap: 1.5rem;
      }

      .nav-links a {
        color: #94a3b8;
        text-decoration: none;
        font-weight: 500;
        padding: 0.5rem 0.75rem;
        border-radius: 0.5rem;
        transition: color 0.2s, background 0.2s;
      }

      .nav-links a:hover {
        color: #f1f5f9;
        background: rgba(255, 255, 255, 0.05);
      }

      .nav-links a.active {
        color: #f97316;
        background: rgba(249, 115, 22, 0.12);
      }

      .nav-profile {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        font-size: 0.9rem;
        color: #e5e7eb;
      }

      .user-name {
        max-width: 180px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .btn-small {
        padding: 0.25rem 0.75rem;
        border-radius: 999px;
        border: 1px solid #4b5563;
        background: transparent;
        color: inherit;
        font-size: 0.8rem;
        cursor: pointer;
      }

      .btn-small:hover {
        background: rgba(148, 163, 184, 0.25);
      }

      /* Botón admin un poco destacado */
      .btn-admin {
        border-color: #f97316;
      }

      .btn-admin.active {
        background: rgba(249, 115, 22, 0.12);
        color: #f97316;
      }

      .main-content {
        flex: 1;
        overflow-y: auto;
        padding: 1rem;
      }
    `,
  ],
})
export class MainLayoutComponent {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  get currentUser(): CurrentUser | null {
    return this.authService.getCurrentUser();
  }

  isAdmin(user: CurrentUser | null): boolean {
    return !!user && Array.isArray(user.roles) && user.roles.includes('ROLE_ADMIN');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
