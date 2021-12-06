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
        //const currentUser = this.authService.currentUserValue;
        if (this.authService.currentUserValue) {
          // If user is already logged in he is automaticly redirected to courses page          
          if(route.routeConfig) {
            console.log(route.routeConfig.path);            
            if (route.routeConfig.path === '') {
              this.router.navigateByUrl('/courses');
            }
          }
          return true;
        }
          else{            
            
            this.router.navigate(['/login'], {queryParams: { returnUrl: state.url }});
            return false;
        }
  }
}
