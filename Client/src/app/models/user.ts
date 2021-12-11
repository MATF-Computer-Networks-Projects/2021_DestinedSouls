import {Gender, Interest} from "./gender";

export interface User {
  id: number;
  email?: string;
  token: string;
  name?: string;
  birthday?: string;
  gender?: Gender;
  interest?: Interest;
}
