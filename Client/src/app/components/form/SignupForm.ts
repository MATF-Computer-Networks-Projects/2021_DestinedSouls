enum Gender{
    Male = 1,
    Female,
    Other
}
enum Interest{
    Male = 1,
    Female,
    Both
}

export class SignupForm {
    name : string | any;
    birthdate: string | any;
    gender : Gender | any;
    interest: Interest | any;
    email: string | any;
    password: string | any;
}
