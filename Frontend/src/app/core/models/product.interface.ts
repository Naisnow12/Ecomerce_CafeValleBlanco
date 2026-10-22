import { notesCata } from "./notesCata.interface";
import { precios } from "./precios.interface";

export interface Product {
    id?: number;
    slug: string;
    imagenUrl: string;
    nombre: string;
    stock: number;
    notasCata: notesCata[];
    precios: precios[];

}