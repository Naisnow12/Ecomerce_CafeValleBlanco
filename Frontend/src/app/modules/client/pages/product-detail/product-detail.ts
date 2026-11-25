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
  puedeComprar = this.authService.puedeComprar;

  // 9 PRESENTACIONES OFICIALES DE CAFÉ VALLE BLANCO
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
    const cant = this.cantidad();

    if (!precio || !precio.id) {
      this.cartError.set('Por favor, selecciona una presentación');
      return;
    }

    this.isAddingToCart.set(true);
    this.cartMessage.set(null);
    this.cartError.set(null);

    this.carritoService.agregarItem({ precioId: precio.id, cantidad: cant }).subscribe({
      next: () => {
        this.isAddingToCart.set(false);
        this.cartMessage.set(`¡Agregaste ${cant} unidad(es) de ${this.product()?.nombre} (${precio.peso}g) a tu carrito!`);
        setTimeout(() => this.cartMessage.set(null), 4000);
      },
      error: (err) => {
        this.isAddingToCart.set(false);
        this.cartError.set(err.error?.message || 'Error al agregar al carrito. Intenta nuevamente.');
      }
    });
  }

  consultarWhatsApp(): void {
    const p = this.product();
    const pr = this.selectedPrecio();
    const cant = this.cantidad();

    if (!p) return;

    const texto =
      `Hola Valle Blanco, deseo consultar sobre el café:\n` +
      `• *${p.nombre} ${p.nombreSecundario || ''}*\n` +
      `• Molienda: ${p.molienda}\n` +
      `• Presentación: ${pr ? pr.peso + 'g' : 'Estándar'}\n` +
      `• Cantidad: ${cant} unidad(es)\n` +
      `• Precio ref: S/ ${pr ? (pr.precio * cant).toFixed(2) : '-'}\n\n` +
      `¿Tienen disponibilidad para envío?`;

    const url = `https://wa.me/${APP_CONFIG.whatsappNumber}?text=${encodeURIComponent(texto)}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  }

  getGuiaMolienda(molienda?: string): string {
    if (!molienda) return 'Café de especialidad de origen Villa Rica, Oxapampa.';
    const m = molienda.toLowerCase();
    if (m.includes('grano')) {
      return 'Grano entero seleccionado para moler al instante en casa. Conserva intactos todos los aceites esenciales y notas aromáticas.';
    }
    if (m.includes('espresso') || m.includes('fino')) {
      return 'Molienda fina calibrada para alta presión: Cafetera Italiana (Moka), Aeropress y máquinas de Espresso profesionales.';
    }
    if (m.includes('grueso') || m.includes('cold')) {
      return 'Molienda gruesa uniforme especialmente diseñada para Prensa Francesa y extracción en frío prolongada (Cold Brew).';
    }
    return 'Molienda equilibrada ideal para métodos de filtrado manual: V60, Chemex, Cafetera de goteo eléctrica y tradicionales.';
  }

  // FICHA TÉCNICA DE CALIBRACIÓN EN MESA DE CATA (CERTIFICACIÓN SCA)
  fichaCata = computed(() => {
    const slug = this.product()?.slug || '';

    switch (slug) {
      case 'valle-blanco-amanecer':
        return {
          puntajeSca: 88.5,
          cuerpoDesc: 'Sedoso & Terso',
          cuerpoVal: 4.0,
          acidezDesc: 'Cítrica & Málica jugosa',
          acidezVal: 4.0,
          dulzorDesc: 'Panela & Caramelo Rubio',
          dulzorVal: 5.0,
          balanceDesc: 'Balance & Postgusto',
          balanceVal: 4.5,
          notaQGrader: 'En taza limpia resalta de inmediato el jazmín elegante típico de las mejores selecciones de altura, seguido de una sensación táctil que recuerda al almíbar de melocotón blanco.',
          etiquetaFloral: 'FLORAL (Jazmín)',
          radar: { floral: 4.8, frutal: 4.2, dulzor: 4.6, cuerpo: 4.0, acidez: 4.0 }
        };

      case 'valle-blanco-miel-montana':
      case 'valle-blanco-miel-de-montana':
        return {
          puntajeSca: 87.5,
          cuerpoDesc: 'Meloso & Envolvente',
          cuerpoVal: 4.3,
          acidezDesc: 'Fosfórica delicada',
          acidezVal: 3.8,
          dulzorDesc: 'Miel Pura & Melocotón',
          dulzorVal: 5.0,
          balanceDesc: 'Prolongado & Sedoso',
          balanceVal: 4.6,
          notaQGrader: 'El proceso honey conserva parte del mucílago durante el secado, otorgando un dulzor profundo y untuoso a miel de bosque, complementado con notas de almendra tostada y frutos amarillos.',
          etiquetaFloral: 'AROMA (Miel Floral)',
          radar: { floral: 3.8, frutal: 4.5, dulzor: 5.0, cuerpo: 4.3, acidez: 3.8 }
        };

      case 'valle-blanco-frutos-rojos':
        return {
          puntajeSca: 88.0,
          cuerpoDesc: 'Denso & Untuoso',
          cuerpoVal: 4.5,
          acidezDesc: 'Tartárica brillante',
          acidezVal: 4.2,
          dulzorDesc: 'Cereza Madura & Fresa',
          dulzorVal: 4.8,
          balanceDesc: 'Complejo & Vibrante',
          balanceVal: 4.7,
          notaQGrader: 'Secado natural en cereza entera. Ofrece una explosión jugosa de fresa silvestre y cereza confitada con una elegante terminación achocolatada de postgusto interminable.',
          etiquetaFloral: 'AROMA (Frutos Rojos)',
          radar: { floral: 4.0, frutal: 5.0, dulzor: 4.8, cuerpo: 4.5, acidez: 4.2 }
        };

      case 'valle-blanco-reserva-altura':
        return {
          puntajeSca: 89.0,
          cuerpoDesc: 'Aterciopelado & Elegante',
          cuerpoVal: 4.3,
          acidezDesc: 'Cítrica Naranja sanguina',
          acidezVal: 4.6,
          dulzorDesc: 'Toffee & Caramelo Florar',
          dulzorVal: 4.8,
          balanceDesc: 'Elegancia y Armonía',
          balanceVal: 4.8,
          notaQGrader: 'Cosechado a 1,900 msnm. Perfil superlativo con una acidez cítrica cristalina de naranja y jazmín, envuelta en un caramelo denso y delicada textura aterciopelada.',
          etiquetaFloral: 'FLORAL (Jazmín & Azahar)',
          radar: { floral: 4.7, frutal: 4.6, dulzor: 4.8, cuerpo: 4.3, acidez: 4.6 }
        };

      case 'valle-blanco-espresso':
        return {
          puntajeSca: 86.5,
          cuerpoDesc: 'Pleno, Cremoso & Pesado',
          cuerpoVal: 5.0,
          acidezDesc: 'Baja & Redondeada',
          acidezVal: 2.8,
          dulzorDesc: 'Panela & Cacao Oscuro',
          dulzorVal: 4.3,
          balanceDesc: 'Potente & Persistente',
          balanceVal: 4.5,
          notaQGrader: 'Calibrado para alta presión en espresso. Crema dorada consistente, cuerpo denso con intensas notas a chocolate amargo al 70%, avellana tostada y azúcar caramelizada.',
          etiquetaFloral: 'INTENSIDAD (Cacao)',
          radar: { floral: 2.8, frutal: 3.0, dulzor: 4.3, cuerpo: 5.0, acidez: 2.8 }
        };

      case 'valle-blanco-origen':
        return {
          puntajeSca: 87.0,
          cuerpoDesc: 'Sedoso & Balanceado',
          cuerpoVal: 4.0,
          acidezDesc: 'Cítrica Limpia',
          acidezVal: 4.2,
          dulzorDesc: 'Miel & Fruta Seca',
          dulzorVal: 4.5,
          balanceDesc: 'Limpio & Refrescante',
          balanceVal: 4.5,
          notaQGrader: 'Grano entero para moler en casa. Retiene una acidez cítrica fresca y viva de mandarina de Oxapampa, balanceada con una dulzura floral de miel silvestre.',
          etiquetaFloral: 'FLORAL (Silvestre)',
          radar: { floral: 4.4, frutal: 4.1, dulzor: 4.5, cuerpo: 4.0, acidez: 4.2 }
        };

      case 'valle-blanco-tradicion':
        return {
          puntajeSca: 86.0,
          cuerpoDesc: 'Medio-Alto Redondo',
          cuerpoVal: 4.2,
          acidezDesc: 'Suave & Agradable',
          acidezVal: 3.2,
          dulzorDesc: 'Azúcar Rubia & Avellana',
          dulzorVal: 4.6,
          balanceDesc: 'Familiar & Reconfortante',
          balanceVal: 4.4,
          notaQGrader: 'Inspirado en la memoria del café de hogar peruano. Cuerpo equilibrado con dulzor tostado a panela y notas reconfortantes a avellana y chocolate de taza.',
          etiquetaFloral: 'TOSTADO (Avellana)',
          radar: { floral: 3.0, frutal: 3.4, dulzor: 4.6, cuerpo: 4.2, acidez: 3.2 }
        };

      case 'valle-blanco-cold-brew':
        return {
          puntajeSca: 86.5,
          cuerpoDesc: 'Sedoso en Frío',
          cuerpoVal: 4.2,
          acidezDesc: 'Muy Baja & Amable',
          acidezVal: 2.2,
          dulzorDesc: 'Ciruela & Vainilla',
          dulzorVal: 4.8,
          balanceDesc: 'Refrescante & Dulce',
          balanceVal: 4.7,
          notaQGrader: 'Molienda gruesa para infusión fría de 18 a 24 horas. Resalta una dulzura natural licorosa a ciruela deshidratada, cacao fino y un toque sedoso de vainilla.',
          etiquetaFloral: 'DULZOR (Vainilla)',
          radar: { floral: 2.8, frutal: 4.0, dulzor: 4.8, cuerpo: 4.2, acidez: 2.2 }
        };

      default: {
        const prod = this.product();
        const notas = prod?.notasCata?.map(n => n.nombre) || [];
        const notasTexto = notas.length > 0 ? notas.join(', ') : 'caramelo, chocolate y frutas frescas';
        const tueste = prod?.tueste?.toLowerCase() || 'medio';
        const altitud = prod?.altitud || '1,650 msnm';
        const variedad = prod?.variedad || 'Arábica de Altura';

        // Autocalibración sensorial inteligente en base a tueste y notas
        let cuerpo = 4.0;
        let acidez = 4.0;
        let dulzor = 4.5;
        let floral = 3.8;
        let frutal = 4.0;

        if (tueste.includes('oscuro')) {
          cuerpo = 4.8;
          acidez = 2.8;
          dulzor = 4.3;
        } else if (tueste.includes('claro')) {
          cuerpo = 3.6;
          acidez = 4.6;
          floral = 4.7;
        }

        const notasLower = notas.map(n => n.toLowerCase());
        if (notasLower.some(n => /floral|jazm[ií]n|azahar|rosa/.test(n))) floral = 4.8;
        if (notasLower.some(n => /frut|c[ií]tric|naranja|fresa|cereza|mandarina|durazno/.test(n))) {
          frutal = 4.7;
          acidez = Math.min(5.0, +(acidez + 0.3).toFixed(1));
        }
        if (notasLower.some(n => /miel|panela|caramelo|az[uú]car|vainilla/.test(n))) {
          dulzor = 4.9;
        }
        if (notasLower.some(n => /cacao|chocolate|nuez|avellana/.test(n))) {
          cuerpo = Math.min(5.0, +(cuerpo + 0.3).toFixed(1));
        }

        // Puntaje SCA dinámico (86.0 - 89.0)
        let sca = 86.5;
        if (altitud.includes('1,8') || altitud.includes('1,9')) sca += 1.5;
        if (variedad.toLowerCase().includes('geisha') || variedad.toLowerCase().includes('bourbon')) sca += 1.0;
        sca = Math.min(90.0, Math.max(85.0, sca));

        return {
          puntajeSca: sca,
          cuerpoDesc: cuerpo >= 4.5 ? 'Denso & Untuoso' : (cuerpo >= 4.0 ? 'Sedoso & Terso' : 'Ligero & Delicado'),
          cuerpoVal: cuerpo,
          acidezDesc: acidez >= 4.2 ? 'Cítrica Brillante & Jugosa' : (acidez >= 3.5 ? 'Equilibrada & Amable' : 'Baja & Redondeada'),
          acidezVal: acidez,
          dulzorDesc: dulzor >= 4.7 ? 'Intenso & Meloso' : 'Panela & Dulce Natural',
          dulzorVal: dulzor,
          balanceDesc: 'Elegante & Prolongado',
          balanceVal: +((cuerpo + acidez + dulzor) / 3).toFixed(1),
          notaQGrader: `En mesa de cata destaca por su pureza de taza cosechada a ${altitud}, revelando un balance notable con notas claras a ${notasTexto} y una textura persistente.`,
          etiquetaFloral: floral >= 4.5 ? 'FLORAL (Jazmín)' : (frutal >= 4.5 ? 'FRUTAL (Intenso)' : 'AROMA (Especialidad)'),
          radar: {
            floral: Math.min(5.0, floral),
            frutal: Math.min(5.0, frutal),
            dulzor: Math.min(5.0, dulzor),
            cuerpo: Math.min(5.0, cuerpo),
            acidez: Math.min(5.0, acidez)
          }
        };
      }
    }
  });

  // Coordenadas trigonométricas para el polígono del radar pentagonal (centro: 160, 130, maxR: 85)
  get radarPolygonPoints(): string {
    const r = this.fichaCata().radar;
    const vals = [r.floral / 5, r.frutal / 5, r.dulzor / 5, r.cuerpo / 5, r.acidez / 5];
    const cx = 160, cy = 130, maxR = 85;
    const angles = [
      -Math.PI / 2,
      -Math.PI / 2 + (2 * Math.PI / 5),
      -Math.PI / 2 + (4 * Math.PI / 5),
      -Math.PI / 2 + (6 * Math.PI / 5),
      -Math.PI / 2 + (8 * Math.PI / 5)
    ];
    return vals.map((v, i) => {
      const x = cx + v * maxR * Math.cos(angles[i]);
      const y = cy + v * maxR * Math.sin(angles[i]);
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    }).join(' ');
  }

  get radarVertices(): { x: number; y: number }[] {
    const r = this.fichaCata().radar;
    const vals = [r.floral / 5, r.frutal / 5, r.dulzor / 5, r.cuerpo / 5, r.acidez / 5];
    const cx = 160, cy = 130, maxR = 85;
    const angles = [
      -Math.PI / 2,
      -Math.PI / 2 + (2 * Math.PI / 5),
      -Math.PI / 2 + (4 * Math.PI / 5),
      -Math.PI / 2 + (6 * Math.PI / 5),
      -Math.PI / 2 + (8 * Math.PI / 5)
    ];
    return vals.map((v, i) => ({
      x: +(cx + v * maxR * Math.cos(angles[i])).toFixed(1),
      y: +(cy + v * maxR * Math.sin(angles[i])).toFixed(1)
    }));
  }

  get radarOuterVertices(): { x: number; y: number }[] {
    const cx = 160, cy = 130, maxR = 85;
    const angles = [
      -Math.PI / 2,
      -Math.PI / 2 + (2 * Math.PI / 5),
      -Math.PI / 2 + (4 * Math.PI / 5),
      -Math.PI / 2 + (6 * Math.PI / 5),
      -Math.PI / 2 + (8 * Math.PI / 5)
    ];
    return angles.map(a => ({
      x: +(cx + maxR * Math.cos(a)).toFixed(1),
      y: +(cy + maxR * Math.sin(a)).toFixed(1)
    }));
  }
}
