import { Directive, EmbeddedViewRef, Input, TemplateRef, ViewContainerRef, effect } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

/**
 * Directiva estructural para mostrar/ocultar elementos según el rol del
 * usuario autenticado. Uso:
 *
 *   <button *appHasRole="['ADMINISTRATOR']">Eliminar</button>
 *   <button *appHasRole="['ADMINISTRATOR', 'RACE_ORGANIZER']">Editar</button>
 *
 * Esto es solo UX (ocultar botones que el backend igual rechazaría): la
 * autorización real siempre la hace SecurityConfig del lado del servidor.
 */
@Directive({
  selector: '[appHasRole]',
  standalone: true,
})
export class HasRoleDirective {
  private roles: Role[] = [];
  private view: EmbeddedViewRef<unknown> | null = null;

  constructor(
    private readonly templateRef: TemplateRef<unknown>,
    private readonly viewContainer: ViewContainerRef,
    private readonly auth: AuthService,
  ) {
    effect(() => {
      // Se re-evalúa cada vez que cambia el perfil (login/logout).
      this.auth.profile();
      this.updateView();
    });
  }

  @Input() set appHasRole(roles: Role[]) {
    this.roles = roles;
    this.updateView();
  }

  private updateView(): void {
    const allowed = this.auth.hasAnyRole(...this.roles);
    if (allowed && !this.view) {
      this.view = this.viewContainer.createEmbeddedView(this.templateRef);
    } else if (!allowed && this.view) {
      this.viewContainer.clear();
      this.view = null;
    }
  }
}
