import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';  // 👈 AQUÍ EL CAMBIO

export interface Ingredient {
  id: number;
  name: string;
  nameEs?: string;
  category?: string;
  unit?: string;
  imageUrl?: string;
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

  // OJO: en tu environment se llama apiBaseUrl, no apiUrl
  private readonly API_URL = `${environment.apiBaseUrl}/ingredients`;

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
