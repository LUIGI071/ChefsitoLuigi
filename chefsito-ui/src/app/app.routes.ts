// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  // ============================
  // LOGIN (sin layout)
  // ============================
  {
    path: 'login',
    component: LoginComponent,
  },

  // ============================
  // REGISTRO (sin layout)
  // ============================
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register').then(
        (m) => m.RegisterComponent
      ),
  },

  // ============================
  // LAYOUT PRINCIPAL PROTEGIDO
  // ============================
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./core/layout/main-layout/main-layout.component').then(
        (m) => m.MainLayoutComponent
      ),
    children: [
      // ============================
      // DESPENSA
      // ============================
      {
        path: 'despensa',
        loadComponent: () =>
          import('./features/despensa/despensa').then(
            (m) => m.DespensaComponent
          ),
      },

      // ============================
      // RECETAS
      // ============================
      {
        path: 'recetas',
        loadComponent: () =>
          import('./features/recetas/recetas.component').then(
            (m) => m.RecetasComponent
          ),
      },

      // ============================
      // PERFIL
      // ============================
      {
        path: 'perfil',
        loadComponent: () =>
          import('./features/perfil/perfil/perfil').then(
            (m) => m.PerfilComponent
          ),
      },

      // ============================
      // PANEL ADMIN
      // ============================
      {
        path: 'admin',
        loadComponent: () =>
          import('./features/admin').then(
            (m) => m.Admin
          ),
      },

      // ============================
      // REDIRECCIÓN POR DEFECTO
      // ============================
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'despensa',
      },
    ],
  },

  // ============================
  // WILDCARD
  // ============================
  {
    path: '**',
    redirectTo: 'despensa',
  },
];
