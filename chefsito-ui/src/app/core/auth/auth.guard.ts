import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

/**
 * authGuard
 *  - Permite el acceso si existe un token en sessionStorage.
 *  - Si no hay token, redirige a /login y guarda la URL a la que se quería ir en returnUrl.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  const token = sessionStorage.getItem('token');

  if (token) {
    return true;
  }

  // No autenticado -> redirigir a login
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};
