import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../constants/app-config';
import { PedidoResponse } from '../models/pedido.interface';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${APP_CONFIG.apiUrl}/admin`;

  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`);
  }

  getUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/usuarios`);
  }

  cambiarEstadoUsuario(id: number, activo: boolean): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/usuarios/${id}/estado`, { activo });
  }

  asignarRoles(id: number, roles: string[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/usuarios/${id}/roles`, { roles });
  }

  getPedidos(): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${this.apiUrl}/pedidos`);
  }

  actualizarEstadoPedido(id: number, estado: string): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(`${this.apiUrl}/pedidos/${id}/estado`, { estado });
  }

  getAuditoria(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/auditoria`);
  }
}
