// src/app/features/despensa/user-profile.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserProfile {
  id?: number;             // opcional
  userId: number;
  allergies: string[];
  intolerances: string[];
}

@Injectable({
  providedIn: 'root',
})
export class UserProfileService {
  private baseUrl = '/api/user-profile';

  constructor(private http: HttpClient) {}

  getByUserId(userId: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/user/${userId}`);
  }

  /**
   * El backend tiene un POST que sirve tanto para crear como para actualizar.
   */
  createOrUpdate(profile: UserProfile): Observable<UserProfile> {
    return this.http.post<UserProfile>(this.baseUrl, profile);
  }
}
