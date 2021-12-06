import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginForm } from '../components/form/LoginForm';
import { SignupForm } from '../components/form/SignupForm';
import { ForgotForm } from '../components/form/ForgotForm';

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
};

@Injectable({
  providedIn: 'root'
})

export class LoginService {
  constructor(private http: HttpClient) {}

  postUserLogin(newLogin: LoginForm):Observable<LoginForm>{
    return this.http.post<LoginForm>('/auth', newLogin);
  }
  postUserSignup(newSignup: SignupForm):Observable<SignupForm>{
    return this.http.post<SignupForm>('/signup', newSignup);
  }
  postUserForgot(newForgot: ForgotForm):Observable<ForgotForm>{
    return this.http.post<ForgotForm>('/forgot', newForgot);
  }

}
