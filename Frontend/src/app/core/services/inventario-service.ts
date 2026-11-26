import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../constants/app-config';
import { Product } from '../models/product.interface';
import { MovimientoStock, MovimientoStockRequest } from '../models/movimientos.interface';

@Service()
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
