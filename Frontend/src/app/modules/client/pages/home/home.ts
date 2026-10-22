import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../../../core/services/product.service';
import { Product } from '../../../../core/models/product.interface';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  selector: 'app-home',
  templateUrl: './home.html',
})
export class Home implements OnInit {
  private readonly productService = inject(ProductService);

  featuredProducts: Product[] = [];
  selectedFilter: string = 'all';

  // Fallback de variedades destacadas si el backend está apagado
  fallbackProducts: Product[] = [
    {
      id: 1,
      slug: 'cusco',
      nombre: 'Cusco Quillabamba Especial',
      imagenUrl: 'products_images/cafe_quillabamba_bolsa.jpg',
      stock: 45,
      notasCata: [
        { id: 1, nombre: 'Chocolate amargo' },
        { id: 2, nombre: 'Naranja confitada' },
        { id: 3, nombre: 'Miel silvestre' }
      ],
      precios: [
        { id: 1, id_product: 1, peso: 250, precio: 38.00 },
        { id: 2, id_product: 1, peso: 500, precio: 70.00 }
      ]
    },
    {
      id: 2,
      slug: 'villa-rica',
      nombre: 'Villa Rica Alto Palomar',
      imagenUrl: 'products_images/cafe_quillabamba_bolsa.jpg',
      stock: 30,
      notasCata: [
        { id: 4, nombre: 'Panela orgánica' },
        { id: 5, nombre: 'Avellana tostada' },
        { id: 6, nombre: 'Cítricos dulces' }
      ],
      precios: [
        { id: 3, id_product: 2, peso: 250, precio: 42.00 },
        { id: 4, id_product: 2, peso: 1000, precio: 150.00 }
      ]
    },
    {
      id: 3,
      slug: 'chanchamayo',
      nombre: 'Chanchamayo Reserve',
      imagenUrl: 'products_images/cafe_quillabamba_bolsa.jpg',
      stock: 60,
      notasCata: [
        { id: 7, nombre: 'Caramelo' },
        { id: 8, nombre: 'Nuez moscada' },
        { id: 9, nombre: 'Cuerpo cremoso' }
      ],
      precios: [
        { id: 5, id_product: 3, peso: 250, precio: 35.00 },
        { id: 6, id_product: 3, peso: 500, precio: 65.00 }
      ]
    },
    {
      id: 5,
      slug: 'sandia-puno',
      nombre: 'Sandia Puno Apu Blend',
      imagenUrl: 'products_images/cafe_quillabamba_bolsa.jpg',
      stock: 18,
      notasCata: [
        { id: 13, nombre: 'Frutos rojos' },
        { id: 14, nombre: 'Flor de jamaica' },
        { id: 15, nombre: 'Azúcar rubia' }
      ],
      precios: [
        { id: 8, id_product: 5, peso: 250, precio: 48.00 },
        { id: 9, id_product: 5, peso: 500, precio: 90.00 }
      ]
    }
  ];

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (products) => {
        if (products && products.length > 0) {
          this.featuredProducts = products.slice(0, 4);
        } else {
          this.featuredProducts = this.fallbackProducts;
        }
      },
      error: () => {
        this.featuredProducts = this.fallbackProducts;
      }
    });
  }

  setFilter(filter: string): void {
    this.selectedFilter = filter;
  }

  get filteredProducts(): Product[] {
    if (this.selectedFilter === 'afrutados') {
      return this.featuredProducts.filter(p => 
        p.notasCata?.some(n => /fruto|naranja|cítric|durazno|jamaica|manzana/i.test(n.nombre))
      );
    }
    if (this.selectedFilter === 'chocolatosos') {
      return this.featuredProducts.filter(p => 
        p.notasCata?.some(n => /chocolate|cacao|avellana|caramelo|nuez|panela/i.test(n.nombre))
      );
    }
    return this.featuredProducts;
  }

  getPrimaryPrice(product: Product): number {
    if (!product.precios || product.precios.length === 0) return 38.00;
    return product.precios[0].precio;
  }

  getPrimaryWeight(product: Product): number {
    if (!product.precios || product.precios.length === 0) return 250;
    return product.precios[0].peso;
  }
}
