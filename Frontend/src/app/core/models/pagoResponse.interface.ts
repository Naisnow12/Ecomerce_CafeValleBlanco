export interface PagoResponse {
    proveedor: string;
    estadoPago: string;
    idTransaccion: string;
    initPointUrl: string;
    monto: number;
    moneda: string;
    mensaje: string;
}