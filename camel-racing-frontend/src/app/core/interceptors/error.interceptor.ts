import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { API_BASE_URL } from '../config/api-config';

/**
 * Si el backend responde 401 (token vencido o inválido) en una request ya
 * autenticada, no tiene sentido seguir mostrando la pantalla como si nada:
 * se cierra la sesión localmente y se manda al login. El mensaje real del
 * error (con tildes, gracias al fix de encoding del backend) sigue
 * disponible para quien haga el catch en el componente.
 *
 * Un 403 significa que el token es válido pero el rol no alcanza para esa
 * acción (la UI ya oculta esos botones, pero esto cubre una llamada
 * directa a la API o un rol que cambió mientras la pestaña seguía
 * abierta): se manda a la pantalla de acceso denegado en vez de dejar la
 * pantalla como si nada hubiera pasado.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && req.url.startsWith(API_BASE_URL) && auth.isAuthenticated()) {
        auth.logout();
        router.navigateByUrl('/login');
      } else if (error.status === 403 && req.url.startsWith(API_BASE_URL)) {
        router.navigateByUrl('/access-denied');
      }
      return throwError(() => error);
    }),
  );
};
