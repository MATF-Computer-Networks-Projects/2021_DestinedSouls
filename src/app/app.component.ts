import { Component } from '@angular/core';
import { LoginForm } from './components/form/Form';
import { LoginService } from './services/login.service';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'destinedsouls';
  logins : LoginForm[] = [];
  constructor(private loginService:LoginService){  }

  postUser(newLogin : LoginForm){
    console.log(newLogin);
    //FIX
    this.loginService.postUser(newLogin).subscribe((newLogin)=>this.logins.push(newLogin));
  }
}
