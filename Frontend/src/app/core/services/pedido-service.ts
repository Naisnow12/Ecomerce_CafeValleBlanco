import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CrearPedidoRequest, PedidoResponse } from '../models/pedido.interface';
import { APP_CONFIG } from '../constants/app-config';

@Service()
export class PedidoService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${APP_CONFIG.apiUrl}/pedidos`;

    crearPedido(req: CrearPedidoRequest): Observable<PedidoResponse> {
        return this.http.post<PedidoResponse>(this.apiUrl, req);
    }

    getMisPedidos(): Observable<PedidoResponse[]> {
        return this.http.get<PedidoResponse[]>(this.apiUrl);
    }

    getDetallePedido(id: number): Observable<PedidoResponse> {
        return this.http.get<PedidoResponse>(`${this.apiUrl}/${id}`);
    }

    descargarComprobante(id: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/${id}/comprobante`, {
            responseType: 'blob'
        });
    }
}
