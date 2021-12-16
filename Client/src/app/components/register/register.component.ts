import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {Subscription} from "rxjs";
import {finalize} from "rxjs/operators";
import {HttpEventType} from "@angular/common/http";
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
  requiredFileType = "image/*";
  fileName = '';
  file:File = null;

  uploadProgress:number;
  uploadSub: Subscription;


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
      password: ['', [Validators.required,Validators.minLength(6)]],
      image: ['', Validators.required]
  });
  }

  get getCtrls() { return this.signupForm.controls; }

  onSubmit() {
    this.submitted = true;

    // stop here if form is invalid
    if (this.signupForm.invalid || !this.file || this.fileName === '') {
      console.log("Invalid form.")
        return;
    }

    this.loading = true;
    this.failed = false;
    const { image, ...userData } = this.signupForm.value;
    this.userService.register(userData)
      .pipe()
      .subscribe(
        data => {
          console.log(data)
          const upload$ = this.userService.upload(this.file, data.token)
            .pipe(
              finalize(() => { this.reset(); this.router.navigate(['/login']); })
            );

          this.uploadSub = upload$.subscribe(event => {
            if (event.type == HttpEventType.UploadProgress) {
              this.uploadProgress = Math.round(100 * (event.loaded / event.total));
            }
          })
          // this.alertService.success('Registration successful', true);
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

  onFileSelected(event) {
    this.file = event.target.files[0];
    this.fileName = this.file.name;
  }

  cancelUpload() {
    this.uploadSub.unsubscribe();
    this.reset();
  }

  reset() {
    this.uploadProgress = null;
    this.uploadSub = null;
    this.fileName = '';
    this.file = null;
  }
}
