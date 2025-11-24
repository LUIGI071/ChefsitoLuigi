// src/app/features/despensa/despensa.service.ts
import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// ==== MODELOS ====

export interface PantryItem {
  id: number;
  ingredientId: number;
  ingredientName?: string;
  ingredientNameEs?: string;
  quantity: number;
  unit: string | null;
}

export interface PantryItemCreateRequest {
  ingredientId: number;
  quantity: number;
  unit?: string;
}

// Si quieres usarlo en el futuro para buscar ingredientes desde aquí:
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

  /**
   * Listar los items de despensa del usuario actual.
   * Usado por this.despensaService.list() en el componente.
   */
  list(): Observable<PantryItem[]> {
    return this.http.get<PantryItem[]>(this.pantryUrl);
  }

  /**
   * Añadir un item a la despensa.
   * Usado por this.despensaService.addItem(nuevo).
   */
  addItem(req: PantryItemCreateRequest): Observable<PantryItem> {
    return this.http.post<PantryItem>(this.pantryUrl, req);
  }

  /**
   * Eliminar item de la despensa.
   * Usado por this.despensaService.deleteItem(item.id).
   */
  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.pantryUrl}/${id}`);
  }

  /**
   * Actualizar cantidad (por si luego quieres +/- en la UI).
   */
  updateQuantity(id: number, quantity: number): Observable<PantryItem> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<PantryItem>(`${this.pantryUrl}/${id}`, null, {
      params,
    });
  }

  /**
   * Búsqueda simple de ingredientes (opcional, por si quieres usarlo desde aquí).
   */
  searchIngredients(query: string): Observable<IngredientSummary[]> {
    const params = new HttpParams().set('query', query.trim());
    return this.http.get<IngredientSummary[]>(
      `${this.ingredientsUrl}/search`,
      { params }
    );
  }
}
