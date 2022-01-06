import { AuthenticationService } from '../authentication.service';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthGuardService implements CanActivate {

  constructor(private router: Router,
              private authService: AuthenticationService) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const currentUser = this.authService.currentUserValue;
    // If user is already logged in he is automatically redirected to home page
    if (currentUser)
      return true;
    else {
      this.router.navigateByUrl('login');
      return false;
    }
  }
}
