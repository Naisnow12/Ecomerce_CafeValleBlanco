
import {
  Component,
  signal,
  computed,
  inject,
  OnInit
} from '@angular/core';

import { DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../../../core/services/admin.service';

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
  standalone: true,
  imports: [
    DecimalPipe,
    DatePipe,
    FormsModule
  ],
  templateUrl: './pedidos-admin.html',
  styleUrl: './pedidos-admin.scss',
})
export class PedidosAdmin implements OnInit {
  private readonly adminService = inject(AdminService);

  // Pedidos administrativos conectados con backend
  pedidos = signal<PedidoAdmin[]>([]);

  // Estados
  cargando = signal(false);
  error = signal('');

  datosDisponibles = signal(false);
  edicionDisponible = signal(true);

  // Filtros
  busqueda = signal('');
  filtroEstado = signal<string>('TODOS');

  // Detalle
  pedidoSeleccionado = signal<PedidoAdmin | null>(null);
  mostrarDetalle = signal(false);

  // Estadísticas
  totalPedidos = computed(() => this.pedidos().length);
  pendientes = computed(() => this.pedidos().filter(p => p.estado === 'PENDIENTE').length);
  enPreparacion = computed(() => this.pedidos().filter(p => p.estado === 'EN_PREPARACION').length);
  entregados = computed(() => this.pedidos().filter(p => p.estado === 'ENTREGADO').length);

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando.set(true);
    this.error.set('');
    this.adminService.getPedidos().subscribe({
      next: (data: any[]) => {
        this.pedidos.set(data.map(p => ({
          id: p.id,
          numeroPedido: p.numeroPedido,
          clienteNombre: p.dirDestinatario || 'Cliente',
          clienteEmail: p.dirTelefono ? `Tel: ${p.dirTelefono}` : 'Registrado',
          estado: p.estado as EstadoPedido,
          items: p.items || [],
          subtotal: p.subtotal,
          costoEnvio: p.costoEnvio,
          total: p.total,
          metodoPago: p.metodoPago,
          creadoEn: p.creadoEn,
          dirDestinatario: p.dirDestinatario,
          dirLinea1: p.dirLinea1,
          dirDistrito: p.dirDistrito,
          dirProvincia: p.dirProvincia,
          dirDepartamento: p.dirDepartamento
        })));
        this.datosDisponibles.set(true);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de pedidos desde el servidor.');
        this.cargando.set(false);
      }
    });
  }

  cambiarEstado(pedidoId: number, nuevoEstado: string): void {
    this.adminService.actualizarEstadoPedido(pedidoId, nuevoEstado).subscribe({
      next: () => {
        this.cargarPedidos();
        if (this.pedidoSeleccionado() && this.pedidoSeleccionado()?.id === pedidoId) {
          const actual = this.pedidoSeleccionado()!;
          actual.estado = nuevoEstado as EstadoPedido;
          this.pedidoSeleccionado.set({ ...actual });
        }
      },
      error: (err) => {
        alert(err.error?.message || 'Error al actualizar estado del pedido');
      }
    });
  }

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
