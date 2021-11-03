import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FormComponent } from './components/form/form.component';
import { IlustrationComponent } from './components/ilustration/ilustration.component';
import { FormInputComponent } from './components/form-input/form-input.component';
import { FormLabelComponent } from './components/form-label/form-label.component';
import { FormBtnComponent } from './components/form-btn/form-btn.component';

@NgModule({
  declarations: [
    AppComponent,
    FormComponent,
    IlustrationComponent,
    FormInputComponent,
    FormLabelComponent,
    FormBtnComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
