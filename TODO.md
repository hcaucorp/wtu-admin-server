##### Backlog
- set up libra wallet in production to see if creating wllets work. Potentiallyu may be broken due to missing 
    description?? in the Wallet entity but it exists in the scema.
- remove dust amount validation from generic flow. It is network specific.
- load bitcoinj and bitcoinj-cash wallets from files if exist, else restore
- add "terms of service" to wix
- [UI] read Swagger api-docs from admin console, make swagger api docs available only from admin panel, not public
- implement "wallet groups"
- store tx hashes with redeemed vouchers (Redemption Log?)
- UI - Create vouchers doesn't work after failing first time, have to refresh the page 
- Clean up after malta gift cards
- migrate old voucher expiration dates with priority:
    - Malta vouchers: expire on 1st December 2019.
    - Purchased ones: expire in 2 years from now.
    - Published ones: expire in 2 years from now.
    - copy production data to local, test migration in local
    - run migration in prod after above test
