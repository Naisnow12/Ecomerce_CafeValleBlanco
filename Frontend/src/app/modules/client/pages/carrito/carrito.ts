import { Component, inject, computed, OnInit } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CarritoService } from '../../../../core/services/carrito.service';
import { DecimalPipe } from '@angular/common';

@Component({
  imports: [RouterLink, DecimalPipe],
  selector: 'app-carrito',
  templateUrl: './carrito.html',
  styleUrl: './carrito.scss',
})
export class Carrito implements OnInit {
  private readonly carritoService = inject(CarritoService);
  private readonly router = inject(Router);

  carrito = this.carritoService.carrito;
  isLoading = computed(() => this.carrito() === null);

  ngOnInit(): void {
    this.carritoService.cargarCarrito().subscribe();
  }

  actualizarCantidad(itemId: number, cantidad: number): void {
    if (cantidad < 1) return;
    this.carritoService.actualizarCantidad(itemId, cantidad).subscribe();
  }

  eliminarItem(itemId: number): void {
    this.carritoService.eliminarItem(itemId).subscribe();
  }

  irACheckout(): void {
    this.router.navigate(['/client/checkout']);
  }

  incrementar(itemId: number, cantidadActual: number): void {
    this.actualizarCantidad(itemId, cantidadActual + 1);
  }

  decrementar(itemId: number, cantidadActual: number): void {
    if (cantidadActual > 1) {
      this.actualizarCantidad(itemId, cantidadActual - 1);
    }
  }
}
