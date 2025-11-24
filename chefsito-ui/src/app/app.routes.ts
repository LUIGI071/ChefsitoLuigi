// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';

export const routes: Routes = [
  // Login sin layout
  {
    path: 'login',
    component: LoginComponent,
  },

  // Rutas con layout principal
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/main-layout/main-layout.component').then(
        (m) => m.MainLayoutComponent
      ),
    children: [
      {
        path: 'despensa',
        loadComponent: () =>
          import('./features/despensa/despensa').then(
            (m) => m.DespensaComponent
          ),
      },
      {
        path: 'recetas',
        loadComponent: () =>
          import('./features/recetas/recetas.component').then(
            (m) => m.RecetasComponent
          ),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'despensa',
      },
    ],
  },

  // Wildcard route
  {
    path: '**',
    redirectTo: 'despensa',
  },
];
