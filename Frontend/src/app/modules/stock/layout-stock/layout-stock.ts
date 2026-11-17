import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-layout-stock',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './layout-stock.html',
  styleUrl: './layout-stock.scss',
})
export class LayoutStock {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  currentUser = this.authService.currentUser;
  isAdmin = this.authService.isAdmin;
  sidebarOpen = signal(false);
  userMenuOpen = signal(false);

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  toggleUserMenu(): void {
    this.userMenuOpen.update(v => !v);
  }

  closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  volverAdmin(): void {
    this.closeSidebar();
    this.router.navigate(['/admin/dashboard']);
  }

  volverTienda(): void {
    this.closeSidebar();
    this.router.navigate(['/client/home']);
  }

  logout(): void {
    this.authService.logout();
    this.sidebarOpen.set(false);
    this.userMenuOpen.set(false);
  }
}
