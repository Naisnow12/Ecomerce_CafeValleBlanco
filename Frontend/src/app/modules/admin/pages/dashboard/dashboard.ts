
import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';

type EstadoPedido =
  | 'PENDIENTE'
  | 'CONFIRMADO'
  | 'EN_PREPARACION'
  | 'ENVIADO'
  | 'ENTREGADO'
  | 'CANCELADO';

interface DashboardStats {
  totalProductos: number | null;
  totalPedidos: number | null;
  totalClientes: number | null;
  ventasTotales: number | null;
  pedidosPendientes: number | null;
  productosStockBajo: number | null;
}

interface PedidoReciente {
  id: number;
  numeroPedido: string;
  cliente: string;
  fecha: string;
  total: number;
  estado: EstadoPedido;
}

interface ProductoStockBajo {
  id: number;
  nombre: string;
  stock: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly adminService = inject(AdminService);

  estadisticas = signal<DashboardStats>({
    totalProductos: null,
    totalPedidos: null,
    totalClientes: null,
    ventasTotales: null,
    pedidosPendientes: null,
    productosStockBajo: null
  });

  pedidosRecientes = signal<PedidoReciente[]>([]);
  productosStockBajo = signal<ProductoStockBajo[]>([]);
  topProductos = signal<any[]>([]);

  cargando = signal(true);
  datosDisponibles = signal(false);
  fechaActual = new Date();

  totalPedidosRecientes = computed(() => this.pedidosRecientes().length);
  totalProductosStockBajo = computed(() => this.productosStockBajo().length);

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.adminService.getDashboardStats().subscribe({
      next: (data) => {
        this.estadisticas.set({
          totalProductos: data.totalProductos,
          totalPedidos: data.totalPedidos,
          totalClientes: data.totalUsuarios,
          ventasTotales: data.ingresosTotales,
          pedidosPendientes: data.pedidosPendientes,
          productosStockBajo: data.stockBajo
        });

        if (data.ultimosPedidos) {
          this.pedidosRecientes.set(data.ultimosPedidos.map((p: any) => ({
            id: p.id,
            numeroPedido: p.numeroPedido,
            cliente: p.dirDestinatario || 'Cliente',
            fecha: p.creadoEn,
            total: p.total,
            estado: p.estado as EstadoPedido
          })));
        }

        if (data.topProductos) {
          this.topProductos.set(data.topProductos);
        }

        this.datosDisponibles.set(true);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
      }
    });
  }

  obtenerEstadoTexto(estado: EstadoPedido): string {
    const estados: Record<EstadoPedido, string> = {
      PENDIENTE: 'Pendiente',
      CONFIRMADO: 'Confirmado',
      EN_PREPARACION: 'En preparación',
      ENVIADO: 'Enviado',
      ENTREGADO: 'Entregado',
      CANCELADO: 'Cancelado'
    };

    return estados[estado] || estado;
  }
}
