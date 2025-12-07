// src/app/features/despensa/recommendations.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

// Equivale a OpenAiRecipeResponse.java
export interface RecipeRecommendation {
  openAiId: string;
  title: string;
  description: string;
  ingredients: string[];
  instructions: string[];
  preparationTime: number | null;
  difficulty: string | null;
  category: string | null;
}

/**
 * Petición para obtener recomendaciones a partir de ingredientes
 * y/o preferencias del usuario.
 */
export interface RecipeRequest {
  userId?: number;
  ingredients?: string[];
  preferences?: string[];
}

@Injectable({
  providedIn: 'root',
})
export class RecommendationsService {
  private readonly http = inject(HttpClient);

  // Base API: https://.../api/recommendations
  private readonly API_URL = `${environment.apiBaseUrl}/recommendations`;

  /**
   * GET /api/recommendations/for-user/{userId}
   * Devuelve las recomendaciones del backend para el usuario.
   */
  getForUser(userId: number): Observable<RecipeRecommendation[]> {
    return this.http.get<RecipeRecommendation[]>(
      `${this.API_URL}/for-user/${userId}`
    );
  }

  /**
   * POST /api/recommendations/by-ingredients
   * (por si quieres mandar los ingredientes actuales de la despensa + preferencias)
   */
  getByIngredients(
    request: RecipeRequest
  ): Observable<RecipeRecommendation[]> {
    return this.http.post<RecipeRecommendation[]>(
      `${this.API_URL}/by-ingredients`,
      request
    );
  }
}
