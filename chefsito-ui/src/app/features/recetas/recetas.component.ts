import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import {
  RecetasService,
  RecipeRecommendation,
} from './recetas.service';

import { AuthService } from '../../core/auth/auth.service';
import {
  UserProfile,
  UserProfileService,
} from '../despensa/user-profile.service';

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

  sortMode: 'none' | 'timeAsc' | 'timeDesc' = 'none';
  difficultyFilter: 'ALL' | 'EASY' | 'MEDIUM' | 'HARD' = 'ALL';

  private userId: number | null = null;

  // receta seleccionada para el modal
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

    // De momento solo avisamos si no hay usuario, no redirigimos
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
      next: (profile) => {
        this.userProfile = profile;
      },
      error: (err) => {
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
      next: (data) => {
        this.recetas = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Error cargando recetas:', err);
        this.error = 'No se pudieron cargar las recetas recomendadas.';
        this.loading = false;
      },
    });
  }

  // =====================================
  //   HELPERS DIFICULTAD
  // =====================================

  /** Normaliza la dificultad a minúsculas y sin acentos */
  private normalizeDifficulty(value?: string | null): string {
    if (!value) return '';
    return value
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, ''); // quita acentos
  }

  private matchesDifficulty(
    recipeDifficulty: string | null | undefined,
    filter: 'ALL' | 'EASY' | 'MEDIUM' | 'HARD'
  ): boolean {
    // "Todas" siempre pasa el filtro
    if (filter === 'ALL') return true;

    const d = this.normalizeDifficulty(recipeDifficulty); // 'facil', 'medio', 'dificil', 'easy', 'medium', 'hard'

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

  // =====================================
  //   ORDEN / FILTRO
  // =====================================

  getFilteredAndSortedRecipes(): RecipeRecommendation[] {
    let list = [...this.recetas];

    // Filtro por dificultad (incluye ALL porque matchesDifficulty lo maneja)
    list = list.filter((r) =>
      this.matchesDifficulty(r.difficulty, this.difficultyFilter)
    );

    // Orden por tiempo
    if (this.sortMode === 'timeAsc') {
      list.sort(
        (a, b) => (a.preparationTime ?? 9999) - (b.preparationTime ?? 9999)
      );
    } else if (this.sortMode === 'timeDesc') {
      list.sort(
        (a, b) => (b.preparationTime ?? 0) - (a.preparationTime ?? 0)
      );
    }

    return list;
  }

  onChangeSort(mode: 'none' | 'timeAsc' | 'timeDesc'): void {
    this.sortMode = mode;
  }

  onChangeDifficulty(filter: 'ALL' | 'EASY' | 'MEDIUM' | 'HARD'): void {
    this.difficultyFilter = filter;
  }

  // =====================================
  //   ALERGIAS
  // =====================================

  getAllergensForRecipe(r: RecipeRecommendation): string[] {
    if (
      !this.userProfile ||
      !this.userProfile.allergies?.length ||
      !r.ingredients?.length
    ) {
      return [];
    }

    const ingredientsText = r.ingredients.join(' ').toLowerCase();

    return this.userProfile.allergies.filter((allergy) =>
      ingredientsText.includes(allergy.toLowerCase())
    );
  }

  // =====================================
  //   MODAL DETALLE RECETA
  // =====================================

  verDetalles(receta: RecipeRecommendation): void {
    this.selectedRecipe = receta;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cerrarDetalles(): void {
    this.selectedRecipe = null;
  }
}
