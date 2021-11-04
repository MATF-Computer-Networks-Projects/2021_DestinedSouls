import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';

import { AppComponent } from './app.component';
import { FormComponent } from './components/form/form.component';
import { IlustrationComponent } from './components/ilustration/ilustration.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ForgotFormComponent } from './components/forgot-form/forgot-form.component';
import { LoginFormComponent } from './components/login-form/login-form.component';
import { SignupFormComponent } from './components/signup-form/signup-form.component';

@NgModule({
  declarations: [
    AppComponent,
    FormComponent,
    IlustrationComponent,
    ForgotFormComponent,
    LoginFormComponent,
    SignupFormComponent    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    FontAwesomeModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
