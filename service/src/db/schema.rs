table! {
    accounts (id) {
        id -> Int4,
        balance -> Int4,
        user_id -> Int4,
    }
}

table! {
    users (id) {
        id -> Int4,
        name -> Varchar,
        password -> Varchar,
        salt_one -> Varchar,
        salt_two -> Varchar,
    }
}

joinable!(accounts -> users (user_id));

allow_tables_to_appear_in_same_query!(
    accounts,
    users,
);
