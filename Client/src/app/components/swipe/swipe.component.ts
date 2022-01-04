import {Component, OnInit} from '@angular/core';
import {AuthenticationService, UserService} from "../../services";
import {Router} from "@angular/router";
import {Gender, Interest, User} from "../../models";
import {filter, map} from "rxjs/operators";

@Component({
  selector: 'app-swipe',
  templateUrl: './swipe.component.html',
  styleUrls: ['./swipe.component.css']
})
export class SwipeComponent implements OnInit {
  public me: User;
  public filtered: User[];
  public usersShown: Array<{name: string, show: boolean, path: string}> = [];
  public displayCandidates: boolean = false;
  //TODO: map is @Output ---> chat component(list of users is @Input)
  public candidates: Map<string, boolean> = new Map<string, boolean>();

  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private router: Router) {
    this.findcandidates();
  }
  private findcandidates() {
    this.me = this.authenticationService.currentUserValue;
     this.userService.getOnline()
      .pipe(
        map(users => users
          .filter(user=>this.compatable(this.me.interest,user.gender))
          .filter(user => user.id!=this.me.id)
        )
      ).subscribe( value => {
          this.filtered = value;
          this.filtered.map( cand => {
            if(!this.usersShown.includes({name : cand.name, show: true, path:""}))
              this.usersShown.push({name : cand.name, show: true, path:`${cand.name + ".png"}`})
            ;
          });
       }
     );
    this.displayCandidates = true;
  }

  private compatable(interest: Interest, gender: Gender) : boolean {
    if (interest.valueOf() == Gender.Other.valueOf())
      return true;
    else if (interest.valueOf() == gender.valueOf())
      return true;
    else
      return false;
  }

  onVote(name: string, vote: boolean) {
    this.candidates.set(name, vote);
    let index =  this.usersShown.map(x => x.name).indexOf(name);
    this.usersShown.splice(index, 1);
    console.log(this.candidates);
  }

  onHome() {
    this.router.navigateByUrl('/');
  }

  ngOnInit(): void {

  }

}
