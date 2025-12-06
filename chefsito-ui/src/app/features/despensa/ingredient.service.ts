import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Ingredient {
  id: number;
  name: string;
  nameEs?: string;
  category?: string;
  unit?: string;

  // URL principal que devuelve tu backend en smart-search
  imageUrl?: string | null;

  // Campos extra por si el backend usa otros nombres
  ingredientImageUrl?: string | null;
  image?: string | null;
}


export interface VoiceSearchResponse {
  searchText: string;
  language: string;
  ingredients: Ingredient[];
}

@Injectable({
  providedIn: 'root',
})
export class IngredientService {
  private http = inject(HttpClient);
  private readonly API_URL = '/api/ingredients';

  /**
   * BÚSQUEDA INTELIGENTE REAL
   * Usa smart-search (pollo, cúrcuma, especias, lo que sea)
   */
  search(
    query: string,
    limit = 10,
    language = 'es'
  ): Observable<Ingredient[]> {
    return this.http.get<Ingredient[]>(`${this.API_URL}/smart-search`, {
      params: { query, limit } as any,
      headers: {
        'Accept-Language': language,
      },
    });
  }

  /**
   * Búsqueda por voz — también inteligente
   */
  voiceSearch(
    text: string,
    language = 'es'
  ): Observable<VoiceSearchResponse> {
    return this.http.post<VoiceSearchResponse>(
      `${this.API_URL}/voice-search`,
      { text },
      {
        headers: {
          'Accept-Language': language,
        },
      }
    );
  }
}
