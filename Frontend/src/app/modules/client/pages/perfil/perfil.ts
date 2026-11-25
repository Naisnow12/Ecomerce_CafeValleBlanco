
import {
  Component,
  OnInit,
  inject,
  signal,
  computed,
  PLATFORM_ID,
} from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

import { UsuarioService } from '../../../../core/services/usuario.service';
import { AuthService } from '../../../../core/services/auth-service';
import { UsuarioProfile } from '../../../../core/models/usuario.interface';

/** Valida que newPassword === confirmNewPassword */
function passwordsCoinciden(control: AbstractControl): ValidationErrors | null {
  const nuevo = control.get('newPassword')?.value;
  const confirmar = control.get('confirmNewPassword')?.value;
  if (nuevo && confirmar && nuevo !== confirmar) {
    return { noCoinciden: true };
  }
  return null;
}

/** Calcula fortaleza de contraseña (0–4) */
function calcularFortaleza(password: string): number {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return score;
}

const AVATAR_KEY_PREFIX = 'vb_avatar_';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class Perfil implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly platformId = inject(PLATFORM_ID);

  // ── Estado del perfil ──────────────────────────────────────────────────────
  usuario = signal<UsuarioProfile | null>(null);
  cargando = signal(true);
  guardando = signal(false);
  editando = signal(false);
  mensajeExito = signal<string | null>(null);
  mensajeError = signal<string | null>(null);

  // ── Foto de perfil ─────────────────────────────────────────────────────────
  avatarSrc = signal<string | null>(null);
  subiendoFoto = signal(false);

  // ── Cambio de contraseña ───────────────────────────────────────────────────
  mostrarCambioPassword = signal(false);
  cambiandoPassword = signal(false);
  passwordExito = signal<string | null>(null);
  passwordError = signal<string | null>(null);

  passwordForm = this.fb.group(
    {
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmNewPassword: ['', [Validators.required]],
    },
    { validators: passwordsCoinciden }
  );

  /** Fortaleza de la nueva contraseña (0–4), calculada reactivamente */
  fortalezaPassword = computed(() =>
    calcularFortaleza(this.passwordForm.controls.newPassword.value ?? '')
  );

  fortalezaLabel = computed(() => {
    const f = this.fortalezaPassword();
    if (f === 0) return '';
    if (f === 1) return 'Muy débil';
    if (f === 2) return 'Débil';
    if (f === 3) return 'Buena';
    return 'Fuerte';
  });

  fortalezaClase = computed(() => {
    const f = this.fortalezaPassword();
    if (f <= 1) return 'muy-debil';
    if (f === 2) return 'debil';
    if (f === 3) return 'buena';
    return 'fuerte';
  });

  // ── Formulario de edición de datos ─────────────────────────────────────────
  form = this.fb.group({
    nombres: ['', [Validators.required, Validators.minLength(2)]],
    apellidos: ['', [Validators.required, Validators.minLength(2)]],
    telefono: [''],
  });

  // ── Auth ───────────────────────────────────────────────────────────────────
  currentUser = this.authService.currentUser;
  puedeComprar = this.authService.puedeComprar;

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.cargarPerfil();
  }

  // ── Perfil ─────────────────────────────────────────────────────────────────
  cargarPerfil(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);

    this.usuarioService.getProfile().subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);
        this.form.patchValue({
          nombres: usuario.nombres,
          apellidos: usuario.apellidos,
          telefono: usuario.telefono || '',
        });
        this.cargarAvatarLocal(usuario.email);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajeError.set(
          'No se pudo cargar tu perfil. Intenta nuevamente.'
        );
      },
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
        telefono: usuario.telefono || '',
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
      telefono: this.form.controls.telefono.value || undefined,
    };

    this.usuarioService.updateProfile(request).subscribe({
      next: (usuarioActualizado) => {
        this.usuario.set(usuarioActualizado);
        this.authService.updateCurrentUserName(
          usuarioActualizado.nombres,
          usuarioActualizado.apellidos
        );
        this.guardando.set(false);
        this.editando.set(false);
        this.mensajeExito.set('Tus datos se actualizaron correctamente.');
        setTimeout(() => this.mensajeExito.set(null), 4000);
      },
      error: () => {
        this.guardando.set(false);
        this.mensajeError.set('No se pudieron guardar los cambios.');
      },
    });
  }

  // ── Avatar ─────────────────────────────────────────────────────────────────
  private cargarAvatarLocal(email: string): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const guardado = localStorage.getItem(AVATAR_KEY_PREFIX + email);
    this.avatarSrc.set(guardado ?? null);
  }

  seleccionarFoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) return;

    // Validaciones del archivo
    if (!archivo.type.startsWith('image/')) {
      this.mensajeError.set('Solo se permiten archivos de imagen.');
      return;
    }
    if (archivo.size > 2 * 1024 * 1024) {
      this.mensajeError.set('La imagen no debe superar 2 MB.');
      return;
    }

    this.subiendoFoto.set(true);
    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      this.avatarSrc.set(dataUrl);
      // Guardar en localStorage vinculado al email del usuario
      const email = this.usuario()?.email;
      if (email && isPlatformBrowser(this.platformId)) {
        localStorage.setItem(AVATAR_KEY_PREFIX + email, dataUrl);
      }
      this.subiendoFoto.set(false);
      this.mensajeExito.set('Foto de perfil actualizada.');
      setTimeout(() => this.mensajeExito.set(null), 3000);
    };
    reader.onerror = () => {
      this.subiendoFoto.set(false);
      this.mensajeError.set('No se pudo leer la imagen.');
    };
    reader.readAsDataURL(archivo);

    // Resetear el input para permitir subir la misma imagen de nuevo
    input.value = '';
  }

  eliminarFoto(): void {
    const email = this.usuario()?.email;
    if (email && isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(AVATAR_KEY_PREFIX + email);
    }
    this.avatarSrc.set(null);
    this.mensajeExito.set('Foto eliminada.');
    setTimeout(() => this.mensajeExito.set(null), 3000);
  }

  obtenerIniciales(): string {
    const usuario = this.usuario();
    if (!usuario) return 'VB';
    const nombre = usuario.nombres?.charAt(0) || '';
    const apellido = usuario.apellidos?.charAt(0) || '';
    return (nombre + apellido).toUpperCase();
  }

  // ── Contraseña ─────────────────────────────────────────────────────────────
  toggleCambioPassword(): void {
    this.mostrarCambioPassword.update((v) => !v);
    this.passwordExito.set(null);
    this.passwordError.set(null);
    this.passwordForm.reset();
  }

  cambiarContrasena(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { currentPassword, newPassword, confirmNewPassword } =
      this.passwordForm.getRawValue();

    // Doble verificación (también capturada por el validator del grupo)
    if (newPassword !== confirmNewPassword) {
      this.passwordError.set(
        'La nueva contraseña y su confirmación no coinciden.'
      );
      return;
    }

    this.cambiandoPassword.set(true);
    this.passwordError.set(null);
    this.passwordExito.set(null);

    this.usuarioService
      .changePassword({
        currentPassword: currentPassword!,
        newPassword: newPassword!,
        confirmNewPassword: confirmNewPassword!,
      })
      .subscribe({
        next: () => {
          this.cambiandoPassword.set(false);
          this.mostrarCambioPassword.set(false);
          this.passwordForm.reset();
          this.passwordExito.set('Tu contraseña se actualizó correctamente.');
          setTimeout(() => this.passwordExito.set(null), 5000);
        },
        error: (err) => {
          this.cambiandoPassword.set(false);
          this.passwordError.set(
            err?.error?.error ||
              err?.error?.message ||
              'No se pudo cambiar la contraseña. Verifica tu contraseña actual.'
          );
        },
      });
  }

  // ── Sesión ─────────────────────────────────────────────────────────────────
  cerrarSesion(): void {
    this.authService.logout();
  }
}
