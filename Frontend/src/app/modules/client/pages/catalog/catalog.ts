import { Component, OnInit, inject } from '@angular/core';
import { Product } from '../../../../core/models/product.interface';
import { ProductService } from '../../../../core/services/product.service';
import { NgOptimizedImage } from '@angular/common';

@Component({
  imports: [NgOptimizedImage],
  selector: 'app-catalog',
  templateUrl: './catalog.html',
})
export class Catalog implements OnInit {
  private readonly productService = inject(ProductService);

  listadeproductos: Product[] = [
    {
      id: 1,
      slug: "cusco",
      imagenUrl: "products_images/cafe_quillabamba_bolsa.jpg",
      nombre: "Cusco Quillabamba",
      stock: 45,
      "notasCata": [
        { "id": 1, "nombre": "Chocolate amargo" },
        { "id": 2, "nombre": "Naranja" },
        { "id": 3, "nombre": "Miel" }
      ],
      "precios": [
        { "id": 1, "id_product": 1, "peso": 250, "precio": 38.00 },
        { "id": 2, "id_product": 1, "peso": 500, "precio": 70.00 }
      ]
    },
    {
      "id": 2,
      slug: "cusco",
      imagenUrl: "products_images/cafe_quillabamba_bolsa.jpg",
      "nombre": "Villa Rica Alto Palomar",
      "stock": 30,
      "notasCata": [
        { "id": 4, "nombre": "Panela" },
        { "id": 5, "nombre": "Avellana" },
        { "id": 6, "nombre": "Cítricos dulces" }
      ],
      "precios": [
        { "id": 3, "id_product": 2, "peso": 250, "precio": 42.00 },
        { "id": 4, "id_product": 2, "peso": 1000, "precio": 150.00 }
      ]
    },
    {
      "id": 3,
      slug: "cusco",
      imagenUrl: "products_images/cafe_quillabamba_bolsa.jpg",
      "nombre": "Chanchamayo Reserve",
      "stock": 60,
      "notasCata": [
        { "id": 7, "nombre": "Caramelo" },
        { "id": 8, "nombre": "Nuez moscada" },
        { "id": 9, "nombre": "Cuerpo cremoso" }
      ],
      "precios": [
        { "id": 5, "id_product": 3, "peso": 250, "precio": 35.00 },
        { "id": 6, "id_product": 3, "peso": 500, "precio": 65.00 }
      ]
    },
    {
      "id": 4,
      slug: "cusco",
      imagenUrl: "products_images/cafe_quillabamba_bolsa.jpg",
      "nombre": "Jaén San Ignacio",
      "stock": 25,
      "notasCata": [
        { "id": 10, "nombre": "Jazmín" },
        { "id": 11, "nombre": "Durazno blanco" },
        { "id": 12, "nombre": "Té negro" }
      ],
      "precios": [
        { "id": 7, "id_product": 4, "peso": 250, "precio": 45.00 }
      ]
    },
    {
      "id": 5,
      slug: "cusco",
      imagenUrl: "products_images/cafe_quillabamba_bolsa.jpg",
      "nombre": "Sandia Puno - Apu Blend",
      "stock": 18,
      "notasCata": [
        { "id": 13, "nombre": "Frutos rojos" },
        { "id": 14, "nombre": "Flor de jamaica" },
        { "id": 15, "nombre": "Azúcar rubia" }
      ],
      "precios": [
        { "id": 8, "id_product": 5, "peso": 250, "precio": 48.00 },
        { "id": 9, "id_product": 5, "peso": 500, "precio": 90.00 }
      ]
    },
    {
      "id": 6,
      slug: "cusco",
      imagenUrl: "products_images/cafe_quillabamba_bolsa.jpg",
      "nombre": "Amazonas Rodríguez de Mendoza",
      "stock": 40,
      "notasCata": [
        { "id": 16, "nombre": "Manzana roja" },
        { "id": 17, "nombre": "Chocolate con leche" },
        { "id": 18, "nombre": "Vainilla" }
      ],
      "precios": [
        { "id": 10, "id_product": 6, "peso": 250, "precio": 40.00 },
        { "id": 11, "id_product": 6, "peso": 1000, "precio": 145.00 }
      ]
    }
  ]

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (products) => {
        if (products && products.length > 0) {
          this.listadeproductos = products;
        }
      },
      error: (err) => {
        console.warn('No se pudo conectar con el backend de Valle Blanco, usando datos locales:', err);
      }
    });
  }

  // Método para formatear los precios según la cantidad de presentaciones
  formatearPrecios(precios: { precio: number }[]): string {
    if (!precios || precios.length === 0) return 'Precio no disponible';

    // Caso 1: Si solo tiene un precio
    if (precios.length === 1) {
      return `S/ ${precios[0].precio.toFixed(2)}`;
    }

    // Caso 2: Si tiene exactamente 2 precios (Ej: "38.00 O 70.00")
    if (precios.length === 2) {
      return `S/ ${precios[0].precio.toFixed(2)} O S/ ${precios[1].precio.toFixed(2)}`;
    }

    // Caso 3: Si son más de 2 precios, sacamos el rango (menor y mayor)
    const preciosOrdenados = [...precios].sort((a, b) => a.precio - b.precio);
    const min = preciosOrdenados[0].precio.toFixed(2);
    const max = preciosOrdenados[preciosOrdenados.length - 1].precio.toFixed(2);

    return `S/ ${min} - S/ ${max}`;
  }
}
