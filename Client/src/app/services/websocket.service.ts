import { Injectable } from '@angular/core';
import { Observable, Observer, Subject } from 'rxjs';
import {Router} from "@angular/router";

import {Message} from "../models";

@Injectable()
export class WebsocketService {
  constructor(private router: Router) { }

  private subject: Subject<MessageEvent>;

  public connect(url: String, token: String): Subject<MessageEvent> {
    url += `?token=${token}`;
    if (!this.subject) {
      this.subject = this.create(url);
    }
    console.log("Successfully connected: " + url);
    return this.subject;
  }

  private create(url): Subject<MessageEvent> {
    let ws = new WebSocket(url);

    let observable = Observable.create(
      (obs: Observer<MessageEvent>) => {
        ws.onmessage = obs.next.bind(obs);
        ws.onerror = obs.error.bind(obs);
        ws.onclose = obs.complete.bind(obs);
        return ws.close.bind(ws);
      })

    let observer = {
      next: (data: Message) => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify(data));
        }
      }
    }

    return Subject.create(observer, observable);
  }



}
