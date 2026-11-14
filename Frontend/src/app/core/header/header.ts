import { Component, inject, signal, computed, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth-service';
import { CarritoService } from '../services/carrito.service';

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  private readonly authService = inject(AuthService);
  private readonly carritoService = inject(CarritoService);

  isMenuOpen = signal(false);
  isStickyActive = signal(false);
  isUserDropdownOpen = signal(false);

  isLoggedIn = this.authService.isLoggedIn;
  isAdmin = this.authService.isAdmin;
  currentUser = this.authService.currentUser;
  cartCount = this.carritoService.itemCount;

  @HostListener('window:scroll')
  onScroll(): void {
    this.isStickyActive.set(window.scrollY > 50);
  }

  toggleMenu(): void {
    this.isMenuOpen.update(v => !v);
  }

  toggleUserDropdown(): void {
    this.isUserDropdownOpen.update(v => !v);
  }

  closeDropdown(): void {
    this.isUserDropdownOpen.set(false);
  }

  logout(): void {
    this.authService.logout();
    this.isUserDropdownOpen.set(false);
    this.isMenuOpen.set(false);
  }
}
