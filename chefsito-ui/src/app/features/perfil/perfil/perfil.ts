import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, CurrentUser } from '../../../core/auth/auth.service';
import {
  UserProfile,
  UserProfileService,
} from '../../despensa/user-profile.service';

type DietType = 'NONE' | 'VEGETARIAN' | 'VEGAN' | 'GLUTEN_FREE' | 'OTHER';
type SkillLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './perfil.html',
  styleUrls: ['./perfil.scss'],
})
export class PerfilComponent implements OnInit {
  loading = false;
  saving = false;
  error: string | null = null;

  currentUser: CurrentUser | null = null;

  profile: UserProfile = {
    userId: 0,
    allergies: [],
    intolerances: [],
    dislikedIngredients: [],
    dietType: 'NONE',
    cookingSkillLevel: 'BEGINNER',
  };

  dietOptions: { value: DietType; label: string }[] = [
    { value: 'NONE', label: 'Sin preferencia' },
    { value: 'VEGETARIAN', label: 'Vegetariana' },
    { value: 'VEGAN', label: 'Vegana' },
    { value: 'GLUTEN_FREE', label: 'Sin gluten' },
    { value: 'OTHER', label: 'Otra' },
  ];

  skillOptions: { value: SkillLevel; label: string }[] = [
    { value: 'BEGINNER', label: 'Principiante' },
    { value: 'INTERMEDIATE', label: 'Intermedio' },
    { value: 'ADVANCED', label: 'Avanzado' },
  ];

  languages = [
    { code: 'es', label: 'Español' },
    { code: 'en', label: 'English' },
  ];
  preferredLanguage = localStorage.getItem('preferredLanguage') || 'es';

  newAllergy = '';
  newIntolerance = '';
  newDisliked = '';

  constructor(
    private readonly authService: AuthService,
    private readonly userProfileService: UserProfileService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    const userId = this.authService.getUserId();
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.profile.userId = userId;
    this.loading = true;

    this.userProfileService.getByUserId(userId).subscribe({
      next: (p) => {
        this.profile = {
          ...this.profile,
          ...p,
          allergies: p.allergies || [],
          intolerances: p.intolerances || [],
          dislikedIngredients: p.dislikedIngredients || [],
        };
        this.loading = false;
      },
      error: (err) => {
        if (err?.status === 404) {
          // No existe perfil todavía: usamos el objeto por defecto
          this.loading = false;
          return;
        }
        console.error('Error cargando perfil', err);
        this.error = 'No se pudo cargar tu perfil';
        this.loading = false;
      },
    });
  }

  private addTag(list: string[], value: string): string[] {
    const trimmed = value.trim();
    if (!trimmed) return list;
    if (list.includes(trimmed)) return list;
    return [...list, trimmed];
  }

  private removeTag(list: string[], value: string): string[] {
    return list.filter((x) => x !== value);
  }

  addAllergy(): void {
    this.profile.allergies = this.addTag(this.profile.allergies, this.newAllergy);
    this.newAllergy = '';
  }

  removeAllergy(a: string): void {
    this.profile.allergies = this.removeTag(this.profile.allergies, a);
  }

  addIntolerance(): void {
    this.profile.intolerances = this.addTag(
      this.profile.intolerances,
      this.newIntolerance
    );
    this.newIntolerance = '';
  }

  removeIntolerance(i: string): void {
    this.profile.intolerances = this.removeTag(this.profile.intolerances, i);
  }

  addDisliked(): void {
    const list = this.profile.dislikedIngredients || [];
    this.profile.dislikedIngredients = this.addTag(list, this.newDisliked);
    this.newDisliked = '';
  }

  removeDisliked(d: string): void {
    const list = this.profile.dislikedIngredients || [];
    this.profile.dislikedIngredients = this.removeTag(list, d);
  }

  onLanguageChange(): void {
    localStorage.setItem('preferredLanguage', this.preferredLanguage);
    // Aquí en el futuro puedes integrar ngx-translate si quieres
  }

  save(): void {
    this.saving = true;
    this.error = null;

    this.userProfileService.createOrUpdate(this.profile).subscribe({
      next: (updated) => {
        this.profile = {
          ...this.profile,
          ...updated,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
        };
        this.saving = false;
      },
      error: (err) => {
        console.error('Error guardando perfil', err);
        this.error = 'No se pudo guardar tu perfil';
        this.saving = false;
      },
    });
  }
}
