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

import { ProductService } from '../../../../../core/services/product-service';
import { Product } from '../../../../../core/models/product.interface';
import { precios } from '../../../../../core/models/precios.interface';

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
  guardando = signal(false);
  errorGuardado = signal('');

  // Filtros
  busqueda = signal('');
  filtroStock = signal<FiltroStock>('TODOS');

  // Formulario
  mostrarFormulario = signal(false);
  modoEdicion = signal(false);

  productoSeleccionado = signal<Product | null>(null);

  // Endpoints administrativos conectados
  escrituraDisponible = signal(true);

  // Subida de imagen
  subiendoImagen = signal(false);
  previewImagen = signal<string | null>(null);

  // Opciones para barras desplegables
  readonly opcionesTipoCafe = [
    'Café Molido',
    'Café en Grano',
    'Espresso',
    'Proceso Honey',
    'Proceso Natural',
    'Edición Especial',
    'Extracción en Frío'
  ];

  readonly opcionesMolienda = [
    'En Grano',
    'Molido fino',
    'Molido medio',
    'Molido grueso',
    'Molido tradicional',
    'Espresso'
  ];

  readonly opcionesTueste = [
    'Claro',
    'Medio claro',
    'Medio',
    'Medio oscuro',
    'Oscuro'
  ];

  readonly opcionesVariedad = [
    'Typica',
    'Caturra',
    'Caturra / Typica',
    'Typica / Bourbon',
    'Bourbon',
    'Geisha'
  ];

  // Nuevas presentaciones en el modal
  nuevoPeso = signal<number>(250);
  nuevoPrecio = signal<number>(35.00);

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
    this.errorGuardado.set('');
    this.previewImagen.set(null);

    this.productoSeleccionado.set({
      slug: '',
      imagenUrl: '',
      nombre: '',
      nombreSecundario: '',
      tipoCafe: 'Café Molido',
      molienda: 'Molido medio',
      tueste: 'Medio',
      variedad: 'Caturra / Typica',
      altitud: '1,650 msnm',
      descripcion: '',
      stock: 50,
      esDestacado: false,
      esOscuro: false,
      badgeText: '',
      notasCata: [],
      precios: [
        { id_product: 0, peso: 250, precio: 35.00 }
      ]
    });

    this.mostrarFormulario.set(true);
  }

  editarProducto(producto: Product): void {
    this.modoEdicion.set(true);
    this.errorGuardado.set('');
    this.previewImagen.set(producto.imagenUrl || null);

    this.productoSeleccionado.set({
      ...producto,
      notasCata: producto.notasCata ? [...producto.notasCata] : [],
      precios: producto.precios ? [...producto.precios] : []
    });

    this.mostrarFormulario.set(true);
  }

  cerrarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.productoSeleccionado.set(null);
    this.previewImagen.set(null);
    this.errorGuardado.set('');
  }

  cancelarEdicion(): void {
    this.cerrarFormulario();
  }

  // SUBIDA DE ARCHIVO DE IMAGEN
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    // Previsualización inmediata en navegador
    const reader = new FileReader();
    reader.onload = () => {
      this.previewImagen.set(reader.result as string);
    };
    reader.readAsDataURL(file);

    // Subida al servidor
    this.subiendoImagen.set(true);
    this.errorGuardado.set('');

    this.productService.uploadImage(file).subscribe({
      next: (res) => {
        this.subiendoImagen.set(false);
        const p = this.productoSeleccionado();
        if (p) {
          p.imagenUrl = res.imageUrl;
          this.productoSeleccionado.set({ ...p });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.subiendoImagen.set(false);
        this.errorGuardado.set(err.error?.error || 'Error al subir la imagen al servidor. Verifica el formato.');
      }
    });
  }

  // GESTIÓN DE PRESENTACIONES Y PRECIOS
  agregarPrecio(): void {
    const p = this.productoSeleccionado();
    if (!p) return;

    const peso = this.nuevoPeso();
    const precio = this.nuevoPrecio();

    if (peso <= 0 || precio <= 0) {
      this.errorGuardado.set('El peso y el precio deben ser mayores a cero.');
      return;
    }

    const nuevoItem: precios = {
      id_product: p.id || 0,
      peso,
      precio
    };
    p.precios = [...(p.precios || []), nuevoItem];
    this.productoSeleccionado.set({ ...p });
    this.errorGuardado.set('');
  }

  eliminarPrecio(index: number): void {
    const p = this.productoSeleccionado();
    if (!p || !p.precios) return;

    p.precios.splice(index, 1);
    this.productoSeleccionado.set({ ...p });
  }

  // GUARDAR PRODUCTO (CREAR O ACTUALIZAR)
  guardarProducto(): void {
    const prod = this.productoSeleccionado();
    if (!prod) return;

    if (!prod.nombre || prod.nombre.trim() === '') {
      this.errorGuardado.set('El nombre del café es obligatorio.');
      return;
    }

    if (!prod.precios || prod.precios.length === 0) {
      this.errorGuardado.set('Debes registrar al menos una presentación con su precio.');
      return;
    }

    this.guardando.set(true);
    this.errorGuardado.set('');

    const peticion = this.modoEdicion() && prod.id
      ? this.productService.updateProduct(prod.id, prod)
      : this.productService.createProduct(prod);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarFormulario();
        this.cargarProductos();
      },
      error: (err: HttpErrorResponse) => {
        this.guardando.set(false);
        this.errorGuardado.set(err.error?.message || 'Error al guardar el producto en el servidor.');
      }
    });
  }

  // ELIMINAR PRODUCTO
  eliminarProducto(producto: Product): void {
    if (!producto.id) return;

    const confirmacion = window.confirm(
      `¿Estás seguro de que deseas eliminar permanentemente la presentación "${producto.nombre} ${producto.nombreSecundario || ''}"?`
    );

    if (!confirmacion) return;

    this.cargando.set(true);
    this.productService.deleteProduct(producto.id).subscribe({
      next: () => {
        this.cargarProductos();
      },
      error: (err) => {
        this.cargando.set(false);
        alert(err.error?.message || 'No se pudo eliminar el producto.');
      }
    });
  }
}
