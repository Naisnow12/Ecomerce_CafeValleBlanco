
import {
  Component,
  signal,
  computed
} from '@angular/core';

import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ClienteAdmin {
  id: number;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  activo: boolean;
  creadoEn: string;
  totalPedidos?: number;
  totalGastado?: number;
}

@Component({
  selector: 'app-clientes-admin',
  imports: [
    FormsModule,
    DatePipe
  ],
  templateUrl: './clientes-admin.html',
  styleUrl: './clientes-admin.scss',
})
export class ClientesAdmin {

  // Clientes registrados.
  // Se cargarán desde el backend administrativo.
  clientes = signal<ClienteAdmin[]>([]);

  // Conexión pendiente
  datosDisponibles = signal(false);

  // Estados
  cargando = signal(false);
  error = signal('');

  // Filtros
  busqueda = signal('');
  filtroEstado = signal<string>('TODOS');

  // Detalle del cliente
  clienteSeleccionado = signal<ClienteAdmin | null>(null);
  mostrarDetalle = signal(false);

  // Estadísticas
  totalClientes = computed(
    () => this.clientes().length
  );

  clientesActivos = computed(
    () => this.clientes().filter(c => c.activo).length
  );

  clientesInactivos = computed(
    () => this.clientes().filter(c => !c.activo).length
  );

  // Listado filtrado
  clientesFiltrados = computed(() => {

    const texto = this.busqueda().toLowerCase().trim();
    const filtro = this.filtroEstado();

    return this.clientes().filter(cliente => {

      const nombreCompleto =
        `${cliente.nombres} ${cliente.apellidos}`
          .toLowerCase();

      const coincideBusqueda =
        !texto ||
        nombreCompleto.includes(texto) ||
        cliente.email.toLowerCase().includes(texto) ||
        (cliente.telefono ?? '').includes(texto);

      let coincideEstado = true;

      if (filtro === 'ACTIVOS') {
        coincideEstado = cliente.activo;
      }

      if (filtro === 'INACTIVOS') {
        coincideEstado = !cliente.activo;
      }

      return coincideBusqueda && coincideEstado;
    });

  });

  cambiarBusqueda(texto: string): void {
    this.busqueda.set(texto);
  }

  cambiarFiltroEstado(estado: string): void {
    this.filtroEstado.set(estado);
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.filtroEstado.set('TODOS');
  }

  verDetalle(cliente: ClienteAdmin): void {
    this.clienteSeleccionado.set(cliente);
    this.mostrarDetalle.set(true);
  }

  cerrarDetalle(): void {
    this.mostrarDetalle.set(false);
    this.clienteSeleccionado.set(null);
  }

  obtenerNombreCompleto(cliente: ClienteAdmin): string {
    return `${cliente.nombres} ${cliente.apellidos}`;
  }

  obtenerIniciales(cliente: ClienteAdmin): string {
    const primera = cliente.nombres.charAt(0);
    const segunda = cliente.apellidos.charAt(0);

    return `${primera}${segunda}`.toUpperCase();
  }

  obtenerEstadoTexto(activo: boolean): string {
    return activo ? 'Activo' : 'Inactivo';
  }
}
