import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-seguridad-admin',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink],
  templateUrl: './seguridad.html',
  styleUrl: './seguridad.scss'
})
export class SeguridadAdmin implements OnInit {
  private readonly adminService = inject(AdminService);

  cargando = signal(true);
  error = signal('');
  auditorias = signal<any[]>([]);

  busqueda = signal('');
  filtroModulo = signal('TODOS');

  modulosDisponibles = computed(() => {
    const set = new Set<string>();
    for (const a of this.auditorias()) {
      if (a.modulo) set.add(a.modulo);
    }
    return Array.from(set).sort();
  });

  auditoriasFiltradas = computed(() => {
    const q = this.busqueda().toLowerCase().trim();
    const mod = this.filtroModulo();

    return this.auditorias().filter(a => {
      const matchMod = mod === 'TODOS' || a.modulo === mod;
      const matchText =
        !q ||
        a.emailUsuario?.toLowerCase().includes(q) ||
        a.accion?.toLowerCase().includes(q) ||
        a.detalles?.toLowerCase().includes(q) ||
        a.modulo?.toLowerCase().includes(q);
      return matchMod && matchText;
    });
  });

  ngOnInit(): void {
    this.cargarAuditoria();
  }

  cargarAuditoria(): void {
    this.cargando.set(true);
    this.error.set('');
    this.adminService.getAuditoria().subscribe({
      next: (data) => {
        this.auditorias.set(data || []);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el registro de auditoría desde el servidor.');
        this.cargando.set(false);
      }
    });
  }
}
