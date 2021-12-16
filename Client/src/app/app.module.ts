import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { APP_BASE_HREF, LocationStrategy, HashLocationStrategy } from '@angular/common';
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressBarModule } from "@angular/material/progress-bar";

import { AppComponent } from './app.component';
//import { FormComponent } from './components/form/form.component';
//import { IlustrationComponent } from './components/ilustration/ilustration.component';
import { httpInterceptorProviders } from './services/http-interceptors';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { LoginComponent } from './components/login/login.component';
import { HomepageComponent } from './components/homepage/homepage.component';
import { RegisterComponent } from './components/register/register.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@NgModule({
  declarations: [
    AppComponent,
    //FormComponent,
    //IlustrationComponent,
    LoginComponent,
    HomepageComponent,
    RegisterComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    FontAwesomeModule,
    BrowserAnimationsModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule
  ],
  providers: [
    { provide: APP_BASE_HREF, useValue: '/'},
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    httpInterceptorProviders
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
