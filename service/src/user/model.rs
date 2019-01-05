pub struct Authentication {
    name: String,
    hash: String,
    user: User,
}

#[derive(Queryable)]
pub struct User {
    id: i32,
    name: String,
    password: String,
    salt_one: String,
    salt_two: String
}

impl Authentication {
    pub fn create(name: String, hash: String, user: User) -> Authentication {
        Authentication { name, hash, user }
    }

    pub fn map_user_id<F, R>(&self, f: F) -> R
        where F: FnOnce(i32) -> R
    {
        f(self.user.id)
    }

}