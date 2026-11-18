
import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { PedidoService } from '../../../../core/services/pedido.service';
import {
  PedidoResponse,
  EstadoPedido
} from '../../../../core/models/pedido.interface';

@Component({
  selector: 'app-mis-pedidos',
  imports: [RouterLink, DecimalPipe, DatePipe],
  templateUrl: './mis-pedidos.html',
  styleUrl: './mis-pedidos.scss',
})
export class MisPedidos implements OnInit {

  private readonly pedidoService = inject(PedidoService);

  // Datos
  pedidos = signal<PedidoResponse[]>([]);

  // Estados
  cargando = signal(true);
  error = signal('');

  // Filtro
  filtroActual = signal<string>('TODOS');

  // Estadísticas
  totalPedidos = computed(() => this.pedidos().length);

  pedidosPendientes = computed(() =>
    this.pedidos().filter(p =>
      p.estado === 'PENDIENTE'
    ).length
  );

  pedidosEntregados = computed(() =>
    this.pedidos().filter(p =>
      p.estado === 'ENTREGADO'
    ).length
  );

  // Lista filtrada
  pedidosFiltrados = computed(() => {
    const filtro = this.filtroActual();
    const lista = this.pedidos();

    if (filtro === 'TODOS') {
      return lista;
    }

    return lista.filter(p => p.estado === filtro);
  });

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando.set(true);
    this.error.set('');

    this.pedidoService.getMisPedidos().subscribe({
      next: (pedidos) => {

        const ordenados = [...pedidos].sort((a, b) =>
          new Date(b.creadoEn).getTime() -
          new Date(a.creadoEn).getTime()
        );

        this.pedidos.set(ordenados);
        this.cargando.set(false);
      },

      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);

        if (err.status === 401 || err.status === 403) {
          this.error.set(
            'Tu sesión no es válida o no tienes permisos para consultar los pedidos.'
          );
        } else {
          this.error.set(
            'No se pudo cargar tu historial de pedidos.'
          );
        }
      }
    });
  }

  cambiarFiltro(filtro: string): void {
    this.filtroActual.set(filtro);
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

    return estados[estado] ?? estado;
  }

  obtenerMetodoPagoTexto(metodo?: string): string {
    const metodos: Record<string, string> = {
      YAPE: 'Yape',
      PLIN: 'Plin',
      TRANSFERENCIA: 'Transferencia bancaria'
    };

    return metodo ? (metodos[metodo] ?? metodo) : 'No especificado';
  }

  obtenerCantidadProductos(pedido: PedidoResponse): number {
    return pedido.items?.reduce(
      (total, item) => total + item.cantidad,
      0
    ) ?? 0;
  }
}
