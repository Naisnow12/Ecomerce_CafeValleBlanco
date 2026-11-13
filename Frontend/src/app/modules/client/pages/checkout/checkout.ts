
import { Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { CarritoService } from '../../../../core/services/carrito.service';
import { PedidoService } from '../../../../core/services/pedido.service';
import { UsuarioService } from '../../../../core/services/usuario.service';

import { Direccion, UsuarioProfile } from '../../../../core/models/usuario.interface';
import { CrearPedidoRequest } from '../../../../core/models/pedido.interface';

@Component({
  selector: 'app-checkout',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout implements OnInit {

  private readonly carritoService = inject(CarritoService);
  private readonly pedidoService = inject(PedidoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly router = inject(Router);

  // Carrito
  carrito = this.carritoService.carrito;

  // Cliente
  usuario = signal<UsuarioProfile | null>(null);

  // Direcciones
  direcciones = signal<Direccion[]>([]);
  direccionSeleccionada = signal<number | null>(null);
  mostrarNuevaDireccion = signal(false);

  nuevaDireccion: Direccion = this.crearDireccionVacia();

  // Pago
  metodoPago = signal<string>('YAPE');
  notas = '';

  // Estados
  cargando = signal(true);
  guardandoDireccion = signal(false);
  procesandoPedido = signal(false);
  error = signal('');
  mensaje = signal('');

  ngOnInit(): void {
    this.cargarDatos();
  }

  private crearDireccionVacia(): Direccion {
    return {
      alias: 'Casa',
      destinatario: '',
      linea1: '',
      linea2: '',
      distrito: '',
      provincia: '',
      departamento: '',
      codigoPostal: '',
      telefono: '',
      esPrincipal: false
    };
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.error.set('');

    this.usuarioService.getProfile().subscribe({
      next: (perfil) => {
        this.usuario.set(perfil);
      },
      error: () => {
        this.error.set('No se pudo cargar tu información personal.');
      }
    });

    this.usuarioService.getDirecciones().subscribe({
      next: (direcciones) => {
        this.direcciones.set(direcciones);

        const principal = direcciones.find(d => d.esPrincipal && d.id);
        const primera = direcciones.find(d => d.id);

        this.direccionSeleccionada.set(
          principal?.id ?? primera?.id ?? null
        );

        this.mostrarNuevaDireccion.set(direcciones.length === 0);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus direcciones.');
        this.cargando.set(false);
      }
    });

    this.carritoService.cargarCarrito().subscribe({
      error: () => {
        this.error.set('No se pudo cargar el carrito.');
      }
    });
  }

  seleccionarDireccion(id: number): void {
    this.direccionSeleccionada.set(id);
    this.mostrarNuevaDireccion.set(false);
    this.error.set('');
  }

  abrirNuevaDireccion(): void {
    this.nuevaDireccion = this.crearDireccionVacia();

    const perfil = this.usuario();

    if (perfil) {
      this.nuevaDireccion.destinatario =
        `${perfil.nombres} ${perfil.apellidos}`;

      this.nuevaDireccion.telefono = perfil.telefono ?? '';
    }

    this.mostrarNuevaDireccion.set(true);
    this.error.set('');
  }

  cancelarNuevaDireccion(): void {
    if (this.direcciones().length > 0) {
      this.mostrarNuevaDireccion.set(false);
    }
  }

  guardarDireccion(): void {
    const direccion = this.nuevaDireccion;

    if (
      !direccion.linea1.trim() ||
      !direccion.distrito.trim() ||
      !direccion.provincia.trim() ||
      !direccion.departamento.trim()
    ) {
      this.error.set('Completa los campos obligatorios de la dirección.');
      return;
    }

    this.guardandoDireccion.set(true);
    this.error.set('');

    this.usuarioService.addDireccion(direccion).subscribe({
      next: (guardada) => {
        this.direcciones.update(lista => [...lista, guardada]);

        if (guardada.id != null) {
          this.direccionSeleccionada.set(guardada.id);
        }

        this.mostrarNuevaDireccion.set(false);
        this.guardandoDireccion.set(false);
        this.mensaje.set('Dirección guardada correctamente.');
      },
      error: (err: HttpErrorResponse) => {
        this.guardandoDireccion.set(false);
        this.error.set(
          err.error?.message || 'No se pudo guardar la dirección.'
        );
      }
    });
  }

  seleccionarMetodoPago(metodo: string): void {
    this.metodoPago.set(metodo);
  }

  confirmarPedido(): void {
    if (this.procesandoPedido()) {
      return;
    }

    const carritoActual = this.carrito();

    if (!carritoActual?.items?.length) {
      this.error.set('Tu carrito está vacío.');
      return;
    }

    const direccionId = this.direccionSeleccionada();

    if (direccionId == null) {
      this.error.set('Selecciona o registra una dirección de entrega.');
      return;
    }

    if (this.mostrarNuevaDireccion()) {
      this.error.set('Guarda primero tu nueva dirección.');
      return;
    }

    const request: CrearPedidoRequest = {
      direccionId,
      metodoPago: this.metodoPago(),
      notas: this.notas.trim() || undefined
    };

    this.procesandoPedido.set(true);
    this.error.set('');
    this.mensaje.set('');

    this.pedidoService.crearPedido(request).subscribe({
      next: (pedido) => {
        this.procesandoPedido.set(false);

        // Actualizamos el carrito después de confirmar la compra.
        this.carritoService.cargarCarrito().subscribe({
          error: () => { }
        });

        this.router.navigate([
          '/client/order-confirm',
          pedido.id
        ]);
      },
      error: (err: HttpErrorResponse) => {
        this.procesandoPedido.set(false);

        this.error.set(
          err.error?.message ||
          err.error?.error ||
          'No se pudo registrar el pedido. Inténtalo nuevamente.'
        );
      }
    });
  }
}
