import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { Proceso } from './modules/client/pages/proceso/proceso';
import { Login } from './modules/auth/pages/login/login';
import { Register } from './modules/auth/pages/register/register';
import { Home } from './modules/client/pages/home/home';
import { Catalog } from './modules/client/pages/catalog/catalog';
import { ProductDetail } from './modules/client/pages/product-detail/product-detail';
import { Carrito } from './modules/client/pages/carrito/carrito';
import { Checkout } from './modules/client/pages/checkout/checkout';
import { OrderConfirm } from './modules/client/pages/order-confirm/order-confirm';
import { MisPedidos } from './modules/client/pages/mis-pedidos/mis-pedidos';
import { Perfil } from './modules/client/pages/perfil/perfil';
import { SobreNosotros } from './modules/client/pages/sobre-nosotros/sobre-nosotros';
import { NuestroCafe } from './modules/client/pages/nuestro-cafe/nuestro-cafe';
import { Dashboard } from './modules/admin/pages/dashboard/dashboard';
import { ProductosAdmin } from './modules/admin/pages/productos/productos-admin/productos-admin';
import { PedidosAdmin } from './modules/admin/pages/pedidos/pedidos-admin/pedidos-admin';
import { ClientesAdmin } from './modules/admin/pages/clientes/clientes-admin/clientes-admin';
import { Configuracion } from './modules/admin/pages/configuracion/configuracion';
import { ReportesAdmin } from './modules/admin/pages/reportes/reportes';
import { StockDashboard } from './modules/stock/pages/dashboard/stock-dashboard';

export const routes: Routes = [
  { path: '', redirectTo: 'client', pathMatch: 'full' },
  {
    path: 'auth',
    loadComponent: () => import('./modules/auth/layout-auth/layout-auth').then(m => m.LayoutAuth),
    canActivate: [guestGuard],
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'login', component: Login },
      { path: 'register', component: Register }
    ]
  },
  {
    path: 'client',
    loadComponent: () => import('./modules/client/layout-client/layout-client').then(m => m.LayoutClient),
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: Home },
      { path: 'catalog', component: Catalog },
      { path: 'product/:slug', component: ProductDetail },
      { path: 'carrito', component: Carrito, canActivate: [authGuard, roleGuard(['ROLE_CLIENTE', 'ROLE_ADMIN'])] },
      { path: 'checkout', component: Checkout, canActivate: [authGuard, roleGuard(['ROLE_CLIENTE', 'ROLE_ADMIN'])] },
      { path: 'order-confirm/:id', component: OrderConfirm, canActivate: [authGuard] },
      { path: 'mis-pedidos', component: MisPedidos, canActivate: [authGuard, roleGuard(['ROLE_CLIENTE', 'ROLE_ADMIN'])] },
      { path: 'perfil', component: Perfil, canActivate: [authGuard] },
      { path: 'proceso', component: Proceso },
      { path: 'nuestro-cafe', component: NuestroCafe },
      { path: 'sobre-nosotros', component: SobreNosotros },
    ]
  },
  {
    path: 'admin',
    loadComponent: () => import('./modules/admin/layout-admin/layout-admin').then(m => m.LayoutAdmin),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: Dashboard },
      { path: 'reportes', component: ReportesAdmin },
      { path: 'productos', component: ProductosAdmin },
      { path: 'pedidos', component: PedidosAdmin },
      { path: 'clientes', component: ClientesAdmin },
      { path: 'configuracion', component: Configuracion },
    ]
  },
  {
    path: 'inventario',
    loadComponent: () => import('./modules/stock/layout-stock/layout-stock').then(m => m.LayoutStock),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_ENCARGADO_STOCK'])],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: StockDashboard }
    ]
  },
  { path: '**', redirectTo: 'client' }
];
