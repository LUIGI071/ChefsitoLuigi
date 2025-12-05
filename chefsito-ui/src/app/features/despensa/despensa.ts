// src/app/features/despensa/despensa.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  DespensaService,
  PantryItem,
  PantryItemCreateRequest,
} from './despensa.service';

import {
  IngredientService,
  Ingredient,
} from './ingredient.service';

import {
  UserProfile,
  UserProfileService,
} from './user-profile.service';

import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-despensa',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './despensa.html',
  styleUrls: ['./despensa.scss'],
})
export class DespensaComponent implements OnInit {
  // ===== ESTADO DESPENSA =====
  loading = false;
  error: string | null = null;
  alimentos: PantryItem[] = [];

  // Confirmación de borrado
  confirmDeleteId: number | null = null;

  // ===== BÚSQUEDA INGREDIENTES =====
  searchText = '';
  searching = false;
  searchResults: Ingredient[] = [];

  // ===== VOZ =====
  listening = false;

  // ===== PERFIL / ALERGIAS =====
  userProfile: UserProfile | null = null;
  allergySearchText = '';
  allergySearchResults: Ingredient[] = [];

  constructor(
    private despensaService: DespensaService,
    private ingredientService: IngredientService,
    private userProfileService: UserProfileService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.cargarAlimentos();
    this.cargarPerfilUsuario();
  }

  // ============================
  //   DESPENSA
  // ============================

  cargarAlimentos(): void {
    this.loading = true;
    this.error = null;

    this.despensaService.list().subscribe({
      next: (data: PantryItem[]) => {
        this.alimentos = data;
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo cargar la despensa';
        this.loading = false;
      },
    });
  }

  // AÑADIR (mock)
  onAddMock(): void {
    const nuevo: PantryItemCreateRequest = {
      ingredientId: 1,
      quantity: 1,
    };

    this.despensaService.addItem(nuevo).subscribe({
      next: (creado: PantryItem) => {
        this.alimentos = [...this.alimentos, creado];
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo añadir el alimento';
      },
    });
  }

  // AÑADIR desde buscador
  addIngredientToPantry(ing: Ingredient): void {
    const nuevo: PantryItemCreateRequest = {
      ingredientId: ing.id,
      quantity: 1,
    };

    this.despensaService.addItem(nuevo).subscribe({
      next: (creado: PantryItem) => {
        this.alimentos = [...this.alimentos, creado];
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo añadir el ingrediente a la despensa';
      },
    });
  }

  // ===== ELIMINAR con confirmación bonita =====

  // mostrar confirmación
  askDelete(item: PantryItem): void {
    this.confirmDeleteId = item.id;
  }

  // cancelar confirmación
  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  // ejecutar borrado (sin window.confirm)
  onDelete(item: PantryItem): void {
    this.confirmDeleteId = null;

    this.despensaService.deleteItem(item.id).subscribe({
      next: () => {
        this.alimentos = this.alimentos.filter(a => a.id !== item.id);
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo eliminar el alimento';
      },
    });
  }

  // CAMBIAR CANTIDAD (+ / -)
  changeQuantity(item: PantryItem, delta: number): void {
    const nuevaCantidad = item.quantity + delta;

    // si baja a 0 o menos, lo eliminamos
    if (nuevaCantidad <= 0) {
      this.onDelete(item);
      return;
    }

    this.despensaService.updateQuantity(item.id, nuevaCantidad).subscribe({
      next: (actualizado: PantryItem) => {
        this.alimentos = this.alimentos.map(a =>
          a.id === item.id ? { ...a, quantity: actualizado.quantity } : a
        );
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo actualizar la cantidad';
      },
    });
  }

  // ============================
  //   BÚSQUEDA NORMAL
  // ============================

  onSearch(): void {
    if (!this.searchText.trim()) {
      this.searchResults = [];
      return;
    }

    this.searching = true;
    this.ingredientService.search(this.searchText.trim(), 10).subscribe({
      next: (results: Ingredient[]) => {
        this.searchResults = results;
        this.searching = false;
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'Error buscando ingredientes';
        this.searching = false;
      },
    });
  }

  // ============================
  //   BÚSQUEDA POR VOZ
  // ============================

  onVoiceSearch(): void {
    const SpeechRecognition =
      (window as any).SpeechRecognition ||
      (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      alert('Tu navegador no soporta reconocimiento de voz');
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'es-ES';
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;

    this.listening = true;
    recognition.start();

    recognition.onresult = (event: any) => {
      const text = event.results[0][0].transcript;
      this.searchText = text;
      this.listening = false;

      this.ingredientService.voiceSearch(text, 'es').subscribe({
        next: (res: any) => {
          this.searchResults = res.ingredients;
        },
        error: (err: unknown) => {
          console.error(err);
          this.error = 'Error en búsqueda por voz';
        },
      });
    };

    recognition.onerror = (event: any) => {
      console.error('Speech recognition error', event);
      this.listening = false;
    };

    recognition.onend = () => {
      this.listening = false;
    };
  }

  // ============================
  //   PERFIL / ALERGIAS
  // ============================

  private cargarPerfilUsuario(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      console.warn('No hay userId en AuthService, no se carga el perfil de usuario.');
      return;
    }

    this.userProfileService.getByUserId(userId).subscribe({
      next: (profile: UserProfile) => {
        this.userProfile = {
          ...profile,
          allergies: profile.allergies || [],
          intolerances: profile.intolerances || [],
          dislikedIngredients: profile.dislikedIngredients || [],
        };
      },
      error: (err: any) => {
        if (err?.status === 404) {
          // No hay perfil aún -> lo crearemos cuando se guarde la primera alergia
          this.userProfile = {
            userId,
            allergies: [],
            intolerances: [],
            dislikedIngredients: [],
            dietType: null,
          };
          return;
        }
        console.error('Error cargando perfil de usuario', err);
      },
    });
  }

  // PANEL "MIS ALERGIAS"
  buscarAlergia(): void {
    const term = this.allergySearchText.trim();
    if (!term) {
      this.allergySearchResults = [];
      return;
    }

    this.ingredientService.search(term, 10).subscribe({
      next: (results: Ingredient[]) => {
        this.allergySearchResults = results || [];
      },
      error: (err: unknown) => {
        console.error('Error buscando alérgenos', err);
        this.allergySearchResults = [];
      },
    });
  }

  agregarAlergia(ing: Ingredient): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    if (!this.userProfile) {
      this.userProfile = {
        userId,
        allergies: [],
        intolerances: [],
        dislikedIngredients: [],
        dietType: null,
      };
    }

    const nombre = ing.nameEs || ing.name;
    if (!this.userProfile.allergies.includes(nombre)) {
      this.userProfile.allergies.push(nombre);
    }

    this.userProfileService.createOrUpdate(this.userProfile).subscribe({
      next: (updated: UserProfile) => {
        this.userProfile = {
          ...updated,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
        };
      },
      error: (err: unknown) => {
        console.error('Error guardando alergia', err);
      },
    });

    this.allergySearchText = '';
    this.allergySearchResults = [];
  }

  eliminarAlergia(a: string): void {
    if (!this.userProfile) return;

    this.userProfile.allergies = this.userProfile.allergies.filter(x => x !== a);

    this.userProfileService.createOrUpdate(this.userProfile).subscribe({
      next: (updated: UserProfile) => {
        this.userProfile = {
          ...updated,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
        };
      },
      error: (err: unknown) => {
        console.error('Error eliminando alergia', err);
      },
    });
  }

  // Badge de alergia en cada item de la despensa
  hasAllergyForItem(item: PantryItem): string[] {
    if (!this.userProfile?.allergies?.length) return [];

    const name = (
      item.ingredientNameEs ||
      item.ingredientName ||
      ''
    ).toLowerCase();

    return this.userProfile.allergies.filter(a =>
      name.includes(a.toLowerCase())
    );
  }
}
