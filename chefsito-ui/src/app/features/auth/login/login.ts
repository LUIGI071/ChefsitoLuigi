// src/app/features/auth/login/login.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  FormGroup,
  FormControl,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class LoginComponent {
  // Formulario tipado
  form!: FormGroup<{
    email: FormControl<string | null>;
    password: FormControl<string | null>;
    rememberMe: FormControl<boolean | null>;
  }>;

  loading = false;
  errorMessage: string | null = null;
  passwordVisible = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private fb: FormBuilder
  ) {
    // 📌 Validación cambiada a minlength(1)
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: [
        '',
        [Validators.required, Validators.minLength(1)], // <--- AQUÍ EL CAMBIO
      ],
      rememberMe: [true],
    }) as FormGroup<{
      email: FormControl<string | null>;
      password: FormControl<string | null>;
      rememberMe: FormControl<boolean | null>;
    }>;
  }

  // ============================
  // Getters usados en el HTML
  // ============================
  get email() {
    return this.form.get('email');
  }

  get password() {
    return this.form.get('password');
  }

  togglePasswordVisibility(): void {
    this.passwordVisible = !this.passwordVisible;
  }

  // ============================
  // Submit del formulario
  // ============================
  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { email, password } = this.form.value;

    if (!email || !password) {
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    this.authService.login(email, password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/despensa']);
      },
      error: (err) => {
        console.error('❌ Error en login', err);
        this.loading = false;
        this.errorMessage =
          err?.error?.message ||
          'Correo o contraseña incorrectos. Vuelve a intentarlo.';
      },
    });
  }
}
