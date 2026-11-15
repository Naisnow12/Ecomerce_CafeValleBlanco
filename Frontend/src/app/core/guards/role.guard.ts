import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth-service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.currentUser();
    if (!user) {
      router.navigate(['/auth/login']);
      return false;
    }

    const hasRole = user.roles && user.roles.some(r => allowedRoles.includes(r));
    if (hasRole) {
      return true;
    }

    // Si no tiene el rol, redirigir a su zona adecuada
    if (user.roles.includes('ROLE_ADMIN')) {
      router.navigate(['/admin/dashboard']);
    } else if (user.roles.includes('ROLE_ENCARGADO_STOCK')) {
      router.navigate(['/inventario/dashboard']);
    } else {
      router.navigate(['/client/home']);
    }
    return false;
  };
};
