import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../constants/app-config';
import { PedidoResponse } from '../models/pedido.interface';

export interface PagoResponse {
  proveedor: string;
  estadoPago: string;
  idTransaccion: string;
  initPointUrl: string;
  monto: number;
  moneda: string;
  mensaje: string;
}

@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${APP_CONFIG.apiUrl}/pagos`;

  iniciarPago(pedidoId: number): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(`${this.apiUrl}/iniciar/${pedidoId}`, {});
  }

  actualizarEstado(pedidoId: number, estado: string): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(`${this.apiUrl}/estado/${pedidoId}`, { estado });
  }
}
