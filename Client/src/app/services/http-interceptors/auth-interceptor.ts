import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthenticationService } from '../authentication.service'
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';


@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private authService: AuthenticationService,
                private router: Router) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let authReq: HttpRequest<any>;

        if(this.authService.currentUserValue) {
            const authToken = this.authService.currentUserValue.token;
            authReq = req.clone({setHeaders: { Authorization: `Bearer ${authToken}` }});
        }
        else {
            authReq = req.clone();
        }

        return next.handle(authReq)
                .pipe(
                    tap( () => {},
                        (err: any) => this.errorHandling(err)
                        )
                    );
    }


    private errorHandling(error: any) {
        if (error instanceof HttpErrorResponse) {
            if (error.status === 401) {
                this.authService.logout();
                this.router.navigate(['login']);
            }
        }
    }
}
