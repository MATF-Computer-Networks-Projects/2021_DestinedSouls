import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { AuthenticationService } from 'src/app/services'

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  failed = false;
  loading = false;
  submitted = false;

  constructor(
      private formBuilder: FormBuilder,
      private route: ActivatedRoute,
      private router: Router,
      private authenticationService: AuthenticationService
  ) {
      if (this.authenticationService.currentUserValue) {
          this.router.navigate(['/']);
      }

      this.loginForm = this.formBuilder.group({
          email: ['', [Validators.required,Validators.email]],
          password: ['', [Validators.required,Validators.minLength(6)]]
      });
  }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
        email: ['', [Validators.required,Validators.email]],
        password: ['', [Validators.required,Validators.minLength(6)]]
    });
      // tslint:disable-next-line: no-string-literal
      //this.returnUrl = this.route.snapshot.queryParams['/'];


  }

  get getCtrls() { return this.loginForm.controls; }


  onSubmit() {
      this.submitted = true;


      if (this.loginForm.invalid) {
          return;
        }



      this.loading = true;
      this.authenticationService.login(this.getCtrls.email.value, this.getCtrls.password.value)
          .subscribe(
              data => {
                  this.failed = false;
                  //this.router.navigate([this.returnUrl]);
                  this.router.navigateByUrl('/');
              },
              error => {
                  console.error(error);
                  this.loading = false;
                  this.submitted = false;
                  this.failed = true;
              });

  }

  onSignup() {
    this.router.navigate(['register']);
  }

}
