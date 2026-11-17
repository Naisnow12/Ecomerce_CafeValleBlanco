import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ProductService } from '../../../../core/services/product.service';
import { CarritoService } from '../../../../core/services/carrito.service';
import { AuthService } from '../../../../core/services/auth-service';
import { Product } from '../../../../core/models/product.interface';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  selector: 'app-home',
  templateUrl: './home.html',
})
export class Home implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  featuredProducts: Product[] = [];
  selectedFilter: string = 'todos';
  agregandoId: number | null | undefined = null;
  mensajeAgregadoId: number | null | undefined = null;

  // Método de preparación activo para la guía rústica del barista
  metodoActivoId: string = 'v60';

  readonly metodos = [
    {
      id: 'v60',
      numero: 'I',
      nombre: 'Filtrado V60 & Chemex',
      molienda: 'Molido Medio',
      proporcion: '1:15 (15g de café por 225ml de agua)',
      temperatura: '92°C — 94°C',
      tiempo: '2:45 — 3:00 min',
      perfil: 'Extracción limpia, aromas florales a jazmín y acidez cítrica brillante.',
      productoNombre: 'Valle Blanco Amanecer',
      slug: 'valle-blanco-amanecer'
    },
    {
      id: 'prensa',
      numero: 'II',
      nombre: 'Prensa Francesa & Cold Brew',
      molienda: 'Molido Grueso',
      proporcion: '1:14 (20g de café por 280ml de agua)',
      temperatura: '90°C — 92°C',
      tiempo: '4:00 min inmersión',
      perfil: 'Cuerpo untuoso y profundo, notas a cacao puro, ciruela y vainilla.',
      productoNombre: 'Valle Blanco Cold Brew',
      slug: 'valle-blanco-cold-brew'
    },
    {
      id: 'moka',
      numero: 'III',
      nombre: 'Cafetera Italiana / Moka',
      molienda: 'Molido Fino',
      proporcion: 'Canastilla completa sin prensar',
      temperatura: 'Fuego medio-bajo constante',
      tiempo: '3:30 min',
      perfil: 'Intensidad clásica, crema persistente y notas a chocolate tostado y avellana.',
      productoNombre: 'Valle Blanco Espresso',
      slug: 'valle-blanco-espresso'
    },
    {
      id: 'grano',
      numero: 'IV',
      nombre: 'Molienda en Casa (Grano Entero)',
      molienda: 'En Grano Entero',
      proporcion: 'Ajustable a cualquier ritual',
      temperatura: 'Agua filtrada a 92°C',
      tiempo: 'Moler al momento',
      perfil: 'Frescura absoluta. Retiene intactos los aceites esenciales y la complejidad del terroir.',
      productoNombre: 'Valle Blanco Origen',
      slug: 'valle-blanco-origen'
    }
  ];

  // 6 Variedades destacadas oficiales de Valle Blanco para Home
  fallbackProducts: Product[] = [
    {
      id: 1,
      slug: 'valle-blanco-clasico',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Clásico',
      tipoCafe: 'Café Molido',
      molienda: 'Molido medio',
      tueste: 'Medio',
      variedad: 'Caturra / Typica',
      altitud: '1,600 msnm',
      descripcion: 'Molido artesanalmente para filtrado y métodos tradicionales. Notas a chocolate, durazno y caramelo.',
      imagenUrl: 'assets/images/producto-molido.jpg',
      stock: 50,
      badgeText: 'MÁS POPULAR',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 1, nombre: 'Chocolate' },
        { id: 2, nombre: 'Durazno' },
        { id: 3, nombre: 'Caramelo' }
      ],
      precios: [
        { id: 1, id_product: 1, peso: 250, precio: 35.00 },
        { id: 2, id_product: 1, peso: 500, precio: 65.00 }
      ]
    },
    {
      id: 2,
      slug: 'valle-blanco-origen',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Origen',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Medio claro',
      variedad: 'Typica',
      altitud: '1,800 msnm',
      descripcion: 'Granos enteros tostados para moler en casa y disfrutar la pureza floral y cítrica de Oxapampa.',
      imagenUrl: 'assets/images/producto-grano.jpg',
      stock: 40,
      badgeText: 'ORIGEN PURO',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 4, nombre: 'Floral' },
        { id: 5, nombre: 'Cítrico' },
        { id: 6, nombre: 'Miel' }
      ],
      precios: [
        { id: 3, id_product: 2, peso: 340, precio: 42.00 },
        { id: 4, id_product: 2, peso: 1000, precio: 120.00 }
      ]
    },
    {
      id: 4,
      slug: 'valle-blanco-amanecer',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Amanecer',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Claro',
      variedad: 'Typica',
      altitud: '1,800 msnm',
      descripcion: 'Un café delicado y aromático de tueste claro con notas florales a jazmín, mandarina y durazno.',
      imagenUrl: 'assets/images/producto-Amanecer.png',
      stock: 35,
      badgeText: 'PERFIL FLORAL',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 10, nombre: 'Jazmín' },
        { id: 11, nombre: 'Mandarina' },
        { id: 12, nombre: 'Durazno' }
      ],
      precios: [
        { id: 7, id_product: 4, peso: 250, precio: 38.00 }
      ]
    },
    {
      id: 5,
      slug: 'valle-blanco-miel-de-montana',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Miel de Montaña',
      tipoCafe: 'Proceso Honey',
      molienda: 'En Grano',
      tueste: 'Medio',
      variedad: 'Caturra',
      altitud: '1,650 msnm',
      descripcion: 'Proceso honey que conserva el mucílago del grano. Taza dulce, equilibrada con miel y frutos amarillos.',
      imagenUrl: 'assets/images/producto-Mieldemontaña.png',
      stock: 30,
      badgeText: 'PROCESO HONEY',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 13, nombre: 'Miel' },
        { id: 14, nombre: 'Frutos amarillos' },
        { id: 15, nombre: 'Almendra' }
      ],
      precios: [
        { id: 8, id_product: 5, peso: 340, precio: 45.00 }
      ]
    },
    {
      id: 6,
      slug: 'valle-blanco-frutos-rojos',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Frutos Rojos',
      tipoCafe: 'Proceso Natural',
      molienda: 'En Grano',
      tueste: 'Medio',
      variedad: 'Caturra / Typica',
      altitud: '1,600 msnm',
      descripcion: 'Secado lento en cereza con notas intensas y dulces a fresa silvestre, cereza madura y chocolate.',
      imagenUrl: 'assets/images/producto-Frutosrojos.png',
      stock: 25,
      badgeText: 'PROCESO NATURAL',
      esDestacado: true,
      esOscuro: true,
      notasCata: [
        { id: 16, nombre: 'Fresa' },
        { id: 17, nombre: 'Cereza' },
        { id: 18, nombre: 'Chocolate' }
      ],
      precios: [
        { id: 9, id_product: 6, peso: 250, precio: 46.00 }
      ]
    },
    {
      id: 3,
      slug: 'valle-blanco-espresso',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Espresso',
      tipoCafe: 'Espresso',
      molienda: 'Molido fino',
      tueste: 'Oscuro',
      variedad: 'Caturra',
      altitud: '1,400 msnm',
      descripcion: 'Tueste de precisión para alta presión. Cuerpo cremoso y aroma intenso a cacao oscuro y nuez tostada.',
      imagenUrl: 'assets/images/producto-espresso.jpg',
      stock: 35,
      badgeText: 'INTENSO',
      esDestacado: true,
      esOscuro: true,
      notasCata: [
        { id: 7, nombre: 'Cacao oscuro' },
        { id: 8, nombre: 'Nuez tostada' },
        { id: 9, nombre: 'Panela' }
      ],
      precios: [
        { id: 5, id_product: 3, peso: 250, precio: 38.00 }
      ]
    }
  ];

  readonly slugsOficialesValleBlanco: string[] = [
    'valle-blanco-clasico',
    'valle-blanco-origen',
    'valle-blanco-espresso',
    'valle-blanco-amanecer',
    'valle-blanco-miel-montana',
    'valle-blanco-frutos-rojos',
    'valle-blanco-reserva-altura',
    'valle-blanco-tradicion',
    'valle-blanco-cold-brew'
  ];

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (products) => {
        if (products && products.length > 0) {
          const oficiales = products.filter(p => this.slugsOficialesValleBlanco.includes(p.slug));
          this.featuredProducts = (oficiales.length > 0 ? oficiales : this.fallbackProducts).slice(0, 6);
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

  setMetodo(metodoId: string): void {
    this.metodoActivoId = metodoId;
  }

  get metodoActivo() {
    return this.metodos.find(m => m.id === this.metodoActivoId) || this.metodos[0];
  }

  get filteredProducts(): Product[] {
    const list = this.featuredProducts.length > 0 ? this.featuredProducts : this.fallbackProducts;
    if (this.selectedFilter === 'grano') {
      return list.filter(p => (p.molienda?.toLowerCase().includes('grano') || p.tipoCafe?.toLowerCase().includes('grano')));
    }
    if (this.selectedFilter === 'molido') {
      return list.filter(p => (p.molienda?.toLowerCase().includes('molido') || p.tipoCafe?.toLowerCase().includes('molido') || p.molienda?.toLowerCase().includes('espresso')));
    }
    if (this.selectedFilter === 'especial') {
      return list.filter(p => (p.tipoCafe?.toLowerCase().includes('honey') || p.tipoCafe?.toLowerCase().includes('natural') || p.tipoCafe?.toLowerCase().includes('especial') || p.badgeText?.toLowerCase().includes('floral')));
    }
    return list;
  }

  getPrimaryPrice(product: Product): number {
    if (!product.precios || product.precios.length === 0) return 35.00;
    return product.precios[0].precio;
  }

  getPrimaryWeight(product: Product): number {
    if (!product.precios || product.precios.length === 0) return 250;
    return product.precios[0].peso;
  }

  agregarAlCarrito(product: Product): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], {
        queryParams: { returnUrl: '/client/home' }
      });
      return;
    }

    if (!product.precios?.length || (product.stock ?? 0) <= 0) return;
    const precioId = product.precios[0].id;
    if (!precioId) return;

    this.agregandoId = product.id;
    this.carritoService.agregarItem({ precioId, cantidad: 1 }).subscribe({
      next: () => {
        this.agregandoId = null;
        this.mensajeAgregadoId = product.id;
        setTimeout(() => {
          this.mensajeAgregadoId = null;
        }, 2500);
      },
      error: () => {
        this.agregandoId = null;
      }
    });
  }
}
