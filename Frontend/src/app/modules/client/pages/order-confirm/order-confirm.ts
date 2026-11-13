
import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { PedidoService } from '../../../../core/services/pedido.service';
import { PedidoResponse } from '../../../../core/models/pedido.interface';

@Component({
  selector: 'app-order-confirm',
  imports: [RouterLink, DecimalPipe, DatePipe],
  templateUrl: './order-confirm.html',
  styleUrl: './order-confirm.scss',
})
export class OrderConfirm implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly pedidoService = inject(PedidoService);

  // Datos del pedido
  pedido = signal<PedidoResponse | null>(null);

  // Estados de la pantalla
  cargando = signal(true);
  error = signal('');

  ngOnInit(): void {
    const idParametro = this.route.snapshot.paramMap.get('id');
    const id = Number(idParametro);

    if (
      !idParametro ||
      !Number.isSafeInteger(id) ||
      id <= 0
    ) {
      this.error.set('El número de pedido no es válido.');
      this.cargando.set(false);
      return;
    }

    this.cargarPedido(id);
  }

  cargarPedido(id: number): void {
    this.cargando.set(true);
    this.error.set('');

    this.pedidoService.getDetallePedido(id).subscribe({
      next: (pedido) => {
        this.pedido.set(pedido);
        this.cargando.set(false);
      },

      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);

        if (err.status === 404) {
          this.error.set('No encontramos el pedido solicitado.');
        } else if (err.status === 403) {
          this.error.set(
            'No tienes permisos para consultar este pedido.'
          );
        } else {
          this.error.set(
            'No se pudo cargar la información del pedido.'
          );
        }
      }
    });
  }

  obtenerEstadoTexto(estado: string): string {
    const estados: Record<string, string> = {
      PENDIENTE: 'Pendiente de confirmación',
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

    return metodo ? (metodos[metodo] ?? metodo) : 'Por confirmar';
  }

  reintentar(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (Number.isSafeInteger(id) && id > 0) {
      this.cargarPedido(id);
    }
  }
}
