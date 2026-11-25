import { Component, signal, computed, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AdminService } from '../../../../core/services/admin.service';

export interface TopProducto {
  nombre: string;
  unidadesVendidas: number;
  totalGenerado: number;
  porcentaje: number;
}

export interface StockCritico {
  id: number;
  nombre: string;
  stockActual: number;
  nivelMinimo: number;
  estado: string;
}

export interface PedidoResumen {
  id: number;
  numeroPedido: string;
  cliente: string;
  fecha: string;
  estado: string;
  metodoPago: string;
  total: number;
}

export interface ReporteData {
  periodo: string;
  rangoTexto: string;
  fechaInicio: string | null;
  fechaFin: string | null;
  totalIngresos: number;
  totalPedidos: number;
  pedidosCompletados: number;
  pedidosPendientes: number;
  pedidosCancelados: number;
  ticketPromedio: number;
  totalUnidadesVendidas: number;
  totalClientesCompradores: number;
  pedidosPorEstado: Record<string, number>;
  ventasPorMetodoPago: Record<string, number>;
  topProductos: TopProducto[];
  stockCritico: StockCritico[];
  pedidos: PedidoResumen[];
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule, DecimalPipe, DatePipe, BaseChartDirective],
  templateUrl: './reportes.html',
  styleUrl: './reportes.scss'
})
export class ReportesAdmin implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly platformId = inject(PLATFORM_ID);
  readonly isBrowser = isPlatformBrowser(this.platformId);

  // Estados
  cargando = signal<boolean>(true);
  descargandoPdf = signal<boolean>(false);
  errorMensaje = signal<string | null>(null);

  // Estados de descarga individuales
  descargandoVentas = signal<boolean>(false);
  descargandoProductos = signal<boolean>(false);
  descargandoPedidos = signal<boolean>(false);
  descargandoClientes = signal<boolean>(false);

  // Filtros
  periodoSeleccionado = signal<string>('30_DIAS');
  estadoSeleccionado = signal<string>('TODOS');
  metodoPagoSeleccionado = signal<string>('TODOS');
  fechaInicio = signal<string>('');
  fechaFin = signal<string>('');

  // Datos
  reporte = signal<ReporteData | null>(null);

  fechaHoy = new Date();

  // Opciones de período
  periodos = [
    { id: 'HOY', label: 'Hoy' },
    { id: '7_DIAS', label: 'Últimos 7 días' },
    { id: 'ESTE_MES', label: 'Este mes' },
    { id: '30_DIAS', label: 'Últimos 30 días' },
    { id: 'ESTE_ANIO', label: 'Año en curso' },
    { id: 'HISTORICO', label: 'Todo el historial' },
    { id: 'PERSONALIZADO', label: 'Rango personalizado' }
  ];

  estados = [
    { id: 'TODOS', label: 'Todos los estados' },
    { id: 'ENTREGADO', label: 'Entregado' },
    { id: 'ENVIADO', label: 'Enviado' },
    { id: 'EN_PREPARACION', label: 'En preparación' },
    { id: 'CONFIRMADO', label: 'Confirmado' },
    { id: 'PENDIENTE', label: 'Pendiente' },
    { id: 'CANCELADO', label: 'Cancelado' }
  ];

  metodosPago = [
    { id: 'TODOS', label: 'Todos los métodos' },
    { id: 'YAPE', label: 'Yape' },
    { id: 'PLIN', label: 'Plin' },
    { id: 'TARJETA', label: 'Tarjeta' },
    { id: 'TRANSFERENCIA', label: 'Transferencia bancaria' }
  ];

  ngOnInit(): void {
    this.consultarReportes();
  }

  consultarReportes(): void {
    this.cargando.set(true);
    this.errorMensaje.set(null);

    const filtros = {
      periodo: this.periodoSeleccionado(),
      estado: this.estadoSeleccionado(),
      metodoPago: this.metodoPagoSeleccionado(),
      fechaInicio: this.periodoSeleccionado() === 'PERSONALIZADO' ? this.fechaInicio() : undefined,
      fechaFin: this.periodoSeleccionado() === 'PERSONALIZADO' ? this.fechaFin() : undefined
    };

    this.adminService.getReportes(filtros).subscribe({
      next: (data) => {
        this.reporte.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        console.error('Error al cargar reporte:', err);
        this.errorMensaje.set('No se pudieron obtener las métricas del servidor. Asegúrate de que el backend esté activo.');
        this.cargando.set(false);
      }
    });
  }

  descargarPdf(): void {
    if (this.descargandoPdf()) return;
    this.descargandoPdf.set(true);

    const filtros = {
      periodo: this.periodoSeleccionado(),
      estado: this.estadoSeleccionado(),
      metodoPago: this.metodoPagoSeleccionado(),
      fechaInicio: this.periodoSeleccionado() === 'PERSONALIZADO' ? this.fechaInicio() : undefined,
      fechaFin: this.periodoSeleccionado() === 'PERSONALIZADO' ? this.fechaFin() : undefined
    };

    this.adminService.descargarReportePdf(filtros).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const fechaStr = new Date().toISOString().slice(0, 10);
        a.download = `Reporte_ValleBlanco_${this.periodoSeleccionado()}_${fechaStr}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.descargandoPdf.set(false);
      },
      error: (err) => {
        console.error('Error al descargar PDF:', err);
        alert('Hubo un inconveniente al generar el PDF. Puedes intentar imprimir directamente con el botón de impresión.');
        this.descargandoPdf.set(false);
      }
    });
  }

  imprimirReporte(): void {
    window.print();
  }

  private getFiltros() {
    return {
      periodo: this.periodoSeleccionado(),
      estado: this.estadoSeleccionado(),
      metodoPago: this.metodoPagoSeleccionado(),
      fechaInicio: this.periodoSeleccionado() === 'PERSONALIZADO' ? this.fechaInicio() : undefined,
      fechaFin: this.periodoSeleccionado() === 'PERSONALIZADO' ? this.fechaFin() : undefined
    };
  }

  private triggerDownload(blob: Blob, nombre: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

  descargarVentas(): void {
    if (this.descargandoVentas()) return;
    this.descargandoVentas.set(true);
    const fechaStr = new Date().toISOString().slice(0, 10);
    this.adminService.descargarReporteVentasPdf(this.getFiltros()).subscribe({
      next: (blob) => {
        this.triggerDownload(blob, `Ventas_ValleBlanco_${this.periodoSeleccionado()}_${fechaStr}.pdf`);
        this.descargandoVentas.set(false);
      },
      error: (err) => {
        console.error('Error al descargar PDF de ventas:', err);
        alert('No se pudo generar el reporte de ventas. Intenta nuevamente.');
        this.descargandoVentas.set(false);
      }
    });
  }

  descargarProductos(): void {
    if (this.descargandoProductos()) return;
    this.descargandoProductos.set(true);
    const fechaStr = new Date().toISOString().slice(0, 10);
    this.adminService.descargarReporteProductosPdf(this.getFiltros()).subscribe({
      next: (blob) => {
        this.triggerDownload(blob, `Productos_ValleBlanco_${this.periodoSeleccionado()}_${fechaStr}.pdf`);
        this.descargandoProductos.set(false);
      },
      error: (err) => {
        console.error('Error al descargar PDF de productos:', err);
        alert('No se pudo generar el reporte de productos. Intenta nuevamente.');
        this.descargandoProductos.set(false);
      }
    });
  }

  descargarPedidos(): void {
    if (this.descargandoPedidos()) return;
    this.descargandoPedidos.set(true);
    const fechaStr = new Date().toISOString().slice(0, 10);
    this.adminService.descargarReportePedidosPdf(this.getFiltros()).subscribe({
      next: (blob) => {
        this.triggerDownload(blob, `Pedidos_ValleBlanco_${this.periodoSeleccionado()}_${fechaStr}.pdf`);
        this.descargandoPedidos.set(false);
      },
      error: (err) => {
        console.error('Error al descargar PDF de pedidos:', err);
        alert('No se pudo generar el reporte de pedidos. Intenta nuevamente.');
        this.descargandoPedidos.set(false);
      }
    });
  }

  descargarClientes(): void {
    if (this.descargandoClientes()) return;
    this.descargandoClientes.set(true);
    const fechaStr = new Date().toISOString().slice(0, 10);
    this.adminService.descargarReporteClientesPdf(this.getFiltros()).subscribe({
      next: (blob) => {
        this.triggerDownload(blob, `Clientes_ValleBlanco_${this.periodoSeleccionado()}_${fechaStr}.pdf`);
        this.descargandoClientes.set(false);
      },
      error: (err) => {
        console.error('Error al descargar PDF de clientes:', err);
        alert('No se pudo generar el reporte de clientes. Intenta nuevamente.');
        this.descargandoClientes.set(false);
      }
    });
  }

  getEstadoClass(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'badge--warning',
      CONFIRMADO: 'badge--info',
      EN_PREPARACION: 'badge--purple',
      ENVIADO: 'badge--blue',
      ENTREGADO: 'badge--success',
      CANCELADO: 'badge--danger'
    };
    return map[estado] || 'badge--default';
  }

  obtenerMetodosArray(): { metodo: string; total: number; porcentaje: number }[] {
    const rep = this.reporte();
    if (!rep || !rep.ventasPorMetodoPago) return [];
    const total = rep.totalIngresos > 0 ? rep.totalIngresos : 1;
    return Object.entries(rep.ventasPorMetodoPago).map(([metodo, monto]) => ({
      metodo,
      total: monto,
      porcentaje: Math.min(100, Math.round((monto / total) * 100))
    }));
  }

  obtenerEstadosArray(): { estado: string; cantidad: number }[] {
    const rep = this.reporte();
    if (!rep || !rep.pedidosPorEstado) return [];
    return Object.entries(rep.pedidosPorEstado).map(([estado, cantidad]) => ({
      estado,
      cantidad
    }));
  }

  // ——————————————————————————————————————
  // Gráficos Chart.js (Signals reactivos)
  // ——————————————————————————————————————

  // 1. Evolución de ingresos en el tiempo (Línea)
  ingresosLineData = computed<ChartData<'line'>>(() => {
    const rep = this.reporte();
    if (!rep || !rep.pedidos || rep.pedidos.length === 0) {
      return { labels: [], datasets: [] };
    }

    const porDia = new Map<string, { label: string; total: number }>();
    const pedidosValidos = rep.pedidos
      .filter(p => p.estado?.toUpperCase() !== 'CANCELADO' && p.fecha)
      .slice()
      .sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime());

    for (const ped of pedidosValidos) {
      const d = new Date(ped.fecha);
      if (isNaN(d.getTime())) continue;
      const key = d.toISOString().slice(0, 10);
      const label = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`;
      const prev = porDia.get(key) ?? { label, total: 0 };
      prev.total += Number(ped.total || 0);
      porDia.set(key, prev);
    }

    const entries = Array.from(porDia.values());
    return {
      labels: entries.map(e => e.label),
      datasets: [
        {
          label: 'Ingresos diarios (S/)',
          data: entries.map(e => Number(e.total.toFixed(2))),
          borderColor: '#1c3829',
          backgroundColor: 'rgba(28, 56, 41, 0.12)',
          pointBackgroundColor: '#b87c43',
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7,
          borderWidth: 2.5,
          tension: 0.3,
          fill: true
        }
      ]
    };
  });

  ingresosLineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1c3829',
        titleColor: '#f9f7f1',
        bodyColor: '#ffffff',
        padding: 10,
        cornerRadius: 8,
        callbacks: {
          label: (ctx) => ` Ingresos: S/ ${Number(ctx.parsed.y ?? 0).toFixed(2)}`
        }
      }
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#64748b', font: { size: 11 } }
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(226, 217, 200, 0.45)' },
        ticks: {
          color: '#64748b',
          font: { size: 11 },
          callback: (val) => `S/ ${val}`
        }
      }
    }
  };

  // 2. Ventas por método de pago (Dona)
  metodosDoughnutData = computed<ChartData<'doughnut'>>(() => {
    const metodos = this.obtenerMetodosArray();
    const paleta = ['#1c3829', '#b87c43', '#2d6a4f', '#d4a46a', '#64748b'];
    return {
      labels: metodos.map(m => m.metodo),
      datasets: [
        {
          data: metodos.map(m => Number(m.total || 0)),
          backgroundColor: metodos.map((_, idx) => paleta[idx % paleta.length]),
          borderColor: '#ffffff',
          borderWidth: 2,
          hoverOffset: 6
        }
      ]
    };
  });

  metodosDoughnutOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '64%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#2e3d2f',
          padding: 14,
          usePointStyle: true,
          pointStyle: 'circle',
          font: { size: 12, weight: 600 }
        }
      },
      tooltip: {
        backgroundColor: '#1c3829',
        padding: 10,
        cornerRadius: 8,
        callbacks: {
          label: (ctx) => ` ${ctx.label}: S/ ${Number(ctx.parsed ?? 0).toFixed(2)}`
        }
      }
    }
  };

  // 3. Top productos más vendidos (Barras horizontales)
  topProductosBarData = computed<ChartData<'bar'>>(() => {
    const rep = this.reporte();
    const prods = (rep?.topProductos ?? []).slice(0, 6);
    return {
      labels: prods.map(p => p.nombre.length > 26 ? p.nombre.slice(0, 24) + '…' : p.nombre),
      datasets: [
        {
          label: 'Unidades vendidas',
          data: prods.map(p => p.unidadesVendidas),
          backgroundColor: '#1c3829',
          hoverBackgroundColor: '#b87c43',
          borderRadius: 6,
          barThickness: 22
        }
      ]
    };
  });

  topProductosBarOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1c3829',
        padding: 10,
        cornerRadius: 8,
        callbacks: {
          label: (ctx) => ` ${ctx.parsed.x} bolsas vendidas`
        }
      }
    },
    scales: {
      x: {
        beginAtZero: true,
        grid: { color: 'rgba(226, 217, 200, 0.45)' },
        ticks: { stepSize: 1, color: '#64748b', font: { size: 11 } }
      },
      y: {
        grid: { display: false },
        ticks: { color: '#2e3d2f', font: { size: 11, weight: 600 } }
      }
    }
  };

  // 4. Pedidos por estado (Barras verticales)
  estadosBarData = computed<ChartData<'bar'>>(() => {
    const estados = this.obtenerEstadosArray();
    const colorMap: Record<string, string> = {
      PENDIENTE: '#d97706',
      CONFIRMADO: '#0284c7',
      EN_PREPARACION: '#7c3aed',
      ENVIADO: '#2d6a4f',
      ENTREGADO: '#1c3829',
      CANCELADO: '#b91c1c'
    };
    return {
      labels: estados.map(e => e.estado.replace('_', ' ')),
      datasets: [
        {
          label: 'Cantidad de pedidos',
          data: estados.map(e => e.cantidad),
          backgroundColor: estados.map(e => colorMap[e.estado] ?? '#64748b'),
          borderRadius: 6,
          barThickness: 30
        }
      ]
    };
  });

  estadosBarOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1c3829',
        padding: 10,
        cornerRadius: 8,
        callbacks: {
          label: (ctx) => ` ${ctx.parsed.y} pedidos`
        }
      }
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#2e3d2f', font: { size: 10, weight: 600 } }
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(226, 217, 200, 0.45)' },
        ticks: { stepSize: 1, color: '#64748b', font: { size: 11 } }
      }
    }
  };
}
