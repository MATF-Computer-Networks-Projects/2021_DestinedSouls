import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { HomepageComponent,
         LoginComponent,
         RegisterComponent,
         ChatComponent,
         SwipeComponent
        } from 'src/app/components'

import { AuthGuardService } from 'src/app/services'

const routes: Routes = [
  {
    path: '',
    component: HomepageComponent,
    canActivate: [AuthGuardService]
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'chat',
    component: ChatComponent
  },
  {
    path: 'swipe',
    component: SwipeComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
