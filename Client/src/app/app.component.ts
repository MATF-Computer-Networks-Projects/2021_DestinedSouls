import { Component } from '@angular/core';
import { LoginForm } from './components/form/LoginForm';
import { SignupForm } from './components/form/SignupForm';
import { ForgotForm } from './components/form/ForgotForm';

import { LoginService } from './services/login.service';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'destinedsouls';
  logins : LoginForm[] = [];
  signups : SignupForm[] = [];
  forgots : ForgotForm[] = [];

  constructor(private loginService:LoginService){  }

  postLogin(newLogin : LoginForm){
    //console.log(newLogin);
    this.loginService.postUserLogin(newLogin).subscribe((newLogin)=>this.logins.push(newLogin));
  }
  postSignup(newSignup : SignupForm){
    console.log(newSignup);
    this.loginService.postUserSignup(newSignup).subscribe((newSignup)=>this.signups.push(newSignup));
  }
  postForgot(newForgot : ForgotForm){
    //console.log(newForgot);
    this.loginService.postUserForgot(newForgot).subscribe((newForgot)=>this.forgots.push(newForgot));
  }
}
