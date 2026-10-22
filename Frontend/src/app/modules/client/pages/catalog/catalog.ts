import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from '../../../../core/models/product.interface';
import { ProductService } from '../../../../core/services/product.service';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  selector: 'app-catalog',
  templateUrl: './catalog.html',
})
export class Catalog implements OnInit {
  private readonly productService = inject(ProductService);

  // Lista completa de productos
  listadeproductos: Product[] = [];

  // Filtros activos
  filtroMolienda: string = 'todos';
  filtroTueste: string = 'todos';
  filtroVariedad: string = 'todos';
  filtroNota: string = 'todos';
  filtroTipoCafe: string = 'todos';
  ordenSeleccionado: string = 'destacados';

  // 10 presentaciones fallback (100% origen Oxapampa, Perú) si el backend no está corriendo
  fallbackProductos: Product[] = [
    {
      id: 1,
      slug: "valle-blanco-clasico",
      nombre: "Valle Blanco",
      nombreSecundario: "Clásico",
      tipoCafe: "Café Molido",
      molienda: "Molido medio",
      tueste: "Medio",
      variedad: "Caturra / Typica",
      altitud: "1,600 msnm",
      descripcion: "Molido artesanalmente para filtrado, prensa francesa y métodos de goteo. Listo para preparar en casa con la frescura y el aroma del grano recién tostado.",
      imagenUrl: "assets/images/producto-molido.jpg",
      stock: 50,
      badgeText: "MÁS POPULAR",
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 1, nombre: "Chocolate" },
        { id: 2, nombre: "Durazno" },
        { id: 3, nombre: "Caramelo" }
      ],
      precios: [
        { id: 1, id_product: 1, peso: 250, precio: 35.00 },
        { id: 2, id_product: 1, peso: 500, precio: 65.00 }
      ]
    },
    {
      id: 2,
      slug: "valle-blanco-origen",
      nombre: "Valle Blanco",
      nombreSecundario: "Origen",
      tipoCafe: "Café en Grano",
      molienda: "En Grano",
      tueste: "Medio claro",
      variedad: "Typica",
      altitud: "1,800 msnm",
      descripcion: "Para los que prefieren controlar cada detalle. Granos enteros tostados artesanalmente, listos para moler al momento y aprovechar al máximo el perfil aromático.",
      imagenUrl: "assets/images/producto-grano.jpg",
      stock: 40,
      badgeText: "ORIGEN",
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 4, nombre: "Floral" },
        { id: 5, nombre: "Cítrico" },
        { id: 6, nombre: "Miel" }
      ],
      precios: [
        { id: 3, id_product: 2, peso: 340, precio: 42.00 },
        { id: 4, id_product: 2, peso: 1000, precio: 120.00 }
      ]
    },
    {
      id: 3,
      slug: "valle-blanco-espresso",
      nombre: "Valle Blanco",
      nombreSecundario: "Espresso",
      tipoCafe: "Espresso",
      molienda: "Espresso",
      tueste: "Oscuro",
      variedad: "Caturra",
      altitud: "1,400 msnm",
      descripcion: "Tueste oscuro de precisión para extracciones intensas. Cuerpo pleno, crema dorada y un aroma profundo que permanece en cada taza de espresso o americano.",
      imagenUrl: "assets/images/producto-espresso.jpg",
      stock: 35,
      badgeText: "INTENSO",
      esDestacado: true,
      esOscuro: true,
      notasCata: [
        { id: 7, nombre: "Cacao oscuro" },
        { id: 8, nombre: "Nuez tostada" },
        { id: 9, nombre: "Panela" }
      ],
      precios: [
        { id: 5, id_product: 3, peso: 250, precio: 38.00 },
        { id: 6, id_product: 3, peso: 500, precio: 70.00 }
      ]
    },
    {
      id: 4,
      slug: "valle-blanco-reserva-oxapampa",
      nombre: "Valle Blanco",
      nombreSecundario: "Reserva Oxapampa",
      tipoCafe: "Café Molido",
      molienda: "Molido fino",
      tueste: "Medio",
      variedad: "Bourbon / Typica",
      altitud: "1,750 msnm",
      descripcion: "Molienda calibrada para cafetera italiana Moka y Aeropress. Perfil dulce con notas acarameladas y acidez suave cultivado en las laderas de Villa Rica.",
      imagenUrl: "assets/images/producto-grano.jpg",
      stock: 28,
      badgeText: "RESERVA",
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 3, nombre: "Caramelo" },
        { id: 9, nombre: "Panela" },
        { id: 10, nombre: "Almendra" }
      ],
      precios: [
        { id: 7, id_product: 4, peso: 250, precio: 38.00 }
      ]
    },
    {
      id: 5,
      slug: "valle-blanco-altura-geisha",
      nombre: "Valle Blanco",
      nombreSecundario: "Altura Geisha",
      tipoCafe: "Especialidad",
      molienda: "En Grano",
      tueste: "Claro",
      variedad: "Geisha",
      altitud: "1,850 msnm",
      descripcion: "Micro-lote exclusivo de gran altitud en Oxapampa. Notas florales a jazmín y frutas tropicales con cuerpo sedoso y elegante.",
      imagenUrl: "assets/images/producto-molido.jpg",
      stock: 15,
      badgeText: "EDICIÓN LIMITADA",
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 11, nombre: "Jazmín" },
        { id: 5, nombre: "Cítrico" },
        { id: 2, nombre: "Durazno" }
      ],
      precios: [
        { id: 8, id_product: 5, peso: 250, precio: 55.00 }
      ]
    },
    {
      id: 6,
      slug: "valle-blanco-prensa-francesa",
      nombre: "Valle Blanco",
      nombreSecundario: "Prensa & Cold Brew",
      tipoCafe: "Café Molido",
      molienda: "Molido grueso",
      tueste: "Medio",
      variedad: "Caturra",
      altitud: "1,550 msnm",
      descripcion: "Molienda gruesa uniforme ideal para inmersión prolongada, prensa francesa o cold brew artesanal. Extrae dulzura sin amargor.",
      imagenUrl: "assets/images/producto-molido.jpg",
      stock: 30,
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 1, nombre: "Chocolate" },
        { id: 12, nombre: "Vainilla" },
        { id: 8, nombre: "Nuez tostada" }
      ],
      precios: [
        { id: 9, id_product: 6, peso: 250, precio: 36.00 },
        { id: 10, id_product: 6, peso: 500, precio: 68.00 }
      ]
    },
    {
      id: 7,
      slug: "valle-blanco-tostado-intenso",
      nombre: "Valle Blanco",
      nombreSecundario: "Intenso Dark",
      tipoCafe: "Espresso",
      molienda: "Espresso",
      tueste: "Oscuro",
      variedad: "Pache / Caturra",
      altitud: "1,450 msnm",
      descripcion: "Café para paladares que adoran notas de chocolate amargo pronunciadas y un postgusto prolongado, perfecto con leche o capuchino.",
      imagenUrl: "assets/images/producto-espresso.jpg",
      stock: 25,
      badgeText: "DARK ROAST",
      esDestacado: false,
      esOscuro: true,
      notasCata: [
        { id: 7, nombre: "Cacao oscuro" },
        { id: 1, nombre: "Chocolate" },
        { id: 13, nombre: "Canela" }
      ],
      precios: [
        { id: 11, id_product: 7, peso: 250, precio: 36.00 },
        { id: 12, id_product: 7, peso: 1000, precio: 110.00 }
      ]
    },
    {
      id: 8,
      slug: "valle-blanco-miel-honey",
      nombre: "Valle Blanco",
      nombreSecundario: "Honey Process",
      tipoCafe: "Especialidad",
      molienda: "En Grano",
      tueste: "Medio claro",
      variedad: "Catimor / Typica",
      altitud: "1,680 msnm",
      descripcion: "Proceso Yellow Honey secado al sol en Oxapampa conservando el mucílago dulce. Desarrolla una taza acaramelada y frutos rojos naturales.",
      imagenUrl: "assets/images/producto-grano.jpg",
      stock: 20,
      badgeText: "HONEY PROCESS",
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 6, nombre: "Miel" },
        { id: 14, nombre: "Frutos rojos" },
        { id: 9, nombre: "Panela" }
      ],
      precios: [
        { id: 13, id_product: 8, peso: 340, precio: 46.00 }
      ]
    },
    {
      id: 9,
      slug: "valle-blanco-filtro-v60",
      nombre: "Valle Blanco",
      nombreSecundario: "Filtro Pour-Over",
      tipoCafe: "Café Molido",
      molienda: "Molido fino",
      tueste: "Medio claro",
      variedad: "Typica",
      altitud: "1,700 msnm",
      descripcion: "Calibración exacta para conos V60 y Chemex. Proporciona una taza limpia y brillante donde destacan sutiles notas a jazmín y durazno.",
      imagenUrl: "assets/images/producto-molido.jpg",
      stock: 32,
      esDestacado: false,
      esOscuro: false,
      notasCata: [
        { id: 2, nombre: "Durazno" },
        { id: 5, nombre: "Cítrico" },
        { id: 11, nombre: "Jazmín" }
      ],
      precios: [
        { id: 14, id_product: 9, peso: 250, precio: 38.00 }
      ]
    },
    {
      id: 10,
      slug: "valle-blanco-seleccion-familiar",
      nombre: "Valle Blanco",
      nombreSecundario: "Selección Familiar",
      tipoCafe: "Café en Grano",
      molienda: "En Grano",
      tueste: "Medio",
      variedad: "Caturra / Typica",
      altitud: "1,650 msnm",
      descripcion: "El café insignia de la familia Valle Blanco en Villa Rica. Un balance redondo entre dulzor achocolatado, notas a nuez y acidez cítrica moderada.",
      imagenUrl: "assets/images/producto-grano.jpg",
      stock: 45,
      badgeText: "INSIGNIA",
      esDestacado: true,
      esOscuro: false,
      notasCata: [
        { id: 1, nombre: "Chocolate" },
        { id: 8, nombre: "Nuez tostada" },
        { id: 6, nombre: "Miel" }
      ],
      precios: [
        { id: 15, id_product: 10, peso: 340, precio: 40.00 },
        { id: 16, id_product: 10, peso: 1000, precio: 115.00 }
      ]
    }
  ];

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (products) => {
        if (products && products.length > 0) {
          this.listadeproductos = products;
        } else {
          this.listadeproductos = this.fallbackProductos;
        }
      },
      error: (err) => {
        console.warn('Backend no disponible, usando presentaciones locales de Valle Blanco:', err);
        this.listadeproductos = this.fallbackProductos;
      }
    });
  }

  // Getter con todos los filtros combinados
  get productosFiltrados(): Product[] {
    return this.listadeproductos.filter(p => {
      // Filtro por molienda
      if (this.filtroMolienda !== 'todos' && p.molienda?.toLowerCase() !== this.filtroMolienda.toLowerCase()) {
        return false;
      }
      // Filtro por tueste
      if (this.filtroTueste !== 'todos' && !p.tueste?.toLowerCase().includes(this.filtroTueste.toLowerCase())) {
        return false;
      }
      // Filtro por variedad
      if (this.filtroVariedad !== 'todos' && !p.variedad?.toLowerCase().includes(this.filtroVariedad.toLowerCase())) {
        return false;
      }
      // Filtro por tipo de café
      if (this.filtroTipoCafe !== 'todos' && p.tipoCafe?.toLowerCase() !== this.filtroTipoCafe.toLowerCase()) {
        return false;
      }
      // Filtro por notas de sabor
      if (this.filtroNota !== 'todos') {
        const tieneNota = p.notasCata?.some(n => n.nombre.toLowerCase().includes(this.filtroNota.toLowerCase()));
        if (!tieneNota) return false;
      }
      return true;
    }).sort((a, b) => {
      if (this.ordenSeleccionado === 'precio-asc') {
        return this.getPrecioMinimo(a) - this.getPrecioMinimo(b);
      }
      if (this.ordenSeleccionado === 'precio-desc') {
        return this.getPrecioMinimo(b) - this.getPrecioMinimo(a);
      }
      // Por defecto: destacados primero
      if (a.esDestacado && !b.esDestacado) return -1;
      if (!a.esDestacado && b.esDestacado) return 1;
      return (a.id || 0) - (b.id || 0);
    });
  }

  limpiarFiltros(): void {
    this.filtroMolienda = 'todos';
    this.filtroTueste = 'todos';
    this.filtroVariedad = 'todos';
    this.filtroNota = 'todos';
    this.filtroTipoCafe = 'todos';
    this.ordenSeleccionado = 'destacados';
  }

  getPrecioMinimo(producto: Product): number {
    if (!producto.precios || producto.precios.length === 0) return 35.00;
    return Math.min(...producto.precios.map(p => p.precio));
  }

  getPesoPrincipal(producto: Product): string {
    if (!producto.precios || producto.precios.length === 0) return '250 g';
    return `${producto.precios[0].peso} g`;
  }

  consultarPedido(producto: Product): void {
    const texto = `Hola Valle Blanco, deseo consultar disponibilidad y precio para el café: ${producto.nombre} ${producto.nombreSecundario || ''} (${producto.molienda}).`;
    const url = `https://wa.me/51999999999?text=${encodeURIComponent(texto)}`;
    window.open(url, '_blank');
  }
}
