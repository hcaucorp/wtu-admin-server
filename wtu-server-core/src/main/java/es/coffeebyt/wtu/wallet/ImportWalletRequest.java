package es.coffeebyt.wtu.wallet;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Value
@Wither
public class ImportWalletRequest {

    @NotBlank
    public String currency;

    @NotBlank
    public String mnemonic;

    /** Epoch seconds. NOT millis */
    @Min(1322697600L)
    @Max(1600000000L)
    public long createdAt;
}
