
class Wallet {
    description: string;
    address: string;
    extendedPrivateKey: string;
}

const MasterWallet: Wallet = {
    description: "Master Wallet keeps funds to be used in voucher creation",
    address: /*bitcoincash:*/ "qps7dvqpf9da8qjsskkdh9h29hdcye62yvztalcew9",
    extendedPrivateKey: "L29MQ9YgkPwpLXCJGMJXGGXfuPLCepM9fXVghM7sU3EGtgfFC8iz"
}

export { MasterWallet, Wallet }