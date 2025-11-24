// src/app/features/recetas/recetas.service.ts
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Debe coincidir con OpenAiRecipeResponse.java
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

// Si algún día quieres llamar a /by-ingredients
export interface RecipeRequest {
  userId: number;
  availableIngredients: string[];
  preferences: {
    allergies: string[];
    intolerances: string[];
    dislikedIngredients: string[];
    dietType: string | null;
    cookingSkillLevel: string | null;
  } | null;
  maxRecipes: number;
}

@Injectable({
  providedIn: 'root',
})
export class RecetasService {
  private http = inject(HttpClient);

  // Usamos ruta relativa para que el proxy + interceptor funcionen
  private readonly API_URL = '/api/recommendations';

  /**
   * GET /api/recommendations/for-user/{userId}
   * Usa la despensa del usuario para generar recetas con OpenAI.
   */
  getForUser(userId: number): Observable<RecipeRecommendation[]> {
    return this.http.get<RecipeRecommendation[]>(
      `${this.API_URL}/for-user/${userId}`
    );
  }

  /**
   * POST /api/recommendations/by-ingredients
   * (por si quieres usarlo más adelante)
   */
  getByIngredients(request: RecipeRequest): Observable<RecipeRecommendation[]> {
    return this.http.post<RecipeRecommendation[]>(
      `${this.API_URL}/by-ingredients`,
      request
    );
  }
}

