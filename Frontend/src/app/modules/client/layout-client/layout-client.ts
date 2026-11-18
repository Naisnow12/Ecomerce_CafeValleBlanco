import { Component, OnInit, inject } from '@angular/core';
import { Header } from '../../../core/header/header';
import { Footer } from '../../../core/footer/footer';
import { RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth-service';
import { CarritoService } from '../../../core/services/carrito.service';

@Component({
  imports: [Header, Footer, RouterOutlet],
  selector: 'app-layout-client',
  templateUrl: './layout-client.html',
})
export class LayoutClient implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly carritoService = inject(CarritoService);

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.carritoService.cargarCarrito().subscribe({ error: () => {} });
    }
  }
}
