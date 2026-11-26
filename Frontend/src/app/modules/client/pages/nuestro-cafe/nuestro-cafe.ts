import { Component, OnInit, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Product } from '../../../../core/models/product.interface';
import { ProductService } from '../../../../core/services/product-service';
import { CarritoService } from '../../../../core/services/carrito-service';
import { AuthService } from '../../../../core/services/auth-service';
import { APP_CONFIG } from '../../../../core/constants/app-config';

@Component({
  selector: 'app-nuestro-cafe',
  standalone: true,
  imports: [CommonModule, RouterLink, DecimalPipe],
  templateUrl: './nuestro-cafe.html',
  styleUrl: './nuestro-cafe.scss'
})
export class NuestroCafe implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  puedeComprar = this.authService.puedeComprar;

  productos = signal<Product[]>([]);
  cargando = signal(true);
  filtroActivo = signal<'TODOS' | 'MOLIDO' | 'GRANO' | 'ESPRESSO' | 'ESPECIALIDAD'>('TODOS');

  agregandoId: number | null | undefined = null;
  mensajeAgregadoId: number | null | undefined = null;

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando.set(true);
    this.productService.getProducts().subscribe({
      next: (items) => {
        this.productos.set(items);
        this.cargando.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.cargando.set(false);
        this.cdr.markForCheck();
      }
    });
  }

  setFiltro(tipo: 'TODOS' | 'MOLIDO' | 'GRANO' | 'ESPRESSO' | 'ESPECIALIDAD'): void {
    this.filtroActivo.set(tipo);
  }

  get productosFiltrados(): Product[] {
    const f = this.filtroActivo();
    const lista = this.productos();

    if (f === 'TODOS') return lista;
    if (f === 'MOLIDO') return lista.filter(p => p.tipoCafe?.toLowerCase().includes('molido') || p.molienda?.toLowerCase().includes('molido'));
    if (f === 'GRANO') return lista.filter(p => p.tipoCafe?.toLowerCase().includes('grano') || p.molienda?.toLowerCase().includes('grano'));
    if (f === 'ESPRESSO') return lista.filter(p => p.tipoCafe?.toLowerCase().includes('espresso') || p.molienda?.toLowerCase().includes('espresso'));
    if (f === 'ESPECIALIDAD') return lista.filter(p => p.tipoCafe?.toLowerCase().includes('especialidad') || p.badgeText);
    return lista;
  }

  getPrecioMinimo(p: Product): number {
    if (!p.precios?.length) return 35.00;
    return Math.min(...p.precios.map(pr => pr.precio));
  }

  getPresentaciones(p: Product): string {
    if (!p.precios?.length) return '250g';
    return p.precios.map(pr => `${pr.peso}g`).join(' · ');
  }

  agregarAlCarrito(p: Product): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/client/nuestro-cafe' } });
      return;
    }

    if (!p.precios?.length || (p.stock ?? 0) <= 0) return;
    const precioId = p.precios[0].id;
    if (!precioId) return;

    this.agregandoId = p.id;
    this.carritoService.agregarItem({ precioId, cantidad: 1 }).subscribe({
      next: () => {
        this.agregandoId = null;
        this.mensajeAgregadoId = p.id;
        this.cdr.markForCheck();
        setTimeout(() => {
          this.mensajeAgregadoId = null;
          this.cdr.markForCheck();
        }, 2200);
      },
      error: () => {
        this.agregandoId = null;
        this.cdr.markForCheck();
      }
    });
  }

  consultarWhatsApp(p: Product): void {
    const texto = `Hola Valle Blanco, deseo consultar disponibilidad del café ${p.nombre} ${p.nombreSecundario || ''} (${p.molienda || ''}).`;
    window.open(`https://wa.me/${APP_CONFIG.whatsappNumber}?text=${encodeURIComponent(texto)}`, '_blank', 'noopener,noreferrer');
  }
}
