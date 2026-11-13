import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService } from '../../../../core/services/product.service';
import { CarritoService } from '../../../../core/services/carrito.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Product } from '../../../../core/models/product.interface';
import { precios } from '../../../../core/models/precios.interface';

@Component({
  imports: [RouterLink, DecimalPipe],
  selector: 'app-product-detail',
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss'
})

export class ProductDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);

  product = signal<Product | null>(null);
  selectedPrecio = signal<precios | null>(null);
  cantidad = signal(1);
  isLoading = signal(true);
  isAddingToCart = signal(false);
  cartMessage = signal<string | null>(null);
  cartError = signal<string | null>(null);

  isLoggedIn = this.authService.isLoggedIn;

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.router.navigate(['/client/catalog']);
      return;
    }
    this.productService.getProductBySlug(slug).subscribe({
      next: (p) => {
        this.product.set(p);
        if (p.precios?.length > 0) {
          this.selectedPrecio.set(p.precios[0]);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.router.navigate(['/client/catalog']);
      }
    });
  }

  selectPrecio(precio: precios): void {
    this.selectedPrecio.set(precio);
  }

  increment(): void {
    const p = this.product();
    if (p && this.cantidad() < p.stock) {
      this.cantidad.update(v => v + 1);
    }
  }

  decrement(): void {
    if (this.cantidad() > 1) {
      this.cantidad.update(v => v - 1);
    }
  }

  agregarAlCarrito(): void {
    if (!this.isLoggedIn()) {
      this.router.navigate(['/auth/login'], {
        queryParams: { returnUrl: this.router.url }
      });
      return;
    }
    const precio = this.selectedPrecio();
    if (!precio?.id) return;

    this.isAddingToCart.set(true);
    this.cartMessage.set(null);
    this.cartError.set(null);

    this.carritoService.agregarItem({ precioId: precio.id, cantidad: this.cantidad() }).subscribe({
      next: () => {
        this.isAddingToCart.set(false);
        this.cartMessage.set('¡Producto agregado al carrito!');
        setTimeout(() => this.cartMessage.set(null), 3000);
      },
      error: () => {
        this.isAddingToCart.set(false);
        this.cartError.set('Error al agregar. Intenta nuevamente.');
      }
    });
  }

  consultarWhatsApp(): void {
    const p = this.product();
    const precio = this.selectedPrecio();
    if (!p) return;
    const pesoText = precio ? `${precio.peso}g` : '';
    const msg = encodeURIComponent(
      `Hola Valle Blanco! Me interesa el producto: ${p.nombre}${p.nombreSecundario ? ' - ' + p.nombreSecundario : ''} ${pesoText}. ¿Podrían darme más información?`
    );
    window.open(`https://wa.me/?text=${msg}`, '_blank');
  }
}
