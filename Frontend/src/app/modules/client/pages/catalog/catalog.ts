import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Product } from '../../../../core/models/product.interface';
import { ProductService } from '../../../../core/services/product-service';
import { CarritoService } from '../../../../core/services/carrito-service';
import { AuthService } from '../../../../core/services/auth-service';
import { Router } from '@angular/router';

import { APP_CONFIG } from '../../../../core/constants/app-config';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-catalog',
  templateUrl: './catalog.html',
})
export class Catalog implements OnInit {

  private readonly productService = inject(ProductService);
  private readonly carritoService = inject(CarritoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  puedeComprar = this.authService.puedeComprar;

  agregandoId: number | null | undefined = null;
  mensajeAgregadoId: number | null | undefined = null;

  // PRODUCTOS
  listadeproductos: Product[] = [];
  cargandoProductos = true;

  // FILTROS
  filtroMolienda = 'todos';
  filtroTueste = 'todos';
  filtroVariedad = 'todos';
  filtroNota = 'todos';
  filtroTipoCafe = 'todos';
  ordenSeleccionado = 'destacados';

  // 9 PRESENTACIONES OFICIALES DE CAFÉ VALLE BLANCO
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

  readonly fallbackProductos: Product[] = [
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
      altitud: '1,800 msnm',
      descripcion: 'Para los que prefieren controlar cada detalle. Granos enteros tostados artesanalmente, listos para moler al momento y aprovechar al máximo el perfil aromático de Oxapampa.',
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
      id: 3,
      slug: 'valle-blanco-espresso',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Espresso',
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
      slug: 'valle-blanco-amanecer',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Amanecer',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Claro',
      variedad: 'Typica',
      altitud: '1,800 msnm',
      descripcion: 'Un café delicado y aromático, elaborado para quienes disfrutan de sabores suaves y complejos. Su tueste claro permite apreciar las características naturales del grano y sus notas florales y frutales.',
      imagenUrl: 'assets/images/producto-Amanecer.png',
      stock: 35,
      badgeText: 'PERFIL FLORAL',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 10, nombre: 'Jazmín' },
        { id: 11, nombre: 'Mandarina' },
        { id: 2, nombre: 'Durazno' }
      ],
      precios: [
        { id: 7, id_product: 4, peso: 250, precio: 44.00 }
      ]
    },
    {
      id: 5,
      slug: 'valle-blanco-miel-montana',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Miel de Montaña',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Medio',
      variedad: 'Caturra',
      altitud: '1,650 msnm',
      descripcion: 'Un café especial procesado mediante el método honey, que conserva parte del mucílago durante el secado. Ofrece una taza dulce, equilibrada y con una textura agradable.',
      imagenUrl: 'assets/images/producto-Mieldemontaña.png',
      stock: 28,
      badgeText: 'PROCESO HONEY',
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 6, nombre: 'Miel' },
        { id: 12, nombre: 'Frutos amarillos' },
        { id: 13, nombre: 'Almendra' }
      ],
      precios: [
        { id: 8, id_product: 5, peso: 340, precio: 46.00 }
      ]
    },
    {
      id: 6,
      slug: 'valle-blanco-frutos-rojos',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Frutos Rojos',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Medio',
      variedad: 'Caturra / Typica',
      altitud: '1,600 msnm',
      descripcion: 'Un café de carácter expresivo, cuyos granos se secan junto con la cereza del café. Su perfil destaca por sus notas frutales, dulces y una experiencia aromática intensa.',
      imagenUrl: 'assets/images/producto-Frutosrojos.png',
      stock: 22,
      badgeText: 'PROCESO NATURAL',
      esDestacado: false,
      esOscuro: true,
      notasCata: [
        { id: 14, nombre: 'Fresa' },
        { id: 15, nombre: 'Cereza' },
        { id: 1, nombre: 'Chocolate' }
      ],
      precios: [
        { id: 9, id_product: 6, peso: 250, precio: 42.00 }
      ]
    },
    {
      id: 7,
      slug: 'valle-blanco-reserva-altura',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Reserva de Altura',
      tipoCafe: 'Café en Grano',
      molienda: 'En Grano',
      tueste: 'Medio claro',
      variedad: 'Typica / Bourbon',
      altitud: '1,900 msnm',
      descripcion: 'Una selección especial de granos cultivados en zonas de mayor altitud de Villa Rica. Su perfil combina delicadeza aromática, dulzor natural y una acidez equilibrada.',
      imagenUrl: 'assets/images/producto-Reservadealtura.png',
      stock: 15,
      badgeText: 'EDICIÓN ESPECIAL',
      esDestacado: true,
      esOscuro: true,
      notasCata: [
        { id: 16, nombre: 'Naranja' },
        { id: 3, nombre: 'Caramelo' },
        { id: 10, nombre: 'Jazmín' }
      ],
      precios: [
        { id: 10, id_product: 7, peso: 340, precio: 58.00 }
      ]
    },
    {
      id: 8,
      slug: 'valle-blanco-tradicion',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Tradición',
      tipoCafe: 'Café Molido',
      molienda: 'Molido medio',
      tueste: 'Medio oscuro',
      variedad: 'Caturra',
      altitud: '1,500 msnm',
      descripcion: 'Inspirado en el sabor del café peruano de toda la vida. Su cuerpo equilibrado y su dulzor tostado lo convierten en una excelente opción para disfrutar diariamente en casa.',
      imagenUrl: 'assets/images/producto-Tradicion.png',
      stock: 50,
      badgeText: 'TRADICIONAL',
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 1, nombre: 'Chocolate' },
        { id: 17, nombre: 'Avellana' },
        { id: 18, nombre: 'Azúcar rubia' }
      ],
      precios: [
        { id: 11, id_product: 8, peso: 250, precio: 33.00 }
      ]
    },
    {
      id: 9,
      slug: 'valle-blanco-cold-brew',
      nombre: 'Valle Blanco',
      nombreSecundario: 'Cold Brew',
      tipoCafe: 'Café Molido',
      molienda: 'Molido grueso',
      tueste: 'Medio',
      variedad: 'Caturra / Typica',
      altitud: '1,600 msnm',
      descripcion: 'Una propuesta pensada para los amantes del café frío. Su molienda gruesa facilita la preparación mediante infusión prolongada en agua fría, obteniendo una bebida refrescante y de perfil dulce.',
      imagenUrl: 'assets/images/producto-ColdBrew.png',
      stock: 32,
      badgeText: 'EXTRACCIÓN FRÍA',
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 7, nombre: 'Cacao' },
        { id: 19, nombre: 'Ciruela' },
        { id: 20, nombre: 'Vainilla' }
      ],
      precios: [
        { id: 12, id_product: 9, peso: 250, precio: 38.00 }
      ]
    }
  ];

  // CARGA DE PRODUCTOS
  ngOnInit(): void {
    this.cargandoProductos = true;

    this.productService.getProducts().subscribe({
      next: (products) => {
        if (Array.isArray(products) && products.length > 0) {
          // Filtrar estrictamente las 9 presentaciones oficiales de Valle Blanco
          const oficiales = products.filter(p => this.slugsOficialesValleBlanco.includes(p.slug));
          this.listadeproductos = oficiales.length > 0 ? oficiales : this.fallbackProductos;
        } else {
          this.listadeproductos = this.fallbackProductos;
        }

        this.cargandoProductos = false;
        this.cdr.markForCheck();
      },

      error: (err) => {
        console.error('Error al cargar productos:', err);

        this.listadeproductos = this.fallbackProductos;
        this.cargandoProductos = false;
        this.cdr.markForCheck();
      }
    });
  }

  // PRODUCTOS FILTRADOS
  get productosFiltrados(): Product[] {
    return this.listadeproductos
      .filter(producto => {

        if (
          this.filtroMolienda !== 'todos' &&
          producto.molienda?.toLowerCase() !==
          this.filtroMolienda.toLowerCase()
        ) {
          return false;
        }

        if (
          this.filtroTueste !== 'todos' &&
          !producto.tueste?.toLowerCase()
            .includes(this.filtroTueste.toLowerCase())
        ) {
          return false;
        }

        if (
          this.filtroVariedad !== 'todos' &&
          !producto.variedad?.toLowerCase()
            .includes(this.filtroVariedad.toLowerCase())
        ) {
          return false;
        }

        if (
          this.filtroTipoCafe !== 'todos' &&
          producto.tipoCafe?.toLowerCase() !==
          this.filtroTipoCafe.toLowerCase()
        ) {
          return false;
        }

        if (this.filtroNota !== 'todos') {
          const tieneNota = producto.notasCata?.some(nota =>
            nota.nombre.toLowerCase()
              .includes(this.filtroNota.toLowerCase())
          );

          if (!tieneNota) {
            return false;
          }
        }

        return true;
      })
      .sort((a, b) => {

        if (this.ordenSeleccionado === 'precio-asc') {
          return this.getPrecioMinimo(a) -
            this.getPrecioMinimo(b);
        }

        if (this.ordenSeleccionado === 'precio-desc') {
          return this.getPrecioMinimo(b) -
            this.getPrecioMinimo(a);
        }

        // DESTACADOS PRIMERO
        if (a.esDestacado && !b.esDestacado) {
          return -1;
        }

        if (!a.esDestacado && b.esDestacado) {
          return 1;
        }

        return (a.id ?? 0) - (b.id ?? 0);
      });
  }

  // AGREGAR AL CARRITO DIRECTO
  agregarAlCarrito(producto: Product): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], {
        queryParams: { returnUrl: '/client/catalog' }
      });
      return;
    }

    if (!producto.precios?.length || (producto.stock ?? 0) <= 0) {
      return;
    }

    const precioId = producto.precios[0].id;
    if (!precioId) return;

    this.agregandoId = producto.id;
    this.carritoService.agregarItem({ precioId, cantidad: 1 }).subscribe({
      next: () => {
        this.agregandoId = null;
        this.mensajeAgregadoId = producto.id;
        this.cdr.markForCheck();
        setTimeout(() => {
          this.mensajeAgregadoId = null;
          this.cdr.markForCheck();
        }, 2500);
      },
      error: () => {
        this.agregandoId = null;
        this.cdr.markForCheck();
      }
    });
  }

  // RESTABLECER FILTROS
  limpiarFiltros(): void {
    this.filtroMolienda = 'todos';
    this.filtroTueste = 'todos';
    this.filtroVariedad = 'todos';
    this.filtroNota = 'todos';
    this.filtroTipoCafe = 'todos';
    this.ordenSeleccionado = 'destacados';
  }

  // PRECIO MÍNIMO
  getPrecioMinimo(producto: Product): number {
    if (!producto.precios?.length) {
      return 35.00;
    }

    return Math.min(
      ...producto.precios.map(precio => precio.precio)
    );
  }

  // PESO PRINCIPAL
  getPesoPrincipal(producto: Product): string {
    if (!producto.precios?.length) {
      return '250 g';
    }

    return `${producto.precios[0].peso} g`;
  }

  // WHATSAPP
  consultarPedido(producto: Product): void {
    const texto =
      `Hola Valle Blanco, deseo consultar disponibilidad y precio ` +
      `para el café: ${producto.nombre} ` +
      `${producto.nombreSecundario || ''} ` +
      `(${producto.molienda || 'Sin especificar'}).`;

    const url =
      `https://wa.me/${APP_CONFIG.whatsappNumber}?text=${encodeURIComponent(texto)}`;

    window.open(url, '_blank', 'noopener,noreferrer');
  }
}
