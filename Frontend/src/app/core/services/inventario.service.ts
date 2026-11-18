import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../constants/app-config';
import { Product } from '../models/product.interface';

export interface MovimientoStockRequest {
  productoId: number;
  tipo: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  cantidad: number;
  motivo?: string;
}

export interface MovimientoStock {
  id: number;
  producto: Product;
  tipo: string;
  cantidad: number;
  stockAnterior: number;
  stockNuevo: number;
  motivo: string;
  emailUsuario: string;
  creadoEn: string;
}

@Injectable({ providedIn: 'root' })
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${APP_CONFIG.apiUrl}/inventario`;

  getDashboard(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`);
  }

  getMovimientos(): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.apiUrl}/movimientos`);
  }

  registrarMovimiento(req: MovimientoStockRequest): Observable<MovimientoStock> {
    return this.http.post<MovimientoStock>(`${this.apiUrl}/movimientos`, req);
  }

  getAlertas(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/alertas`);
  }

  actualizarNivelMinimo(productoId: number, nivelMinimo: number): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/productos/${productoId}/nivel-minimo`, { nivelMinimo });
  }
}
