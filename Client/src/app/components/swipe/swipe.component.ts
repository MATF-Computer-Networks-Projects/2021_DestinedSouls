import {Component, OnInit} from '@angular/core';
import {AuthenticationService, UserService} from "../../services";
import {Router} from "@angular/router";
import {Gender, Interest, User} from "../../models";
import {Swipe} from "../../models/swipe";

@Component({
  selector: 'app-swipe',
  templateUrl: './swipe.component.html',
  styleUrls: ['./swipe.component.css']
})
export class SwipeComponent implements OnInit {
  public usersShown: User[] = [];
  public displayCandidates: boolean = false;
  //TODO: map is @Output ---> chat component(list of users is @Input)
  // public candidates: Map<string, boolean> = new Map<string, boolean>();
  public candidates: Swipe[] = [];

  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private router: Router) {
    this.findCandidates();
  }
  private findCandidates() {
     this.userService.getSwipes()
      .pipe()
       .subscribe( (value: User[]) => {
         if(!value) {
           this.displayCandidates = this.displayCandidates || false;
           return;
         }
          this.usersShown = value;
          this.displayCandidates = true;
       },
         error => { console.error(error) }
     );
  }


  onVote(user: User, vote: boolean) {
    this.candidates.push({id: user.id, like: (vote?"true":"false")});
    this.usersShown.splice(0, 1);
    if(this.usersShown.length === 0)
      this.userService.sendSwipeVotes(this.candidates)
        .pipe().subscribe(data => {});

  }

  onHome() {
    this.router.navigateByUrl('/');
  }

  ngOnInit(): void {

  }

}
