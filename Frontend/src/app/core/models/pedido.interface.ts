export type EstadoPedido =
  | 'PENDIENTE'
  | 'CONFIRMADO'
  | 'EN_PREPARACION'
  | 'ENVIADO'
  | 'ENTREGADO'
  | 'CANCELADO';

export interface PedidoItem {
  productoId: number;
  nombreProducto: string;
  nombreSecundario?: string;
  peso: number;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
  imagenUrl?: string;
}

export interface PedidoResponse {
  id: number;
  numeroPedido: string;
  estado: EstadoPedido;
  items: PedidoItem[];
  subtotal: number;
  costoEnvio: number;
  total: number;
  metodoPago?: string;
  creadoEn: string;
  dirLinea1?: string;
  dirDistrito?: string;
  dirProvincia?: string;
  dirDepartamento?: string;
  dirDestinatario?: string;
  clienteEmail?: string;
  clienteNombre?: string;
}

export interface CrearPedidoRequest {
  direccionId: number;
  metodoPago: string;
  notas?: string;
}
