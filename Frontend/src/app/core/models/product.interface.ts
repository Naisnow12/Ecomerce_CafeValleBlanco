import { notesCata } from "./notesCata.interface";
import { precios } from "./precios.interface";

export interface Product {
    id?: number;
    slug: string;
    imagenUrl: string;
    nombre: string;
    nombreSecundario?: string;
    tipoCafe?: string;
    molienda?: string;
    tueste?: string;
    variedad?: string;
    altitud?: string;
    descripcion?: string;
    stock: number;
    esDestacado?: boolean;
    esOscuro?: boolean;
    badgeText?: string;
    notasCata: notesCata[];
    precios: precios[];
}