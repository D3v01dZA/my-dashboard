use diesel::sql_types::Array;
use diesel::sql_types::Int4;

pub struct Authentication {
    user_id: i32,
    hash: Vec<u8>
}

#[derive(Queryable)]
pub struct User {
    id: i32,
    name: String,
    password: Vec<u8>,
    salt_one: String,
    salt_two: String
}

impl Authentication {
    pub fn create(hash: Vec<u8>, user: User) -> Authentication {
        Authentication { hash, user_id: user.id }
    }

    pub fn map_user_id<F, R>(&self, f: F) -> R
        where F: FnOnce(i32) -> R
    {
        f(self.user_id)
    }

}

impl User {

    pub fn get_salt_one(&self) -> String {
        self.salt_one.clone()
    }

    pub fn get_salt_two(&self) -> String {
        self.salt_two.clone()
    }

    pub fn password_hash_correct(&self, password_hash: String) -> bool {
        false
    }

}