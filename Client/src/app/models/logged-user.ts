import { User } from './'

export interface LoggedUser extends User {
    email?: string;
    token: string;
    matches?: User[];    
}