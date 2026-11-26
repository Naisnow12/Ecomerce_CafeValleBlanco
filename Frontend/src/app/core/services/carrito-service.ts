import { Service, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { CarritoResponse, AgregarAlCarritoRequest } from '../models/carrito.interface';
import { APP_CONFIG } from '../constants/app-config';

@Service()
export class CarritoService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${APP_CONFIG.apiUrl}/carrito`;
    private readonly localCartKey = 'vb_local_cart';

    carrito = signal<CarritoResponse | null>(this.loadLocalCart());
    itemCount = computed(() => {
        const items = this.carrito()?.items;
        if (!items || items.length === 0) return 0;
        return items.reduce((acc, curr) => acc + curr.cantidad, 0);
    });

    private loadLocalCart(): CarritoResponse | null {
        if (typeof localStorage === 'undefined') return null;
        try {
            const data = localStorage.getItem(this.localCartKey);
            return data ? JSON.parse(data) : null;
        } catch {
            return null;
        }
    }

    private saveLocalCart(cart: CarritoResponse | null): void {
        if (typeof localStorage === 'undefined') return;
        try {
            if (cart) {
                localStorage.setItem(this.localCartKey, JSON.stringify(cart));
            } else {
                localStorage.removeItem(this.localCartKey);
            }
        } catch {
            // Ignorar restricciones de almacenamiento
        }
    }

    cargarCarrito(): Observable<CarritoResponse> {
        return this.http.get<CarritoResponse>(this.apiUrl).pipe(
            tap(res => {
                this.carrito.set(res);
                this.saveLocalCart(res);
            })
        );
    }

    agregarItem(req: AgregarAlCarritoRequest): Observable<CarritoResponse> {
        return this.http.post<CarritoResponse>(`${this.apiUrl}/items`, req).pipe(
            tap(res => {
                this.carrito.set(res);
                this.saveLocalCart(res);
            })
        );
    }

    actualizarCantidad(itemId: number, cantidad: number): Observable<CarritoResponse> {
        return this.http.put<CarritoResponse>(`${this.apiUrl}/items/${itemId}`, { cantidad }).pipe(
            tap(res => {
                this.carrito.set(res);
                this.saveLocalCart(res);
            })
        );
    }

    eliminarItem(itemId: number): Observable<CarritoResponse> {
        return this.http.delete<CarritoResponse>(`${this.apiUrl}/items/${itemId}`).pipe(
            tap(res => {
                this.carrito.set(res);
                this.saveLocalCart(res);
            })
        );
    }

    vaciarCarrito(): Observable<void> {
        return this.http.delete<void>(this.apiUrl).pipe(
            tap(() => {
                this.carrito.set(null);
                this.saveLocalCart(null);
            })
        );
    }
}
