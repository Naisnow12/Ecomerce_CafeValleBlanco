import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  UsuarioProfile,
  UpdateProfileRequest,
  ChangePasswordRequest,
  Direccion
} from '../models/usuario.interface';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/usuario';

  getProfile(): Observable<UsuarioProfile> {
    return this.http.get<UsuarioProfile>(`${this.apiUrl}/perfil`);
  }

  updateProfile(req: UpdateProfileRequest): Observable<UsuarioProfile> {
    return this.http.put<UsuarioProfile>(`${this.apiUrl}/perfil`, req);
  }

  changePassword(req: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/password`, req);
  }

  getDirecciones(): Observable<Direccion[]> {
    return this.http.get<Direccion[]>(`${this.apiUrl}/direcciones`);
  }

  addDireccion(req: Direccion): Observable<Direccion> {
    return this.http.post<Direccion>(`${this.apiUrl}/direcciones`, req);
  }

  updateDireccion(id: number, req: Direccion): Observable<Direccion> {
    return this.http.put<Direccion>(`${this.apiUrl}/direcciones/${id}`, req);
  }

  deleteDireccion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/direcciones/${id}`);
  }
}
