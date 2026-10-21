import { Routes } from '@angular/router';
import { Home } from './modules/client/pages/home/home';
import { Login } from './modules/auth/pages/login/login';
import { Register } from './modules/auth/pages/register/register';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'client',
        pathMatch: 'full'
    },
    {
        path: 'auth',
        loadComponent: () => import('./modules/auth/layout-auth/layout-auth').then(m => m.LayoutAuth),
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
        ]
    }
];
