import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router'
import {AuthenticationService, UserService} from 'src/app/services'

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {

  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private router: Router) { }

  ngOnInit(): void {
  }

  onLogout() {
    this.authenticationService.logout();
    this.router.navigateByUrl('/login');
  }

  onGetAll() {
    this.userService.getAll().subscribe(
      data => {
        console.log(`Data: ${data}`);
      },
      error => {
        console.error(error);
      });


  }
}
