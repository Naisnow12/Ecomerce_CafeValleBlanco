import { Component, inject, signal, OnDestroy } from '@angular/core';
import { Router, RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-layout-auth',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './layout-auth.html',
  styleUrl: './layout-auth.scss',
})
export class LayoutAuth implements OnDestroy {
  private readonly router = inject(Router);

  private readonly targetRegister = signal<boolean | null>(null);

  readonly isTransitioning = signal(false);

  private transitionTimer?: ReturnType<typeof setTimeout>;

  get isRegister(): boolean {
    return (
      this.targetRegister() ??
      this.router.url.split('?')[0].endsWith('/register')
    );
  }

  cambiarFormulario(destino: 'login' | 'register'): void {
    if (this.isTransitioning()) {
      return;
    }

    const registrar = destino === 'register';

    if (registrar === this.isRegister) {
      return;
    }

    this.isTransitioning.set(true);
    this.targetRegister.set(registrar);

    this.transitionTimer = setTimeout(async () => {
      try {
        await this.router.navigate([
          registrar ? '/auth/register' : '/auth/login'
        ]);
      } finally {
        this.targetRegister.set(null);
        this.isTransitioning.set(false);
      }
    }, 800);
  }

  ngOnDestroy(): void {
    if (this.transitionTimer) {
      clearTimeout(this.transitionTimer);
    }
  }
}