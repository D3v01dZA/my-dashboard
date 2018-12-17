use crate::finance::account::*;

use std::vec::Vec;
use std::sync::RwLock;
use std::sync::RwLockWriteGuard;

pub struct AccountService {
    entries: RwLock<Vec<Account>>
}

impl AccountService {
    pub fn create() -> AccountService {
        AccountService { entries: RwLock::new(vec![]) }
    }

    pub fn get_accounts(&self) -> Vec<Account> {
        self.entries.read().unwrap().to_vec()
    }

    pub fn get_account(&self, id: u64) -> Option<Account> {
        self.entries.read().unwrap().get(id as usize).map(|f| f.clone())
    }

    pub fn create_account(&self, unsaved_account: UnsavedAccount) -> Account {
        let mut entries: RwLockWriteGuard<Vec<Account>> = self.entries.write().unwrap();
        let account: Account = unsaved_account.to_saved(entries.len() as u64);
        entries.push(account.clone());
        account
    }
}