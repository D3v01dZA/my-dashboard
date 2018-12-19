use rocket::request::FromRequest;
use rocket::Request;
use rocket::http::Status;
use rocket::request::Outcome;
use rocket::outcome::Outcome::Success;
use rocket::outcome::Outcome::Failure;

#[derive(Clone, Debug)]
pub struct User {
    user_name: String,
    user_password: String,
}

impl User {

    fn from_header(auth: String) -> User {
        User { user_name: auth, user_password: "Bleh".to_string() }
    }

}

impl<'a, 'r> FromRequest<'a, 'r> for User {
    type Error = ();

    fn from_request(request: &'a Request<'r>) -> Outcome<User, ()> {
        let authorization: Vec<&str> = request.headers().get("Authorization").collect();
        match authorization.as_slice() {
            [something] => Success(User::from_header(something.to_string())),
            _ => Failure((Status::Unauthorized, ()))
        }

    }
}