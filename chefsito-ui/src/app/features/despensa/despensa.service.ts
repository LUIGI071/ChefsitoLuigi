// src/app/features/despensa/despensa.service.ts
import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// ==== MODELOS ====

export interface PantryItem {
  id: number;
  ingredientId: number;

  // nombres que se usan en el componente (líneas 220, 722, 723)
  ingredientName?: string;
  ingredientNameEs?: string;

  quantity: number;
  unit: string | null;

  // 🔹 Campo requerido por el HTML (evita error en Render si no hay imagen)
  ingredientImageUrl?: string | null;
}

export interface PantryItemCreateRequest {
  ingredientId: number;
  quantity: number;
  unit?: string;
}

export interface IngredientSummary {
  id: number;
  name: string;
}

// ==== SERVICIO ====

@Injectable({
  providedIn: 'root',
})
export class DespensaService {
  private http = inject(HttpClient);

  // Base URLs del backend
  private readonly pantryUrl = `${environment.apiBaseUrl}/pantry`;
  private readonly ingredientsUrl = `${environment.apiBaseUrl}/ingredients`;

  /** Listar items de la despensa */
  list(): Observable<PantryItem[]> {
    return this.http.get<PantryItem[]>(this.pantryUrl);
  }

  /** Añadir item */
  addItem(req: PantryItemCreateRequest): Observable<PantryItem> {
    return this.http.post<PantryItem>(this.pantryUrl, req);
  }

  /** Eliminar item */
  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.pantryUrl}/${id}`);
  }

  /** Actualizar cantidad */
  updateQuantity(id: number, quantity: number): Observable<PantryItem> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<PantryItem>(`${this.pantryUrl}/${id}`, null, {
      params,
    });
  }

  /** Buscar ingredientes */
  searchIngredients(query: string): Observable<IngredientSummary[]> {
    const params = new HttpParams().set('query', query.trim());
    return this.http.get<IngredientSummary[]>(
      `${this.ingredientsUrl}/search`,
      { params }
    );
  }
}
