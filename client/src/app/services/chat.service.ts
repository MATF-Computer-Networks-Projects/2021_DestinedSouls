import { Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { WebsocketService } from './websocket.service';
import {Message, ChatMessage, ChatConversation} from "../models";
import {AuthenticationService} from "./authentication.service";

//const CHAT_URL = "ws://" + window.location.href.split('/')[2] + "/chat";
const CHAT_URL = "ws://" + window.location.href.split('/')[2].split(':')[0] + ":3000/chat";

@Injectable()
export class ChatService {
  public messages: Subject<Message>;

  constructor(wsService: WebsocketService,
              authService: AuthenticationService) {
    this.messages = <Subject<Message>>wsService
      .connect(CHAT_URL, authService.currentUserValue.token)
      .pipe(map((response: MessageEvent): Message => {
        let data = JSON.parse(response.data);

        if(data.msg)
          return { id: data.id, msg: data.msg };

        if(data.id)
          return { id: data.id }
        return {}
      }));
  }
}
