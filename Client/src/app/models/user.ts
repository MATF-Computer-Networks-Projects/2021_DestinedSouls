import {Gender, Interest, ChatMessage} from "./";

export interface User {
  id: number;
  name?: string;
  birthday?: string;
  gender?: Gender;
  interest?: Interest;
  image?: string;
}

export interface MatchUser extends User {
  messages?: ChatMessage[];
}

export interface LoggedUser extends User {
  email?: string;
  token: string;
  matches?: MatchUser[];
}
