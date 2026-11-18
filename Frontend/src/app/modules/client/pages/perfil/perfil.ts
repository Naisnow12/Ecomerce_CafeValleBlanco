
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { UsuarioService } from '../../../../core/services/usuario.service';
import { AuthService } from '../../../../core/services/auth-service';
import { UsuarioProfile } from '../../../../core/models/usuario.interface';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss'
})
export class Perfil implements OnInit {

  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  // Estado del perfil
  usuario = signal<UsuarioProfile | null>(null);

  cargando = signal(true);
  guardando = signal(false);
  editando = signal(false);

  mensajeExito = signal<string | null>(null);
  mensajeError = signal<string | null>(null);

  // Formulario de edición
  form = this.fb.group({
    nombres: ['', [
      Validators.required,
      Validators.minLength(2)
    ]],

    apellidos: ['', [
      Validators.required,
      Validators.minLength(2)
    ]],

    telefono: ['']
  });

  // Usuario autenticado
  currentUser = this.authService.currentUser;

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);

    this.usuarioService.getProfile().subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);

        this.form.patchValue({
          nombres: usuario.nombres,
          apellidos: usuario.apellidos,
          telefono: usuario.telefono || ''
        });

        this.cargando.set(false);
      },

      error: () => {
        this.cargando.set(false);
        this.mensajeError.set(
          'No se pudo cargar tu perfil. Intenta nuevamente.'
        );
      }
    });
  }

  activarEdicion(): void {
    this.editando.set(true);
    this.mensajeExito.set(null);
    this.mensajeError.set(null);
  }

  cancelarEdicion(): void {
    const usuario = this.usuario();

    if (usuario) {
      this.form.patchValue({
        nombres: usuario.nombres,
        apellidos: usuario.apellidos,
        telefono: usuario.telefono || ''
      });
    }

    this.editando.set(false);
    this.mensajeError.set(null);
  }

  guardarCambios(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.mensajeError.set(null);
    this.mensajeExito.set(null);

    const request = {
      nombres: this.form.controls.nombres.value!,
      apellidos: this.form.controls.apellidos.value!,
      telefono: this.form.controls.telefono.value || undefined
    };

    this.usuarioService.updateProfile(request).subscribe({
      next: (usuarioActualizado) => {
        this.usuario.set(usuarioActualizado);

        this.guardando.set(false);
        this.editando.set(false);

        this.mensajeExito.set(
          'Tus datos se actualizaron correctamente.'
        );
      },

      error: () => {
        this.guardando.set(false);

        this.mensajeError.set(
          'No se pudieron guardar los cambios.'
        );
      }
    });
  }

  obtenerIniciales(): string {
    const usuario = this.usuario();

    if (!usuario) {
      return 'VB';
    }

    const nombre = usuario.nombres?.charAt(0) || '';
    const apellido = usuario.apellidos?.charAt(0) || '';

    return (nombre + apellido).toUpperCase();
  }

  cerrarSesion(): void {
    this.authService.logout();
  }
}
