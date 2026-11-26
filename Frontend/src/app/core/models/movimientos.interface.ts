import { Product } from "./product.interface";

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