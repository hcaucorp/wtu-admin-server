import { Wallet } from "./wallet.model";


class WalletService {

    getWalletBalance(wallet: Wallet): number {
        const request = require('request');

        request(`https://blockchain.info/rawaddr/${wallet.address}`, { json: true }, (err:any, res:any, body:any) => {
            if (err) { return console.log(err); }
            console.log(res);
        });

        return 1;
    }
}

export { WalletService }