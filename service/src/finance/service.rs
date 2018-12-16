use crate::finance::account::*;

use std::vec::Vec;
use std::sync::Mutex;

pub struct AccountService {
    entries: Vec<Account>
}

impl AccountService {
    pub fn create() -> AccountService {
        AccountService { entries: vec![] }
    }

    pub fn get_accounts(&self) -> Vec<Account> {
        self.entries.to_vec()
    }

    pub fn get_account(&self, id: u64) -> Option<Account> {
        self.entries.get(id as usize).map(|f| f.clone())
    }

    pub fn create_account(&mut self, unsaved_account: UnsavedAccount) -> Account {
        let len: u64 = self.entries.len() as u64;
        self.entries.push(unsaved_account.to_saved(len));
        self.get_account(len).unwrap()
    }
}