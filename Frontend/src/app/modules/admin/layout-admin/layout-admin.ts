
import { Component, inject, signal } from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-layout-admin',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './layout-admin.html',
  styleUrl: './layout-admin.scss',
})
export class LayoutAdmin {

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Usuario autenticado
  currentUser = this.authService.currentUser;
  isAdmin = this.authService.isAdmin;

  // Menú lateral
  sidebarOpen = signal(false);

  // Menú de usuario
  userMenuOpen = signal(false);

  // Navegación
  toggleSidebar(): void {
    this.sidebarOpen.update(value => !value);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  toggleUserMenu(): void {
    this.userMenuOpen.update(value => !value);
  }

  closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  // Volver a la tienda
  volverTienda(): void {
    this.closeSidebar();
    this.router.navigate(['/client/home']);
  }

  // Cerrar sesión
  logout(): void {
    this.authService.logout();

    this.sidebarOpen.set(false);
    this.userMenuOpen.set(false);

    this.router.navigate(['/auth/login']);
  }
}
