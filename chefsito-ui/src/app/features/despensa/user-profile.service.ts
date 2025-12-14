
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface UserProfile {
  id?: number;
  userId: number;
  allergies: string[];
  intolerances: string[];
  dislikedIngredients: string[];
  dietType: string | null;
  cookingSkillLevel?: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class UserProfileService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/user-profile`;

  /**
   * Obtiene el perfil culinario por id de usuario.
   */
  getByUserId(userId: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/user/${userId}`);
  }

  /**
   * Crea o actualiza el perfil culinario.
   */
  createOrUpdate(profile: UserProfile): Observable<UserProfile> {
    return this.http.post<UserProfile>(this.API_URL, profile);
  }
}
