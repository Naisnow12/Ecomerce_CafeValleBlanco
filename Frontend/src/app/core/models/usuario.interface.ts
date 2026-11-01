export interface UsuarioProfile {
  id: number;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
}

export interface UpdateProfileRequest {
  nombres: string;
  apellidos: string;
  telefono?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface Direccion {
  id?: number;
  alias?: string;
  destinatario?: string;
  linea1: string;
  linea2?: string;
  distrito: string;
  provincia: string;
  departamento: string;
  codigoPostal?: string;
  telefono?: string;
  esPrincipal?: boolean;
}
