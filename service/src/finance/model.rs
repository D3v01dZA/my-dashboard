use crate::db::schema::accounts;

#[derive(Serialize, Queryable, Clone)]
pub struct Account {
    id: i32,
    balance: i32,
}

#[derive(Deserialize, Insertable)]
#[table_name="accounts"]
pub struct UnsavedAccount {
    balance: i32
}