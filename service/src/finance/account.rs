#[derive(Serialize, Clone)]
pub struct Account {
    id: u64,
    balance: u64,
}

#[derive(Deserialize)]
pub struct UnsavedAccount {
    balance: u64
}

impl Account {
    pub fn create(id: u64, balance: u64) -> Account {
        Account { id, balance }
    }
}

impl UnsavedAccount {
    pub fn to_saved(&self, id: u64) -> Account {
        Account::create(id, self.balance)
    }
}