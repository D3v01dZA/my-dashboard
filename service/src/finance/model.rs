use crate::db::schema::accounts;
use crate::user::model::Authentication;

#[derive(Serialize, Queryable, Clone)]
pub struct Account {
    id: i32,
    balance: i32,
    user_id: i32
}

#[derive(Insertable)]
#[table_name="accounts"]
pub struct UnsavedAccount {
    balance: i32,
    user_id: i32
}

#[derive(Deserialize)]
pub struct UnattachedAccount {
    balance: i32
}

impl UnsavedAccount {

    pub fn create(authentication: &Authentication, account: UnattachedAccount) -> UnsavedAccount {
        authentication.map_user_id(|user_id| UnsavedAccount { balance: account.balance, user_id })
    }

}