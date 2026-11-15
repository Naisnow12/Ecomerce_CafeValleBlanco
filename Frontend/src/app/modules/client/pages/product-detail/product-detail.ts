import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService } from '../../../../core/services/product.service';
import { CarritoService } from '../../../../core/services/carrito.service';
import { AuthService } from '../../../../core/services/auth-service';
import { Product } from '../../../../core/models/product.interface';
import { precios } from '../../../../core/models/precios.interface';

import { APP_CONFIG } from '../../../../core/constants/app-config';

@Component({
  imports: [RouterLink, DecimalPipe],
  selector: 'app-product-detail',
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss'
})

export class ProductDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);

  product = signal<Product | null>(null);
  selectedPrecio = signal<precios | null>(null);
  cantidad = signal(1);
  isLoading = signal(true);
  isAddingToCart = signal(false);
  cartMessage = signal<string | null>(null);
  cartError = signal<string | null>(null);

  isLoggedIn = this.authService.isLoggedIn;

  // Fallback catálogo local si el backend no está disponible o el producto viene de fallback
  private readonly fallbackProductos: Product[] = [
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
      descripcion: 'Molido artesanalmente para filtrado, prensa francesa y métodos de goteo. Listo para preparar en casa con la frescura y el aroma del grano recién tostado en Villa Rica.',
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
      altitud: '1,700 msnm',
      descripcion: 'Para los apasionados del ritual del café. Granos enteros seleccionados a mano, tostados suavemente para preservar la pureza, los aceites esenciales y los matices florales.',
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
        { id: 3, id_product: 2, peso: 250, precio: 36.00 },
        { id: 4, id_product: 2, peso: 1000, precio: 110.00 }
      ]
    },
    {
      id: 3,
      slug: 'valle-blanco-intenso',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Intenso',
      tipoCafe: 'Espresso',
      molienda: 'Espresso',
      tueste: 'Oscuro',
      variedad: 'Caturra',
      altitud: '1,400 msnm',
      descripcion: 'Tueste oscuro de precisión para extracciones intensas. Cuerpo pleno, crema dorada y un aroma profundo que permanece en cada taza de espresso o americano.',
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
        { id: 5, id_product: 3, peso: 250, precio: 38.00 },
        { id: 6, id_product: 3, peso: 500, precio: 70.00 }
      ]
    },
    {
      id: 4,
      slug: 'valle-blanco-reserva-oxapampa',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Reserva Oxapampa',
      tipoCafe: 'Café Molido',
      molienda: 'Molido fino',
      tueste: 'Medio',
      variedad: 'Bourbon / Typica',
      altitud: '1,750 msnm',
      descripcion: 'Molienda calibrada para cafetera italiana Moka y Aeropress. Perfil dulce con notas acarameladas y acidez suave cultivado en las laderas de Villa Rica.',
      imagenUrl: 'assets/images/producto-grano.jpg',
      stock: 28,
      badgeText: 'RESERVA',
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 3, nombre: 'Caramelo' },
        { id: 9, nombre: 'Panela' },
        { id: 10, nombre: 'Almendra' }
      ],
      precios: [
        { id: 7, id_product: 4, peso: 250, precio: 40.00 },
        { id: 8, id_product: 4, peso: 500, precio: 75.00 }
      ]
    },
    {
      id: 5,
      slug: 'valle-blanco-altiplano',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Altiplano',
      tipoCafe: 'Especialidad',
      molienda: 'Molido grueso',
      tueste: 'Medio',
      variedad: 'Geisha / Typica',
      altitud: '1,800 msnm',
      descripcion: 'Molienda gruesa diseñada especialmente para cold brew y prensa francesa. Extracción limpia, sedosa y refrescante con notas a frutos rojos y flor de jazmín.',
      imagenUrl: 'assets/images/producto-molido.jpg',
      stock: 20,
      badgeText: 'ESPECIALIDAD',
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 11, nombre: 'Frutos rojos' },
        { id: 12, nombre: 'Jazmín' },
        { id: 13, nombre: 'Vainilla' }
      ],
      precios: [
        { id: 9, id_product: 5, peso: 250, precio: 45.00 }
      ]
    },
    {
      id: 6,
      slug: 'valle-blanco-seleccion-familiar',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Selección Familiar',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Medio',
      variedad: 'Caturra / Typica',
      altitud: '1,650 msnm',
      descripcion: 'El café insignia de la familia Valle Blanco en Villa Rica. Un balance redondo entre dulzor achocolatado, notas a nuez y acidez cítrica moderada.',
      imagenUrl: 'assets/images/producto-grano.jpg',
      stock: 45,
      badgeText: 'INSIGNIA',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 1, nombre: 'Chocolate' },
        { id: 8, nombre: 'Nuez tostada' },
        { id: 6, nombre: 'Miel' }
      ],
      precios: [
        { id: 10, id_product: 6, peso: 340, precio: 40.00 },
        { id: 11, id_product: 6, peso: 1000, precio: 115.00 }
      ]
    },
    {
      id: 7,
      slug: 'cusco',
      nombre: 'Cusco Quillabamba Especial',
      nombreSecundario: 'Edición Valle',
      tipoCafe: 'Café Molido',
      molienda: 'Molido medio',
      tueste: 'Medio',
      variedad: 'Typica',
      altitud: '1,600 msnm',
      descripcion: 'Café de especialidad con notas a chocolate amargo y miel silvestre recolectado artesanalmente.',
      imagenUrl: 'assets/images/producto-molido.jpg',
      stock: 45,
      badgeText: 'ESPECIAL',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 1, nombre: 'Chocolate amargo' },
        { id: 6, nombre: 'Miel silvestre' }
      ],
      precios: [
        { id: 12, id_product: 7, peso: 250, precio: 38.00 },
        { id: 13, id_product: 7, peso: 500, precio: 70.00 }
      ]
    },
    {
      id: 8,
      slug: 'villa-rica',
      nombre: 'Villa Rica Alto Palomar',
      nombreSecundario: 'Finca Tradición',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Medio',
      variedad: 'Caturra',
      altitud: '1,650 msnm',
      descripcion: 'Cultivado en las altas laderas de Oxapampa con secado solar prolongado en camas africanas.',
      imagenUrl: 'assets/images/producto-grano.jpg',
      stock: 30,
      badgeText: 'PALOMAR',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 9, nombre: 'Panela orgánica' },
        { id: 8, nombre: 'Avellana tostada' }
      ],
      precios: [
        { id: 14, id_product: 8, peso: 250, precio: 42.00 },
        { id: 15, id_product: 8, peso: 1000, precio: 150.00 }
      ]
    },
    {
      id: 9,
      slug: 'chanchamayo',
      nombre: 'Chanchamayo Reserve',
      nombreSecundario: 'Cosecha Dorada',
      tipoCafe: 'Café Molido',
      molienda: 'Molido fino',
      tueste: 'Medio',
      variedad: 'Bourbon',
      altitud: '1,500 msnm',
      descripcion: 'Cuerpo cremoso con acidez delicada y notas acarameladas de los valles centrales.',
      imagenUrl: 'assets/images/producto-molido.jpg',
      stock: 60,
      badgeText: 'RESERVA',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 3, nombre: 'Caramelo' },
        { id: 8, nombre: 'Nuez moscada' }
      ],
      precios: [
        { id: 16, id_product: 9, peso: 250, precio: 35.00 },
        { id: 17, id_product: 9, peso: 500, precio: 65.00 }
      ]
    },
    {
      id: 10,
      slug: 'sandia-puno',
      nombre: 'Sandia Puno Apu Blend',
      nombreSecundario: 'Alturas del Sur',
      tipoCafe: 'Especialidad',
      molienda: 'En Grano',
      tueste: 'Medio claro',
      variedad: 'Typica',
      altitud: '1,800 msnm',
      descripcion: 'Notas a frutos rojos y flor de jamaica cultivado en microclimas de altitud extrema.',
      imagenUrl: 'assets/images/producto-grano.jpg',
      stock: 18,
      badgeText: 'APU BLEND',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 11, nombre: 'Frutos rojos' },
        { id: 4, nombre: 'Flor de jamaica' }
      ],
      precios: [
        { id: 18, id_product: 10, peso: 250, precio: 48.00 },
        { id: 19, id_product: 10, peso: 500, precio: 90.00 }
      ]
    }
  ];

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.router.navigate(['/client/catalog']);
      return;
    }

    this.productService.getProductBySlug(slug).subscribe({
      next: (p) => {
        this.asignarProducto(p);
      },
      error: () => {
        // Fallback resiliente: buscar en catálogo local por slug
        const localProduct = this.fallbackProductos.find(p => p.slug === slug);
        if (localProduct) {
          this.asignarProducto(localProduct);
        } else {
          this.isLoading.set(false);
          this.router.navigate(['/client/catalog']);
        }
      }
    });
  }

  private asignarProducto(p: Product): void {
    this.product.set(p);
    if (p.precios?.length > 0) {
      this.selectedPrecio.set(p.precios[0]);
    }
    this.isLoading.set(false);
  }

  selectPrecio(precio: precios): void {
    this.selectedPrecio.set(precio);
  }

  increment(): void {
    const p = this.product();
    if (p && this.cantidad() < p.stock) {
      this.cantidad.update(v => v + 1);
    }
  }

  decrement(): void {
    if (this.cantidad() > 1) {
      this.cantidad.update(v => v - 1);
    }
  }

  agregarAlCarrito(): void {
    if (!this.isLoggedIn()) {
      this.router.navigate(['/auth/login'], {
        queryParams: { returnUrl: this.router.url }
      });
      return;
    }
    const precio = this.selectedPrecio();
    if (!precio?.id) return;

    this.isAddingToCart.set(true);
    this.cartMessage.set(null);
    this.cartError.set(null);

    this.carritoService.agregarItem({ precioId: precio.id, cantidad: this.cantidad() }).subscribe({
      next: () => {
        this.isAddingToCart.set(false);
        this.cartMessage.set('¡Producto agregado al carrito!');
        setTimeout(() => this.cartMessage.set(null), 3000);
      },
      error: () => {
        this.isAddingToCart.set(false);
        this.cartError.set('Error al agregar. Intenta nuevamente.');
      }
    });
  }

  consultarWhatsApp(): void {
    const p = this.product();
    const precio = this.selectedPrecio();
    if (!p) return;
    const pesoText = precio ? `${precio.peso}g` : '';
    const msg = encodeURIComponent(
      `Hola Valle Blanco! Me interesa el producto: ${p.nombre}${p.nombreSecundario ? ' - ' + p.nombreSecundario : ''} ${pesoText}. ¿Podrían darme más información?`
    );
    window.open(`https://wa.me/${APP_CONFIG.whatsappNumber}?text=${msg}`, '_blank');
  }
}
