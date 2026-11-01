import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'client', pathMatch: 'full' },
  {
    path: 'auth',
    loadComponent: () => import('./modules/auth/layout-auth/layout-auth').then(m => m.LayoutAuth),
    canActivate: [guestGuard],
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'login', loadComponent: () => import('./modules/auth/pages/login/login').then(m => m.Login) },
      { path: 'register', loadComponent: () => import('./modules/auth/pages/register/register').then(m => m.Register) }
    ]
  },
  {
    path: 'client',
    loadComponent: () => import('./modules/client/layout-client/layout-client').then(m => m.LayoutClient),
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', loadComponent: () => import('./modules/client/pages/home/home').then(m => m.Home) },
      { path: 'catalog', loadComponent: () => import('./modules/client/pages/catalog/catalog').then(m => m.Catalog) },
      { path: 'product/:slug', loadComponent: () => import('./modules/client/pages/product-detail/product-detail').then(m => m.ProductDetail) },
      { path: 'carrito', loadComponent: () => import('./modules/client/pages/carrito/carrito').then(m => m.CarritoPage), canActivate: [authGuard] },
      { path: 'checkout', loadComponent: () => import('./modules/client/pages/checkout/checkout').then(m => m.Checkout), canActivate: [authGuard] },
      { path: 'order-confirm/:id', loadComponent: () => import('./modules/client/pages/order-confirm/order-confirm').then(m => m.OrderConfirm), canActivate: [authGuard] },
      { path: 'mis-pedidos', loadComponent: () => import('./modules/client/pages/mis-pedidos/mis-pedidos').then(m => m.MisPedidos), canActivate: [authGuard] },
      { path: 'perfil', loadComponent: () => import('./modules/client/pages/perfil/perfil').then(m => m.Perfil), canActivate: [authGuard] },
      { path: 'sobre-nosotros', loadComponent: () => import('./modules/client/pages/sobre-nosotros/sobre-nosotros').then(m => m.SobreNosotros) },
    ]
  },
  {
    path: 'admin',
    loadComponent: () => import('./modules/admin/layout-admin/layout-admin').then(m => m.LayoutAdmin),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./modules/admin/pages/dashboard/dashboard').then(m => m.Dashboard) },
      {
        path: 'productos',
        loadComponent: () =>
          import('./modules/admin/pages/productos/productos-admin/productos-admin')
            .then(m => m.ProductosAdmin)
      },

      {
        path: 'pedidos',
        loadComponent: () =>
          import('./modules/admin/pages/pedidos/pedidos-admin/pedidos-admin')
            .then(m => m.PedidosAdmin)
      },

      {
        path: 'clientes',
        loadComponent: () =>
          import('./modules/admin/pages/clientes/clientes-admin/clientes-admin')
            .then(m => m.ClientesAdmin)
      },
      { path: 'configuracion', loadComponent: () => import('./modules/admin/pages/configuracion/configuracion').then(m => m.Configuracion) },
    ]
  },
  { path: '**', redirectTo: 'client' }
];
