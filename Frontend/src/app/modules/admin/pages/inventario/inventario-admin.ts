import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventarioService, MovimientoStock } from '../../../../core/services/inventario.service';
import { Product } from '../../../../core/models/product.interface';

@Component({
  selector: 'app-inventario-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './inventario-admin.html',
  styleUrl: './inventario-admin.scss'
})
export class InventarioAdmin implements OnInit {
  private readonly inventarioService = inject(InventarioService);

  cargando = signal(true);
  error = signal('');
  tabActiva = signal<'RESUMEN' | 'PRODUCTOS' | 'MOVIMIENTOS' | 'ALERTAS'>('RESUMEN');

  stats = signal<any>({
    totalProductos: 0,
    totalExistencias: 0,
    stockBajo: 0,
    agotados: 0,
    totalEntradas: 0,
    totalSalidas: 0
  });

  productos = signal<Product[]>([]);
  movimientos = signal<MovimientoStock[]>([]);
  alertas = signal<Product[]>([]);

  busquedaProducto = signal('');
  filtroTipoMovimiento = signal('TODOS');

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.error.set('');
    this.inventarioService.getDashboard().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.productos.set(data.productos || []);
        this.movimientos.set(data.ultimosMovimientos || []);
        this.alertas.set(data.alertasStock || []);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo obtener la información de inventario desde el servidor.');
        this.cargando.set(false);
      }
    });
  }

  setTab(tab: 'RESUMEN' | 'PRODUCTOS' | 'MOVIMIENTOS' | 'ALERTAS'): void {
    this.tabActiva.set(tab);
  }

  productosFiltrados = computed(() => {
    const q = this.busquedaProducto().toLowerCase().trim();
    if (!q) return this.productos();
    return this.productos().filter(p =>
      p.nombre?.toLowerCase().includes(q) ||
      p.nombreSecundario?.toLowerCase().includes(q) ||
      p.molienda?.toLowerCase().includes(q) ||
      p.tipoCafe?.toLowerCase().includes(q)
    );
  });

  movimientosFiltrados = computed(() => {
    const f = this.filtroTipoMovimiento();
    if (f === 'TODOS') return this.movimientos();
    return this.movimientos().filter(m => m.tipo === f);
  });
}
