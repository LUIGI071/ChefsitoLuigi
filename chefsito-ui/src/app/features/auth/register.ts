import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import {
  UserProfile,
  UserProfileService,
} from '../despensa/user-profile.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.scss'],
})
export class RegisterComponent {
  public form: FormGroup;

  public loading = false;
  public errorMessage: string | null = null;

  public dietOptions = [
    { value: '', label: 'Sin preferencia' },
    { value: 'VEGAN', label: 'Vegana' },
    { value: 'VEGETARIAN', label: 'Vegetariana' },
    { value: 'KETO', label: 'Keto' },
    { value: 'GLUTEN_FREE', label: 'Sin gluten' },
  ];

  constructor(
    public fb: FormBuilder,
    public authService: AuthService,
    public userProfileService: UserProfileService,
    public router: Router
  ) {
    this.form = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      dietType: [''],
      allergiesText: [''],
      intolerancesText: [''],
      dislikedText: [''],
    });
  }

  // Convierte texto → lista
  private parseList(value: string | null | undefined): string[] {
    if (!value) return [];
    return value
      .split(/[,;\n]/)
      .map(v => v.trim())
      .filter(v => v.length > 0);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const {
      fullName,
      email,
      password,
      confirmPassword,
      dietType,
      allergiesText,
      intolerancesText,
      dislikedText,
    } = this.form.value;

    if (password !== confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden.';
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    this.authService
      .register(email, password, fullName)
      .subscribe({
        next: (res) => {
          const userId: number = res.id;

          const profile: UserProfile = {
            userId,
            allergies: this.parseList(allergiesText),
            intolerances: this.parseList(intolerancesText),
            dislikedIngredients: this.parseList(dislikedText),
            dietType: dietType || null,
          };

          this.userProfileService.createOrUpdate(profile).subscribe({
            next: () => {
              this.loading = false;
              this.router.navigate(['/despensa']);
            },
            error: () => {
              this.loading = false;
              this.router.navigate(['/despensa']);
            },
          });
        },

        error: (err) => {
          console.error('❌ Error en registro', err);
          this.loading = false;

          this.errorMessage =
            err?.error?.message ||
            'No se pudo completar el registro. Inténtalo más tarde.';
        },
      });
  }
}
