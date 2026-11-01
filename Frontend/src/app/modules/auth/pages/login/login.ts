import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  showPassword = signal(false);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  get f() {
    return this.form.controls;
  }

  togglePassword(): void {
    this.showPassword.update(value => !value);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.login({
      email: this.f.email.value!,
      password: this.f.password.value!
    }).subscribe({
      next: () => {
        this.isLoading.set(false);

        const returnUrl =
          this.route.snapshot.queryParamMap.get('returnUrl') ||
          '/client/home';

        this.router.navigateByUrl(returnUrl);
      },

      error: (err) => {
        this.isLoading.set(false);

        if (err.status === 401 || err.status === 403) {
          this.errorMessage.set(
            'Credenciales incorrectas. Verifica tu correo y contraseña.'
          );
        } else if (err.status === 0) {
          this.errorMessage.set(
            'No se pudo conectar al servidor. Intenta más tarde.'
          );
        } else {
          this.errorMessage.set(
            'Ocurrió un error inesperado. Intenta nuevamente.'
          );
        }
      }
    });
  }
}