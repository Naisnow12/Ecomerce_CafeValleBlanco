export interface CarritoItem {
  id: number;
  productoId: number;
  productoNombre: string;
  nombreSecundario?: string;
  imagenUrl: string;
  peso: number;
  precioUnitario: number;
  cantidad: number;
  subtotalItem: number;
  precioId: number;
}

export interface CarritoResponse {
  id: number;
  items: CarritoItem[];
  subtotal: number;
  total: number;
}

export interface AgregarAlCarritoRequest {
  precioId: number;
  cantidad: number;
}
