import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

// Equivale a OpenAiRecipeRequest.java + UserPreferencesDto.java
export interface UserPreferencesDto {
  allergies: string[];
  intolerances: string[];
  dislikedIngredients: string[];
  dietType: string | null;
  cookingSkillLevel: string | null;
}

export interface RecipeRequest {
  userId: number;
  availableIngredients: string[];
  preferences: UserPreferencesDto;
  maxRecipes: number;
}

@Injectable({
  providedIn: 'root',
})
export class RecommendationsService {
  private readonly API_URL = '/api/recommendations';

  constructor(private http: HttpClient) {}

  // GET /api/recommendations/for-user/{userId}
  getForUser(userId: number): Observable<RecipeRecommendation[]> {
    return this.http.get<RecipeRecommendation[]>(
      `${this.API_URL}/for-user/${userId}`
    );
  }

  // POST /api/recommendations/by-ingredients
  // (por si quieres mandar los ingredientes actuales de la despensa + preferencias)
  getByIngredients(request: RecipeRequest): Observable<RecipeRecommendation[]> {
    return this.http.post<RecipeRecommendation[]>(
      `${this.API_URL}/by-ingredients`,
      request
    );
  }
}
