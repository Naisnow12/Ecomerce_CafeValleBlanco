
import {
  Component,
  signal,
  computed,
  inject,
  OnInit
} from '@angular/core';

import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../../../core/services/admin-service';

interface ClienteAdmin {
  id: number;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  activo: boolean;
  creadoEn: string;
  roles?: string[];
  totalPedidos?: number;
  totalGastado?: number;
}

@Component({
  selector: 'app-clientes-admin',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
    DecimalPipe
  ],
  templateUrl: './clientes-admin.html',
  styleUrl: './clientes-admin.scss',
})
export class ClientesAdmin implements OnInit {
  private readonly adminService = inject(AdminService);

  // Clientes / Usuarios registrados desde backend
  clientes = signal<ClienteAdmin[]>([]);
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
  totalClientes = computed(() => this.clientes().length);
  clientesActivos = computed(() => this.clientes().filter(c => c.activo).length);
  clientesInactivos = computed(() => this.clientes().filter(c => !c.activo).length);

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.cargando.set(true);
    this.error.set('');
    this.adminService.getUsuarios().subscribe({
      next: (usuarios: any[]) => {
        this.clientes.set(usuarios.map(u => ({
          id: u.id,
          nombres: u.nombres,
          apellidos: u.apellidos,
          email: u.email,
          telefono: u.telefono,
          activo: u.activo,
          creadoEn: u.creadoEn,
          roles: u.roles,
          totalPedidos: Number(u.totalPedidos ?? 0),
          totalGastado: Number(u.totalGastado ?? 0)
        })));
        this.datosDisponibles.set(true);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('No se pudo cargar la lista de usuarios desde el servidor.');
        this.cargando.set(false);
      }
    });
  }

  toggleEstado(cliente: ClienteAdmin): void {
    const nuevoEstado = !cliente.activo;
    this.adminService.cambiarEstadoUsuario(cliente.id, nuevoEstado).subscribe({
      next: () => {
        this.cargarClientes();
      },
      error: (err) => {
        alert(err.error?.message || 'Error al cambiar estado del usuario');
      }
    });
  }

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
