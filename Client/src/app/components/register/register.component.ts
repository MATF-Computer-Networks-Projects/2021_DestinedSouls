import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from 'src/app/services';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  signupForm: FormGroup;
  loading = false;
  submitted = false;
  failed = false;
  returnUrl: string = "/";

  constructor(private formBuilder: FormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private userService: UserService) {



              }

  ngOnInit(): void {
    this.signupForm = this.formBuilder.group({
      name: ['', Validators.required],
      birthday: ['', Validators.required],
      gender: ['', Validators.required],
      interest: ['', Validators.required],
      email: ['', [Validators.required,Validators.email]],
      password: ['', [Validators.required,Validators.minLength(6)]]
  });
  }

  get getCtrls() { return this.signupForm.controls; }

  onSubmit() {
    this.submitted = true;

    // stop here if form is invalid
    if (this.signupForm.invalid) {
      console.log("Invalid form.")
        return;
    }

    this.loading = true;
    this.failed = false;
    this.userService.register(this.signupForm.value)
      .pipe()
      .subscribe(
        data => {
          // this.alertService.success('Registration successful', true);
          this.router.navigate(['/login']);
        },
        error => {
          document.getElementById("errorMsg").innerText = error.stringify();
          this.loading = false;
          this.submitted = false;
          this.failed = true;
        });
  }

  onLogin() {
    this.router.navigate(['login']);
  }
}
