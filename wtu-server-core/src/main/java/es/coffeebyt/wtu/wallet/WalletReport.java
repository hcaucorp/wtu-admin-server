package es.coffeebyt.wtu.wallet;

import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
public class WalletReport {

    private Wallet wallet;

    // balance that is locked in redeemable vouchers
    private long requiredBalance;
}
