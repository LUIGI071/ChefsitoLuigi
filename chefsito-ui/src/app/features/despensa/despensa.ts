// src/app/features/despensa/despensa.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

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
  imports: [CommonModule, FormsModule, RouterLink, NgClass],
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

  // Categorías
  categoryFilter: string = 'ALL';
  availableCategories: string[] = ['ALL'];

  // ===== BÚSQUEDA INGREDIENTES =====
  searchText = '';
  searching = false;
  searchResults: Ingredient[] = [];

  // ===== VOZ =====
  listening = false;
  private recognition: any = null;
  voiceSupported = false;
  /** Texto en vivo que se muestra mientras hablas */
  liveTranscript = '';
  /** Texto final reconocido (solo segmentos finales, sin parciales) */
  private recognizedText = '';

  // ===== PERFIL USUARIO =====
  userProfile: UserProfile | null = null;
  userName: string | null = null;

  // Alergias
  allergySearchText = '';
  allergySearchResults: Ingredient[] = [];

  // Ingredientes que no le gustan
  dislikedInput = '';

  // Sugerencias base y filtradas para "Ingredientes que no me gustan"
  dislikedSuggestions: string[] = [
    'aceitunas',
    'pepino',
    'cilantro',
    'cebolla cruda',
    'ajo',
    'pimiento verde',
    'pimiento rojo',
    'brócoli',
    'coliflor',
    'hígado',
    'marisco',
    'picante',
  ];
  filteredDislikedSuggestions: string[] = [];

  // 👉 Diccionario provisional de traducciones EN → ES solo en el front
  private ingredientTranslations: Record<string, string> = {
    // pollo y derivados
    chicken: 'Pollo',
    'chicken leg': 'Pierna de pollo',
    'chicken legs': 'Piernas de pollo',
    'chicken thigh': 'Muslo de pollo',
    'chicken thighs': 'Muslos de pollo',
    'chicken breast': 'Pechuga de pollo',
    'chicken breasts': 'Pechugas de pollo',
    'chicken liver': 'Hígado de pollo',
    'chicken stock': 'Caldo de pollo',
    'chicken stock cube': 'Cubito de caldo de pollo',
    'chicken drumstick': 'Muslo de pollo',
    'chicken drumsticks': 'Muslos de pollo',

    // básicos frecuentes
    beef: 'Ternera',
    pork: 'Cerdo',
    egg: 'Huevo',
    eggs: 'Huevos',
    onion: 'Cebolla',
    garlic: 'Ajo',
    milk: 'Leche',
    butter: 'Mantequilla',
    bread: 'Pan',
  };

  // Barra lateral (menú hamburguesa)
  sidebarOpen = false;

  constructor(
    private despensaService: DespensaService,
    private ingredientService: IngredientService,
    private userProfileService: UserProfileService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.cargarAlimentos();
    this.cargarPerfilUsuario();
    this.initSpeechRecognition();

    // Intentamos obtener el nombre del usuario si el AuthService lo expone
    const anyAuth = this.authService as any;
    if (anyAuth.getUserName) {
      this.userName = anyAuth.getUserName();
    }
  }

  // ============================
  //   HELPERS NOMBRES (TRADUCCIÓN FRONT)
  // ============================

  /**
   * Devuelve un nombre en español a partir de nameEs y name,
   * usando el diccionario local si hace falta.
   */
  private getDisplayNameFromPair(
    nameEs?: string | null,
    name?: string | null
  ): string {
    // 1) Si ya viene en español desde backend, respetamos
    if (nameEs && nameEs.trim()) {
      return nameEs;
    }

    // 2) Miramos en el diccionario EN → ES
    const raw = (name || '').toLowerCase().trim();
    if (raw && this.ingredientTranslations[raw]) {
      return this.ingredientTranslations[raw];
    }

    // 3) Si no hay nada mejor, devolvemos el name original
    return name || '';
  }

  /** Para objetos Ingredient (búsqueda, alergias, etc.) */
  getIngredientDisplayName(ing: Ingredient): string {
    return this.getDisplayNameFromPair(ing.nameEs, ing.name);
  }

  /** Para objetos PantryItem (lo que ya está en la despensa) */
  getPantryItemDisplayName(item: PantryItem): string {
    const anyItem: any = item as any;
    return this.getDisplayNameFromPair(
      anyItem.ingredientNameEs,
      anyItem.ingredientName
    );
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
        this.rebuildCategories();
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo cargar la despensa';
        this.loading = false;
      },
    });
  }

  // AÑADIR (mock de ejemplo, por si lo quieres usar)
  onAddMock(): void {
    const nuevo: PantryItemCreateRequest = {
      ingredientId: 1,
      quantity: 1,
    };

    this.despensaService.addItem(nuevo).subscribe({
      next: (creado: PantryItem) => {
        // 👉 nuevo arriba del todo
        this.alimentos = [creado, ...this.alimentos];
        this.rebuildCategories();
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo añadir el alimento';
      },
    });
  }

  // AÑADIR desde buscador
  addIngredientToPantry(ing: Ingredient): void {
    if (!ing || ing.id == null) {
      this.error = 'Ingrediente no válido (sin id).';
      return;
    }

    const nuevo: PantryItemCreateRequest = {
      ingredientId: ing.id,
      quantity: 1,
    };

    this.despensaService.addItem(nuevo).subscribe({
      next: (creado: PantryItem) => {
        // 👉 lo metemos al principio para que se vea arriba
        this.alimentos = [creado, ...this.alimentos];
        this.rebuildCategories();
        this.error = null;
      },
      error: (err: any) => {
        console.error('Error añadiendo ingrediente', err);
        this.error =
          err?.error?.message ||
          `No se pudo añadir el ingrediente a la despensa (código ${
            err?.status ?? 'desconocido'
          })`;
      },
    });
  }

  // ===== ELIMINAR con confirmación =====

  askDelete(item: PantryItem): void {
    this.confirmDeleteId = item.id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  onDelete(item: PantryItem): void {
    this.confirmDeleteId = null;

    this.despensaService.deleteItem(item.id).subscribe({
      next: () => {
        this.alimentos = this.alimentos.filter((a) => a.id !== item.id);
        this.rebuildCategories();
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

    if (nuevaCantidad <= 0) {
      this.onDelete(item);
      return;
    }

    this.despensaService.updateQuantity(item.id, nuevaCantidad).subscribe({
      next: (actualizado: PantryItem) => {
        this.alimentos = this.alimentos.map((a) =>
          a.id === item.id ? { ...a, quantity: actualizado.quantity } : a
        );
        this.rebuildCategories();
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'No se pudo actualizar la cantidad';
      },
    });
  }

  // ============================
  //   CATEGORÍAS
  // ============================

  private getCategoryForItemInternal(item: PantryItem): string {
    const anyItem: any = item as any;
    const rawName =
      anyItem.ingredientNameEs || anyItem.ingredientName || '';
    const name = rawName.toLowerCase();

    // Tokenizamos: "repollo chino" -> ["repollo", "chino"]
    const tokens: string[] = name
      .split(/[\s,;.\-]+/)
      .filter((w: string) => !!w);

    const hasToken = (candidates: string[]): boolean =>
      tokens.some((t: string) => candidates.includes(t));

    // Verduras
    if (
      hasToken([
        'tomate',
        'lechuga',
        'zanahoria',
        'cebolla',
        'patata',
        'papa',
        'pimiento',
        'pepino',
        'col',
        'repollo',
        'brocoli',
        'brócoli',
        'espinaca',
        'espinacas',
        'calabacin',
        'calabacín',
        'berenjena',
        'ajo',
        'cebollino',
        'apio',
      ])
    ) {
      return 'Verduras';
    }

    // Frutas
    if (
      hasToken([
        'manzana',
        'plátano',
        'platano',
        'naranja',
        'pera',
        'fresa',
        'limón',
        'limon',
        'uva',
        'melón',
        'melon',
        'kiwi',
        'piña',
        'mango',
      ])
    ) {
      return 'Frutas';
    }

    // Lácteos
    if (
      hasToken([
        'leche',
        'queso',
        'yogur',
        'yogurt',
        'mantequilla',
        'nata',
      ])
    ) {
      return 'Lácteos';
    }

    // Cereales / harinas / panes
    if (
      hasToken([
        'arroz',
        'pasta',
        'espagueti',
        'espaguetti',
        'pan',
        'harina',
        'cuscus',
        'couscous',
        'cereal',
        'fideo',
      ])
    ) {
      return 'Cereales';
    }

    // Aceites
    if (
      hasToken(['aceite', 'oliva', 'girasol', 'margarina'])
    ) {
      return 'Aceites';
    }

    // Especias
    if (
      hasToken([
        'sal',
        'azucar',
        'azúcar',
        'pimienta',
        'curcuma',
        'cúrcuma',
        'oregano',
        'orégano',
        'comino',
        'paprika',
      ])
    ) {
      return 'Especias';
    }

    // Carnes
    if (
      hasToken([
        'carne',
        'pollo',
        'solomillo',
        'ternera',
        'cerdo',
        'jamon',
        'jamón',
        'pavo',
        'chorizo',
        'lomo',
        'panceta',
        'salchicha',
      ])
    ) {
      return 'Carnes';
    }

    // Pescados y mariscos
    if (
      hasToken([
        'pescado',
        'atun',
        'atún',
        'salmon',
        'salmón',
        'merluza',
        'marisco',
        'gamba',
        'gambas',
        'pulpo',
        'calamar',
      ])
    ) {
      return 'Pescados';
    }

    return 'Otros';
  }

  getCategoryForItem(item: PantryItem): string {
    return this.getCategoryForItemInternal(item);
  }

  private rebuildCategories(): void {
    const cats = new Set<string>();
    this.alimentos.forEach((item) => {
      cats.add(this.getCategoryForItemInternal(item));
    });
    this.availableCategories = ['ALL', ...Array.from(cats)];

    if (!this.availableCategories.includes(this.categoryFilter)) {
      this.categoryFilter = 'ALL';
    }
  }

  getAlimentosFiltrados(): PantryItem[] {
    if (this.categoryFilter === 'ALL') {
      return this.alimentos;
    }
    return this.alimentos.filter(
      (item) =>
        this.getCategoryForItemInternal(item) === this.categoryFilter
    );
  }

  // ============================
  //   BÚSQUEDA NORMAL
  // ============================

  onSearch(): void {
    const q = this.searchText.trim();
    if (!q) {
      this.searchResults = [];
      return;
    }

    this.searching = true;
    this.ingredientService.search(q, 10, 'es').subscribe({
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

  private initSpeechRecognition(): void {
    const w = window as any;
    const SpeechRecognition =
      w.SpeechRecognition || w.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      console.warn('SpeechRecognition NO soportado en este navegador');
      this.voiceSupported = false;
      return;
    }

    console.log('SpeechRecognition soportado, inicializando...');
    this.voiceSupported = true;
    this.recognition = new SpeechRecognition();
    this.recognition.lang = 'es-ES';
    this.recognition.interimResults = true;

    this.recognition.onstart = () => {
      this.listening = true;
      this.liveTranscript = '';
      this.searchText = '';
      this.recognizedText = '';
      console.log('Reconocimiento de voz: START');
    };

    this.recognition.onend = () => {
      console.log('Reconocimiento de voz: END');
      this.listening = false;
      // Cuando deja de escuchar, usamos el texto final reconocido
      this.voiceSearchFromText();
    };

    this.recognition.onerror = (event: any) => {
      console.error('SpeechRecognition error', event);
      this.listening = false;
    };

    this.recognition.onresult = (event: any) => {
      let finalChunk = '';
      let interimChunk = '';

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;

        if (event.results[i].isFinal) {
          finalChunk += transcript + ' ';
        } else {
          interimChunk += transcript + ' ';
        }
      }

      // Acumulamos texto FINAL en recognizedText
      if (finalChunk.trim()) {
        this.recognizedText = (
          this.recognizedText + ' ' + finalChunk
        ).trim();
      }

      // Lo que mostramos en pantalla: final + parcial
      const display = (
        this.recognizedText +
        (interimChunk ? ' ' + interimChunk : '')
      ).trim();

      this.liveTranscript = display;
      this.searchText = display;
    };
  }

  onVoiceSearchToggle(): void {
    if (!this.voiceSupported || !this.recognition) {
      console.warn('Búsqueda por voz no disponible.');
      return;
    }

    if (this.listening) {
      // Marcamos como parado YA para que el botón cambie al instante
      this.listening = false;
      this.recognition.stop();
    } else {
      // Nuevo dictado limpio
      this.liveTranscript = '';
      this.searchText = '';
      this.recognizedText = '';
      this.listening = true; // el botón pasa a "Parar" al momento
      try {
        this.recognition.start();
      } catch (e) {
        console.error('No se pudo iniciar SpeechRecognition', e);
        this.listening = false;
      }
    }
  }

  private voiceSearchFromText(): void {
    const text = (this.liveTranscript || this.searchText || '').trim();
    if (!text) return;

    this.searching = true;
    this.ingredientService.voiceSearch(text, 'es').subscribe({
      next: (res) => {
        if (res?.searchText) {
          this.searchText = res.searchText;
          this.liveTranscript = res.searchText;
        }
        this.searchResults = res?.ingredients || [];
        this.searching = false;
      },
      error: (err: unknown) => {
        console.error('Error en búsqueda por voz', err);
        this.searching = false;
      },
    });
  }

  // ============================
  //   PERFIL USUARIO (ALERGIAS / DIETA / GUSTOS)
  // ============================

  private normalizeText(value?: string | null): string {
    if (!value) return '';
    return value
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  cargarPerfilUsuario(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.userProfileService.getByUserId(userId).subscribe({
      next: (profile) => {
        this.userProfile = {
          userId: profile.userId,
          allergies: profile.allergies || [],
          intolerances: profile.intolerances || [],
          dislikedIngredients: profile.dislikedIngredients || [],
          dietType: profile.dietType || null,
          cookingSkillLevel: profile.cookingSkillLevel || null,
        };
      },
      error: (err) => {
        if ((err as any)?.status === 404) {
          // Si no existe perfil, creamos uno vacío en memoria
          this.userProfile = {
            userId,
            allergies: [],
            intolerances: [],
            dislikedIngredients: [],
            dietType: null,
            cookingSkillLevel: null,
          };
          return;
        }
        console.error('Error cargando perfil de usuario', err);
      },
    });
  }

  // ---- Alergias ----

  buscarAlergia(): void {
    const term = this.allergySearchText.trim();
    if (!term) {
      this.allergySearchResults = [];
      return;
    }

    const normalizedTerm = this.normalizeText(term);

    this.ingredientService.search(term, 10, 'es').subscribe({
      next: (results: Ingredient[]) => {
        if (results && results.length > 0) {
          this.allergySearchResults = results;
          return;
        }

        // Fallback local (frutos secos típicos)
        const fallback = [
          { id: 9001, name: 'peanut', nameEs: 'maní' },
          { id: 9002, name: 'peanuts', nameEs: 'maní' },
          { id: 9003, name: 'cacahuete', nameEs: 'cacahuete' },
          { id: 9004, name: 'almond', nameEs: 'almendra' },
          { id: 9005, name: 'walnut', nameEs: 'nuez' },
          { id: 9006, name: 'hazelnut', nameEs: 'avellana' },
        ];

        this.allergySearchResults = fallback.filter((f) => {
          const nameNorm = this.normalizeText(f.name);
          const nameEsNorm = this.normalizeText(f.nameEs);
          return (
            nameNorm.includes(normalizedTerm) ||
            nameEsNorm.includes(normalizedTerm) ||
            normalizedTerm.includes(nameNorm) ||
            normalizedTerm.includes(nameEsNorm)
          );
        });
      },
      error: (err: unknown) => {
        console.error('Error buscando alérgenos', err);
        this.allergySearchResults = [
          { id: 9001, name: 'peanut', nameEs: 'maní' },
        ];
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
        cookingSkillLevel: null,
      };
    }

    const nombre = this.getIngredientDisplayName(ing);
    if (nombre && !this.userProfile.allergies.includes(nombre)) {
      this.userProfile.allergies = [
        ...this.userProfile.allergies,
        nombre,
      ];
    }

    this.userProfileService.createOrUpdate(this.userProfile).subscribe({
      next: (updated: UserProfile) => {
        this.userProfile = {
          userId: updated.userId,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
          dietType: updated.dietType || null,
          cookingSkillLevel: updated.cookingSkillLevel || null,
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

    this.userProfile.allergies = this.userProfile.allergies.filter(
      (x) => x !== a
    );

    this.userProfileService.createOrUpdate(this.userProfile).subscribe({
      next: (updated: UserProfile) => {
        this.userProfile = {
          userId: updated.userId,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
          dietType: updated.dietType || null,
          cookingSkillLevel: updated.cookingSkillLevel || null,
        };
      },
      error: (err: unknown) => {
        console.error('Error eliminando alergia', err);
      },
    });
  }

  hasAllergyForItem(item: PantryItem): string[] {
    if (!this.userProfile?.allergies?.length) return [];

    const anyItem: any = item as any;
    const itemNameNorm = this.normalizeText(
      anyItem.ingredientNameEs ||
      anyItem.ingredientName ||
      ''
    );

    return this.userProfile.allergies.filter((a) => {
      const allergyNorm = this.normalizeText(a);
      return (
        itemNameNorm.includes(allergyNorm) ||
        allergyNorm.includes(itemNameNorm)
      );
    });
  }

  // ---- Ingredientes que no le gustan ----

  /**
   * Añadir un ingrediente que no le gusta al usuario.
   * Puede venir desde el input libre o desde una sugerencia (chip).
   */
  addDislikedIngredient(suggestion?: string): void {
    if (!this.userProfile) return;

    const value = (suggestion ?? this.dislikedInput).trim();
    if (!value) return;

    const list = this.userProfile.dislikedIngredients || [];
    const normValue = this.normalizeText(value);

    if (!list.some((x) => this.normalizeText(x) === normValue)) {
      this.userProfile.dislikedIngredients = [...list, value];
    }

    this.userProfileService.createOrUpdate(this.userProfile).subscribe({
      next: (updated: UserProfile) => {
        this.userProfile = {
          userId: updated.userId,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
          dietType: updated.dietType || null,
          cookingSkillLevel: updated.cookingSkillLevel || null,
        };
        this.dislikedInput = '';
        this.filteredDislikedSuggestions = [];
      },
      error: (err: unknown) => {
        console.error(
          'Error guardando ingredientes que no le gustan',
          err
        );
      },
    });
  }

  /**
   * Filtra las sugerencias según lo que se escribe en el input.
   */
  onDislikedInputChange(term: string): void {
    const q = term.trim().toLowerCase();

    if (!q) {
      this.filteredDislikedSuggestions = [];
      return;
    }

    this.filteredDislikedSuggestions = this.dislikedSuggestions
      .filter((s) => s.toLowerCase().includes(q))
      .filter(
        (s) =>
          !this.userProfile?.dislikedIngredients?.some(
            (x) =>
              this.normalizeText(x) === this.normalizeText(s)
          )
      );
  }

  removeDislikedIngredient(value: string): void {
    if (!this.userProfile) return;

    this.userProfile.dislikedIngredients =
      (this.userProfile.dislikedIngredients || []).filter(
        (x) => x !== value
      );

    this.userProfileService.createOrUpdate(this.userProfile).subscribe({
      next: (updated: UserProfile) => {
        this.userProfile = {
          userId: updated.userId,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
          dietType: updated.dietType || null,
          cookingSkillLevel: updated.cookingSkillLevel || null,
        };
      },
      error: (err: unknown) => {
        console.error(
          'Error eliminando ingrediente que no le gusta',
          err
        );
      },
    });
  }

  // ---- Tipo de dieta ----

  onDietTypeChange(value: string): void {
    if (!this.userProfile) return;

    this.userProfile.dietType = value || null;

    this.userProfileService.createOrUpdate(this.userProfile).subscribe({
      next: (updated: UserProfile) => {
        this.userProfile = {
          userId: updated.userId,
          allergies: updated.allergies || [],
          intolerances: updated.intolerances || [],
          dislikedIngredients: updated.dislikedIngredients || [],
          dietType: updated.dietType || null,
          cookingSkillLevel: updated.cookingSkillLevel || null,
        };
      },
      error: (err: unknown) => {
        console.error('Error actualizando tipo de dieta', err);
      },
    });
  }

  getDietLabel(): string {
    if (!this.userProfile?.dietType) {
      return 'Sin preferencia';
    }
    return this.userProfile.dietType;
  }

  // ============================
  //   MENÚ HAMBURGUESA
  // ============================

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  // ============================
  //   TAMAÑO DINÁMICO IMAGEN
  // ============================

  getQuantitySizeClass(item: PantryItem): string {
    if (item.quantity >= 10) return 'qty-large';
    if (item.quantity >= 5) return 'qty-medium';
    return 'qty-small';
  }
}
