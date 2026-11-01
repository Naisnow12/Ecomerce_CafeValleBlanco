
import { Component, signal, computed } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

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
  imports: [RouterLink, DecimalPipe, DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {

  // Se conectará con los endpoints administrativos.
  // null significa que todavía no tenemos datos disponibles.
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

  cargando = signal(false);

  // Indica que el backend administrativo está pendiente.
  datosDisponibles = signal(false);

  fechaActual = new Date();

  totalPedidosRecientes = computed(
    () => this.pedidosRecientes().length
  );

  totalProductosStockBajo = computed(
    () => this.productosStockBajo().length
  );

  obtenerEstadoTexto(estado: EstadoPedido): string {
    const estados: Record<EstadoPedido, string> = {
      PENDIENTE: 'Pendiente',
      CONFIRMADO: 'Confirmado',
      EN_PREPARACION: 'En preparación',
      ENVIADO: 'Enviado',
      ENTREGADO: 'Entregado',
      CANCELADO: 'Cancelado'
    };

    return estados[estado];
  }
}
