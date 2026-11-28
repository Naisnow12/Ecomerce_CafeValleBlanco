import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventarioService } from '../../../../core/services/inventario-service';
import { MovimientoStock } from '../../../../core/models/movimientos.interface';
import { Product } from '../../../../core/models/product.interface';

@Component({
  selector: 'app-stock-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './stock-dashboard.html',
  styleUrl: './stock-dashboard.scss'
})
export class StockDashboard implements OnInit {
  private readonly inventarioService = inject(InventarioService);

  cargando = signal(true);
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

  // Filtros
  busquedaProducto = signal('');
  filtroTipoMovimiento = signal('TODOS');

  // Modal / Formulario de Movimiento
  mostrarModalMovimiento = signal(false);
  movimientoForm = {
    productoId: 0,
    tipo: 'ENTRADA' as 'ENTRADA' | 'SALIDA' | 'AJUSTE',
    cantidad: 1,
    motivo: ''
  };

  // Modal / Formulario Nivel Mínimo
  mostrarModalNivel = signal(false);
  productoSeleccionadoParaNivel: Product | null = null;
  nuevoNivelMinimo = 10;

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.inventarioService.getDashboard().subscribe({
      next: (data) => {
        this.stats.set(data);
        const ordenados = [...(data.productos || [])].sort((a, b) => (a.id ?? 0) - (b.id ?? 0));
        this.productos.set(ordenados);
        this.movimientos.set(data.ultimosMovimientos || []);
        this.alertas.set(data.alertasStock || []);
        this.cargando.set(false);
      },
      error: () => {
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

  abrirModalMovimiento(productoId?: number): void {
    if (productoId) {
      this.movimientoForm.productoId = productoId;
    } else if (this.productos().length > 0 && this.productos()[0].id) {
      this.movimientoForm.productoId = this.productos()[0].id!;
    }
    this.movimientoForm.tipo = 'ENTRADA';
    this.movimientoForm.cantidad = 1;
    this.movimientoForm.motivo = '';
    this.mostrarModalMovimiento.set(true);
  }

  cerrarModalMovimiento(): void {
    this.mostrarModalMovimiento.set(false);
  }

  guardarMovimiento(): void {
    if (!this.movimientoForm.productoId || this.movimientoForm.cantidad <= 0) return;

    this.inventarioService.registrarMovimiento(this.movimientoForm).subscribe({
      next: () => {
        this.cerrarModalMovimiento();
        this.cargarDatos();
      },
      error: (err) => {
        alert(err.error?.message || 'Error al registrar movimiento');
      }
    });
  }

  abrirModalNivel(p: Product): void {
    this.productoSeleccionadoParaNivel = p;
    this.nuevoNivelMinimo = p.nivelMinimoStock || 10;
    this.mostrarModalNivel.set(true);
  }

  cerrarModalNivel(): void {
    this.mostrarModalNivel.set(false);
    this.productoSeleccionadoParaNivel = null;
  }

  guardarNivelMinimo(): void {
    if (!this.productoSeleccionadoParaNivel || !this.productoSeleccionadoParaNivel.id) return;

    this.inventarioService.actualizarNivelMinimo(this.productoSeleccionadoParaNivel.id, this.nuevoNivelMinimo).subscribe({
      next: () => {
        this.cerrarModalNivel();
        this.cargarDatos();
      },
      error: (err) => {
        alert(err.error?.message || 'Error al actualizar nivel mínimo');
      }
    });
  }
}
