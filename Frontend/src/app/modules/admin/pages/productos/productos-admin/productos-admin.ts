import {
  Component,
  inject,
  OnInit,
  signal,
  computed
} from '@angular/core';

import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ProductService } from '../../../../../core/services/product.service';
import { Product } from '../../../../../core/models/product.interface';

type FiltroStock = 'TODOS' | 'DISPONIBLE' | 'BAJO' | 'AGOTADO';

@Component({
  selector: 'app-productos-admin',
  imports: [FormsModule, DecimalPipe, RouterLink],
  templateUrl: './productos-admin.html',
  styleUrl: './productos-admin.scss',
})
export class ProductosAdmin implements OnInit {

  private readonly productService = inject(ProductService);

  // Productos
  productos = signal<Product[]>([]);

  // Estados
  cargando = signal(true);
  error = signal('');

  // Filtros
  busqueda = signal('');
  filtroStock = signal<FiltroStock>('TODOS');

  // Formulario
  mostrarFormulario = signal(false);
  modoEdicion = signal(false);

  productoSeleccionado = signal<Product | null>(null);

  // Se habilitará cuando existan los endpoints administrativos.
  escrituraDisponible = signal(false);

  // Estadísticas
  totalProductos = computed(
    () => this.productos().length
  );

  productosDisponibles = computed(
    () => this.productos().filter(p => p.stock > 0).length
  );

  productosStockBajo = computed(
    () => this.productos().filter(
      p => p.stock > 0 && p.stock < 5
    ).length
  );

  productosAgotados = computed(
    () => this.productos().filter(p => p.stock === 0).length
  );

  productosFiltrados = computed(() => {
    const texto = this.busqueda().toLowerCase().trim();
    const filtro = this.filtroStock();

    return this.productos().filter(producto => {

      const coincideTexto =
        !texto ||
        producto.nombre.toLowerCase().includes(texto) ||
        (producto.nombreSecundario ?? '')
          .toLowerCase().includes(texto) ||
        producto.slug.toLowerCase().includes(texto);

      let coincideStock = true;

      switch (filtro) {
        case 'DISPONIBLE':
          coincideStock = producto.stock > 0;
          break;

        case 'BAJO':
          coincideStock =
            producto.stock > 0 && producto.stock < 5;
          break;

        case 'AGOTADO':
          coincideStock = producto.stock === 0;
          break;
      }

      return coincideTexto && coincideStock;
    });
  });

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando.set(true);
    this.error.set('');

    this.productService.getProducts().subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.cargando.set(false);
      },

      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);

        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el backend.'
            : 'No se pudieron cargar los productos.'
        );
      }
    });
  }

  cambiarBusqueda(texto: string): void {
    this.busqueda.set(texto);
  }

  cambiarFiltroStock(filtro: FiltroStock): void {
    this.filtroStock.set(filtro);
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.filtroStock.set('TODOS');
  }

  obtenerEstadoStock(stock: number): string {
    if (stock <= 0) {
      return 'Agotado';
    }

    if (stock < 5) {
      return 'Stock bajo';
    }

    return 'Disponible';
  }

  obtenerPrecioMinimo(producto: Product): number | null {
    if (!producto.precios?.length) {
      return null;
    }

    const valores = producto.precios.map(p => p.precio);

    return Math.min(...valores);
  }

  abrirNuevoProducto(): void {
    this.modoEdicion.set(false);

    this.productoSeleccionado.set({
      slug: '',
      imagenUrl: '',
      nombre: '',
      nombreSecundario: '',
      tipoCafe: '',
      molienda: '',
      tueste: '',
      variedad: '',
      altitud: '',
      descripcion: '',
      stock: 0,
      esDestacado: false,
      esOscuro: false,
      badgeText: '',
      notasCata: [],
      precios: []
    });

    this.mostrarFormulario.set(true);
  }

  editarProducto(producto: Product): void {
    this.modoEdicion.set(true);

    this.productoSeleccionado.set({
      ...producto,
      notasCata: [...producto.notasCata],
      precios: [...producto.precios]
    });

    this.mostrarFormulario.set(true);
  }

  cerrarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.productoSeleccionado.set(null);
  }

  // Solo cierra el formulario.
  // El guardado estará disponible cuando exista la API administrativa.
  cancelarEdicion(): void {
    this.cerrarFormulario();
  }
}
