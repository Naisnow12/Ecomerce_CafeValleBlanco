import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  CarritoResponse,
  AgregarAlCarritoRequest
} from '../models/carrito.interface';

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/carrito';

  carrito = signal<CarritoResponse | null>(null);
  itemCount = computed(() => this.carrito()?.items?.length ?? 0);

  cargarCarrito(): Observable<CarritoResponse> {
    return this.http.get<CarritoResponse>(this.apiUrl).pipe(
      tap(res => this.carrito.set(res))
    );
  }

  agregarItem(req: AgregarAlCarritoRequest): Observable<CarritoResponse> {
    return this.http.post<CarritoResponse>(`${this.apiUrl}/items`, req).pipe(
      tap(res => this.carrito.set(res))
    );
  }

  actualizarCantidad(itemId: number, cantidad: number): Observable<CarritoResponse> {
    return this.http.put<CarritoResponse>(`${this.apiUrl}/items/${itemId}`, { cantidad }).pipe(
      tap(res => this.carrito.set(res))
    );
  }

  eliminarItem(itemId: number): Observable<CarritoResponse> {
    return this.http.delete<CarritoResponse>(`${this.apiUrl}/items/${itemId}`).pipe(
      tap(res => this.carrito.set(res))
    );
  }

  vaciarCarrito(): Observable<void> {
    return this.http.delete<void>(this.apiUrl).pipe(
      tap(() => this.carrito.set(null))
    );
  }
}
