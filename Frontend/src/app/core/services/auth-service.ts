import { Service, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.interface';

@Service()
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly router = inject(Router);

    private readonly apiUrl = 'http://localhost:8080/api/auth';
    private readonly tokenKey = 'vb_token';
    private readonly userKey = 'vb_user';

    currentUser = signal<AuthResponse | null>(this.loadUser());
    isLoggedIn = computed(() => !!this.currentUser());
    isAdmin = computed(() => this.currentUser()?.roles.includes('ROLE_ADMIN') ?? false);

    register(req: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register`, req).pipe(
            tap(res => this.saveSession(res))
        );
    }

    login(req: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, req).pipe(
            tap(res => this.saveSession(res))
        );
    }

    logout(): void {
        if (typeof localStorage !== 'undefined') {
            localStorage.removeItem(this.tokenKey);
            localStorage.removeItem(this.userKey);
        }
        this.currentUser.set(null);
        this.router.navigate(['/auth/login']);
    }

    private saveSession(response: AuthResponse): void {
        if (typeof localStorage !== 'undefined') {
            localStorage.setItem(this.tokenKey, response.token);
            localStorage.setItem(this.userKey, JSON.stringify(response));
        }
        this.currentUser.set(response);
    }

    getToken(): string | null {
        if (typeof localStorage !== 'undefined') {
            return localStorage.getItem(this.tokenKey);
        }
        return null;
    }

    private loadUser(): AuthResponse | null {
        try {
            if (typeof localStorage !== 'undefined') {
                const raw = localStorage.getItem(this.userKey);
                return raw ? JSON.parse(raw) : null;
            }
        } catch {
            return null;
        }
        return null;
    }

}
