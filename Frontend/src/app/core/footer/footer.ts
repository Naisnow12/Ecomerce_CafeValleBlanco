import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth-service';
import { APP_CONFIG } from '../constants/app-config';

@Component({
  imports: [RouterLink],
  selector: 'app-footer',
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
  private readonly authService = inject(AuthService);

  readonly currentYear = new Date().getFullYear();
  readonly whatsappUrl = `https://wa.me/${APP_CONFIG.whatsappNumber}?text=${encodeURIComponent(
    'Hola Valle Blanco, quisiera información sobre sus cafés de especialidad.'
  )}`;

  readonly isLoggedIn = this.authService.isLoggedIn;
  readonly puedeComprar = this.authService.puedeComprar;
  readonly isAdmin = this.authService.isAdmin;
  readonly isStockManager = this.authService.isStockManager;
}
