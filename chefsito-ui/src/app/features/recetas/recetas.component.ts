
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import {
  RecetasService,
  RecipeRecommendation,
} from './recetas.service';

//  rutas corregidas
import { AuthService } from '../../core/auth/auth.service';
import {
  UserProfile,
  UserProfileService,
} from '../despensa/user-profile.service';

type DifficultyFilter = 'ALL' | 'EASY' | 'MEDIUM' | 'HARD';
type SortMode = 'none' | 'timeAsc' | 'timeDesc';

@Component({
  selector: 'app-recetas',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './recetas.html',
  styleUrls: ['./recetas.scss'],
})
export class RecetasComponent implements OnInit {
  loading = false;
  error: string | null = null;

  recetas: RecipeRecommendation[] = [];

  userProfile: UserProfile | null = null;

  sortMode: SortMode = 'none';
  difficultyFilter: DifficultyFilter = 'ALL';

  private userId: number | null = null;

  // Receta seleccionada para el modal de detalle
  selectedRecipe: RecipeRecommendation | null = null;

  constructor(
    private readonly recetasService: RecetasService,
    private readonly authService: AuthService,
    private readonly userProfileService: UserProfileService
  ) {}

  // =====================================
  //   CICLO DE VIDA
  // =====================================

  ngOnInit(): void {
    this.userId = this.authService.getUserId();

    if (!this.userId) {
      console.warn(
        'No hay userId en AuthService. Haz login antes de venir a /recetas para usar el backend real.'
      );
      return;
    }

    this.cargarPerfilUsuario();
    this.cargarRecetas();
  }

  cargarPerfilUsuario(): void {
    if (!this.userId) return;

    this.userProfileService.getByUserId(this.userId).subscribe({
      next: (profile: UserProfile) => {
        this.userProfile = {
          userId: profile.userId,
          allergies: profile.allergies || [],
          intolerances: profile.intolerances || [],
          dislikedIngredients: profile.dislikedIngredients || [],
          dietType: profile.dietType || null,
          cookingSkillLevel: profile.cookingSkillLevel || null,
        };
      },
      error: (err: any) => {
        if (err?.status === 404) {
          this.userProfile = null;
          return;
        }
        console.error('❌ Error cargando perfil de usuario:', err);
      },
    });
  }

  cargarRecetas(): void {
    if (!this.userId) return;

    this.loading = true;
    this.error = null;

    this.recetasService.getForUser(this.userId).subscribe({
      next: (data: RecipeRecommendation[]) => {
        this.recetas = data || [];
        this.loading = false;
      },
      error: (err: any) => {
        console.error('❌ Error cargando recetas:', err);
        this.error =
          'No se pudieron cargar las recetas recomendadas.';
        this.loading = false;
      },
    });
  }

  // =====================================
  //   HELPERS TEXTO
  // =====================================

  private normalizeText(value?: string | null): string {
    if (!value) return '';
    return value
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  /**
   *  filtra por dieta, gustos y alergias.
   */
  private getCombinedIngredientsText(r: RecipeRecommendation): string {
    const list: string[] = [];

    if (Array.isArray(r.ingredients)) {
      list.push(...r.ingredients);
    }

    if (Array.isArray(r.ingredientNames)) {
      list.push(...r.ingredientNames);
    }

    return list.join(' ');
  }

  getShortDescription(r: RecipeRecommendation): string {
    const text = r.description || '';
    const limit = 140;
    if (text.length <= limit) return text;
    return text.slice(0, limit).trimEnd() + '…';
  }

  // =====================================
  //   DIFICULTAD
  // =====================================

  private matchesDifficulty(
    recipeDifficulty: string | null | undefined,
    filter: DifficultyFilter
  ): boolean {
    if (filter === 'ALL') return true;

    const d = this.normalizeText(recipeDifficulty);

    switch (filter) {
      case 'EASY':
        return d === 'easy' || d === 'facil';
      case 'MEDIUM':
        return d === 'medium' || d === 'medio';
      case 'HARD':
        return d === 'hard' || d === 'dificil';
      default:
        return false;
    }
  }

  formatDifficulty(difficulty: string | null): string {
    const d = this.normalizeText(difficulty);
    if (!d) return 'Sin dato';

    if (d === 'easy' || d === 'facil') return 'fácil';
    if (d === 'medium' || d === 'medio') return 'media';
    if (d === 'hard' || d === 'dificil') return 'difícil';

    return difficulty || 'Sin dato';
  }


  private recipeMatchesDiet(r: RecipeRecommendation): boolean {
    if (!this.userProfile?.dietType) return true;

    const diet = this.normalizeText(this.userProfile.dietType);
    const ingredientsText = this.getCombinedIngredientsText(r);

    const text = this.normalizeText(
      `${r.title} ${r.description} ${ingredientsText}`
    );

    // Vegana
    if (diet.includes('vegan')) {
      if (
        /(carne|pollo|cerdo|ternera|jamon|jamón|pavo|chorizo|lomo|panceta)/.test(
          text
        )
      )
        return false;
      if (
        /(pescado|atun|atún|salmon|salmón|merluza|marisco|gamba|pulpo|calamar)/.test(
          text
        )
      )
        return false;
      if (
        /(leche|queso|yogur|yogurt|nata|mantequilla)/.test(
          text
        )
      )
        return false;
      if (/(huevo|huevos)/.test(text)) return false;
    }

    // Vegetariana
    if (diet.includes('vegetar')) {
      if (
        /(carne|pollo|cerdo|ternera|jamon|jamón|pavo|chorizo|lomo|panceta)/.test(
          text
        )
      )
        return false;
      if (
        /(pescado|atun|atún|salmon|salmón|merluza|marisco|gamba|pulpo|calamar)/.test(
          text
        )
      )
        return false;
    }

    // Sin gluten
    if (diet.includes('gluten')) {
      if (
        /(pan|harina|trigo|cuscus|cús-cús|pasta|espagueti|espaguetti|fideo)/.test(
          text
        )
      )
        return false;
    }

    // Keto
    if (diet.includes('keto')) {
      if (
        /(azucar|azúcar|pasta|espagueti|espaguetti|pan|arroz|dulce|pastel|tarta)/.test(
          text
        )
      )
        return false;
    }

    return true;
  }

  private recipeRespectsDislikes(
    r: RecipeRecommendation
  ): boolean {
    if (
      !this.userProfile?.dislikedIngredients ||
      this.userProfile.dislikedIngredients.length === 0
    ) {
      return true;
    }

    const ingredientsText = this.getCombinedIngredientsText(r);

    const text = this.normalizeText(
      `${r.title} ${r.description} ${ingredientsText}`
    );

    return !this.userProfile.dislikedIngredients.some(
      (d: string) => {
        const dNorm = this.normalizeText(d);
        return dNorm && text.includes(dNorm);
      }
    );
  }

  getFilteredAndSortedRecipes(): RecipeRecommendation[] {
    let list = [...this.recetas];

    // Filtrado por dieta y gustos
    list = list.filter((r) => this.recipeMatchesDiet(r));
    list = list.filter((r) => this.recipeRespectsDislikes(r));

    // Filtro por dificultad
    list = list.filter((r) =>
      this.matchesDifficulty(r.difficulty, this.difficultyFilter)
    );

    // Orden por tiempo
    if (this.sortMode === 'timeAsc') {
      list.sort(
        (a, b) =>
          (a.preparationTime ?? 9999) -
          (b.preparationTime ?? 9999)
      );
    } else if (this.sortMode === 'timeDesc') {
      list.sort(
        (a, b) =>
          (b.preparationTime ?? 0) -
          (a.preparationTime ?? 0)
      );
    }

    return list;
  }

  //  Getter para usar en el HTML y poder saber si hay recetas visibles
  get visibleRecipes(): RecipeRecommendation[] {
    return this.getFilteredAndSortedRecipes();
  }

  onChangeSort(mode: SortMode): void {
    this.sortMode = mode;
  }

  onChangeDifficulty(filter: DifficultyFilter): void {
    this.difficultyFilter = filter;
  }

  // =====================================
  //   ALERGIAS
  // =====================================

  getAllergensForRecipe(
    r: RecipeRecommendation
  ): string[] {
    if (
      !this.userProfile ||
      !this.userProfile.allergies?.length ||
      (!r.ingredients?.length && !r.ingredientNames?.length)
    ) {
      return [];
    }

    const ingredientsText = this.normalizeText(
      this.getCombinedIngredientsText(r)
    );

    return this.userProfile.allergies.filter(
      (allergy: string) => {
        const aNorm = this.normalizeText(allergy);
        return aNorm && ingredientsText.includes(aNorm);
      }
    );
  }



  verDetalles(receta: RecipeRecommendation): void {
    this.selectedRecipe = receta;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cocinarAhora(receta: RecipeRecommendation): void {
    this.verDetalles(receta);
  }

  cerrarDetalles(): void {
    this.selectedRecipe = null;
  }
}
