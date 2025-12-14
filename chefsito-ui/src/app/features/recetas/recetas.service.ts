import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RecipeRecommendation {
  openAiId: string;
  title: string;
  description: string;
  ingredients: string[];
  instructions: string[];
  preparationTime: number | null;
  difficulty: string | null;
  category: string | null;

  /**
   * Lista de nombres de ingredientes generada por IA
   */
  ingredientNames?: string[] | null;

  // Opcional: para poder usar r.imageUrl en recetas.component.ts
  imageUrl?: string | null;
}



@Injectable({
  providedIn: 'root',
})
export class RecetasService {
  private http = inject(HttpClient);

  // En prod -> https://chefsito-backend.onrender.com/api/recommendations
  // En dev  -> http://localhost:8080/api/recommendations
  private readonly API_URL = `${environment.apiBaseUrl}/recommendations`;

  /**
   * Usa la despensa del usuario para generar recetas con OpenAI.
   */
  getForUser(userId: number): Observable<RecipeRecommendation[]> {
    return this.http.get<RecipeRecommendation[]>(
      `${this.API_URL}/for-user/${userId}`
    );
  }
}
