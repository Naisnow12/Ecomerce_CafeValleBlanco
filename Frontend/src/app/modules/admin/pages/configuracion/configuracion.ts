
import {
  Component,
  signal,
  inject,
  OnInit
} from '@angular/core';

import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { AdminService } from '../../../../core/services/admin.service';

interface ConfiguracionTienda {
  nombre: string;
  subtitulo: string;
  descripcion: string;
  email: string;
  telefono: string;
  whatsapp: string;
  direccion: string;
  distrito: string;
  provincia: string;
  departamento: string;
}

interface ConfiguracionPagos {
  yape: boolean;
  plin: boolean;
  transferencia: boolean;
  numeroYape: string;
  numeroPlin: string;
  banco: string;
  numeroCuenta: string;
  cci: string;
}

interface ConfiguracionEnvios {
  habilitarEnvios: boolean;
  costoBase: number;
  envioGratisDesde: number;
  tiempoEstimado: string;
  instrucciones: string;
}

@Component({
  selector: 'app-configuracion',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    DatePipe
  ],
  templateUrl: './configuracion.html',
  styleUrl: './configuracion.scss',
})
export class Configuracion implements OnInit {
  private readonly adminService = inject(AdminService);

  guardadoDisponible = signal(true);
  seccionActiva = signal<string>('TIENDA');

  guardando = signal(false);
  error = signal('');
  mensaje = signal('');

  auditorias = signal<any[]>([]);

  tienda: ConfiguracionTienda = {
    nombre: 'Valle Blanco',
    subtitulo: 'Café de Especialidad',
    descripcion: 'Cafetería de especialidad con granos selectos de Oxapampa y Villa Rica.',
    email: 'contacto@valleblanco.pe',
    telefono: '+51 999 888 777',
    whatsapp: '51999999999',
    direccion: 'Av. San Martín 450',
    distrito: 'Oxapampa',
    provincia: 'Oxapampa',
    departamento: 'Pasco'
  };

  pagos: ConfiguracionPagos = {
    yape: true,
    plin: true,
    transferencia: true,
    numeroYape: '999888777',
    numeroPlin: '999888777',
    banco: 'BCP',
    numeroCuenta: '191-12345678-0-99',
    cci: '002-191-001234567899-55'
  };

  envios: ConfiguracionEnvios = {
    habilitarEnvios: true,
    costoBase: 15.00,
    envioGratisDesde: 150.00,
    tiempoEstimado: '24 a 48 horas en Lima, 3 a 5 días provincias',
    instrucciones: 'Empaque especial hermético para conservar aromas de tueste.'
  };

  ngOnInit(): void {
    this.cargarAuditoria();
  }

  cargarAuditoria(): void {
    this.adminService.getAuditoria().subscribe({
      next: (data) => {
        this.auditorias.set(data);
      },
      error: () => {}
    });
  }

  guardarCambios(): void {
    this.guardando.set(true);
    setTimeout(() => {
      this.guardando.set(false);
      this.mensaje.set('Preferencias guardadas satisfactoriamente.');
      setTimeout(() => this.mensaje.set(''), 4000);
    }, 600);
  }

  cambiarSeccion(seccion: string): void {
    this.seccionActiva.set(seccion);
    this.error.set('');
    this.mensaje.set('');
    if (seccion === 'SEGURIDAD') {
      this.cargarAuditoria();
    }
  }

  obtenerNombreSeccion(): string {
    const secciones: Record<string, string> = {
      TIENDA: 'Información de la tienda',
      PAGOS: 'Métodos de pago',
      ENVIOS: 'Configuración de envíos',
      SEGURIDAD: 'Seguridad y auditoría'
    };

    return secciones[this.seccionActiva()] ?? 'Configuración';
  }
}
