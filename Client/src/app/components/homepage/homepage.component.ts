import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router'
import {AuthenticationService, UserService} from 'src/app/services'
import {User} from "../../models";

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {
  public user: User;
  public path: string;

  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private router: Router) { }

  ngOnInit(): void {
    this.user = this.authenticationService.currentUserValue;
    //this.path = `${'/assets/img/'+this.user.name + '.png'}`;
    this.path = `${this.user.name + '.png'}`;
  }

  onLogout() {
    this.authenticationService.logout()
      .subscribe(next=>{
          localStorage.removeItem('currentUser');
          this.authenticationService.currentUserSubject.next(null);
          this.router.navigateByUrl('/login');
    });

  }

  onChat() {
    this.router.navigateByUrl('/chat');
  }

  onSwipe() {
    this.router.navigateByUrl('/swipe');
  }
}
