
import {
  Component,
  signal,
  computed
} from '@angular/core';

import { DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  EstadoPedido,
  PedidoItem
} from '../../../../../core/models/pedido.interface';

interface PedidoAdmin {
  id: number;
  numeroPedido: string;
  clienteNombre: string;
  clienteEmail: string;
  estado: EstadoPedido;
  items: PedidoItem[];
  subtotal: number;
  costoEnvio: number;
  total: number;
  metodoPago?: string;
  creadoEn: string;
  dirDestinatario?: string;
  dirLinea1?: string;
  dirDistrito?: string;
  dirProvincia?: string;
  dirDepartamento?: string;
}

@Component({
  selector: 'app-pedidos-admin',
  imports: [
    DecimalPipe,
    DatePipe,
    FormsModule
  ],
  templateUrl: './pedidos-admin.html',
  styleUrl: './pedidos-admin.scss',
})
export class PedidosAdmin {

  // Pedidos administrativos.
  // Se conectarán cuando implementemos la API.
  pedidos = signal<PedidoAdmin[]>([]);

  // Estados
  cargando = signal(false);
  error = signal('');

  // Conexión pendiente
  datosDisponibles = signal(false);
  edicionDisponible = signal(false);

  // Filtros
  busqueda = signal('');
  filtroEstado = signal<string>('TODOS');

  // Detalle
  pedidoSeleccionado = signal<PedidoAdmin | null>(null);
  mostrarDetalle = signal(false);

  // Estadísticas
  totalPedidos = computed(
    () => this.pedidos().length
  );

  pendientes = computed(
    () => this.pedidos().filter(
      p => p.estado === 'PENDIENTE'
    ).length
  );

  enPreparacion = computed(
    () => this.pedidos().filter(
      p => p.estado === 'EN_PREPARACION'
    ).length
  );

  entregados = computed(
    () => this.pedidos().filter(
      p => p.estado === 'ENTREGADO'
    ).length
  );

  // Pedidos filtrados
  pedidosFiltrados = computed(() => {

    const texto = this.busqueda().toLowerCase().trim();
    const estado = this.filtroEstado();

    return this.pedidos().filter(pedido => {

      const coincideBusqueda =
        !texto ||
        pedido.numeroPedido.toLowerCase().includes(texto) ||
        pedido.clienteNombre.toLowerCase().includes(texto) ||
        pedido.clienteEmail.toLowerCase().includes(texto);

      const coincideEstado =
        estado === 'TODOS' ||
        pedido.estado === estado;

      return coincideBusqueda && coincideEstado;
    });

  });

  cambiarBusqueda(texto: string): void {
    this.busqueda.set(texto);
  }

  cambiarFiltroEstado(estado: string): void {
    this.filtroEstado.set(estado);
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.filtroEstado.set('TODOS');
  }

  verDetalle(pedido: PedidoAdmin): void {
    this.pedidoSeleccionado.set(pedido);
    this.mostrarDetalle.set(true);
  }

  cerrarDetalle(): void {
    this.mostrarDetalle.set(false);
    this.pedidoSeleccionado.set(null);
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

    return estados[estado];
  }

  obtenerMetodoPagoTexto(metodo?: string): string {
    const metodos: Record<string, string> = {
      YAPE: 'Yape',
      PLIN: 'Plin',
      TRANSFERENCIA: 'Transferencia bancaria'
    };

    return metodo
      ? metodos[metodo] ?? metodo
      : 'No especificado';
  }

  obtenerCantidadProductos(pedido: PedidoAdmin): number {
    return pedido.items.reduce(
      (total, item) => total + item.cantidad,
      0
    );
  }

  // Esta función determina si un pedido puede cambiar de estado.
  puedeEditarEstado(pedido: PedidoAdmin): boolean {
    return this.edicionDisponible() &&
      pedido.estado !== 'CANCELADO' &&
      pedido.estado !== 'ENTREGADO';
  }
}
