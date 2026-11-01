
import {
  Component,
  signal
} from '@angular/core';

import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

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
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './configuracion.html',
  styleUrl: './configuracion.scss',
})
export class Configuracion {

  // Se habilitará cuando exista el backend de configuración.
  guardadoDisponible = signal(false);

  // Secciones
  seccionActiva = signal<string>('TIENDA');

  // Estados
  guardando = signal(false);
  error = signal('');
  mensaje = signal('');

  // Datos iniciales conocidos.
  // Los demás campos se completarán con información real.
  tienda: ConfiguracionTienda = {
    nombre: 'Valle Blanco',
    subtitulo: 'Café de Especialidad',
    descripcion: '',
    email: '',
    telefono: '',
    whatsapp: '',
    direccion: '',
    distrito: '',
    provincia: 'Oxapampa',
    departamento: 'Pasco'
  };

  // Configuración de pagos.
  // Estos valores son borradores, no datos bancarios reales.
  pagos: ConfiguracionPagos = {
    yape: true,
    plin: true,
    transferencia: true,
    numeroYape: '',
    numeroPlin: '',
    banco: '',
    numeroCuenta: '',
    cci: ''
  };

  // Configuración de envíos.
  // Los importes son valores iniciales del formulario,
  // no tarifas oficiales de la tienda.
  envios: ConfiguracionEnvios = {
    habilitarEnvios: false,
    costoBase: 0,
    envioGratisDesde: 0,
    tiempoEstimado: '',
    instrucciones: ''
  };

  cambiarSeccion(seccion: string): void {
    this.seccionActiva.set(seccion);
    this.error.set('');
    this.mensaje.set('');
  }

  obtenerNombreSeccion(): string {
    const secciones: Record<string, string> = {
      TIENDA: 'Información de la tienda',
      PAGOS: 'Métodos de pago',
      ENVIOS: 'Configuración de envíos',
      SEGURIDAD: 'Seguridad y acceso'
    };

    return secciones[this.seccionActiva()] ??
      'Configuración';
  }
}
