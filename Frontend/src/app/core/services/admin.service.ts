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

  getReportes(filtros: { periodo?: string; estado?: string; metodoPago?: string; fechaInicio?: string; fechaFin?: string } = {}): Observable<any> {
    const params: any = {};
    if (filtros.periodo) params.periodo = filtros.periodo;
    if (filtros.estado && filtros.estado !== 'TODOS') params.estado = filtros.estado;
    if (filtros.metodoPago && filtros.metodoPago !== 'TODOS') params.metodoPago = filtros.metodoPago;
    if (filtros.fechaInicio) params.fechaInicio = filtros.fechaInicio;
    if (filtros.fechaFin) params.fechaFin = filtros.fechaFin;
    return this.http.get<any>(`${this.apiUrl}/reportes`, { params });
  }

  descargarReportePdf(filtros: { periodo?: string; estado?: string; metodoPago?: string; fechaInicio?: string; fechaFin?: string } = {}): Observable<Blob> {
    const params: any = {};
    if (filtros.periodo) params.periodo = filtros.periodo;
    if (filtros.estado && filtros.estado !== 'TODOS') params.estado = filtros.estado;
    if (filtros.metodoPago && filtros.metodoPago !== 'TODOS') params.metodoPago = filtros.metodoPago;
    if (filtros.fechaInicio) params.fechaInicio = filtros.fechaInicio;
    if (filtros.fechaFin) params.fechaFin = filtros.fechaFin;
    return this.http.get(`${this.apiUrl}/reportes/pdf`, {
      params,
      responseType: 'blob'
    });
  }

  descargarReporteVentasPdf(filtros: { periodo?: string; estado?: string; metodoPago?: string; fechaInicio?: string; fechaFin?: string } = {}): Observable<Blob> {
    const params: any = {};
    if (filtros.periodo) params.periodo = filtros.periodo;
    if (filtros.estado && filtros.estado !== 'TODOS') params.estado = filtros.estado;
    if (filtros.metodoPago && filtros.metodoPago !== 'TODOS') params.metodoPago = filtros.metodoPago;
    if (filtros.fechaInicio) params.fechaInicio = filtros.fechaInicio;
    if (filtros.fechaFin) params.fechaFin = filtros.fechaFin;
    return this.http.get(`${this.apiUrl}/reportes/ventas/pdf`, {
      params,
      responseType: 'blob'
    });
  }

  descargarReporteProductosPdf(filtros: { periodo?: string; estado?: string; metodoPago?: string; fechaInicio?: string; fechaFin?: string } = {}): Observable<Blob> {
    const params: any = {};
    if (filtros.periodo) params.periodo = filtros.periodo;
    if (filtros.estado && filtros.estado !== 'TODOS') params.estado = filtros.estado;
    if (filtros.metodoPago && filtros.metodoPago !== 'TODOS') params.metodoPago = filtros.metodoPago;
    if (filtros.fechaInicio) params.fechaInicio = filtros.fechaInicio;
    if (filtros.fechaFin) params.fechaFin = filtros.fechaFin;
    return this.http.get(`${this.apiUrl}/reportes/productos/pdf`, {
      params,
      responseType: 'blob'
    });
  }

  descargarReportePedidosPdf(filtros: { periodo?: string; estado?: string; metodoPago?: string; fechaInicio?: string; fechaFin?: string } = {}): Observable<Blob> {
    const params: any = {};
    if (filtros.periodo) params.periodo = filtros.periodo;
    if (filtros.estado && filtros.estado !== 'TODOS') params.estado = filtros.estado;
    if (filtros.metodoPago && filtros.metodoPago !== 'TODOS') params.metodoPago = filtros.metodoPago;
    if (filtros.fechaInicio) params.fechaInicio = filtros.fechaInicio;
    if (filtros.fechaFin) params.fechaFin = filtros.fechaFin;
    return this.http.get(`${this.apiUrl}/reportes/pedidos/pdf`, {
      params,
      responseType: 'blob'
    });
  }

  descargarReporteClientesPdf(filtros: { periodo?: string; estado?: string; metodoPago?: string; fechaInicio?: string; fechaFin?: string } = {}): Observable<Blob> {
    const params: any = {};
    if (filtros.periodo) params.periodo = filtros.periodo;
    if (filtros.estado && filtros.estado !== 'TODOS') params.estado = filtros.estado;
    if (filtros.metodoPago && filtros.metodoPago !== 'TODOS') params.metodoPago = filtros.metodoPago;
    if (filtros.fechaInicio) params.fechaInicio = filtros.fechaInicio;
    if (filtros.fechaFin) params.fechaFin = filtros.fechaFin;
    return this.http.get(`${this.apiUrl}/reportes/clientes/pdf`, {
      params,
      responseType: 'blob'
    });
  }
}
