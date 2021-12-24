import { Component, OnInit } from '@angular/core';
import {AuthenticationService, UserService} from "../../services";
import {Router} from "@angular/router";
import { User } from 'src/app/models';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent implements OnInit{

  onlineUsers: string[] = [];
  usersArray : User[];
  usersNames : string[] = [];
  chatActiveWith : string;
  displayChat: boolean = false;
  displayOnline : boolean = false;

  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private router: Router) {
    this.addOnline();
  }

  ngOnInit(): void {

  }


  openChatWith(user: string) {
    if(!this.usersNames.includes(user)){
      console.log("User: " + user + "is not online, can't chat with him anymore :(");
    }
    this.chatActiveWith = user;
    this.displayChat = true;
  }


  addOnline() {
    this.userService.getOnline()
      .subscribe( value  => {
        this.usersArray = value;
        this.usersArray.map( user => {
          if(!this.usersNames.includes(user.name))
              this.usersNames.push(user.name); });
        console.log("New login!");
        this.displayOnline=true;
      }, error => {
        this.displayOnline=true;
      });
  }

  toHome() {
    this.router.navigateByUrl('/');
  }
}
