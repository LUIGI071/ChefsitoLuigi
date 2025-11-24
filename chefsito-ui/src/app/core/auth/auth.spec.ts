// src/app/core/auth.spec.ts
import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import {
  AuthService,
  LoginRequest,
  AuthResponse,
} from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería crearse', () => {
    expect(service).toBeTruthy();
  });

  it('debería llamar a /api/auth/login en login()', () => {
    const payload: LoginRequest = {
      usernameOrEmail: 'test',
      password: 'secret',
    };

    let respuesta: AuthResponse | undefined;

    service.login(payload).subscribe(res => {
      respuesta = res;
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    const mockResponse: AuthResponse = {
      accessToken: 'fake-token',
      tokenType: 'Bearer',
    };

    req.flush(mockResponse);

    expect(respuesta).toEqual(mockResponse);
  });
});
